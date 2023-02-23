package opensavvy.logger

class IosLogger(private val self: Any): Logger {
    override var level = LogLevel.default

    private val tag = self::class.simpleName

    override fun forceTrace(message: String, vararg objects: Any?) {
        println("T: $tag: $message ${objects.joinToString(" ")}")
    }

    override fun forceDebug(message: String, vararg objects: Any?) {
        println("D: $tag: $message ${objects.joinToString(" ")}")
    }

    override fun forceInfo(message: String, vararg objects: Any?) {
        println("I: $tag: $message ${objects.joinToString(" ")}")
    }

    override fun forceWarn(message: String, vararg objects: Any?) {
        println("W: $tag: $message ${objects.joinToString(" ")}")
    }

    override fun forceError(message: String, vararg objects: Any?) {
        println("E: $tag: $message ${objects.joinToString(" ")}")
    }

}

actual fun loggerFor(obj: Any): Logger = IosLogger(obj)
