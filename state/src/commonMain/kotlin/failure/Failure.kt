package opensavvy.state.failure

/**
 * Common failure interface.
 *
 * Exceptions have notorious downsides to performance and readability.
 * Instead, we encourage using this [Failure] supertype.
 * It is our goal that this interface is implemented most of the time via delegation to [BasicFailure] (or another similar type).
 *
 * The semantic type of failure is represented by a failure's [Key].
 */
interface Failure {

    /**
     * The [Key] to this [Failure].
     */
    val key: Key

    /**
     * A user-readable representation of this failure.
     */
    val message: String

    /**
     * Another failure which caused this one.
     *
     * If this property is `null`, then no known failure caused this one.
     */
    val cause: Failure?

    /**
     * The key of a [Failure], most commonly accessed by [Failure.key].
     *
     * The key represents the *semantic type* of failure.
     * The semantic type of failure is different from the Kotlin type of the failure class because of delegation:
     * to facilitate implementation, it is possible to delegate to an existing implementation, providing it with a unique key.
     *
     * It is also possible to wrap failures into other failure implementations without losing the semantic type information,
     * as each wrapper can delegate accesses to the key to the underlying implementation.
     *
     * Creating your own failure hierarchies thus becomes simple:
     * ```kotlin
     * sealed interface UserFailure {
     *     // Delegate to an existing failure and its key
     *     class NotFound(id: UserId) : Failure by NotFound("user $id")
     *
     *     // Create your own failure type
     *     object InvalidUserId(id: String) : Failure by BasicFailure(Companion, "The identifier '$id' is invalid") {
     *
     *         // Custom key for this failure
     *         companion object : Failure.Key
     *     }
     * }
     * ```
     *
     * Programs should reuse the existing keys as much as possible, to allow frameworks to extract meaning from errors.
     * This can be done either by reusing an existing failure type (as seen in the previous example), or giving an existing
     * key to a new failure type, for example to [encapsulate exceptions][asFailure]:
     * ```kotlin
     * fun foo(): Outcome<Failure, Int> {
     *     try {
     *         return someExpensiveComputation().success()
     *     } catch (e: NoSuchElementException) {
     *         return e.asFailure(NotFound)
     *     }
     * }
     * ```
     */
    interface Key

    companion object
}
