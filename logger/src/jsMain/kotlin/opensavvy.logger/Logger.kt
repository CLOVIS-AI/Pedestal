package opensavvy.logger

private class ConsoleLogger(private val self: Any) : Logger {
	override var level = LogLevel.default

	override fun forceTrace(message: String, vararg objects: Any) {
		console.log(self::class.simpleName, message, *objects)
	}

	override fun forceDebug(message: String, vararg objects: Any) {
		console.log(self::class.simpleName, message, *objects)
	}

	override fun forceInfo(message: String, vararg objects: Any) {
		console.info(self::class.simpleName, message, *objects)
	}

	override fun forceWarn(message: String, vararg objects: Any) {
		console.warn(self::class.simpleName, message, *objects)
	}

	override fun forceError(message: String, vararg objects: Any) {
		console.error(self::class.simpleName, message, *objects)
	}
}

actual fun loggerFor(obj: Any): Logger = ConsoleLogger(obj)
