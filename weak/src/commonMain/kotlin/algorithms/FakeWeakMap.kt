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
import opensavvy.pedestal.weak.WeakMap

private class FakeWeakMapImpl<K, V : Any> : WeakMap<K, V> {
	private val map = LinkedHashMap<K, V>()

	override fun get(key: K): V? =
		map[key]

	override fun set(key: K, value: V) {
		map[key] = value
	}

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean =
		map.containsKey(key)

	@ExperimentalWeakApi
	override fun remove(key: K): V? =
		map.remove(key)

	override fun toString() = "FakeWeakMap"
}

/**
 * A [WeakMap] implementation that isn't weak.
 *
 * That is, all stored elements are strongly held and are never freed automatically.
 * Elements are only removed when [WeakMap.remove] is called by the user.
 *
 * @see EmptyWeakMap Opposite behavior: values are immediately freed.
 */
@Suppress("FunctionName")
@ExperimentalWeakApi
fun <K, V : Any> FakeWeakMap(): WeakMap<K, V> =
	FakeWeakMapImpl()
