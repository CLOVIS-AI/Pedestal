/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.pedestal.weak

import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

private class JavaReferenceHolder<T>(
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
actual fun <T> WeakRef(value: T): WeakRef<T> =
	JavaReferenceHolder(WeakReference(value))

/**
 * Implementation of [WeakRef] backed by a JVM [SoftReference].
 */
@Suppress("FunctionName")
actual fun <T> SoftRef(value: T): WeakRef<T> =
	JavaReferenceHolder(SoftReference(value))
