package opensavvy.state.arrow

import arrow.core.left
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful

class OutcomeDslTest : PreparedSpec({
    test("Success") {
        check(out<String, Int> { 2 } == 2.successful())
    }

    test("Failure") {
        check(out<String, Int> { raise("test") } == "test".failed())
    }

    test("Bind success") {
        check(out<String, Int> { 2.successful().bind() } == 2.successful())
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // that's the purpose of the test!
    test("Bind failure") {
        check(out<String, Int> { "test".failed().bind() } == "test".failed())
    }

    test("To either") {
        check("test".failed().toEither() == "test".left())
    }
})
