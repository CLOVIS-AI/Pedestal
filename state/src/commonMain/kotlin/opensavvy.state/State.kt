package opensavvy.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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
typealias State<I, T> = Flow<Slice<I, T>>

/**
 * Skips the loading events in [State].
 */
fun <I : Identifier<T>, T> State<I, T>.skipLoading() = filter { it.progression !is Progression.Loading }

/**
 * Returns the first non-loading element of this [State].
 *
 * Because this transforms a flow into a single value, the reactivity is lost.
 * Only use this method in contexts where being notified on new values is not important.
 */
suspend fun <I : Identifier<T>, T> State<I, T>.firstResult() = skipLoading().first()

/**
 * Returns the first non-loading element's [valueOrNull] of this [State].
 *
 * Because this transforms a flow into a single value, the reactivity is lost.
 * Only use this method in contexts where being notified on new values is not important.
 */
suspend fun <I : Identifier<T>, T : Any> State<I, T>.firstResultOrNull() = firstResult().valueOrNull

/**
 * Returns the first non-loading element's [valueOrThrow] of this [State].
 *
 * Because this transforms a flow into a single value, the reactivity is lost.
 * Only use this method in contexts where being notified on new values is not important.
 */
suspend fun <I : Identifier<T>, T> State<I, T>.firstResultOrThrow() = firstResult().valueOrThrow
