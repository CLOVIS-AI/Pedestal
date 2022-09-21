package opensavvy.spine

/**
 * Services are the top-level endpoints of a Spine API.
 *
 * ### Spine API structure
 *
 * A Spine API is composed of multiple services.
 * Services are logical groupings of resources identified by a unique name.
 * Services may be heterogeneous: multiple services exposed by a single server may have a different structure.
 *
 * Exposing multiple services may be necessary to represent:
 * - multiple versions of the same API (with services `v1`, `v2`…)
 * - multiple implementations of the same API (with services `mongodb`, `external`…)
 * - all of the above
 *
 * ### Declaring a new API
 *
 * APIs are inner class structures with an outer class that inherits from `Service`:
 * ```kotlin
 * class Api1 : Service("v1") {
 *     inner class Users : StaticResource<User>("users")
 *
 *     val users = Users()  // this is important!
 * }
 * ```
 * In the current version, it is necessary to instantiate each inner class once as an attribute.
 *
 * Resources are declared as [StaticResource][ResourceGroup.StaticResource] or [DynamicResource][ResourceGroup.DynamicResource].
 * Resources can be nested.
 */
abstract class Service(
	/**
	 * The name of this service.
	 */
	val name: Route.Segment,
) : ResourceGroup() {

	/**
	 * Creates a [Service].
	 *
	 * Because [name] appears in URIs, it must satisfy all constraints of [Route.Segment].
	 */
	constructor(name: String) : this(Route.Segment(name))

	override val routeTemplate get() = name.segment

	override val service get() = this

}
