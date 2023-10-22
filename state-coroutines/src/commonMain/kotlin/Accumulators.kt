package opensavvy.state.coroutines

import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.ProgressiveOutcome

@Suppress("DuplicatedCode") // Yes, it's a duplicate, but this one suspends, so it has a different signature
fun <Failure, Value> ProgressiveFlow<Failure, Value>.combineCompleted(): ProgressiveFlow<Failure, Value> {
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
