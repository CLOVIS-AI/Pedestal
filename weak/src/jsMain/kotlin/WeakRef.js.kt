package opensavvy.pedestal.weak

import opensavvy.pedestal.weak.algorithms.EmptyWeakRef

private class JsWeakRef<T : Any>(
	value: T
) : WeakRef<T> {

	private val reference = js.memory.WeakRef(value)

	override fun read(): T? =
		reference.deref()
			.takeIf { it != undefined }

	override fun toString(): String =
		reference.toString()
}

/**
 * Implementation of [WeakRef] backed by a JS [WeakRef][js.memory.WeakRef].
 *
 * JS doesn't make a difference between weak and soft references.
 */
@ExperimentalWeakApi
actual fun <T> WeakRef(value: T): WeakRef<T> =
	if (value == null) EmptyWeakRef(value)
	else JsWeakRef(value)

/**
 * Implementation of [WeakRef] backed by a JS [WeakRef][js.memory.WeakRef].
 *
 * JS doesn't make a difference between weak and soft references.
 */
@ExperimentalWeakApi
@Suppress("FunctionName")
actual fun <T> SoftRef(value: T): WeakRef<T> =
	WeakRef(value)
