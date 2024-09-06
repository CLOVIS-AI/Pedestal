package opensavvy.pedestal.weak

import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

private class JavaReferenceHolder<T : Any>(
	private val reference: Reference<T>,
) : WeakRef<T> {

	override fun read(): T? =
		reference.get()

	override fun toString(): String =
		reference.toString()
}

/**
 * Interprets a Java [reference] into a Kotlin [WeakRef].
 */
fun <T : Any> WeakRef.Companion.fromJava(reference: Reference<T>): WeakRef<T> =
	JavaReferenceHolder(reference)

/**
 * Implementation of [WeakRef] backed by a JVM [WeakReference].
 */
@ExperimentalWeakApi
actual fun <T : Any> WeakRef(value: T): WeakRef<T> =
	JavaReferenceHolder(WeakReference(value))

/**
 * Implementation of [WeakRef] backed by a JVM [SoftReference].
 */
@ExperimentalWeakApi
@Suppress("FunctionName")
actual fun <T : Any> SoftRef(value: T): WeakRef<T> =
	JavaReferenceHolder(SoftReference(value))
