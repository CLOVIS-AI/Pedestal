package opensavvy.pedestal.weak

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

@ExperimentalNativeApi
private class NativeWeakRef<T : Any>(
	value: T,
) : WeakRef<T> {

	private val reference = WeakReference(value)

	override fun read(): T? =
		reference.get()

}

/**
 * Implementation of [WeakRef] backed by a native [WeakReference].
 *
 * Kotlin Native doesn't make a difference between weak and soft references.
 */
@ExperimentalWeakApi
@ExperimentalNativeApi
actual fun <T : Any> WeakRef(value: T): WeakRef<T> =
	NativeWeakRef(value)

/**
 * Implementation of [WeakRef] backed by a native [WeakReference].
 *
 * Kotlin Native doesn't make a difference between weak and soft references.
 */
@ExperimentalWeakApi
@ExperimentalNativeApi
@Suppress("FunctionName")
actual fun <T : Any> SoftRef(value: T): WeakRef<T> =
	NativeWeakRef(value)
