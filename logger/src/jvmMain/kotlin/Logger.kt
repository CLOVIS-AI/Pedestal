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

import org.slf4j.LoggerFactory

private class SlfLogger(self: Any) : Logger {
	override var level = LogLevel.default
	private val logger = LoggerFactory.getLogger(self::class.java)
		?: error("Could not find a logger for ${self::class.java}")

	private fun generateFormat(message: String, vararg objects: Any?): String {
		var result = message

		repeat(objects.size) {
			result += " {}"
		}

		return result
	}

	override fun forceTrace(message: String, vararg objects: Any?) {
		logger.trace(generateFormat(message, *objects), *objects)
	}

	override fun forceDebug(message: String, vararg objects: Any?) {
		logger.debug(generateFormat(message, *objects), *objects)
	}

	override fun forceInfo(message: String, vararg objects: Any?) {
		logger.info(generateFormat(message, *objects), *objects)
	}

	override fun forceWarn(message: String, vararg objects: Any?) {
		logger.warn(generateFormat(message, *objects), *objects)
	}

	override fun forceError(message: String, vararg objects: Any?) {
		logger.error(generateFormat(message, *objects), *objects)
	}

}

actual fun loggerFor(obj: Any): Logger = SlfLogger(obj)
