@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.backbone

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.Ref.Companion.expire
import opensavvy.backbone.Ref.Companion.request
import opensavvy.state.firstValue
import opensavvy.state.slice.ensureValid
import opensavvy.state.slice.orThrow
import opensavvy.state.slice.slice
import kotlin.test.Test
import kotlin.test.assertEquals

class BackboneCacheTest {

	// Id("12") -> 12
	private class Bone(override val cache: RefCache<Int>) : Backbone<Int> {
		override suspend fun directRequest(ref: Ref<Int>) = slice {
			ensureValid(ref is Ref.Basic) { "Only basic references are accepted by ${this@Bone}" }
			val int = ref.id.toIntOrNull()
			ensureValid(int != null) { "The reference $ref does not refer to a valid integer" }
			int
		}

		fun of(int: Int) = Ref.Basic(int.toString(), this)
	}

	@Test
	fun default() = runTest {
		val bone = Bone(defaultRefCache())
		val id5 = bone.of(5)
		val id2 = bone.of(2)

		assertEquals(5, id5.request().firstValue().orThrow())
		assertEquals(2, id2.request().firstValue().orThrow())

		id2.expire()
		assertEquals(2, id2.request().firstValue().orThrow())
	}

	@Test
	fun batching() = runTest {
		val job = Job()

		val bone = Bone(batchingRefCache(coroutineContext + job))
		val id5 = bone.of(5)
		val id2 = bone.of(2)

		assertEquals(5, id5.request().firstValue().orThrow())
		assertEquals(2, id2.request().firstValue().orThrow())

		id2.expire()
		assertEquals(2, id2.request().firstValue().orThrow())

		job.cancel()
	}
}
