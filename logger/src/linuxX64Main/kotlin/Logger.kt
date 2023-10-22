package opensavvy.logger

private class LinuxLogger(obj: Any) : Logger {
	override var level: LogLevel = LogLevel.default

	private val objName = obj::class.qualifiedName ?: obj::class.toString()

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

actual fun loggerFor(obj: Any): Logger =
	LinuxLogger(obj)
