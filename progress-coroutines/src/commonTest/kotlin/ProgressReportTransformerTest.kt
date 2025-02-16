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

package opensavvy.progress.coroutines

import kotlinx.coroutines.withContext
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.progress.report.ProgressReporter

class ProgressReportTransformerTest : PreparedSpec({

	test("Using the progress transformer") {
		var value: Progress? = null

		withContext(ProgressReporter { value = it }.asCoroutineContext()) {
			report(loading(0.1))
			check(value == loading(0.1))

			mapProgressTo(0.2..0.5) {
				report(loading(0.0))
				check(value == loading(0.2))

				report(loading(0.5))
				check(value == loading(0.35))

				report(loading(1.0))
				check(value == loading(0.5))
			}

			report(loading(0.9))
			check(value == loading(0.9))
		}

		// Calling the function without reporter should no-op, NOT fail
		mapProgressTo(0.2..0.5) {
			report(loading(0.0))
			check(value == loading(0.9)) // The value shouldn't be impacted, since we should no-op
		}
	}

})
