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

import kotlinx.coroutines.Job
import opensavvy.cache.MemoryCache
import opensavvy.cache.cachedInMemory
import opensavvy.state.coroutines.ProgressiveFlow

internal class ContextualMemoryCache<I, C, F, V>(
	upstream: ContextualCache<I, C, F, V>,
	job: Job,
) : ContextualCache<I, C, F, V> {

	private val cache = ContextualWrapper(upstream)
		.cachedInMemory(job) as MemoryCache<Pair<I, C>, F, V>

	override fun get(id: I, context: C): ProgressiveFlow<F, V> =
		cache[id to context]

	override suspend fun update(values: Collection<Triple<I, C, V>>) =
		cache.update(values.map { (id, context, value) -> id to context to value })

	override suspend fun expire(ids: Collection<I>) {
		val filterIds = ids.toSet()

		cache.expireIf { (id, _) -> id in filterIds }
	}

	override suspend fun expireContextual(ids: Collection<Pair<I, C>>) =
		cache.expire(ids)

	override suspend fun expireAll() =
		cache.expireAll()

}

/**
 * In-memory [ContextualCache] layer.
 *
 * @see opensavvy.cache.cachedInMemory Non-contextual equivalent
 */
fun <Identifier, Context, Failure, Value> ContextualCache<Identifier, Context, Failure, Value>.cachedInMemory(job: Job): ContextualCache<Identifier, Context, Failure, Value> =
	ContextualMemoryCache(this, job)
