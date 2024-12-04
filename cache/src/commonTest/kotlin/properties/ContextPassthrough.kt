package opensavvy.cache.properties

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import opensavvy.cache.Cache
import opensavvy.cache.cache
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.TestDsl
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.successful
import opensavvy.state.outcome.value
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private class CustomContext : AbstractCoroutineContextElement(Companion) {
	companion object : CoroutineContext.Key<CustomContext>
}

fun SuiteDsl.contextPassthrough(
	cacheWrapper: suspend TestDsl.(Cache<Unit, Nothing, Any?>) -> Cache<Unit, Nothing, Any?>,
) = suite("Context passthrough") {

	test("The coroutine context of the caller should passthrough to the cache implementation") {
		// This is important to allow storing auth etc in the coroutine context

		val cache = cacheWrapper(
			cache { id: Unit ->
				val context = currentCoroutineContext()
				println("Coroutine context within the cache callback: $context")
				context[CustomContext].successful()
			}
		)

		val context = CustomContext()
		withContext(context) {
			check(cache[Unit].now().value == context)
		}
	}

	test("Sanity check: the context shouldn't magically appear") {
		val cache = cacheWrapper(
			cache { id: Unit ->
				val context = currentCoroutineContext()
				println("Coroutine context within the cache callback: $context")
				context[CustomContext].successful()
			}
		)

		check(cache[Unit].now().value == null)
	}

}
