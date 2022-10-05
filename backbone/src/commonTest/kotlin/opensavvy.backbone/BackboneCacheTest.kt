@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.backbone

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.Ref.Companion.expire
import opensavvy.backbone.Ref.Companion.requestValue
import opensavvy.state.emitSuccessful
import opensavvy.state.ensureValid
import opensavvy.state.state
import kotlin.test.Test
import kotlin.test.assertEquals

class BackboneCacheTest {

	// Id("12") -> 12
	private class Bone(override val cache: BackboneCache<Int>) : Backbone<Int> {
		override fun directRequest(ref: Ref<Int>): RefState<Int> = state {
			ensureValid(ref, ref is Ref.Basic) { "Only basic references are accepted by ${this@Bone}" }
			val int = ref.id.toIntOrNull()
			ensureValid(ref, int != null) { "The reference $ref does not refer to a valid integer" }
			emitSuccessful(ref, int)
		}

		fun of(int: Int) = Ref.Basic(int.toString(), this)
	}

	@Test
	fun default() = runTest {
		val bone = Bone(defaultBackboneCache())
		val id5 = bone.of(5)
		val id2 = bone.of(2)

		assertEquals(5, id5.requestValue())
		assertEquals(2, id2.requestValue())

		id2.expire()
		assertEquals(2, id2.requestValue())
	}

	@Test
	fun batching() = runTest {
		val job = Job()

		val bone = Bone(batchingBackboneCache(coroutineContext + job))
		val id5 = bone.of(5)
		val id2 = bone.of(2)

		assertEquals(5, id5.requestValue())
		assertEquals(2, id2.requestValue())

		id2.expire()
		assertEquals(2, id2.requestValue())

		job.cancel()
	}
}
