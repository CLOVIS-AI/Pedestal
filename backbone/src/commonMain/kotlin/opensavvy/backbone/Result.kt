package opensavvy.backbone

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
	 * The request has not finished yet, and no previous result is available.
	 */
	object NoData : Result<Nothing> {
		override fun toString() = "NoData"
	}

	/**
	 * The request is successful, [value] contains its result.
	 */
	data class Success<O>(val value: O) : Result<O> {
		override fun toString() = "Success($value)"
	}

	/**
	 * The request has failed.
	 */
	interface Failure : Result<Nothing> {
		/**
		 * Converts this failure into a [Throwable] object.
		 */
		fun toThrowable(): Throwable
	}
}
