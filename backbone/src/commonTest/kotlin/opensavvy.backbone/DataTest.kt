@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.backbone

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.Data.Companion.markCompleted
import opensavvy.backbone.Data.Companion.markInvalid
import opensavvy.backbone.Data.Companion.markLoading
import opensavvy.backbone.Data.Companion.markNotFound
import opensavvy.backbone.Data.Companion.markUnauthenticated
import opensavvy.backbone.Data.Companion.markUnauthorized
import opensavvy.backbone.Data.Companion.state
import opensavvy.backbone.Data.Companion.value
import opensavvy.backbone.Ref.Companion.request
import opensavvy.backbone.cache.ExpirationCache.Companion.expireAfter
import opensavvy.backbone.cache.MemoryCache.Companion.cachedInMemory
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class DataTest {

	@Test
	fun percent() {
		val loadingNoInfo = Data.Status.Loading.Basic()
		assertEquals(null, loadingNoInfo.progression)
		assertEquals(null, loadingNoInfo.percent)
		assertEquals("Loading", loadingNoInfo.toString())

		val loadingStart = Data.Status.Loading.Basic(0f)
		assertEquals(0f, loadingStart.progression)
		assertEquals(0, loadingStart.percent)

		val loadingThird = Data.Status.Loading.Basic(0.33f)
		assertEquals(0.33f, loadingThird.progression)
		assertEquals(33, loadingThird.percent)
		assertEquals("Loading(33%)", loadingThird.toString())

		val loadingDone = Data.Status.Loading.Basic(1.0f)
		assertEquals(1.0f, loadingDone.progression)
		assertEquals(100, loadingDone.percent)
	}

	private class IntBone(context: CoroutineContext) : Backbone<Int> {
		override val cache = Cache.Default<Int>().cachedInMemory(context).expireAfter(10.seconds, context)

		override fun directRequest(ref: Ref<Int>): Flow<Data<Int>> = state {
			markLoading(ref, 0f)
			delay(20)

			if (ref !is Ref.Basic)
				markInvalid(ref, "The passed reference is invalid")

			markLoading(ref, 0.2f)
			delay(20)

			val value = ref.id.toIntOrNull() ?: markInvalid(ref, "Could not convert '${ref.id}' to an integer")

			markLoading(ref, 0.4f)
			delay(20)

			// just imagine we're testing the credentials here
			if (value < 0)
				markUnauthenticated(ref, "Only logged-in users can access the number $value")

			markLoading(ref, 0.6f)
			delay(20)

			// just imagine we're testing the credentials here
			if (value == 0)
				markUnauthorized(ref, "Only administrators can access the number $value")

			markLoading(ref, 0.8f)
			delay(20)

			if (value >= 100)
				markNotFound(ref, "Only values lesser than 100 can be found: $value")

			markLoading(ref, 0.9f)
			delay(20)

			markCompleted(ref, value)
		}
	}

	@Test
	fun successful() = runTest {
		val job = Job()

		val ref = Ref.Basic("5", IntBone(coroutineContext + job))
		val real = ref
			.request()
			.transformWhile {
				emit(it)
				it.data !is Result.Success<*>
			}
			.toList()

		val expected = listOf(
			Data(Result.NoData, Data.Status.Loading.Basic(), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.0f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.2f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.4f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.6f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.8f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.9f), ref),
			Data(Result.Success(5), Data.Status.Completed, ref),
		)

		assertEquals(expected, real)
		assertEquals(5, real.last().value)

		job.cancel()
	}

	@Test
	fun invalidReference() = runTest {
		val job = Job()

		val ref = object : Ref<Int> {
			override val backbone = IntBone(coroutineContext + job)
		}
		val real = ref
			.request()
			.transformWhile {
				emit(it)
				it.status !is Data.Status.Completed
			}
			.toList()

		val expected = listOf(
			Data(Result.NoData, Data.Status.Loading.Basic(), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.0f), ref),
			Data(
				Result.Failure.Standard(Result.Failure.Standard.Kind.Invalid, "The passed reference is invalid"),
				Data.Status.Completed,
				ref
			)
		)

		assertEquals(expected, real)

		job.cancel()
	}

	@Test
	fun invalidId() = runTest {
		val job = Job()

		val ref = Ref.Basic("foo", IntBone(coroutineContext + job))
		val real = ref
			.request()
			.transformWhile {
				emit(it)
				it.status !is Data.Status.Completed
			}
			.toList()

		val expected = listOf(
			Data(Result.NoData, Data.Status.Loading.Basic(), ref),
			Data(
				Result.Failure.Standard(Result.Failure.Standard.Kind.Invalid, "Could not convert 'foo' to an integer"),
				Data.Status.Completed,
				ref
			)
		)

		assertEquals(expected, real)

		job.cancel()
	}

	@Test
	fun unauthenticated() = runTest {
		val job = Job()

		val ref = Ref.Basic("-1", IntBone(coroutineContext + job))
		val real = ref
			.request()
			.transformWhile {
				emit(it)
				it.status !is Data.Status.Completed
			}
			.toList()

		val expected = listOf(
			Data(Result.NoData, Data.Status.Loading.Basic(), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.0f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.2f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.4f), ref),
			Data(
				Result.Failure.Standard(
					Result.Failure.Standard.Kind.Unauthenticated,
					"Only logged-in users can access the number -1"
				), Data.Status.Completed, ref
			)
		)

		assertEquals(expected, real)

		job.cancel()
	}

	@Test
	fun unauthorized() = runTest {
		val job = Job()

		val ref = Ref.Basic("0", IntBone(coroutineContext + job))
		val real = ref
			.request()
			.transformWhile {
				emit(it)
				it.status !is Data.Status.Completed
			}
			.toList()

		val expected = listOf(
			Data(Result.NoData, Data.Status.Loading.Basic(), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.0f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.2f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.4f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.6f), ref),
			Data(
				Result.Failure.Standard(
					Result.Failure.Standard.Kind.Unauthorized,
					"Only administrators can access the number 0"
				), Data.Status.Completed, ref
			)
		)

		assertEquals(expected, real)

		job.cancel()
	}

	@Test
	fun notFound() = runTest {
		val job = Job()

		val ref = Ref.Basic("207", IntBone(coroutineContext + job))
		val real = ref
			.request()
			.transformWhile {
				emit(it)
				it.status !is Data.Status.Completed
			}
			.toList()

		val expected = listOf(
			Data(Result.NoData, Data.Status.Loading.Basic(), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.0f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.2f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.4f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.6f), ref),
			Data(Result.NoData, Data.Status.Loading.Basic(0.8f), ref),
			Data(
				Result.Failure.Standard(
					Result.Failure.Standard.Kind.NotFound,
					"Only values lesser than 100 can be found: 207"
				), Data.Status.Completed, ref
			)
		)

		assertEquals(expected, real)

		job.cancel()
	}
}
