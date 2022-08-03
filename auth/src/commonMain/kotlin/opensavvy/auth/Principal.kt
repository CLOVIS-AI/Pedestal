package opensavvy.auth

/**
 * A [user][account] has proved that they have the specified [role].
 *
 * Objects of this type can only be constructed by the [Authenticator] instance.
 * The [Authenticator] instance will only create an instance of this object if it can verify the user's role.
 *
 * Callers of this class should not keep instances for a long time.
 * By instancing this object, [Authenticator] guarantees that the user is properly authenticated _at the exact moment where the question was asked_.
 * Callers can assume that this [Principal] is valid for a short amount of time (e.g. until the end of the current request), but not longer.
 */
class Principal<R : Role> internal constructor(

	/**
	 * The user that made the request.
	 */
	val account: Account.Ref<R>,

	/**
	 * The role of the user who made the request.
	 *
	 * An instance of this class can only be created if an [Authenticator] verified that the user in fact has this role.
	 * However, the role may change, or the user may be banned, and this object will not be updated.
	 * Only use this object for a short period of time (e.g. until the end of the current request).
	 * For more information, see [Principal].
	 */
	val role: R,
)
