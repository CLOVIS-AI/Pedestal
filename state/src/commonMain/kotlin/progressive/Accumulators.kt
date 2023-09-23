package opensavvy.state.progressive

/**
 * Replaces incomplete outcomes by the previous completed outcome, with the updated progress information.
 *
 * For example, if the incoming iterable contains:
 * ```
 * [Incomplete(loading(0.23%)), Success(5, Done), Incomplete(loading(0.5%))]
 * ```
 * then this method returns:
 * ```
 * [Incomplete(loading(0.23%)), Success(5, Done), Success(5, loading(0.5%))]
 * ```
 * You can see how the last incomplete value has been transformed into a loading success.
 * The same transformation is applied to failures.
 *
 * This operator is useful when the iterable represents changes to a single value over time: when we receive an
 * incomplete event, we merge it with the previous complete event to still display a value.
 * For example, in a UI, this would allow displaying old values while refresh requests are ongoing.
 */
fun <Failure, Value> Iterable<ProgressiveOutcome<Failure, Value>>.combineCompleted(): Iterable<ProgressiveOutcome<Failure, Value>> {
	var lastComplete: ProgressiveOutcome<Failure, Value>? = null

	return map {
		when (it) {
			is ProgressiveOutcome.Success -> {
				lastComplete = it
				it
			}

			is ProgressiveOutcome.Failure -> {
				lastComplete = it
				it
			}

			is ProgressiveOutcome.Incomplete -> {
				when (val lastCompleteCopy = lastComplete) {
					// No previous completed elements has been stored, just return the incomplete state
					null -> it

					// A previous completed element is stored, return it with the new progress
					is ProgressiveOutcome.Success -> lastCompleteCopy.copy(progress = it.progress)
					is ProgressiveOutcome.Failure -> lastCompleteCopy.copy(progress = it.progress)

					is ProgressiveOutcome.Incomplete -> error("Impossible case: stored an incomplete value in the complete accumulator: $lastCompleteCopy")
				}
			}
		}
	}
}
