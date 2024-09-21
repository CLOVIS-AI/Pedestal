package opensavvy.state.outcome

import opensavvy.prepared.runner.kotest.PreparedSpec

class AccessorsTest : PreparedSpec({

    @Suppress("LocalVariableName")
    val Failed = "FAILED"

    suite("valueOrNull") {
        test("Success") {
            check(5.successful().valueOrNull == 5)
        }

        @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION", "SENSELESS_COMPARISON") // it's the goal of the test!
        test("Failure") {
            check(Failed.failed().valueOrNull == null)
        }
    }

    suite("failureOrNull") {
        @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION", "SENSELESS_COMPARISON") // it's the goal of the test!
        test("Success") {
            check(5.successful().failureOrNull == null)
        }

        test("Failure") {
            check(Failed.failed().failureOrNull == Failed)
        }
    }

    suite("Nothing variants") {
        test("value") {
            check((5.successful() as Outcome<Nothing, Int>).value == 5)
        }

        test("failure") {
            check((Failed.failed() as Outcome<Any, Nothing>).failure == Failed)
        }
    }
})
