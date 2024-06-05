package opensavvy.progress

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import opensavvy.prepared.runner.kotest.PreparedSpec

@OptIn(ExperimentalProgressApi::class)
class ProgressiveTest : PreparedSpec({

	test("toString") {
		assertSoftly {
			Progressive(5, loading(0.2)).toString() shouldBe "5 Loading(20%)"

			Progressive("foo", done()).toString() shouldBe "foo Done"

			listOf(
				Progressive(5.2, loading(0.1)),
				Progressive(true, loading(0.9)),
			).toString() shouldBe "[5.2 Loading(10%), true Loading(90%)]"
		}
	}

})
