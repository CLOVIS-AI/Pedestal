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
import opensavvy.prepared.runner.kotest.PreparedSpec

@Suppress("DEPRECATION")
class LoggerTest : PreparedSpec({

	test("Output") {
		val log = loggerFor(this)
		log.level = LogLevel.TRACE

		log.trace { "This is a trace message!" }
		log.debug { "This is a debug message!" }
		log.info { "This is an info message!" }
		log.warn { "This is a warning!" }
		log.error { "This is an error!" }
	}

	data class Message(val int: Int, val text: String)

	test("Output with object") {
		val message = Message(5, "hello")

		val log = loggerFor(this)
		log.level = LogLevel.TRACE

		log.trace(message) { "This is a trace message!" }
		log.debug(message) { "This is a debug message!" }
		log.info(message) { "This is an info message!" }
		log.warn(message) { "This is a warning!" }
		log.error(message) { "This is an error!" }
	}
})
