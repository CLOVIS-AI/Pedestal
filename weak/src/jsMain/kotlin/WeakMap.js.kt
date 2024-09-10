package opensavvy.pedestal.weak

import js.array.tupleOf

private class JsWeakMap<K, V>(
	private val wrapped: js.collections.WeakMap<K & Any, V>
) : WeakMap<K, V> {
	override fun get(key: K): V? {
		if (key == null)
			return null

		return wrapped[key]
	}

	override fun set(key: K, value: V) {
		if (key == null)
			return

		wrapped[key] = value
	}

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean {
		if (key == null)
			return false

		return wrapped.has(key)
	}

	@ExperimentalWeakApi
	override fun remove(key: K): V? {
		if (key == null)
			return null

		val current = get(key)
		wrapped.delete(key)
		return current
	}

	override fun toString(): String =
		wrapped.toString()
}

/**
 * Instantiates a new, empty [WeakMap].
 *
 * This implementation is backed by a JS [WeakMap][js.collections.WeakMap].
 */
@ExperimentalWeakApi
actual fun <K, V> WeakMap(): WeakMap<K, V> =
	JsWeakMap(js.collections.WeakMap<K & Any, V>())

/**
 * Instantiates a new [WeakMap] by copying [values].
 *
 * This implementation is backed by a JS [WeakMap][js.collections.WeakMap].
 */
@ExperimentalWeakApi
actual fun <K, V> WeakMap(values: Map<K, V>): WeakMap<K, V> {
	val map = js.collections.WeakMap<K & Any, V>(
		values
			.mapNotNull { (k, v) ->
				if (k == null)
					null
				else
					tupleOf(k, v)
			}
			.toTypedArray()
	)

	return JsWeakMap(map)
}
