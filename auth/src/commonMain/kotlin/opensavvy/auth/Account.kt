package opensavvy.auth

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import opensavvy.auth.Account.Bone
import opensavvy.auth.Account.Ref
import opensavvy.backbone.Backbone

/**
 * A user.
 *
 * As a caller of this library, you are expected to implement [Bone] and [Ref].
 */
class Account<R : Role>(
	/**
	 * The user's identifier.
	 *
	 * The value itself could be any [String], however it must be a valid parameter to the [Bone.fromId] function.
	 * It is recommended to keep the identifier short.
	 */
	val id: String,

	/**
	 * The [Role] of this user.
	 */
	val role: R,

	/**
	 * Whether this user is blocked, and if so, for how long.
	 *
	 * If the returned instant is in the past, this user is not blocked.
	 * If the returned instant is in the future, this user is blocked.
	 *
	 * To check if this user is currently blocked, see [blocked].
	 */
	val blockedUntil: Instant = Instant.DISTANT_PAST,

	/**
	 * The authentication epoch.
	 *
	 * Everytime the user edits their password or otherwise resets their credentials, this value is incremented by 1.
	 */
	val authEpoch: Int = 0,
) {

	/**
	 * Convenience attribute to easily check if this user is blocked.
	 *
	 * @see blockedUntil
	 */
	val blocked get() = Clock.System.now() <= blockedUntil

	interface Ref<R : Role> : opensavvy.backbone.Ref<Account<R>> {
		override val backbone: Bone<R>
	}

	interface Bone<R : Role> : Backbone<Account<R>> {

		/**
		 * Searches for an account from its [id].
		 *
		 * The caller MUST provide a value that previously came from a [Account.id] returned by this [Bone] implementation.
		 */
		fun fromId(id: String): Ref<R>

	}
}
