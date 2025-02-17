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

package opensavvy.progress.report

import opensavvy.progress.Progress

private class MapProgressReporter(
    private val upstream: ProgressReporter,
    private val transform: (Progress) -> Progress,
) : ProgressReporter {

    override fun report(progress: Progress) {
        upstream.report(transform(progress))
    }

    override fun toString() = "$upstream.map()"
}

/**
 * Creates a new reporter that applies [transform] to each progress event it receives.
 */
fun ProgressReporter.map(transform: (Progress) -> Progress): ProgressReporter =
    MapProgressReporter(this, transform)
