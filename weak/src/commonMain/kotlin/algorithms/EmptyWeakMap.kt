package opensavvy.pedestal.weak.algorithms

import opensavvy.pedestal.weak.ExperimentalWeakApi
import opensavvy.pedestal.weak.WeakMap

private class EmptyWeakMapImpl<K, V : Any> : WeakMap<K, V> {
	override fun get(key: K): V? = null
	override fun set(key: K, value: V) = Unit

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean = false

	@ExperimentalWeakApi
	override fun remove(key: K): V? = null

	override fun toString(): String = "EmptyWeakMap"
}

/**
 * A [WeakMap] implementation that immediately frees its elements.
 *
 * Values passed to [set][WeakMap.set] are never stored, so the map is always empty.
 *
 * Use this implementation when testing algorithms that use a weak map, to ensure they don't rely on the value
 * still existing.
 *
 * @see FakeWeakMap Opposite behavior: values are never freed.
 */
@Suppress("FunctionName")
@ExperimentalWeakApi
fun <K, V : Any> EmptyWeakMap(): WeakMap<K, V> =
	EmptyWeakMapImpl()
