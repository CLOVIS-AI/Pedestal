package opensavvy.pedestal.weak.algorithms

import opensavvy.pedestal.weak.ExperimentalWeakApi
import opensavvy.pedestal.weak.WeakRef

private object EmptyWeakRefImpl : WeakRef<Nothing> {
	override fun read(): Nothing? = null

	override fun toString() = "EmptyWeakRef"
}

/**
 * A [WeakRef] implementation that doesn't store any value.
 *
 * It acts as if there was a value, but it was immediately freed.
 */
@Suppress("FunctionName")
@ExperimentalWeakApi
fun EmptyWeakRef(): WeakRef<Nothing> =
	EmptyWeakRefImpl

/**
 * A [WeakRef] implementation that immediately frees its [value].
 */
@Suppress("UNUSED_PARAMETER", "FunctionName")
@ExperimentalWeakApi
fun <T> EmptyWeakRef(value: T): WeakRef<T> =
	EmptyWeakRefImpl
