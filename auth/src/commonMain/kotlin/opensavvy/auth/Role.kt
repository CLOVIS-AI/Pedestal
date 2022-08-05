package opensavvy.auth

/**
 * The role of an [Account].
 *
 * By itself, this interface does nothing.
 * The user is responsible for writing their own [Role] implementation and deciding what it means.
 */
interface Role {

	/**
	 * The identifier of this role.
	 *
	 * Different roles should have a different identifiers.
	 * The identifier should be short.
	 * All characters must be either alphanumeric, `-` or `_` (e.g. spaces are not allowed).
	 */
	val id: String

}

/**
 * Helper to easily find a role from its [Role.id].
 *
 * If your roles do not change over time, see the [Immutable] implementation of this interface.
 */
interface Roles<R : Role> {

	/**
	 * Used to iterate over all possible roles.
	 */
	val roles: Iterable<R>

	/**
	 * Searches for a [Role] by its [id].
	 *
	 * This function returns `null` if no known role matches the given [id].
	 */
	fun findById(id: String): R?

	/**
	 * Basic implementation of [Roles] when the list of [roles] does not change over time.
	 */
	class Immutable<R : Role>(
		override val roles: List<R>
	) : Roles<R> {

		// Prepares the roles to make them fast to search
		// Thanks to this, findById is O(1) expected instead of O(n)
		private val roleById = roles.associateBy { it.id }

		override fun findById(id: String): R?  = roleById[id]
	}
}
