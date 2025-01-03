package opensavvy.logger

import opensavvy.logger.Logger.Companion.debug
import opensavvy.logger.Logger.Companion.error
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.Logger.Companion.trace
import opensavvy.logger.Logger.Companion.warn
import opensavvy.prepared.runner.kotest.PreparedSpec

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
