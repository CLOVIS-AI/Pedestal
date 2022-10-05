package opensavvy.logger

import org.slf4j.LoggerFactory

private class SlfLogger(self: Any): Logger {
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
