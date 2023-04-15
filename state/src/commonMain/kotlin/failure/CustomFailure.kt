package opensavvy.state.failure

/**
 * Convenience class to implement [Failure] by delegating all methods to [BasicFailure],
 * including [equals], [hashCode] and [toString] (which are not delegated by Kotlin's `by` keyword).
 */
abstract class CustomFailure(private val failure: BasicFailure) : Failure by failure {

    constructor(
        key: Failure.Key,
        message: String,
        cause: Failure? = null,
    ) : this(BasicFailure(key, message, cause))

    constructor(
        failure: CustomFailure,
    ) : this(failure.failure)

    //region equals & hashCode

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomFailure) return false

        return failure == other.failure
    }

    override fun hashCode(): Int {
        return failure.hashCode()
    }

    //endregion

    override fun toString() = failure.toString()
}
