/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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
import opensavvy.pedestal.weak.WeakMap
import java.util.*

private class JvmWeakHashMap<K, V>(
	private val java: WeakHashMap<K, V>,
) : WeakMap<K, V> {

	override fun get(key: K): V? =
		java[key]

	override fun set(key: K, value: V) {
		java[key] = value
	}

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean =
		key in java

	@ExperimentalWeakApi
	override fun remove(key: K): V? =
		java.remove(key)

}

/**
 * Wraps a JVM [WeakHashMap] into a multiplatform [WeakMap].
 *
 * The resulting weak map has the exact same properties as the JVM map.
 * Most importantly:
 * - The keys are held weakly.
 * - The values are held strongly.
 * - The map is not thread-safe.
 */
@ExperimentalWeakApi
fun <K, V> WeakHashMap<K, V>.asMultiplatform(): WeakMap<K, V> =
	JvmWeakHashMap(this)
