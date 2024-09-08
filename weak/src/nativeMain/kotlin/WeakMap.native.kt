package opensavvy.pedestal.weak

import opensavvy.pedestal.weak.algorithms.WeakKeyArrayMap

/**
 * Instantiates a new, empty [WeakMap].
 *
 * This implementation uses [WeakKeyArrayMap].
 */
@ExperimentalWeakApi
actual fun <K, V> WeakMap(): WeakMap<K, V> =
	WeakKeyArrayMap()

/**
 * Instantiates a new [WeakMap] by copying [values].
 *
 * This implementation uses [WeakKeyArrayMap].
 */
@ExperimentalWeakApi
actual fun <K, V> WeakMap(values: Map<K, V>): WeakMap<K, V> =
	WeakKeyArrayMap<K, V>()
		.also { it.setAll(values) }
