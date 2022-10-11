package opensavvy.spine

import kotlin.reflect.KProperty

/**
 * Additional parameters for [operations][Operation].
 *
 * Resources and operations declare mandatory parameters, such as the identifier of the resource.
 * This abstract class allows to declare additional parameters in a type-safe manner.
 *
 * ### Usage
 *
 * In common code, declare a class that inherits from [Parameters], and use it to declare the name and type of the available parameters:
 * ```kotlin
 * class MyRequestParameters : Parameters() {
 *     var param1: String? by parameter("param1")
 *     var isSubscribed: Boolean by parameter("is_subscribed", default = false)
 * }
 * ```
 *
 * To create a new parameter bundle, use the standard library [apply] function:
 * ```kotlin
 * val example = MyRequestParameters().apply {
 *     param1 = "value"
 *     isSubscribed = true
 * }
 * ```
 *
 * The values can be accessed in a type-safe manner:
 * ```kotlin
 * println(example.param1)
 * ```
 * The values can also be accessed via their string representation:
 * ```kotlin
 * println(example.data["param1"])
 * ```
 */
abstract class Parameters {

	/**
	 * Internal string representation of the parameters.
	 */
	val data = HashMap<String, String>()

	/**
	 * Declares an optional parameter [name] of type [T].
	 *
	 * If the parameter is missing, reading it will return [default] instead.
	 */
	fun <T : Any> parameter(name: String, default: T) = Parameter(name, default)

	/**
	 * Declares a parameter [name] of type [T].
	 *
	 * If [T] is nullable, this function declares an optional parameter with a default value of `null`.
	 * If [T] is non-nullable, this function declares a mandatory parameter which will throw a [NoSuchElementException] if no value is provided.
	 */
	fun <T : Any?> parameter(name: String) = Parameter<T>(name, defaultValue = null)

	// Internal type used to store the name of a parameter
	// End users should not need to use it directly
	inner class Parameter<@Suppress("unused") T>(val name: String, val defaultValue: T?) {

		init {
			require(name.isNotBlank()) { "The name of a parameter cannot be empty: '$name'" }
			require(name.none { it.isWhitespace() }) { "The name of a parameter cannot contain whitespace: '$name'" }
		}
	}

	/**
	 * The default parameter instance.
	 *
	 * Use this instance for operations that take no parameters.
	 */
	object Empty : Parameters()

	// Accesses the value and converts it from a String
	// This is what the 'by' keyword calls when reading from the value
	// Everything it does is explained in the 'parameter' documentation
	inline operator fun <reified T> Parameter<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
		val value = data[name] ?: return run {
			if (defaultValue is T)
				defaultValue
			else
				throw NoSuchElementException("The parameter '$name' is mandatory, but no value was provided.")
		}

		return when (T::class) {
			String::class -> value as T
			Boolean::class -> value.toBooleanStrict() as T
			Byte::class -> value.toByte() as T
			Short::class -> value.toShort() as T
			Int::class -> value.toInt() as T
			Long::class -> value.toLong() as T
			UByte::class -> value.toUByte() as T
			UShort::class -> value.toUShort() as T
			UInt::class -> value.toUInt() as T
			ULong::class -> value.toULong() as T
			Float::class -> value.toFloat() as T
			Double::class -> value.toDouble() as T
			else -> throw UnsupportedOperationException("The type ${T::class.qualifiedName ?: T::class.simpleName ?: T::class.toString()} is not currently supported in parameters.")
		}
	}

	// Writes the value and converts it to a String
	// This is what the 'by' keyword calls when writing to the value
	// Everything it does is explained in the 'parameter' documentation
	inline operator fun <reified T> Parameter<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		val str: String = when (T::class) {
			String::class -> value.toString()
			Boolean::class -> value.toString()
			Byte::class -> value.toString()
			Short::class -> value.toString()
			Int::class -> value.toString()
			Long::class -> value.toString()
			UByte::class -> value.toString()
			UShort::class -> value.toString()
			UInt::class -> value.toString()
			ULong::class -> value.toString()
			Float::class -> value.toString()
			Double::class -> value.toString()
			else -> throw UnsupportedOperationException("The type ${T::class.qualifiedName ?: T::class.simpleName ?: T::class.toString()} is not currently supported in parameters.")
		}

		data[name] = str
	}
}
