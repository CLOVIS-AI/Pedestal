package opensavvy.state

/**
 * Is work currently happening?
 *
 * Values of this type can be in two different states:
 * - [Done]: no work is happening.
 * - [Loading]: work is happening, more information may be available depending on the subtype.
 *
 * For ease of use, the factory functions [done] and [loading] are provided.
 */
sealed interface Progression {

	/**
	 * No work is currently happening.
	 *
	 * For ease of use, see the factory [done].
	 */
	object Done : Progression {
		override fun toString() = "Done"
	}

	/**
	 * Work is currently happening.
	 *
	 * [Unquantified] work is work for which no progression information is available.
	 * It is built with the factory [loading()][loading].
	 *
	 * [Quantified] work is work for which progression information is available.
	 * By default, the [loading(Double)][loading] factory stores progression as a percentage point of the work to be done.
	 * If more information is available (e.g. bandwidth, bitrate, estimated time of completion…), it is possible to create
	 * your own class which implements [Quantified].
	 */
	sealed interface Loading : Progression {

		/**
		 * Work is currently happening, but we have no information on its progression.
		 *
		 * For ease of use, see the factory [loading()][loading].
		 */
		object Unquantified : Loading {
			override fun toString() = "Loading"
		}

		/**
		 * Work is currently happening, and we have some information on its progression.
		 *
		 * By default, the progression is stored as the ratio of how much work has been done so far to the total amount
		 * of work expected.
		 * This ratio is available in two formats, see [normalized] and [percent].
		 * If this ratio of work is enough for your usage, see the factory [loading(Double)][loading].
		 *
		 * To store more information (e.g. bandwidth, estimated time of completion…), implement this interface for your
		 * own objects.
		 */
		interface Quantified : Loading {

			/**
			 * The normalized ratio of the amount of work done and the total amount of work to be done.
			 *
			 * Allowed values are between `0.0` and `1.0`, both inclusive.
			 * - `0.0` means the work has not started (nothing was done out of the total),
			 * - `1.0` means the work has finished (everything was done out of the total).
			 *
			 * Although `1.0` is semantically the same as [Progression.Done], it is still a legal value to simplify usage.
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
			 * Although `100` is semantically the same as [Progression.Done], it is still a legal value to simplify usage.
			 *
			 * @see normalized
			 */
			val percent: Int
				get() = (normalized * 100).toInt()

		}

		/**
		 * Basic implementation of [Quantified].
		 *
		 * You should rarely need to interact with this class.
		 * To create an instance, see [loading].
		 *
		 * When downcasting to access information about the progression, we recommend downcasting to [Quantified] instead
		 * of to this class. This will let your codebase handle other implementations of [Quantified] transparently.
		 */
		// The coding style says 'Implementation' should not appear in class names.
		// This class is more or less for internal use, it shouldn't appear in user's code anyway.
		data class QuantifiedImplementation(override val normalized: Double) : Quantified {

			init {
				require(normalized in 0.0..1.0) { "The progression should be a value between 0 and 1, found $normalized" }
			}

			override fun toString() = "Loading($percent%)"
		}
	}

	companion object {
		/**
		 * Returns [Done].
		 *
		 * @see loading
		 */
		fun done() = Done

		/**
		 * Returns a [Loading] instance with no particular progression information.
		 *
		 * @see done
		 * @see Loading.Unquantified
		 */
		fun loading() = Loading.Unquantified

		/**
		 * Returns a [Loading] instance with a given [progression].
		 *
		 * @param progression Progression information, between `0.0` (just started, no progression made so far) and `1.0` (completely finished).
		 * @see done
		 * @see Loading.Quantified
		 * @see Loading.Quantified.normalized
		 */
		fun loading(progression: Double) = Loading.QuantifiedImplementation(progression)
	}
}
