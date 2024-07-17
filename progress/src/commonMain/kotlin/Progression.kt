package opensavvy.progress

import opensavvy.progress.Progress.Done
import opensavvy.progress.Progress.Loading
import opensavvy.progress.Progress.Loading.Quantified
import opensavvy.progress.Progress.Loading.Unquantified

/**
 * Is work currently happening?
 *
 * Values of this type can be in two different states:
 * - [Done]: no work is happening.
 * - [Loading]: work is happening.
 *
 * For ease of use, the factory functions [done] and [loading] are provided.
 *
 * ### Implementation notes
 *
 * All implementations of [Progress] must be deeply immutable.
 * To communicate the advancement of progress over time, report multiple progress objects.
 */
sealed interface Progress {

    /**
     * No work is currently happening.
     *
     * For ease of use, see the factory [done].
     */
    data object Done : Progress

    /**
     * Work is currently happening.
     *
     * The ongoing work can be [quantified][Quantified] or [unquantified][Unquantified].
     * Unquantified work gives no information on its progress, whereas quantified work is able to return some information to the user.
     */
    sealed interface Loading : Progress {

        /**
         * Work is currently happening, but we have no information on its progression.
         *
         * For ease of use, see the factory [loading].
         */
        data object Unquantified : Loading {
            override fun toString() = "Loading"
        }

        /**
         * Work is currently happening, and we have some information on its progression.
         *
         * By default, the progression is stored as the ratio of how much work has been done so far and the total amount
         * of work expected. This ratio is available in two formats, [normalized] and [percent]. If this ratio of work
         * is enough for your usage, see [loading].
         *
         * New implementations of this interface can be provided by downstream users to provide more information
         * (e.g. bandwidth, estimated time of completion…).
         */
        interface Quantified : Loading {

            /**
             * The normalized ratio of the amount of work done to the total amount of work to be done.
             *
             * Allowed values are between `0.0` and `1.0`, both inclusive.
             * - `0.0` means the work has not started (nothing was done out of the total),
             * - `1.0` means the work has finished (everything was done out of the total).
             *
             * Although `1.0` is semantically the same as [Progress.Done], it is still a legal value to simplify usage.
             *
             * @see percent
             */
            val normalized: Double

            /**
             * The percentage of the amount of work done and the total amount of work to be done.
             *
             * Allowed values are between `0` and `100`, both inclusive.
             * - `0` means the work has not started (nothing was done out of the total),
             * - `100` means the work has finished (everything was done out of the total).
             *
             * Although `100` is semantically the same as [Progress.Done], it is still a legal value to simplify usage.
             *
             * @see normalized
             */
            val percent: Int
                get() = (normalized * 100).toInt()
        }
    }

    companion object
}

/**
 * All scheduled work for this task has been done.
 *
 * This method is a convenience factory for [Progress.Done].
 * See also [loading].
 */
fun done() = Done

/**
 * Some scheduled work is ongoing, but we have no information on how much is left.
 *
 * This method is a convenience factory for [Progress.Loading.Unquantified].
 * See also [Done].
 */
fun loading() = Unquantified
