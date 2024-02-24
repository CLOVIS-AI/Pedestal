package opensavvy.progress

import io.kotest.matchers.shouldBe
import opensavvy.prepared.runner.kotest.PreparedSpec

@Suppress("unused")
class UnquantifiedLoadingTest : PreparedSpec({
    test("String representation") {
        loading().toString() shouldBe "Loading"
    }
})
