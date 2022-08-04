package opensavvy.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import opensavvy.auth.*
import opensavvy.backbone.Ref.Companion.requestValue
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.loggerFor
import java.time.Instant
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/**
 * Implements [Authenticator] with JSON Web Tokens (JWT).
 *
 * JWTs are part of [RFC 7519](https://tools.ietf.org/html/rfc7519).
 * More information is available at [jwt.io](https://jwt.io/).
 * This class uses the [auth0 implementation](https://github.com/auth0/java-jwt).
 *
 * The access token verification is stateless.
 * However, the refresh token verification is stateful, and checks whether the account is blocked.
 */
class JwtAuthenticator<R : Role>(
	/**
	 * The name of the service this token authentifies for.
	 */
	private val issuer: String,

	/**
	 * The JWT secret.
	 *
	 * For more information, read [the jwt.io introduction](https://jwt.io/introduction).
	 */
	secret: String,

	/**
	 * The available roles.
	 */
	private val roles: Roles<R>,

	private val accounts: Account.Bone<R>,

	/**
	 * How long access tokens stay valid.
	 *
	 * For more information on access tokens, see [Authenticator].
	 */
	private val accessTokenExpiration: Duration = 30.minutes,

	/**
	 * How long refresh tokens stay valid.
	 *
	 * For more information on refresh tokens, see [Authenticator].
	 */
	private val refreshTokenExpiration: Duration = 7.days,
) : Authenticator<R> {

	private val log = loggerFor(this)

	private val algorithm = Algorithm.HMAC256(secret)

	private val verifierBuilder get() = JWT
		.require(algorithm)
		.withIssuer(issuer)
		.withClaimPresence("role")
		.withClaimPresence("sub")

	private val accessVerifier = verifierBuilder
		.withClaim("mode", "access")
		.build()

	private val refreshVerifier = verifierBuilder
		.withClaim("mode", "refresh")
		.withClaimPresence("epoch")
		.build()

	override suspend fun checkAccessToken(token: String): Principal<R>? {
		val decoded = try {
			accessVerifier.verify(token)
		} catch (e: Exception) {
			log.info(token) { "Could not check access token because the provided JWT is invalid: $e" }
			return null
		}

		val user = decoded.subject ?: run {
			log.info(decoded) { "Could not check access token because it doesn't have a subject" }
			return null
		}

		val roleString = decoded.getClaim("role").asString() ?: run {
			log.info(decoded) { "Could not check access token for user $user because it doesn't have a role, or the role isn't a string" }
			return null
		}

		val role = roles.findById(roleString) ?: run {
			log.info(decoded) { "Could not check access token for user $user because the role doesn't exist: $roleString" }
			return null
		}

		return Principal(
			accounts.fromId(user),
			role,
		)
	}

	override suspend fun checkRefreshToken(token: String): Principal<R>? {
		val decoded = try {
			refreshVerifier.verify(token)
		} catch (e: Exception) {
			log.info(token) { "Could not check refresh token because the provided JWT is invalid: $e" }
			return null
		}

		val user = decoded.subject ?: run {
			log.info(decoded) { "Could not check refresh token because it doesn't have a subject" }
			return null
		}

		val accountRef = accounts.fromId(user)
		val account = accountRef.requestValue()

		if (account.blocked) {
			log.info(decoded, account) { "Invalid refresh token: the user $user is blocked" }
			return null
		}

		val epoch = decoded.getClaim("epoch").asInt() ?: run {
			log.info(decoded) { "Could not check refresh token because it doesn't have an epoch, or the epoch isn't an integer" }
			return null
		}

		if (account.authEpoch != epoch) {
			log.info(decoded, account) { "Invalid refresh token: the user $user has an epoch different from the token's: $epoch, expected ${account.authEpoch}" }
			return null
		}

		val roleString = decoded.getClaim("role").asString() ?: run {
			log.info(decoded) { "Could not check refresh token for user $user because it doesn't have a role, or the role isn't a string" }
			return null
		}

		val role = roles.findById(roleString) ?: run {
			log.info(decoded) { "Could not check refresh token for user $user because the role doesn't exist: $roleString" }
			return null
		}

		return Principal(
			accountRef,
			role,
		)
	}

	private fun createTokenCommon(user: Account<R>) = JWT
		.create()
		.withIssuer(issuer)
		.withIssuedAt(Date.from(Instant.now()))
		// User information
		.withSubject(user.id)
		.withClaim("role", user.role.id)

	override fun createAccessToken(user: Account<R>): String = createTokenCommon(user)
		.withClaim("mode", "access")
		.withExpiresAt(Date.from(Instant.now() + accessTokenExpiration.toJavaDuration()))
		.sign(algorithm)

	override fun createRefreshToken(user: Account<R>): String  = createTokenCommon(user)
		.withClaim("mode", "refresh")
		.withClaim("epoch", user.authEpoch)
		.withExpiresAt(Date.from(Instant.now() + refreshTokenExpiration.toJavaDuration()))
		.sign(algorithm)
}
