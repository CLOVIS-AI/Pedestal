package opensavvy.spine

sealed class SpineFailure<out Payload : Any> {

    abstract val type: Type

    abstract val payload: Payload?

    data class Message(
        override val type: Type,
        val message: String? = null,
    ) : SpineFailure<Nothing>() {
        override val payload: Nothing?
            get() = null

        override fun toString() = "$type: “${message}”"
    }

    data class Payload<Payload : Any>(
        override val type: Type,
        override val payload: Payload,
    ) : SpineFailure<Payload>() {

        override fun toString() = "$type: “${payload}”"
    }

    enum class Type {
        Unauthenticated,
        Unauthorized,
        NotFound,
        InvalidRequest,
        InvalidState,
        ;
    }
}

fun SpineFailure(
    type: SpineFailure.Type,
    message: String? = null,
) = SpineFailure.Message(type, message)

fun <Payload : Any> SpineFailure(
    type: SpineFailure.Type,
    payload: Payload,
) = SpineFailure.Payload(type, payload)
