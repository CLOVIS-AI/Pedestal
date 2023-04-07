package opensavvy.state.failure

/**
 * Basic implementation of [Failure], used to create your own implementation through interface delegation.
 */
data class BasicFailure(
    override val key: Failure.Key,
    override val message: String,
    override val cause: Failure? = null,
) : Failure {

    override fun toString() = buildString {
        append(key.toString())
        append(": ")

        append(message)

        if (cause != null) {
            appendLine()

            append("Caused by ")
            append(cause.toString())
        }
    }
}
