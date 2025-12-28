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
import js.collections.WeakMap as JsWeakMap

internal class JsWrappedWeakMap<K, V>(
	private val js: JsWeakMap<K & Any, V>,
) : WeakMap<K, V> {

	override fun get(key: K): V? {
		if (key == null) return null

		return js.get(key)
	}

	override fun set(key: K, value: V) {
		if (key == null) return

		js.set(key, value)
	}

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean {
		if (key == null) return false

		return js.has(key)
	}

	@ExperimentalWeakApi
	override fun remove(key: K): V? {
		if (key == null) return null

		/*
		 * js.delete(key) does not return the value, it just returns true/false.
		 * We emulate returning the value by 'getting' it first.
		 *
		 * There is a chance that:
		 * 1. We get the value
		 * 2. The GC deletes the value
		 * 3. We call 'js.delete(key)', which returns false
		 * 4. Yet will still return the value.
		 *
		 * This doesn't really matter because the value is indeed removed after the call.
		 * This would look to users like the GC removed the value just before the call to 'remove'
		 * rather than during it.
		 * Since the GC is allowed to remove values at any point, this is fine.
		 */

		val ret = js.get(key)

		js.delete(key)

		return ret
	}
}

/**
 * Wraps a JS [`WeakMap`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/WeakMap) into a multiplatform [WeakMap].
 *
 * The resulting weak map has similar properties as the JS map. Most importantly:
 * - The keys are held weakly.
 * - The values are held strongly.
 *
 * ### Type restrictions
 *
 * The returned map can accept `null` as a key even though the JS map would throw an error.
 * However, all mappings with a `null` key are ignored. That is, they behave as if they were
 * immediately deleted by the GC.
 *
 * The underlying map doesn't support JS primitive types, like [String], [Int], [Double] and [Boolean].
 */
@ExperimentalWeakApi
fun <K : Any, V> JsWeakMap<K, V>.asMultiplatform(): WeakMap<K, V> =
	JsWrappedWeakMap(this)
