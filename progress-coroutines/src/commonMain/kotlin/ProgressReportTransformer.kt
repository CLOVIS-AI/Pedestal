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
import opensavvy.progress.loading
import opensavvy.progress.report.ProgressReporter
import opensavvy.progress.report.emptyProgressReporter
import opensavvy.progress.report.reduceToInterval

/**
 * Passes the current [ProgressReporter] to [createChildReporter] to create a new child reporter of the current coroutine,
 * and adds it to [block]'s context.
 */
suspend fun <R> transformProgress(
    createChildReporter: (ProgressReporter) -> ProgressReporter,
    block: suspend () -> R
): R {
    val upstreamReporter = currentCoroutineContext()[CoroutineProgressReporter.Key]
        ?: emptyProgressReporter()

    val reporter = createChildReporter(upstreamReporter)
        .asCoroutineContext()

    return withContext(reporter) {
        block()
    }
}

/**
 * Reduces progress events emitted in [block] to fit into [progressInterval].
 *
 * ### Example
 *
 * ```kotlin
 * suspend fun main() {
 *     reportProgress(::println) {
 *         mapProgressTo(0.0..0.5) {
 *             task1()
 *         }
 *
 *         mapProgressTo(0.5..1.0) {
 *             task2()
 *         }
 *     }
 * }
 *
 * suspend fun task1() {
 *     report(loading(0.0))
 *     delay(500)
 *     report(loading(1.0))
 * }
 *
 * suspend fun task2() {
 *     report(loading(0.0))
 *     delay(500)
 *     report(loading(0.5))
 *     delay(500)
 *     report(loading(1.0))
 * }
 * ```
 * ```text
 * Loading(0%)
 * Loading(50%)
 * Loading(50%)
 * Loading(75%)
 * Loading(100%)
 * ```
 *
 * @see reduceToInterval Equivalent without coroutines
 * @see transformProgress Transform progress events arbitrarily instead of only reducing their interval
 */
suspend fun <R> mapProgressTo(progressInterval: ClosedFloatingPointRange<Double>, block: suspend () -> R): R =
    transformProgress({ it.reduceToInterval(progressInterval) }) {
        try {
            report(loading(0.0))
            block()
        } finally {
            report(loading(1.0))
        }
    }
