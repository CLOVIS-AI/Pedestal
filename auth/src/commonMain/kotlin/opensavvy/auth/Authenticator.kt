package opensavvy.auth

/**
 * The OpenSavvy Authenticator.
 *
 * The Authenticator is responsible for managing user tokens.
 * Two types of tokens exist: access tokens and refresh tokens.
 *
 * Access tokens are typically short-lived.
 * They are provided with all requests, and are used to authenticate the user against the service.
 *
 * Refresh tokens are typically longer-lived.
 * They are only sent when an access token will soon expire, or has already expired, in the goal of acquiring a new access token.
 * Refresh tokens cannot be used to perform any other action.
 *
 * This interface does not dictate how these tokens are verified, their validity time or whether they can be stateless.
 * See the documentation of your chosen implementation to learn about these information.
 */
interface Authenticator<R : Role> {

	/**
	 * Decides whether a given [access token][token] is valid.
	 *
	 * If this access token is valid, a [Principal] is returned.
	 * Otherwise, this function returns `null`.
	 *
	 * For more information about access tokens, see [Authenticator].
	 * To create an access token for a user, see [createAccessToken].
	 */
	suspend fun checkAccessToken(token: String): Principal<R>?

	/**
	 * Decides whether a given [refresh token][token] is valid.
	 *
	 * If this refresh token is valid, a [Principal] is returned.
	 * Otherwise, this function returns `null`.
	 *
	 * For more information about refresh tokens, see [Authenticator].
	 * To create a refresh token for a user, see [createRefreshToken].
	 */
	suspend fun checkRefreshToken(token: String): Principal<R>?

	/**
	 * Creates an access token for [user].
	 *
	 * For more information about access tokens, see [Authenticator].
	 */
	fun createAccessToken(user: Account<R>): String

	/**
	 * Creates a refresh token for [user].
	 *
	 * For more information about refresh tokens, see [Authenticator].
	 */
	fun createRefreshToken(user: Account<R>): String

}
