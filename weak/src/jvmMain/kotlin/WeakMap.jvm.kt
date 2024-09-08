package opensavvy.pedestal.weak

import java.util.*

private class JavaWeakMap<K, V : Any>(
	private val wrapped: WeakHashMap<K, V>
) : WeakMap<K, V> {

	override fun get(key: K): V? =
		wrapped[key]

	override fun set(key: K, value: V) {
		wrapped[key] = value
	}

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean =
		wrapped.containsKey(key)

	@ExperimentalWeakApi
	override fun remove(key: K): V? =
		wrapped.remove(key)

	override fun toString(): String =
		wrapped.toString()
}

/**
 * Instantiates a new, empty [WeakMap].
 *
 * This implementation is backed by a Java [WeakHashMap].
 */
@ExperimentalWeakApi
actual fun <K, V : Any> WeakMap(): WeakMap<K, V> =
	JavaWeakMap(WeakHashMap())

/**
 * Instantiates a new [WeakMap] by copying [values].
 *
 * This implementation is backed by a Java [WeakHashMap].
 */
@ExperimentalWeakApi
actual fun <K, V : Any> WeakMap(values: Map<K, V>): WeakMap<K, V> =
	JavaWeakMap(WeakHashMap(values))
