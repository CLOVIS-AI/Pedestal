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
