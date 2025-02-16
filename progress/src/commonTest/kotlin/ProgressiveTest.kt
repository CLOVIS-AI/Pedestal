/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
