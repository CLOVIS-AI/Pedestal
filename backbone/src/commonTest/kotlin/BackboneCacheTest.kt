package opensavvy.backbone

import arrow.core.raise.ensure
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import opensavvy.cache.cache
import opensavvy.state.arrow.out
import opensavvy.state.outcome.valueOrNull
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BackboneCacheTest {

	data class BasicRef(val id: String, val backbone: Bone) : Ref<Bone.Invalid, Int> {

		override fun request() = backbone.cache[this]
	}

	// Id("12") -> 12
	class Bone : Backbone<BasicRef, Bone.Invalid, Int> {
		val cache = cache<BasicRef, Invalid, Int> {
			out {
				val int = it.id.toIntOrNull()
				ensure(int != null) { Invalid }
				int
			}
		}

		fun of(int: Int) = BasicRef(int.toString(), this)

		object Invalid
	}

	@Test
	fun default() = runTest {
		val bone = Bone()
		val id5 = bone.of(5)
		val id2 = bone.of(2)

		assertEquals(5, id5.now().valueOrNull)
		assertEquals(2, id2.now().valueOrNull)

		bone.cache.expire(id2)
		assertEquals(2, id2.now().valueOrNull)
	}

	@Test
	fun batching() = runTest {
		val job = Job()

		val bone = Bone()
		val id5 = bone.of(5)
		val id2 = bone.of(2)

		assertEquals(5, id5.now().valueOrNull)
		assertEquals(2, id2.now().valueOrNull)

		bone.cache.expire(id2)
		assertEquals(2, id2.now().valueOrNull)

		job.cancel()
	}

	@Test
	fun companions() {
		println(Ref)
		println(Backbone)
		println()
	}
}
