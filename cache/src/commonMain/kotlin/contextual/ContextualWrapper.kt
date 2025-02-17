/*
 * Copyright (c) 2023-2025, OpenSavvy and contributors.
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

package opensavvy.cache.contextual

import opensavvy.cache.Cache
import opensavvy.state.coroutines.ProgressiveFlow

/**
 * Implementation of [Cache] for a [ContextualCache].
 */
internal class ContextualWrapper<I, C, F, V>(
	private val upstream: ContextualCache<I, C, F, V>,
) : Cache<Pair<I, C>, F, V> {

	override fun get(id: Pair<I, C>): ProgressiveFlow<F, V> {
		val (ref, context) = id
		return upstream[ref, context]
	}

	override suspend fun update(values: Collection<Pair<Pair<I, C>, V>>) {
		upstream.update(values.map {
			val (identifier, value) = it
			val (id, context) = identifier
			Triple(id, context, value)
		})
	}

	override suspend fun expire(ids: Collection<Pair<I, C>>) {
		upstream.expireContextual(ids)
	}

	override suspend fun expireAll() {
		upstream.expireAll()
	}
}
