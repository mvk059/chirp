-- ============================================================================
-- Redis Lua Script for Rate Limiting with Exponential Backoff. Validate first, mutate last
-- ============================================================================
--
-- KEYS[1] = rateLimitKey        -> temporary lock key to enforce cooldown
-- KEYS[2] = attemptCountKey     -> persistent counter key tracking attempts
--
-- ARGV[1] = optional comma-separated list of backoff seconds (default: "60,300,3600")
--           e.g. "30,120,600" would mean 30s, 2min, 10min backoff's
-- ARGV[2] = optional TTL (in seconds) for attemptCountKey (default: 86400 = 24h)
--
-- Return format:
--   { -1, ttl }   -> when user is currently rate-limited; ttl = seconds remaining
--   { count, 0 }  -> when allowed; count = new attempt count, cooldown = 0
--
-- ============================================================================

-- --------------------------------------------------------------------------
-- Helper: split string into table of numbers by delimiter (used for ARGV[1])
-- --------------------------------------------------------------------------
local function split_to_numbers(s, sep)
    local res = {}
    if not s or s == '' then return res end
    for token in string.gmatch(s, "[^" .. sep .. "]+") do
        local n = tonumber(token)
        if not n then
            return nil -- parse failure
        end
        table.insert(res, n)
    end
    return res
end

local function is_nonneg_integer(n)
    return type(n) == "number" and n >= 0 and math.floor(n) == n
end

-- --------------------------------------------------------------------------
-- Parse & Validate KEYS presence (fail fast, no mutations)
-- --------------------------------------------------------------------------
if not KEYS or #KEYS < 2 then
    return redis.error_reply("ERR missing KEYS: expected 2 keys (rateLimitKey, attemptCountKey)")
end

local rateLimitKey = KEYS[1]
local attemptCountKey = KEYS[2]

if not rateLimitKey or rateLimitKey == '' or not attemptCountKey or attemptCountKey == '' then
    return redis.error_reply("ERR keys must be non-empty strings")
end

-- --------------------------------------------------------------------------
-- Parse & validate ARGV (backoff array + attempt TTL)
-- --------------------------------------------------------------------------
-- Default backoff sequence: 60s, 300s (5 min), 3600s (1h)
local defaultBackoff = {60, 300, 3600}

-- If ARGV[1] is provided, parse it; otherwise use defaults
local backoffSeconds = defaultBackoff
if ARGV[1] and ARGV[1] ~= '' then
    local parsed = split_to_numbers(ARGV[1], ",")
    if not parsed then
        return redis.error_reply("ERR ARGV[1] malformed: backoff CSV must contain only integers")
    end
    if #parsed == 0 then
        return redis.error_reply("ERR ARGV[1] empty after parsing")
    end
    if #parsed > 0 then
        backoffSeconds = parsed
    end
end

-- Attempt counter expiry (default: 24h)
local attemptTtl = tonumber(ARGV[2]) or 86400
if not is_nonneg_integer(attemptTtl) then
    return redis.error_reply("ERR ARGV[2] attempt TTL must be a non-negative integer")
end

-- Validate backoff entries
for i = 1, #backoffSeconds do
    if not is_nonneg_integer(backoffSeconds[i]) or backoffSeconds[i] == 0 then
        return redis.error_reply("ERR backoff values must be positive integers")
    end
end

-- --------------------------------------------------------------------------
-- Preconditions done — now do reads + mutations atomically
-- --------------------------------------------------------------------------

-- --------------------------------------------------------------------------
-- Step 1: If cooldown lock already exists, return -1 with TTL
-- --------------------------------------------------------------------------
if redis.call('EXISTS', rateLimitKey) == 1 then
    local ttl = tonumber(redis.call('TTL', rateLimitKey)) or -2

    -- TTL special cases:
    --   -2 = key does not exist (should not happen since EXISTS == 1)
    --   -1 = key exists but has no expiry
    -- In both cases, fallback to the first backoff value
    if ttl < 0 then
        ttl = backoffSeconds[1]
    end

    return {-1, ttl}
end

-- --------------------------------------------------------------------------
-- Step 2: Increment attempt count atomically
-- --------------------------------------------------------------------------
local newCount = redis.call('INCR', attemptCountKey)
local prevCount = tonumber(newCount) - 1

-- --------------------------------------------------------------------------
-- Step 3: Determine backoff duration based on attempt history
-- --------------------------------------------------------------------------
-- Clamp prevCount into [0, #backoffSeconds - 1]
local maxIndex = #backoffSeconds - 1
local index = prevCount
if index < 0 then index = 0 end
if index > maxIndex then index = maxIndex end

-- --------------------------------------------------------------------------
-- Step 4: Set the cooldown lock for chosen backoff duration
-- --------------------------------------------------------------------------
local backoff = backoffSeconds[index + 1]
redis.call('SETEX', rateLimitKey, backoff, '1')

-- --------------------------------------------------------------------------
-- Step 5: Ensure attempt counter expires (only set TTL if not set already)
-- --------------------------------------------------------------------------
local attemptTtlCurrent = tonumber(redis.call('TTL', attemptCountKey)) or -2
if attemptTtlCurrent < 0 then
    redis.call('EXPIRE', attemptCountKey, attemptTtl)
end

-- Return new count and 0 meaning "no active cooldown"
return { tonumber(newCount), 0 }

-- ============================================================================
-- Improvements & Future Considerations
-- ============================================================================
-- 1. Sliding vs. fixed expiry for attempts:
--    - Current code sets 24h TTL only if missing → "fixed window" behavior.
--    - Change to unconditional EXPIRE if you want a "sliding window".
--
-- 2. Millisecond precision:
--    - Use PTTL (ms TTL) and PSETEX instead of TTL/SETEX if sub-second accuracy is needed.
--
-- 3. Dynamic backoff logic:
--    - Right now it clamps at the last backoff (1h). Could expand to N steps
--      or exponential growth formula (e.g., 2^n * base).
--
-- 4. Return richer data:
--    - Could return JSON string or map-like structure for clarity, instead of array.
--      e.g., return { count = newCount, cooldown = 0 } in RESP3.
--
-- 5. Cluster considerations:
--    - Ensure KEYS[1] and KEYS[2] hash to the same slot in Redis Cluster
--      (e.g., use user:{id}:rate and user:{id}:attempts).
--
-- 6. Script deployment:
--    - Use SCRIPT LOAD and EVALSHA for efficiency, with fallback to EVAL if NOSCRIPT occurs.
--
-- 7. Security / safety:
--    - Validate ARGV inputs if they come from untrusted sources.
--    - Ensure backoffSeconds array has at least one entry.
--
-- ============================================================================