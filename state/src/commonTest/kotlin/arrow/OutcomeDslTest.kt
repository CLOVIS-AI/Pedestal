/*
 * Copyright (c) 2023-2025, OpenSavvy and contributors.
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
