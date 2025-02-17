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

package opensavvy.progress

private class SimpleLoadingImplementation(
    override val normalized: Double,
) : Progress.Loading.Quantified {

    init {
        require(normalized in 0.0..1.0) { "The normalized progression should be a value between 0 and 1, found $normalized" }
    }

    //region Equals & hashCode
    //They both use the percent value instead of the normalized value to avoid floating-point precision errors

    override fun hashCode(): Int {
        return percent.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === null) return false
        if (other === this) return true

        if (other !is SimpleLoadingImplementation) return false

        return other.percent == percent
    }

    //endregion

    override fun toString() = "Loading($percent%)"
}

/**
 * Some scheduled work is ongoing, and we can estimate the ratio of what has been done compared to what's left to do.
 *
 * See also [done].
 *
 * @param progress A normalized progress, see [Progress.Loading.Quantified.normalized].
 */
fun loading(
    progress: Double,
): Progress.Loading.Quantified = SimpleLoadingImplementation(progress)
