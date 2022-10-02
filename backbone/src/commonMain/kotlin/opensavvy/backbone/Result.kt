package opensavvy.backbone

import opensavvy.backbone.Result.*

/**
 * The various possible results of querying a [reference][Ref].
 *
 * There are three major possible cases:
 * - [NoData]: the request has not finished yet, the request is neither successful nor failed,
 * - [Success]: the request is successful, [Success.value] contains the result,
 * - [Failure]: the request has failed, the object contains the reason why.
 */
sealed interface Result<out O> {

	/**
	 * Applies a [transform] to the value stored by this [Result].
	 */
	fun <T> map(transform: (O) -> T): Result<T>

	/**
	 * The request has not finished yet, and no previous result is available.
	 */
	object NoData : Result<Nothing> {
		override fun toString() = "NoData"

		override fun <T> map(transform: (Nothing) -> T) = NoData
	}

	/**
	 * The request is successful, [value] contains its result.
	 */
	data class Success<O>(val value: O) : Result<O> {
		override fun toString() = value.toString()

		override fun <T> map(transform: (O) -> T): Result<T> = Success(transform(value))
	}

	/**
	 * The request has failed.
	 */
	sealed interface Failure : Result<Nothing> {
		val message: String

		/**
		 * Converts this failure into a [Throwable] object.
		 */
		val throwable: Throwable

		override fun <T> map(transform: (Nothing) -> T): Result<T> = this

		data class Standard(
			val kind: Kind,
			override val message: String,
		) : Failure {

			override val throwable get() = StandardException()

			inner class StandardException : Exception("$kind: $message")

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

		data class Kotlin(override val throwable: Throwable) : Failure {
			override val message = throwable.message ?: "No known message"
		}
	}
}
