package opensavvy.state

/**
 * Common failure reasons.
 *
 * Encoding these reasons at all levels of the project allows APIs (such as Spine) to encode them during communication
 * with other services, for example as HTTP status codes.
 */
data class Failure(
	val kind: Kind,
	val message: String,
	val cause: Throwable? = null,
) {

	override fun toString() = "$kind: $message"

	fun toException() = FailureException(this, kind, message, cause)

	class FailureException(val failure: Failure, val kind: Kind, message: String, cause: Throwable? = null) :
		RuntimeException(message, cause)

	enum class Kind {
		/**
		 * The request is not valid.
		 *
		 * All prerequisites are correct (the user is correctly authenticated and authorized…) but the
		 * request payload is invalid.
		 */
		Invalid,

		/**
		 * The implementation could not determine which user is making the request.
		 */
		Unauthenticated,

		/**
		 * The implementation could determine which user is making the request, but they do not have
		 * sufficient rights to make the request.
		 */
		Unauthorized,

		/**
		 * The request is well-formed and the user is allowed to make it,
		 * but the resource it should apply to could not be found.
		 */
		NotFound,

		/**
		 * The implementation caught an unknown error.
		 */
		Unknown,
		;
	}
}
