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

/**
 * SAM interface to communicate progress to a caller.
 *
 * Instances of this interface can be created by a caller and passed to a downstream user.
 * The downstream user can then call the [report] function to communicate its progress to the caller.
 *
 * If you are attempting to call a function which requires an instance of [ProgressReporter], but you do not care about
 * the progress events, you can use [emptyProgressReporter] to access a no-op implementation.
 */
fun interface ProgressReporter {

    /**
     * Reports that the current task has reached [progress].
     */
    fun report(progress: Progress)

    companion object

}
