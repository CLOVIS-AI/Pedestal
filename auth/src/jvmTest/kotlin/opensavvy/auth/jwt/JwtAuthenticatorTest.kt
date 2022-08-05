package opensavvy.auth.jwt

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import opensavvy.auth.Account
import opensavvy.auth.Roles
import opensavvy.backbone.Cache
import opensavvy.backbone.Data
import opensavvy.backbone.Result
import opensavvy.logger.LogLevel
import opensavvy.logger.Logger.Companion.debug
import opensavvy.logger.loggerFor
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class JwtAuthenticatorTest {

	private val log = loggerFor(this).apply {
		level = LogLevel.TRACE
	}

	enum class Role : opensavvy.auth.Role {
		USER,
		ADMIN;

		override val id = name
	}

	val roles = Roles.Immutable(Role.values().asList())

	data class Ref(val id: String, override val backbone: Accounts) : Account.Ref<Role>

	class Accounts : Account.Bone<Role> {
		override fun fromId(id: String) = Ref(id, this)

		override val cache: Cache<Account<Role>> = Cache.Default()

		override fun directRequest(ref: opensavvy.backbone.Ref<Account<Role>>): Flow<Data<Account<Role>>> = flow {
			require(ref is Ref) { "$this doesn't support the reference $ref" }

			val account = when (ref.id) {
				"user1" -> Account("user1", Role.USER)
				"user2" -> Account("user2", Role.USER)
				"admin1" -> Account("admin1", Role.ADMIN)
				else -> error("Invalid user id: ${ref.id}")
			}

			emit(Data(Result.Success(account), Data.Status.Completed, ref))
		}
	}

	@Test
	fun test() = runTest {
		val accounts = Accounts()

		val authenticator = JwtAuthenticator(
			"jwt-authenticator-test",
			"this is the super secret used for testing JWTs",
			roles,
			accounts,
		)

		val user1 = Account("user1", Role.USER)
		val user2 = Account("user2", Role.USER)
		val admin1 = Account("admin1", Role.ADMIN)

		val access1 = authenticator.createAccessToken(user1).also { log.debug(it) { "Access token for user1" } }
		val refresh2 = authenticator.createRefreshToken(user2).also { log.debug(it) { "Refresh token for user2" } }
		val access3 = authenticator.createAccessToken(admin1).also { log.debug(it) { "Access token for admin1" } }

		// verify the first access token (should work)
		run {
			val decoded = authenticator.checkAccessToken(access1)
			assertNotNull(decoded)
			assertEquals(user1.role, decoded.role)
			assertEquals(user1.id, (decoded.account as Ref).id)
		}

		// verify the first access token as a refresh token (should not work)
		run {
			val decoded = authenticator.checkRefreshToken(access1)
			assertNull(decoded)
		}

		// verify that the second refresh token is an access token (should not work)
		run {
			val decoded = authenticator.checkAccessToken(refresh2)
			assertNull(decoded)
		}

		// verify the second refresh token (should work)
		run {
			val decoded = authenticator.checkRefreshToken(refresh2)
			assertNotNull(decoded)
			assertEquals(user2.role, decoded.role)
			assertEquals(user2.id, (decoded.account as Ref).id)
		}

		// verify the admin access token (should work)
		run {
			val decoded = authenticator.checkAccessToken(access3)
			assertNotNull(decoded)
			assertEquals(admin1.role, decoded.role)
			assertEquals(admin1.id, (decoded.account as Ref).id)
		}

		// verify the admin access token as a refresh token (should not work)
		run {
			val decoded = authenticator.checkRefreshToken(access3)
			assertNull(decoded)
		}
	}
}
