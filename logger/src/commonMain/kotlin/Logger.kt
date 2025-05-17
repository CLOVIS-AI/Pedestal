/*
 * Copyright (c) 2022-2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.logger

import opensavvy.logger.Logger.Companion.debug
import opensavvy.logger.Logger.Companion.error
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.Logger.Companion.trace
import opensavvy.logger.Logger.Companion.warn

/**
 * Multiplatform interface for logger implementations.
 *
 * Logging for a class can be implemented as follows:
 * ```kotlin
 * class Foo {
 *     init {
 *         log.trace { "Foo's constructor was called!" }
 *     }
 *
 *     companion object {
 *         private val log = loggerFor(this)
 *     }
 * }
 * ```
 *
 * The messages included or excluded can be selected using [level].
 * For example, to only display error messages for a specific class, use:
 * ```kotlin
 * class Foo {
 *     companion object {
 *         private val log = loggerFor(this)
 *
 *         init {
 *             log.level = LogLevel.ERROR
 *         }
 *     }
 * }
 * ```
 * To set the log level for the entire program, see [LogLevel.default].
 *
 * The available logging methods are:
 * - [trace],
 * - [debug],
 * - [info],
 * - [warn],
 * - [error].
 *
 * They all accept the same arguments:
 * - zero or more objects, which will be appended to the message
 * - a lambda producing a message, which will only be evaluated if the given level is enabled.
 *
 * Examples:
 * ```kotlin
 * log.level { "Simple form" }
 * log.level(obj1, obj2) { "With two objects" }
 * ```
 *
 * Because the various logging methods are inline and check that their level is enabled early, logging using this
 * interface is performant no matter the implementation.
 *
 * The [loggerFor] method is used to instantiate a platform-specific implementation.
 * On Kotlin/JS, it uses the `console` object.
 * On Kotlin/JVM, it uses the [Slf4j](https://www.slf4j.org/) library, you will need to include a Slf4j binding in your project.
 */
@Deprecated(DEPRECATION_MESSAGE)
interface Logger {

	/**
	 * The log level of this logger.
	 *
	 * Implementations of the [Logger] interface may add their own system to decide whether a message is printed,
	 * additionally to this attribute.
	 * For example, LogBack has its own log level configuration, Slf4j-simple never prints trace and debug messages.
	 */
	@Deprecated(DEPRECATION_MESSAGE)
	var level: LogLevel

	fun forceTrace(message: String, vararg objects: Any?)
	fun forceDebug(message: String, vararg objects: Any?)
	fun forceInfo(message: String, vararg objects: Any?)
	fun forceWarn(message: String, vararg objects: Any?)
	fun forceError(message: String, vararg objects: Any?)

	companion object {
		inline fun Logger.trace(vararg objects: Any?, message: () -> String) {
			if (level.trace)
				forceTrace(message(), *objects)
		}

		inline fun Logger.debug(vararg objects: Any?, message: () -> String) {
			if (level.debug)
				forceDebug(message(), *objects)
		}

		inline fun Logger.info(vararg objects: Any?, message: () -> String) {
			if (level.info)
				forceInfo(message(), *objects)
		}

		inline fun Logger.warn(vararg objects: Any?, message: () -> String) {
			if (level.warn)
				forceWarn(message(), *objects)
		}

		inline fun Logger.error(vararg objects: Any?, message: () -> String) {
			if (level.error)
				forceError(message(), *objects)
		}
	}
}

@Deprecated(DEPRECATION_MESSAGE)
fun loggerFor(obj: Any): Logger =
	VerySimpleLogger(obj)

private class VerySimpleLogger(obj: Any) : Logger {
	@Deprecated(DEPRECATION_MESSAGE)
	override var level: LogLevel = LogLevel.default

	private val objName = obj::class.toString()

	private fun buildMessage(vararg values: Any?) =
		values.joinToString(separator = " • ")

	override fun forceTrace(message: String, vararg objects: Any?) {
		println("[TRACE] $objName: ${buildMessage(message, *objects)}")
	}

	override fun forceDebug(message: String, vararg objects: Any?) {
		println("[DEBUG] $objName: ${buildMessage(message, *objects)}")
	}

	override fun forceInfo(message: String, vararg objects: Any?) {
		println("[INFO]  $objName: ${buildMessage(message, *objects)}")
	}

	override fun forceWarn(message: String, vararg objects: Any?) {
		println("[WARN]  $objName: ${buildMessage(message, *objects)}")
	}

	override fun forceError(message: String, vararg objects: Any?) {
		println("[ERROR] $objName: ${buildMessage(message, *objects)}")
	}
}
