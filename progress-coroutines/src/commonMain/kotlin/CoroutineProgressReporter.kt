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

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import opensavvy.progress.Progress
import opensavvy.progress.report.ProgressReporter
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Captures progress information about the currently running task.
 *
 * KotlinX.Coroutines uses a [CoroutineContext] object throughout `suspend` functions to store information about the
 * currently-running task. To report the current [Progress] of a coroutine, [CoroutineProgressReporter] is added to its context.
 *
 *
 */
class CoroutineProgressReporter(
    private val reporter: ProgressReporter,
) : AbstractCoroutineContextElement(Key),
    ProgressReporter by reporter {

    override fun toString() = "$reporter.asCoroutineContext()"

    object Key : CoroutineContext.Key<CoroutineProgressReporter>
}

/**
 * Wraps this [ProgressReporter] into a [CoroutineProgressReporter] so it can be inserted in a [CoroutineContext].
 */
fun ProgressReporter.asCoroutineContext() =
    CoroutineProgressReporter(this)

/**
 * Reports [progress] to the progress reporter stored in the [currentCoroutineContext].
 *
 * If there is no progress reporter in the current context, this function does nothing.
 *
 * ### Example
 *
 * ```kotlin
 * suspend fun main() {
 *     reportProgress(::println) {
 *         task1()
 *     }
 * }
 *
 * suspend fun task1() {
 *     report(loading(0.0))
 *     delay(500)
 *     report(loading(0.5))
 *     delay(500)
 *     report(done())
 * }
 * ```
 * ```text
 * Loading(0%)
 * Loading(50%)
 * Done
 * ```
 *
 * @see CoroutineProgressReporter
 */
suspend fun report(progress: Progress) {
    val reporter = currentCoroutineContext()[CoroutineProgressReporter.Key] ?: return
    reporter.report(progress)
}

/**
 * Captures all calls to [report] in [block] and transmits them to [onProgress].
 *
 * ### Example
 *
 * ```kotlin
 * suspend fun main() {
 *     // Function reference syntax
 *     reportProgress(::println) {
 *         // All 'report' calls in this block will be printed
 *         someComplicatedTask()
 *     }
 *
 *     // Lambda syntax
 *     reportProgress({ println(it) }) {
 *         someComplicatedTask()
 *     }
 * }
 * ```
 */
suspend fun reportProgress(onProgress: (Progress) -> Unit, block: suspend () -> Unit) {
    withContext(ProgressReporter { onProgress(it) }.asCoroutineContext()) {
        block()
    }
}
