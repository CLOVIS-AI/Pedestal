package opensavvy.state

import kotlinx.coroutines.flow.map
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
class Slice<I : Identifier<T>, out T> private constructor(
	val id: I?,
	val status: Status<T>,
	val progression: Progression,
) {

	//region Destructuration

	operator fun component1() = status
	operator fun component2() = progression
	operator fun component3() = id

	//endregion
	//region equals & hashCode

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Slice<*, *>) return false

		if (id != other.id) return false
		if (status != other.status) return false
		if (progression != other.progression) return false

		return true
	}

	override fun hashCode(): Int {
		var result = id?.hashCode() ?: 0
		result = 31 * result + status.hashCode()
		result = 31 * result + progression.hashCode()
		return result
	}

	//endregion
	//region toString

	override fun toString(): String {
		val builder = StringBuilder()

		if (id != null) {
			builder.append(id.toString())
			builder.append(": ")
		}

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
		fun <I : Identifier<T>, T> pending(
			id: I?,
			progression: Progression.Loading,
		) = Slice(id, Status.Pending, progression)

		/**
		 * Creates a [Slice] with a [pending status][Status.Pending] and a current progression.
		 */
		fun <I : Identifier<T>, T> pending(
			id: I?,
		) = Slice(id, Status.Pending, Progression.loading())

		/**
		 * Creates a [Slice] with a [pending status][Status.Pending] and a current [progression].
		 */
		fun <I : Identifier<T>, T> pending(
			id: I?,
			progression: Double,
		) = Slice(id, Status.Pending, Progression.loading(progression))

		/**
		 * Creates a [Slice] with a [successful status][Status.Successful].
		 */
		fun <I : Identifier<T>, T> successful(
			id: I,
			value: T,
			progression: Progression = Progression.Done,
		) = Slice(id, Status.Successful(value), progression)

		/**
		 * Creates a [Slice] with a [failed status][Status.Failed].
		 *
		 * More specifically, the status is a [Status.ExceptionFailure].
		 */
		fun <I : Identifier<T>, T> failed(
			id: I?,
			exception: RuntimeException,
			message: String,
			progression: Progression = Progression.Done,
		) = Slice(id, Status.ExceptionFailure(message, exception), progression)

		/**
		 * Creates a [Slice] with a [failed status][Status.Failed].
		 *
		 * More specifically, the status is a [Status.StandardFailure].
		 */
		fun <I : Identifier<T>, T> failed(
			id: I?,
			kind: Status.StandardFailure.Kind,
			message: String,
			cause: Throwable? = null,
			progression: Progression = Progression.Done,
		) = Slice(id, Status.StandardFailure(kind, message, cause), progression)

		//endregion
		//region Accessors

		/**
		 * Returns the value of this slice, or `null` if it isn't successful.
		 *
		 * @see Status.Successful.value
		 */
		val <I : Identifier<T>, T : Any> Slice<I, T>.valueOrNull: T?
			get() = (status as? Status.Successful)?.value

		/**
		 * Returns the value of this slice, or throws an exception if it isn't successful.
		 *
		 * @see Status.Successful.value
		 * @see Status
		 * @throws NoSuchElementException This slice is in the [pending][Status.Pending] status.
		 * @throws Status.Failed This slice is in the [failed][Status.Failed] status.
		 */
		val <I : Identifier<T>, T> Slice<I, T>.valueOrThrow: T
			get() = when (status) {
				is Status.Successful -> status.value
				is Status.Pending -> throw NoSuchElementException("No value is available for this object, it is still pending: $this")
				is Status.Failed -> throw status
			}

		//endregion
		//region Operators

		/**
		 * Converts the identifier of the incoming state from [I] to [O] using [transform].
		 */
		fun <I : Identifier<T>, O : Identifier<T>, T> State<I, T>.mapIdentifier(transform: (I) -> O) =
			map { Slice(it.id?.let(transform), it.status, it.progression) }

		//endregion

	}
}
