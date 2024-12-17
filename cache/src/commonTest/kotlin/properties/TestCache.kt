package opensavvy.cache.properties

import arrow.core.raise.ensure
import kotlinx.coroutines.delay
import opensavvy.cache.Cache
import opensavvy.cache.cache
import opensavvy.prepared.suite.TestDsl
import opensavvy.progress.coroutines.report
import opensavvy.progress.loading
import opensavvy.state.arrow.out

sealed interface Failures {
	data class Negative(val id: Int) : Failures
}

val testIntCache = cache<Int, Failures, String> {
	out {
		delay(100)
		report(loading(0.2))
		delay(10)
		ensure(it >= 0) { Failures.Negative(it) }
		it.toString()
	}
}

typealias TestIntCacheDecorator = suspend TestDsl.(Cache<Int, Failures, String>) -> Cache<Int, Failures, String>
