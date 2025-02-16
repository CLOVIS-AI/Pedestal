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

package opensavvy.cache

import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.onSuccess
import opensavvy.state.progressive.successfulWithProgress
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.time.Duration.Companion.seconds

@ExperimentalCacheApi
private class BrowserStorageCache<Identifier, Failure, Value>(
	private val upstream: Cache<Identifier, Failure, Value>,
	private val keyPrefix: String,
	private val storage: Storage,
	private val serializeIdentifier: (Identifier) -> String?,
	private val serializeValue: (Value) -> String?,
	private val deserializeValue: (String) -> Value?,
) : Cache<Identifier, Failure, Value> {

	/**
	 * Generates the storage key for the identifier [id].
	 *
	 * This mixes the [id] with the [keyPrefix], to avoid conflicts between multiple cache instances at the storage layer.
	 *
	 * If this function returns `null`, generating the key failed and caching in this layer should be aborted.
	 */
	private fun keyFor(id: Identifier): String? {
		val serializedId = serializeIdentifier(id) ?: return null
		return "$keyPrefix-$serializedId"
	}

	override fun get(id: Identifier): ProgressiveFlow<Failure, Value> = flow {
		val key = keyFor(id)

		// We are in an infinite flow, it ends when the subscriber unsubscribes
		while (true) {
			val stored = key
				?.let(storage::get)
				?.let(deserializeValue)

			if (stored != null) {
				emit(stored.successfulWithProgress())
			} else {
				emitAll(
					upstream[id]
						.onEach { outcome -> outcome.onSuccess { update(id, it) } }
				)
			}

			delay(5.seconds)
		}
	}

	override suspend fun update(values: Collection<Pair<Identifier, Value>>) {
		for ((id, value) in values) {
			val key = keyFor(id) ?: continue
			val serializedValue = serializeValue(value) ?: continue
			storage[key] = serializedValue
		}

		upstream.update(values)
	}

	override suspend fun expire(ids: Collection<Identifier>) {
		for (id in ids) {
			val key = keyFor(id) ?: continue
			storage.removeItem(key)
		}

		upstream.expire(ids)
	}

	override suspend fun expireAll() {
		storage.clear()
		upstream.expireAll()
	}
}

/**
 * In-browser [Cache] layer.
 *
 * ### General behavior
 *
 * Updates from the previous cache are stored in the [storage] engine.
 * When [get][Cache.get] is called, results are returned from the storage engine if available.
 * Otherwise, the request is transmitted to the previous layer.
 *
 * Elements are deleted when the [storage] engine when [expire][Cache.expire] is called.
 * To reduce the size of the cache automatically, add a subsequent layer for it (e.g. [expireAfter]).
 *
 * ### Observability
 *
 * Currently, the implementation is based on polling.
 * If the value changes in the cache, all cache instances notice after a few seconds.
 * This can be used to share values between tabs: if a cache instance in a tab gets a result, other instances in other tabs
 * will notice them and avoid starting new requests.
 *
 * However, the current implementation only stores values when they are successful, so if two tabs start the same request at the same time,
 * they won't be deduplicated (both requests will go through). Failed values are not stored either.
 *
 * ### Example
 *
 * ```kotlin
 * fun serializeKey(id: Int) = id.toString()
 * fun serializeValue(value: Double) = value.toString()
 * fun deserializeValue(value: String) = value.toDouble()
 *
 * val squareRoot = cache<Int, Double> { sqrt(it) }
 *     .cachedInBrowserStorage(window.localStorage, "sqrt", serializeKey, serializeValue, deserializeValue)
 *     .cachedInMemory(…)
 *     .expireAfter(…)
 * ```
 *
 * @param storage The browser storage engine to use.
 * @param keyPrefix A short string that is appended before the identifier.
 * The prefix is important to avoid naming conflicts with other values that may be stored in the [storage] engine.
 * @param serializeIdentifier A function that converts an identifier to a [String]. If the function returns `null`, this layer assumes the value cannot be stored and no-ops.
 * @param serializeValue A function that converts a cached value to a [String]. If the function returns `null`, this layer assumes the value cannot be stored and no-ops.
 * @param deserializeValue A function that converts a string generated by [serializeValue] into its original value. If the function returns `null`, this layer assumes the value cannot be stored and no-ops.
 */
@ExperimentalCacheApi
fun <Identifier, Failure, Value : Any> Cache<Identifier, Failure, Value>.cachedInBrowserStorage(
	storage: Storage,
	keyPrefix: String,
	serializeIdentifier: (Identifier) -> String?,
	serializeValue: (Value) -> String?,
	deserializeValue: (String) -> Value?,
): Cache<Identifier, Failure, Value> =
	BrowserStorageCache(this, keyPrefix, storage, serializeIdentifier, serializeValue, deserializeValue)

/**
 * In-browser [Cache] implementation that is shared between tabs and persists when the browser is closed.
 *
 * This function is equivalent for [cachedInBrowserStorage] using the [local storage](https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage) engine.
 * See the [cachedInBrowserStorage] documentation for more information.
 */
@ExperimentalCacheApi
fun <Identifier, Failure, Value : Any> Cache<Identifier, Failure, Value>.cachedInLocalStorage(
	keyPrefix: String,
	serializeIdentifier: (Identifier) -> String?,
	serializeValue: (Value) -> String?,
	deserializeValue: (String) -> Value?,
) = cachedInBrowserStorage(localStorage, keyPrefix, serializeIdentifier, serializeValue, deserializeValue)

/**
 * In-browser [Cache] implementation that is cleared when the tab is closed.
 *
 * This function is equivalent for [cachedInBrowserStorage] using the [session storage](https://developer.mozilla.org/en-US/docs/Web/API/Window/sessionStorage) engine.
 * See the [cachedInBrowserStorage] documentation for more information.
 */
@ExperimentalCacheApi
fun <Identifier, Failure, Value : Any> Cache<Identifier, Failure, Value>.cachedInSessionStorage(
	keyPrefix: String,
	serializeIdentifier: (Identifier) -> String?,
	serializeValue: (Value) -> String?,
	deserializeValue: (String) -> Value?,
) = cachedInBrowserStorage(sessionStorage, keyPrefix, serializeIdentifier, serializeValue, deserializeValue)
