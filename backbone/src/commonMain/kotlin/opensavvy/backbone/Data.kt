package opensavvy.backbone

import kotlinx.coroutines.flow.*
import opensavvy.backbone.Data.Status
import opensavvy.backbone.Data.Status.Completed
import opensavvy.backbone.Data.Status.Loading
import opensavvy.backbone.Data.Status.Loading.Basic

/**
 * Successive values.
 *
 * Model objects should be immutable.
 * The [Data] type is immutable as well.
 * Mutation is represented as change over time.
 *
 * [State] is an asynchronous stream of immutable values.
 * By collecting the [State], it is possible to be notified when the value is updated.
 */
typealias State<O> = Flow<Data<O>>

/**
 * Builder type for [State].
 *
 * This type is used for conveniently building asynchronous flows in synchronous environments:
 * ```kotlin
 * val ref = /* … */
 * val result = flow {
 *     loading(ref)      // Mark the value as loading
 *     delay(1000)       // Wait for 1 second
 *     completed(ref, 5) // Mark the value as completed with result 5
 * }
 * ```
 */
typealias StateBuilder<O> = FlowCollector<Data<O>>

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
	 *
	 * In some contexts, this data is not associated with any object (for example, progression or error messages in a `create` endpoint).
	 * In these cases, [ref] may be `null` and [data] may not be [successful][Result.Success].
	 */
	val ref: Ref<O>?,
) {

	init {
		if (ref == null)
			require(data !is Result.Success) { "Data linked to no reference cannot be successful: $this" }
	}

	override fun toString() = "$data is $status for $ref"

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

		//region Access

		val <O> Data<O>.value: O? get() = (this.data as? Result.Success)?.value

		//endregion
		//region Creation DSL

		/**
		 * Marks [ref] as [successful][Result.Success] and [completed][Completed].
		 */
		suspend fun <O> StateBuilder<O>.markCompleted(ref: Ref<O>?, value: O) = emit(Data(Result.Success(value), Completed, ref))

		/**
		 * Marks [ref] as [failed][Result.Failure] and [completed][Completed].
		 */
		suspend fun <O> StateBuilder<O>.markFailed(ref: Ref<O>?, error: Result.Failure) = emit(Data(error, Completed, ref))

		/**
		 * Marks [ref] as [invalid][Result.Failure.Standard.Kind.Invalid] and [completed][Completed].
		 */
		suspend fun <O> StateBuilder<O>.markInvalid(ref: Ref<O>?, message: String) = markFailed(ref, Result.Failure.Standard(Result.Failure.Standard.Kind.Invalid, message))

		/**
		 * Marks [ref] as [unauthenticated][Result.Failure.Standard.Kind.Unauthenticated] and [completed][Completed].
		 */
		suspend fun <O> StateBuilder<O>.markUnauthenticated(ref: Ref<O>?, message: String) = markFailed(ref, Result.Failure.Standard(Result.Failure.Standard.Kind.Unauthenticated, message))

		/**
		 * Marks [ref] as [unauthorized][Result.Failure.Standard.Kind.Unauthorized] and [completed][Completed].
		 */
		suspend fun <O> StateBuilder<O>.markUnauthorized(ref: Ref<O>?, message: String) = markFailed(ref, Result.Failure.Standard(Result.Failure.Standard.Kind.Unauthorized, message))

		/**
		 * Marks [ref] as [not found][Result.Failure.Standard.Kind.NotFound] and [completed][Completed].
		 */
		suspend fun <O> StateBuilder<O>.markNotFound(ref: Ref<O>?, message: String) = markFailed(ref, Result.Failure.Standard(Result.Failure.Standard.Kind.NotFound, message))

		/**
		 * Marks [ref] as an [unknown failure][Result.Failure.Standard.Kind.Unknown] and [completed][Completed].
		 */
		suspend fun <O> StateBuilder<O>.markUnknownFailure(ref: Ref<O>?, message: String) = markFailed(ref, Result.Failure.Standard(Result.Failure.Standard.Kind.Unknown, message))

		/**
		 * Marks [ref] as [loading][Basic].
		 */
		suspend fun <O> StateBuilder<O>.markLoading(ref: Ref<O>?, progression: Float? = null) = emit(Data(Result.NoData, Basic(progression), ref))

		//endregion
		//region Result selection

		/**
		 * Skips the loading events in the [Flow].
		 *
		 * After calling this method, the flow will only emit values that are completely finished.
		 *
		 * @see firstResult
		 */
		fun <O> State<O>.skipLoading() = filter { it.status !is Loading }

		/**
		 * Returns the first non-loading element of this [Flow].
		 *
		 * Because this transforms a flow into a single value, the reactivity is lost.
		 * Only use this method in contexts where being notified on new values is not important.
		 *
		 * @see skipLoading
		 * @see firstSuccessOrThrow
		 */
		suspend fun <O> State<O>.firstResult() = skipLoading().first()

		/**
		 * Waits for the first concrete value ([Completed] and not [Result.NoData]).
		 *
		 * If it is [successful][Result.Success], the value is returned.
		 * If it is a [failure][Result.Failure], [Result.Failure.throwable] is thrown.
		 */
		suspend fun <O> State<O>.firstSuccessOrThrow(): O {
			val result = skipLoading()
				.filter { it.data !is Result.NoData }
				.first()

			return when (result.data) {
				is Result.NoData -> error("Impossible, we just filtered them out")
				is Result.Success -> result.data.value
				is Result.Failure -> throw result.data.throwable
			}
		}

		fun <In, Out> State<In>.map(ref: (Ref<In>) -> Ref<Out>, value: (In) -> Out): State<Out> = map {
			Data(
				it.data.map(value),
				it.status,
				it.ref?.let(ref),
			)
		}

		//endregion
	}
}
