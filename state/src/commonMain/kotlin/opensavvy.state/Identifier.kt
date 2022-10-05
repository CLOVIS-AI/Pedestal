package opensavvy.state

/**
 * Simple marker interface to identify an object of type [T].
 *
 * ### Contract
 *
 * In the following section, "the same" and "different" refer to the [equals] method.
 * - If two implementations of [Identifier] are the same, the object their refer to must be the same.
 * - Different implementations of [Identifier] are allowed to refer to the same object, however this is not recommended as it negatively affects caches.
 */
interface Identifier<@Suppress("unused") out T>
