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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.progress.report.ProgressReporter

/**
 * [ProgressReporter] implementation which stores the latest progress information in [progress], a [StateFlow].
 */
class StateFlowProgressReporter : ProgressReporter {
    private val state = MutableStateFlow<Progress>(loading(0.0))

    val progress = state as StateFlow<Progress>

    override fun report(progress: Progress) {
        state.value = progress
    }

    override fun toString() = "StateFlowProgressReporter(progress=${progress.value})"
}
