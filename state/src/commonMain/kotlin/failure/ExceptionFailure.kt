package opensavvy.state.failure

class ExceptionFailure(
    override val key: Failure.Key,
    val exception: Throwable,
    override val cause: Failure? = null,
) : Failure {

    override val message: String get() = exception.message ?: "No message available"

    override fun toString() = exception.stackTraceToString()
}

fun Throwable.asFailure(key: Failure.Key, cause: Failure? = null) = ExceptionFailure(key, this, cause)
