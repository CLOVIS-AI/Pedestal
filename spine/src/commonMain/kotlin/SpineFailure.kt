package opensavvy.spine

import opensavvy.state.failure.CustomFailure
import opensavvy.state.failure.Failure

class SpineFailure(
    val type: Type,
    message: String,
    cause: Failure? = null,
) : CustomFailure(Companion, "$type: $message", cause) {

    enum class Type {
        Unauthenticated,
        Unauthorized,
        NotFound,
        InvalidRequest,
        InvalidState,
        ;
    }

    companion object : Failure.Key
}
