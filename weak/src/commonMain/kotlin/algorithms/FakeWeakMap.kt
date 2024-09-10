package opensavvy.pedestal.weak.algorithms

import opensavvy.pedestal.weak.ExperimentalWeakApi
import opensavvy.pedestal.weak.WeakMap

private class FakeWeakMapImpl<K, V : Any> : WeakMap<K, V> {
	private val map = LinkedHashMap<K, V>()

	override fun get(key: K): V? =
		map[key]

	override fun set(key: K, value: V) {
		map[key] = value
	}

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean =
		map.containsKey(key)

	@ExperimentalWeakApi
	override fun remove(key: K): V? =
		map.remove(key)

	override fun toString() = "FakeWeakMap"
}

/**
 * A [WeakMap] implementation that isn't weak.
 *
 * That is, all stored elements are strongly held and are never freed automatically.
 * Elements are only removed when [WeakMap.remove] is called by the user.
 *
 * @see EmptyWeakMap Opposite behavior: values are immediately freed.
 */
@Suppress("FunctionName")
@ExperimentalWeakApi
fun <K, V : Any> FakeWeakMap(): WeakMap<K, V> =
	FakeWeakMapImpl()
