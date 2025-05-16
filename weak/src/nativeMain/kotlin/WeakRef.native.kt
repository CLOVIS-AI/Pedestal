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

import opensavvy.pedestal.weak.algorithms.EmptyWeakRef
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
@ExperimentalNativeApi
actual fun <T> WeakRef(value: T): WeakRef<T> =
	if (value == null) EmptyWeakRef()
	else NativeWeakRef(value)

/**
 * Implementation of [WeakRef] backed by a native [WeakReference].
 *
 * Kotlin Native doesn't make a difference between weak and soft references.
 */
@ExperimentalNativeApi
@Suppress("FunctionName")
actual fun <T> SoftRef(value: T): WeakRef<T> =
	WeakRef(value)
