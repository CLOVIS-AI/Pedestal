/*
 * Copyright (c) 2022-2025, OpenSavvy and contributors.
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

package opensavvy.backbone

import opensavvy.cache.Cache
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.coroutines.now

/**
 * A reference to a specific [object][Value].
 *
 * A reference is a small object that allows to pass around an object from an API without querying it.
 * A reference should always be immutable.
 * Each reference has a matching [Backbone] object responsible for managing it.
 *
 * [Ref] implementation should ensure that their [equals] and [hashCode] functions are correct.
 *
 * To access the value behind a reference, use [request].
 *
 * ### Note for implementors
 *
 * When implementing this interface, it is common to provide functions to all mutating methods from the matching
 * [Backbone] as wrappers to it. This makes using the reference easier.
 *
 * @param Value The object this reference refers to.
 * @param Failure Failures that may be returned when calling [request].
 */
interface Ref<Failure, Value> {

	/**
	 * Requests the referenced data.
	 *
	 * It is encouraged, but not mandatory, to implement this method using [Cache].
	 */
	fun request(): ProgressiveFlow<Failure, Value>

	companion object
}

/**
 * Requests the referenced data, returning the first value returned by the cache.
 *
 * This function returns a single value and not a subscription, it is not recommended to use it when being notified of
 * new values is important (e.g. in a UI), in which case you should use [Ref.request].
 * This function is intended for non-reactive environments (e.g. server requests, tests…).
 */
suspend fun <Failure, Value> Ref<Failure, Value>.now() = request().now()
