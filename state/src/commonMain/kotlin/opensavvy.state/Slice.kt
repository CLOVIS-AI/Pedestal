package opensavvy.state

import opensavvy.state.Slice.Companion.failed
import opensavvy.state.Slice.Companion.pending
import opensavvy.state.Slice.Companion.successful

/**
 * Some data at a specific point in time.
 *
 * The [State] of a value can change over time.
 * At each specific instant in time, the 'current state' is represented by this class.
 *
 * Instances can be generated with the provided factories: [pending], [successful] and [failed].
 */
data class Slice<out T>(
	val status: Status<T>,
	val progression: Progression,
) {

	//region toString

	override fun toString(): String {
		val builder = StringBuilder()

		if (status != Status.Pending) {
			builder.append(status)
			builder.append(" ")
		}

		builder.append(progression)

		return builder.toString()
	}

	//endregion

	companion object {

		//region Factories

		/**
		 * Creates a [Slice] with a [pending status][Status.Pending].
		 */
		fun pending(
			progression: Progression.Loading,
		) = Slice(Status.Pending, progression)

		/**
		 * Creates a [Slice] with a [pending status][Status.Pending] and a current progression.
		 */
		fun pending() = Slice(Status.Pending, Progression.loading())

		/**
		 * Creates a [Slice] with a [pending status][Status.Pending] and a current [progression].
		 */
		fun pending(
			progression: Double,
		) = Slice(Status.Pending, Progression.loading(progression))

		/**
		 * Creates a [Slice] with a [successful status][Status.Successful].
		 */
		fun <T> successful(
			value: T,
			progression: Progression = Progression.Done,
		) = Slice(Status.Successful(value), progression)

		/**
		 * Creates a [Slice] with a [failed status][Status.Failed].
		 *
		 * More specifically, the status is a [Status.ExceptionFailure].
		 */
		fun failed(
			exception: RuntimeException,
			message: String,
			progression: Progression = Progression.Done,
		) = Slice(Status.ExceptionFailure(message, exception), progression)

		/**
		 * Creates a [Slice] with a [failed status][Status.Failed].
		 *
		 * More specifically, the status is a [Status.StandardFailure].
		 */
		fun failed(
			kind: Status.StandardFailure.Kind,
			message: String,
			cause: Throwable? = null,
			progression: Progression = Progression.Done,
		) = Slice(Status.StandardFailure(kind, message, cause), progression)

		//endregion
		//region Accessors

		/**
		 * Returns the value of this slice, or `null` if it isn't successful.
		 *
		 * @see Status.Successful.value
		 */
		val <T : Any> Slice<T>.valueOrNull: T?
			get() = (status as? Status.Successful)?.value

		/**
		 * Returns the value of this slice, or throws an exception if it isn't successful.
		 *
		 * @see Status.Successful.value
		 * @see Status
		 * @throws NoSuchElementException This slice is in the [pending][Status.Pending] status.
		 * @throws Status.Failed This slice is in the [failed][Status.Failed] status.
		 */
		val <T> Slice<T>.valueOrThrow: T
			get() = when (status) {
				is Status.Successful -> status.value
				is Status.Pending -> throw NoSuchElementException("No value is available for this object, it is still pending: $this")
				is Status.Failed -> throw status
			}

		//endregion

	}
}

//region Error management

/**
 * Maps a successful slice from [I] to [O] using [transform].
 *
 * If the slice is not successful, it is kept unchanged.
 */
inline fun <I, O> Slice<I>.mapSuccess(transform: (I) -> O): Slice<O> {
	val (status, progression) = this

	val newStatus: Status<O> = when (status) {
		is Status.Failed -> status
		is Status.Pending -> status
		is Status.Successful -> Status.Successful(transform(status.value))
	}

	return Slice(newStatus, progression)
}

//endregion
