package fyi.manpreet.core.data.logging

import co.touchlab.kermit.Logger
import fyi.manpreet.chat.domain.logging.ChirpLogger

object KermitLogger: ChirpLogger {

    override fun debug(message: String) {
        Logger.Companion.d(message)
    }

    override fun info(message: String) {
        Logger.Companion.i(message)
    }

    override fun warn(message: String) {
        Logger.Companion.w(message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Logger.Companion.e(message, throwable)
    }
}