package opensavvy.spine

data class SpineFailure(
    val type: Type,
    val message: String,
) {

    override fun toString() = "$type: “$message”"

    enum class Type {
        Unauthenticated,
        Unauthorized,
        NotFound,
        InvalidRequest,
        InvalidState,
        ;
    }
}
