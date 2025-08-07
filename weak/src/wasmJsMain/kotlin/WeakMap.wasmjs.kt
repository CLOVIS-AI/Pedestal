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

package opensavvy.pedestal.weak

import opensavvy.pedestal.weak.algorithms.WeakKeyArrayMap

/**
 * Instantiates a new, empty [WeakMap].
 *
 * This implementation uses [WeakKeyArrayMap].
 */
@ExperimentalWeakApi
actual fun <K, V> WeakMap(): WeakMap<K, V> =
	WeakKeyArrayMap()

/**
 * Instantiates a new [WeakMap] by copying [values].
 *
 * This implementation uses [WeakKeyArrayMap].
 */
@ExperimentalWeakApi
actual fun <K, V> WeakMap(values: Map<K, V>): WeakMap<K, V> =
	WeakKeyArrayMap<K, V>()
		.also { it.setAll(values) }
