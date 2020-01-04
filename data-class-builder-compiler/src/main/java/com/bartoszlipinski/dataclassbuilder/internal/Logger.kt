/*
 * Copyright 2020 Bartosz Lipinski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bartoszlipinski.dataclassbuilder.internal

import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

object Logger {
    internal var retainedInstance: LoggerImpl? = null

    @Synchronized
    fun retainInstance(
            pe: ProcessingEnvironment,
            messagePrefix: String
    ) {
        if (retainedInstance == null) {
            retainedInstance = LoggerImpl(pe, messagePrefix)
        }
    }

    @Synchronized
    fun clearInstance() {
        retainedInstance = null
    }
}

internal class LoggerImpl internal constructor(
        pe: ProcessingEnvironment,
        private val messagePrefix: String
) {
    private val messager: Messager = pe.messager
    private val logToSystemOut = isJUnitTest()

    private fun isJUnitTest(): Boolean {
        val stackTrace = Thread.currentThread().stackTrace
        val list = listOf(*stackTrace)
        for (element in list) {
            if (element.className.startsWith("org.junit.")) {
                return true
            }
        }
        return false
    }

    fun log(message: String) {
        val prefixedMessage = message.addPrefix()
        if (logToSystemOut) {
            println("[LOG] $prefixedMessage")
        }
        messager.printMessage(Diagnostic.Kind.NOTE, prefixedMessage)
    }

    fun error(message: String) {
        val prefixedMessage = message.addPrefix()
        if (logToSystemOut) {
            println("[ERROR] $prefixedMessage")
        }
        messager.printMessage(Diagnostic.Kind.ERROR, prefixedMessage)
    }

    fun warning(message: String) {
        val prefixedMessage = message.addPrefix()
        if (logToSystemOut) {
            println("[WARNING] $prefixedMessage")
        }
        messager.printMessage(Diagnostic.Kind.WARNING, prefixedMessage)
    }

    private inline fun String.addPrefix(): String = "$messagePrefix:\n$this\n"
}

fun compilerLog(message: String) {
    checkNotNull(Logger.retainedInstance) { "No retained instance" }
    Logger.retainedInstance!!.log(message)
}

fun compilerError(message: String) {
    checkNotNull(Logger.retainedInstance) { "No retained instance" }
    Logger.retainedInstance!!.error(message)
}

fun compilerWarning(message: String) {
    checkNotNull(Logger.retainedInstance) { "No retained instance" }
    Logger.retainedInstance!!.warning(message)
}

