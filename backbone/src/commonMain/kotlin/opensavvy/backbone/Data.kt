package opensavvy.backbone

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import opensavvy.backbone.Data.Status
import opensavvy.backbone.Data.Status.Completed
import opensavvy.backbone.Data.Status.Loading
import opensavvy.backbone.Data.Status.Loading.Basic

/**
 * Wrapper around a data object of type [O].
 *
 * This class represents the state at a specific time of the result of requesting a [reference][Ref].
 * At that specific time, the following combinaisons are possible:
 * - the request is finished and successful,
 * - the request is finished and failed,
 * - the request is not finished,
 * - the request is not finished, but we remember the previous value for the same request.
 *
 * These states are encoded by the values of [data] and [status], for example a finished request that was successful will
 * have [data] be an instance of [Result.Success], with the result being available using [Result.Success.value], and the
 * [status] attribute will be [Status.Completed].
 * Read the documentation of the various values to understand all the possible combinaisons.
 *
 * Use destructuration to easily access the various values:
 * ```kotlin
 * val (data, status) = someData()
 * ```
 *
 * This class represents the state of some data at a specific instant in time, which it does not store.
 * This class will often appear inside a [Flow], allowing to subscribe to updates to the state as requests terminate.
 */
data class Data<O>(
	/**
	 * The data itself.
	 *
	 * For more information, read the [Result] documentation.
	 */
	val data: Result<O>,
	/**
	 * Whether there is an ongoing request to update the [data].
	 *
	 * For more information, read the [Status] documentation.
	 */
	val status: Status,
	/**
	 * The reference to the data this object represents, which was used to initiate the request.
	 */
	val ref: Ref<O>,
) {

	override fun toString() = "Data($data is $status for $ref)"

	/**
	 * Whether a piece of [Data] is [Completed] or still [Loading].
	 */
	sealed class Status {

		/**
		 * No requests are ongoing for the given [reference][ref].
		 *
		 * @see Loading
		 */
		object Completed : Status() {
			override fun toString() = "Completed"
		}

		/**
		 * A request is ongoing for the given [reference][ref].
		 *
		 * This object stores the amount of [progress][progression] of the ongoing request, which can be used to display
		 * an indicator to a user.
		 *
		 * See [Basic] for a simple implementation of this class.
		 *
		 * @see Completed
		 */
		abstract class Loading(
			/**
			 * How much progress the ongoing request is at.
			 *
			 * If it is not possible to know the amount of progress, `null` is used.
			 * Otherwise, the progression is a number between 0 (inclusive, no progress has been made)
			 * and 1 (inclusive, the request is completely finished).
			 *
			 * The value of 1 is allowed for ease of programming, but it doesn't mean anything: a value which has fully
			 * finished loading should have a status of [Completed], not "Loading 100%".
			 */
			val progression: Float?,
		) : Status() {

			/**
			 * How much progress the ongoing request is at, in percents.
			 *
			 * This is a convenience property around [progression].
			 * Much like it, `null` is returned when the progress is unknown.
			 * When progress is known, this returns an integer value between 0 (inclusive, no progress has been made)
			 * and 100 (inclusive, the request has finished).
			 */
			val percent: Int?
				get() = progression
					?.times(100)
					?.toInt()

			init {
				if (progression != null)
					require(progression in 0f..1f) { "The progression must be either null or between 0 and 1 (inclusive), found $progression" }
			}

			/**
			 * Simple implementation of the [Loading] abstract class.
			 *
			 * This class is a strict implementation without any additional features.
			 * If you need to add another field (e.g. for transfer units like Mb, speed indicators like Mb/s), create
			 * your own subclass of [Loading] instead of using this one.
			 *
			 * @param progression See [Loading.progression]
			 */
			class Basic(progression: Float? = null) : Loading(progression) {
				override fun toString() =
					if (progression != null) "Loading.Basic(progression = $progression)"
					else "Loading.Basic"

				override fun equals(other: Any?): Boolean {
					if (this === other) return true
					if (other !is Loading) return false
					return progression == other.progression
				}

				override fun hashCode(): Int {
					return progression.hashCode()
				}
			}
		}
	}

	companion object {
		/**
		 * The default empty data that can be used as a stand-in for a future value.
		 *
		 * This can be used as the argument of React's `useState` or Compose's `collectAsState`.
		 */
		val <O> Ref<O>.initialData
			get() = Data(Result.NoData, Basic(), this)

		/**
		 * Skips the loading events in the [Flow].
		 *
		 * After calling this method, the flow will only emit values that are completely finished.
		 *
		 * @see firstResult
		 */
		fun <O> Flow<Data<O>>.skipLoading() = filter { it.status !is Loading }

		/**
		 * Returns the first non-loading element of this [Flow].
		 *
		 * Because this transforms a flow into a single value, the reactivity is lost.
		 * Only use this method in contexts where being notified on new values is not important.
		 *
		 * @see skipLoading
		 * @see firstSuccessOrThrow
		 */
		suspend fun <O> Flow<Data<O>>.firstResult() = skipLoading().first()

		/**
		 * Waits for the first concrete value ([Completed] and not [Result.NoData]).
		 *
		 * If it is [successful][Result.Success], the value is returned.
		 * If it is a [failure][Result.Failure], [Result.Failure.toThrowable] is thrown.
		 */
		suspend fun <O> Flow<Data<O>>.firstSuccessOrThrow(): O {
			val result = skipLoading()
				.filter { it.data !is Result.NoData }
				.first()

			return when (result.data) {
				is Result.NoData -> error("Impossible, we just filtered them out")
				is Result.Success -> result.data.value
				is Result.Failure -> throw result.data.toThrowable()
			}
		}
	}
}
