package opensavvy.state.failure

private fun createMessage(title: String, additional: String? = null) =
    if (additional != null) "$title. $additional"
    else title

/**
 * The requested element could not be found.
 */
class NotFound(
    /**
     * The identifier of the resource that could not be found.
     */
    val identifier: Any,

    /**
     * See [Failure.message].
     */
    message: String? = null,

    /**
     * See [Failure.cause].
     */
    cause: Failure? = null,
) : CustomFailure(
    Companion,
    createMessage("Could not find resource '$identifier'", message),
    cause,
) {

    companion object : Failure.Key {
        override fun toString() = "NotFound"
    }
}

/**
 * The current user is not authenticated, yet authentication is required to access this information.
 */
class Unauthenticated(
    /**
     * See [Failure.message].
     */
    message: String? = null,

    /**
     * See [Failure.cause].
     */
    cause: Failure? = null,
) : CustomFailure(
    Companion,
    createMessage("Authentication is required to access this resource", message),
    cause,
) {

    companion object : Failure.Key {
        override fun toString() = "Unauthenticated"
    }
}

/**
 * The current user's identity has been recognized by the service, but the service decided they do not hold sufficient
 * rights to proceed.
 */
class Unauthorized(
    /**
     * See [Failure.message].
     */
    message: String? = null,

    /**
     * See [Failure.cause].
     */
    cause: Failure? = null,
) : CustomFailure(
    Companion,
    createMessage("You are not authorized to access this information", message),
    cause,
) {

    companion object : Failure.Key {
        override fun toString() = "Unauthorized"
    }
}
