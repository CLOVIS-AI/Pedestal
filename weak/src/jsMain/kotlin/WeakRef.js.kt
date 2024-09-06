package opensavvy.pedestal.weak

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
actual fun <T : Any> WeakRef(value: T): WeakRef<T> =
	JsWeakRef(value)

/**
 * Implementation of [WeakRef] backed by a JS [WeakRef][js.memory.WeakRef].
 *
 * JS doesn't make a difference between weak and soft references.
 */
@ExperimentalWeakApi
@Suppress("FunctionName")
actual fun <T : Any> SoftRef(value: T): WeakRef<T> =
	JsWeakRef(value)
