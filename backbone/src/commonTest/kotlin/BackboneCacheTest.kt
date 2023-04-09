package opensavvy.backbone

import arrow.core.raise.ensure
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.Ref.Companion.expire
import opensavvy.backbone.Ref.Companion.request
import opensavvy.state.arrow.out
import opensavvy.state.coroutines.firstValue
import opensavvy.state.failure.CustomFailure
import opensavvy.state.failure.Failure
import opensavvy.state.outcome.valueOrNull
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BackboneCacheTest {

	// Id("12") -> 12
	private class Bone(override val cache: RefCache<Invalid, Int>) : Backbone<Bone.Invalid, Int> {
		override suspend fun directRequest(ref: Ref<Invalid, Int>) = out {
			ensure(ref is Ref.Basic) { Invalid }
			val int = ref.id.toIntOrNull()
			ensure(int != null) { Invalid }
			int
		}

		fun of(int: Int) = Ref.Basic(int.toString(), this)

		object Invalid : CustomFailure(Invalid, "Invalid"), Failure.Key
	}

	@Test
	fun default() = runTest {
		val bone = Bone(defaultRefCache())
		val id5 = bone.of(5)
		val id2 = bone.of(2)

		assertEquals(5, id5.request().firstValue().valueOrNull)
		assertEquals(2, id2.request().firstValue().valueOrNull)

		id2.expire()
		assertEquals(2, id2.request().firstValue().valueOrNull)
	}

	@Test
	fun batching() = runTest {
		val job = Job()

		val bone = Bone(batchingRefCache(backgroundScope))
		val id5 = bone.of(5)
		val id2 = bone.of(2)

		assertEquals(5, id5.request().firstValue().valueOrNull)
		assertEquals(2, id2.request().firstValue().valueOrNull)

		id2.expire()
		assertEquals(2, id2.request().firstValue().valueOrNull)

		job.cancel()
	}
}
