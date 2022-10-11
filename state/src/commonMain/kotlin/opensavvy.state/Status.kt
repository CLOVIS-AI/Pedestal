package opensavvy.state

/**
 * The status of a value.
 *
 * The status can be in three different states:
 * - [Pending] represents a value which is not yet available.
 * - [Successful] represents a successful result.
 * - [Failed] represents an unsuccessful result.
 */
sealed interface Status<out T> {

	object Pending : Status<Nothing> {
		override fun toString() = "Pending"
	}

	data class Successful<T>(
		val value: T,
	) : Status<T> {
		override fun toString() = "$value"
	}

	abstract class Failed(message: String?, cause: Throwable?) : RuntimeException(message, cause), Status<Nothing>

	//region Failure implementations

	class ExceptionFailure(message: String, cause: RuntimeException) : Failed(message, cause)

	class StandardFailure(
		val kind: Kind,
		message: String,
		cause: Throwable? = null,
	) : Failed(message, cause) {

		//region toString, equals, hashCode

		override fun toString() = "$kind($message)"
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (other !is StandardFailure) return false

			if (kind != other.kind) return false
			if (message != other.message) return false

			return true
		}

		override fun hashCode(): Int {
			var result = kind.hashCode()
			result = 31 * result + message.hashCode()
			return result
		}

		//endregion

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

	//endregion

}
