package opensavvy.progress

/**
 * Stores a [value] along with [progress] information.
 */
@ExperimentalProgressApi
data class Progressive<out Value>(
	val value: Value,
	val progress: Progress,
) {

	override fun toString() = "$value $progress"
}
