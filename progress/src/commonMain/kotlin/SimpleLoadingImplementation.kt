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
