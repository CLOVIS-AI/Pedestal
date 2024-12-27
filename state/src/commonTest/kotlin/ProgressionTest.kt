package opensavvy.state

import com.benwoodworth.parameterize.parameterOf
import com.benwoodworth.parameterize.parameterize
import io.kotest.assertions.throwables.shouldThrow
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading

class ProgressionTest : PreparedSpec({

	test("String representation of loading states") {
		check(loading().toString() == "Loading")
		check(loading(0.0).toString() == "Loading(0%)")
		check(loading(0.33).toString() == "Loading(33%)")
		check(loading(1.0).toString() == "Loading(100%)")
	}

	test("0% loading") {
		val start = loading(0.0)
		check(start.normalized == 0.0)
		check(start.percent == 0)
	}

	test("33% loading") {
		val third = loading(1.0 / 3)
		check(third.normalized == 0.3333333333333333)
		check(third.percent == 33)
	}

	test("100% loading") {
		val end = loading(1.0)
		check(end.normalized == 1.0)
		check(end.percent == 100)
	}

	suite("Illegal progression values") {
		parameterize {
			val parameter by parameterOf(-1.0, 1.01, 1.00000001, Double.MAX_VALUE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)

			test("A progress value of $parameter is not allowed") {
				shouldThrow<IllegalArgumentException> {
					loading(parameter)
				}
			}
		}
	}
})
