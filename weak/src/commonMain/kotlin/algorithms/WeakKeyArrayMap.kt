package opensavvy.pedestal.weak.algorithms

import opensavvy.pedestal.weak.ExperimentalWeakApi
import opensavvy.pedestal.weak.SoftRef
import opensavvy.pedestal.weak.WeakMap
import opensavvy.pedestal.weak.WeakRef

@ExperimentalWeakApi
internal class WeakKeyMapImpl<K, V>(
	private val keyGenerator: (K) -> WeakRef<K>,
	private val keysAreTheSame: (K, K) -> Boolean
) : WeakMap<K, V> {

	private val data = ArrayList<WeakKeyMapNode<K, V>>()

	override fun get(key: K): V? {
		val iter = data.iterator()

		while (iter.hasNext()) {
			val node = iter.next()
			val storedKey = node.key.read()

			if (storedKey == null) {
				// This is a weak reference that has been cleared by the GC, remove it from the list.
				iter.remove()
			} else if (keysAreTheSame(storedKey, key)) {
				// This is the key we're searching for!
				return node.value
			} // else: not the key we're searching for, continue looping
		}

		return null
	}

	override fun set(key: K, value: V) {
		remove(key)
		data.add(WeakKeyMapNode(keyGenerator(key), value))
	}

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean =
		get(key) != null

	@ExperimentalWeakApi
	override fun remove(key: K): V? {
		val iter = data.iterator()

		while (iter.hasNext()) {
			val node = iter.next()

			if (node.key.read() == key) {
				iter.remove()
				return node.value
			}
		}

		return null
	}

	override fun toString(): String = buildString {
		append("WeakKeyArrayMap {")

		val iter = data.iterator()

		while (iter.hasNext()) {
			val node = iter.next()
			val key = node.key.read()

			if (key == null) {
				iter.remove()
			} else {
				append(key)
				append(" = ")
				append(node.value)
				append(", ")
			}
		}

		if (data.isNotEmpty())
			deleteRange(length - 2, length)

		append("}")
	}
}

private data class WeakKeyMapNode<K, V>(
	val key: WeakRef<K>,
	val value: V
)

private val compareByEquality = { a: Any?, b: Any? -> a == b }
private val compareByIdentity = { a: Any?, b: Any? -> a === b }

/**
 * A pure-Kotlin common [WeakMap] implementation using [WeakRef].
 *
 * Keys are compared using [equals][Any.equals].
 *
 * This implementation is ideal when caching requests that are relatively cheap
 * or when the keys have short lifetimes: we prefer to free memory early.
 *
 * ### Performance characteristics
 *
 * Elements are stored in an [ArrayList].
 * On each operation, elements are scanned linearly until the
 * element on which the operation is requested is found.
 * Expired elements are freed when they are visited.
 * This implies that all operations are in `O(n)`.
 *
 * Additionally, older elements are visited more often, so they are more likely to be freed early.
 */
@ExperimentalWeakApi
fun <K, V> WeakKeyArrayMap(): WeakMap<K, V> =
	WeakKeyMapImpl(
		keyGenerator = { WeakRef(it) },
		keysAreTheSame = compareByEquality
	)

/**
 * A pure-Kotlin common [WeakMap] implementation using [SoftRef].
 *
 * Keys are compared using [equals][Any.equals].
 *
 * This implementation is ideal when caching requests that are relatively expensive
 * or when the same keys are requested throughout the app's lifetime:
 * we prefer to keep the cached value longer as long as there is memory available.
 *
 * ### Performance characteristics
 *
 * Elements are stored in an [ArrayList].
 * On each operation, elements are scanned linearly until the
 * element on which the operation is requested is found.
 * Expired elements are freed when they are visited.
 * This implies that all operations are in `O(n)`.
 *
 * Additionally, older elements are visited more often, so they are more likely to be freed early.
 */
@ExperimentalWeakApi
fun <K, V> SoftKeyArrayMap(): WeakMap<K, V> =
	WeakKeyMapImpl(
		keyGenerator = { SoftRef(it) },
		keysAreTheSame = compareByEquality
	)

/**
 * A pure-Kotlin common [WeakMap] implementation using [WeakRef].
 *
 * Keys are compared by identity.
 *
 * This implementation is ideal when storing mappings from large complex objects to other large objects.
 *
 * ### Performance characteristics
 *
 * Elements are stored in an [ArrayList].
 * On each operation, elements are scanned linearly until the
 * element on which the operation is requested is found.
 * Expired elements are freed when they are visited.
 * This implies that all operations are in `O(n)`.
 *
 * Additionally, older elements are visited more often, so they are more likely to be freed early.
 */
@ExperimentalWeakApi
fun <K, V> IdentityWeakKeyArrayMap(): WeakMap<K, V> =
	WeakKeyMapImpl(
		keyGenerator = { WeakRef(it) },
		keysAreTheSame = compareByIdentity
	)

/**
 * A pure-Kotlin common [WeakMap] implementation using [SoftRef].
 *
 * Keys are compared by identity.
 *
 * This implementation is ideal when storing mappings from specific long-lived objects.
 *
 * Do not use this implementation with short-lived objects! It will waste memory, as the cached
 * values will be retained longer than necessary in case the same keys appears again,
 * but this cannot happen since another key with the same identity cannot be created.
 *
 * ### Performance characteristics
 *
 * Elements are stored in an [ArrayList].
 * On each operation, elements are scanned linearly until the
 * element on which the operation is requested is found.
 * Expired elements are freed when they are visited.
 * This implies that all operations are in `O(n)`.
 *
 * Additionally, older elements are visited more often, so they are more likely to be freed early.
 */
@ExperimentalWeakApi
fun <K, V> IdentitySoftKeyArrayMap(): WeakMap<K, V> =
	WeakKeyMapImpl(
		keyGenerator = { SoftRef(it) },
		keysAreTheSame = compareByIdentity
	)
