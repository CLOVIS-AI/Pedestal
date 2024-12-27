package opensavvy.progress

import com.benwoodworth.parameterize.parameterOf
import com.benwoodworth.parameterize.parameterize
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import opensavvy.prepared.runner.kotest.PreparedSpec

@Suppress("unused")
class SimpleLoadingImplementationTest : PreparedSpec({
    suite("Constructor range validation") {
        parameterize {
            val legal by parameterOf(0.0, 0.1, 0.00001, 0.33, 1.0)

            test("The loading constructor should accept the value $legal") {
                loading(legal).normalized shouldBe legal
            }
        }

        parameterize {
            val illegal by parameterOf(-1.0, 1.01, 1.000001, -0.000001, Double.MAX_VALUE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)

            test("The loading constructor should not accept the value $illegal") {
                shouldThrow<IllegalArgumentException> { loading(illegal) }
            }
        }
    }

    suite("Conversion to integer percent") {
        parameterize {
            val value by parameterOf(
                0.0 to 0,
                0.33 to 33,
                1.0 to 100,
                0.01 to 1,
                0.001 to 0,
                0.4597 to 45,
            )
            val (input, expected) = value

            test("Converting $input should give $expected") {
                loading(input).percent shouldBe expected
            }
        }
    }

    suite("String conversion") {
        parameterize {
            val value by parameterOf(
                0.0 to "Loading(0%)",
                0.2 to "Loading(20%)",
                0.99 to "Loading(99%)",
                1.0 to "Loading(100%)",
            )
            val (input, expected) = value

            test("loading($input) should be represented by the string $expected") {
                loading(input).toString() shouldBe expected
            }
        }
    }

    test("The hashCode implementation is correct") {
        val set = hashSetOf(
            done(),
            loading(0.0),
            loading(0.7),
            loading(0.9),
            loading(1.0),
        )

        check(done() in set)
        check(loading(0.0) in set)
        check(loading(0.7) in set)
        check(loading(0.9) in set)
        check(loading(1.0) in set)
    }

    suite("Equality") {
        test("Equal") {
            loading(0.0) shouldBe loading(0.0)
        }

        test("Not equal") {
            loading(0.0) shouldNotBe loading(0.7)
            loading(0.0) shouldNotBe Unit
            loading(0.0) shouldNotBe null
        }
    }
})
