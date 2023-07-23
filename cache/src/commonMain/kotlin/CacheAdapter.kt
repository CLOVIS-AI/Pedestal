package opensavvy.cache

import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.coroutines.captureProgress
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.success
import kotlin.jvm.JvmName

internal class CacheAdapter<I, F, T>(
	private val query: suspend (I) -> Outcome<F, T>,
) : Cache<I, F, T> {

	override fun get(id: I): ProgressiveFlow<F, T> = captureProgress { query(id) }

	override suspend fun update(values: Collection<Pair<I, T>>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expire(ids: Collection<I>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expireAll() {
		// This cache layer has no state, nothing to do
	}

	companion object
}

/**
 * Cache implementation which calls a given [transform] suspending function.
 *
 * This adapter is meant to be used as the first layer in a layer chain.
 * By itself, it does no caching (all calls to [get][Cache.get] call [transform]).
 * To learn more about layer chaining, or about the type parameters, see [Cache].
 *
 * ### Example
 *
 * ```kotlin
 * object NegativeNumber
 *
 * val squaredRoot = cache<Double, NegativeNumber, Double> {
 *     if (it >= 0) {
 *         sqrt(it).success()
 *     } else {
 *         NegativeInteger.failed()
 *     }
 * }
 *
 * println(squaredRoot[25.0].now()) // Success(5.0)
 * println(squaredRoot[-5.0].now()) // Failure(NegativeNumber)
 * ```
 */
fun <I, F, T> cache(transform: suspend (I) -> Outcome<F, T>): Cache<I, F, T> =
	CacheAdapter(transform)

/**
 * Cache implementation which calls a given [transform] suspending function.
 *
 * The [transform] function is considered always successful.
 * This allows to bypass the cache's error encoding.
 * For more information, see [InfallibleCache].
 *
 * This adapter is meant to be used as the first layer in a layer chain.
 * By itself, it does no caching (all calls to [get][Cache.get] call [transform]).
 * To learn more about layer chaining, or about the type parameters, see [Cache].
 *
 * ### Example
 *
 * ```kotlin
 * val squared = cache<Int, Int> { it * 2 }
 *
 * println(squared[5]) // Success(25)
 * ```
 */
@JvmName("infallibleCache")
fun <I, T> cache(transform: suspend (I) -> T): InfallibleCache<I, T> =
	CacheAdapter { transform(it).success() }
