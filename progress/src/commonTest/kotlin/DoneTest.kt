package opensavvy.progress

import io.kotest.matchers.shouldBe
import opensavvy.prepared.runner.kotest.PreparedSpec

@Suppress("unused")
class DoneTest : PreparedSpec({
    test("String representation") {
        done().toString() shouldBe "Done"
    }
})
