package opensavvy.state

import kotlinx.coroutines.flow.*
import opensavvy.state.Slice.Companion.valueOrNull
import opensavvy.state.Slice.Companion.valueOrThrow

/**
 * Tracks changes to values through time.
 *
 * Stored values should be immutable.
 * At any given time, the current value is represented by the immutable [Slice] class.
 * This [State] flow allows to subscribe to the future slices.
 *
 * To create an instance of [State], see the [state] builder.
 */
typealias State<T> = Flow<Slice<T>>

//region Event selection

/**
 * Skips the loading events in [State].
 */
fun <T> State<T>.skipLoading() = filter { it.progression !is Progression.Loading }

/**
 * Returns the first non-loading element of this [State].
 *
 * Because this transforms a flow into a single value, the reactivity is lost.
 * Only use this method in contexts where being notified on new values is not important.
 */
suspend fun <T> State<T>.firstResult() = skipLoading().first()

/**
 * Returns the first non-loading element's [valueOrNull] of this [State].
 *
 * Because this transforms a flow into a single value, the reactivity is lost.
 * Only use this method in contexts where being notified on new values is not important.
 */
suspend fun <T : Any> State<T>.firstResultOrNull() = firstResult().valueOrNull

/**
 * Returns the first non-loading element's [valueOrThrow] of this [State].
 *
 * Because this transforms a flow into a single value, the reactivity is lost.
 * Only use this method in contexts where being notified on new values is not important.
 */
suspend fun <T> State<T>.firstResultOrThrow() = firstResult().valueOrThrow

//endregion
//region Error management

inline fun <T> State<T>.onEachSuccess(crossinline block: (T) -> Unit): State<T> = onEach { slice ->
	val (status, _) = slice

	if (status is Status.Successful)
		block(status.value)
}

inline fun <I, O> State<I>.mapSuccess(crossinline transform: (I) -> O): State<O> = map { slice ->
	slice.mapSuccess(transform)
}

inline fun <I, O> State<I>.mapSuccessSlice(crossinline transform: (I) -> Slice<O>): State<O> = map {
	val (status, progression) = it

	when (status) {
		is Status.Failed -> Slice(status, progression)
		is Status.Pending -> Slice(status, progression)
		is Status.Successful -> transform(status.value)
	}
}

fun <I, O> State<I>.flatMapSuccess(transform: suspend StateBuilder<O>.(I) -> Unit): State<O> = transform {
	val (status, progression) = it

	when (status) {
		is Status.Failed -> emit(Slice(status, progression))
		is Status.Pending -> emit(Slice(status, progression))
		is Status.Successful -> emitAll(state { transform(status.value) })
	}
}

//endregion
