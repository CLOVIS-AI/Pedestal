package opensavvy.cache.properties

import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.prepared
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull

fun SuiteDsl.readingValues(
	cacheWrapper: TestIntCacheDecorator,
) = suite("Reading values") {

	val testCache by prepared {
		cacheWrapper(testIntCache)
	}

	test("Simple value read") {
		val cache = testCache()

		check("0" == cache[0].now().valueOrNull)
	}

	test("Failing value read") {
		val cache = testCache()

		check(null == cache[-1].now().valueOrNull)
	}
}

fun SuiteDsl.updateAndExpire(
	cacheWrapper: TestIntCacheDecorator,
) = suite("Update and expire") {
	val testCache by prepared {
		cacheWrapper(testIntCache)
	}

	test("Overwriting a value with update") {
		val cache = testCache()

		check("2" == cache[2].now().valueOrNull)

		println("Overwriting…")
		cache.update(2, "3")
		check("3" == cache[2].now().valueOrNull)
	}

	test("Using expire to un-overwrite a value") {
		val cache = testCache()
		cache.update(2, "3")

		cache.expire(2)
		check("2" == cache[2].now().valueOrNull)
	}

	test("Using expireAll to un-overwrite multiple values") {
		val cache = testCache()
		cache.update(1, "2")
		cache.update(5, "6")

		cache.expireAll()

		check("2" == cache[2].now().valueOrNull)
		check("5" == cache[5].now().valueOrNull)
	}
}
