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
import arrow.core.right
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.progress.loading
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.failedWithProgress
import opensavvy.state.progressive.successfulWithProgress
import opensavvy.state.progressive.withProgress

val ConverterTest by preparedSuite {

	data class NotFound(val value: Int)

	suite("To either") {
		suite("Outcome") {

			test("Success") {
				check(5.successful().toEither() == 5.right())
			}

			test("Failure") {
				check(NotFound(5).failed().toEither() == NotFound(5).left())
			}
		}

		suite("ProgressiveOutcome") {
			test("Success") {
				check(5.successful().withProgress(loading(0.2)).toEither() == 5.right())
			}

			test("Failure") {
				check(NotFound(5).failedWithProgress().toEither() == NotFound(5).left())
			}

			test("Incomplete") {
				check(ProgressiveOutcome.Incomplete().toEither() == null)
			}
		}
	}

	suite("From either") {
		suite("Outcome") {
			test("Success") {
				check(5.right().toOutcome() == 5.successful())
			}

			test("Failure") {
				check(NotFound(5).left().toOutcome() == NotFound(5).failed())
			}
		}

		suite("ProgressiveOutcome") {
			test("Success") {
				check(5.right().toOutcome(loading(0.5)) == 5.successfulWithProgress(loading(0.5)))
			}

			test("Failure") {
				check(NotFound(5).left().toOutcome(loading(0.5)) == NotFound(5).failedWithProgress(loading(0.5)))
			}
		}
	}
}
