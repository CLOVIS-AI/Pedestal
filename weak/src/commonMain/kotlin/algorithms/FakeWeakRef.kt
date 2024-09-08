package opensavvy.pedestal.weak.algorithms

import opensavvy.pedestal.weak.ExperimentalWeakApi
import opensavvy.pedestal.weak.WeakRef

/**
 * Fake implementation of [WeakRef].
 *
 * Instead of being freed by the garbage-collector, this implementation is only
 * freed when [clear] is called.
 *
 * Use this implementation to help trigger edge cases in algorithms that use weak references.
 */
@ExperimentalWeakApi
class FakeWeakRef<T>(
	value: T
) : WeakRef<T> {
	private var value: T? = value

	override fun read(): T? =
		value

	fun clear() {
		value = null
	}
}
