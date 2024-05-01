import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure

typealias PropertyName = String
typealias PropertyMap = Map<PropertyName, Any>
typealias MutablePropertyMap = MutableMap<PropertyName, Any>

/**
 * Represents a property of a [DynObject].
 * Properties are immutable.
 * @property name The name of the property.
 * @property value The value of the property. Supported value types are primitive types, [DynObject]s, and [List]s of primitive values or [DynObject]s.
 */
data class Property<T>(val name: PropertyName, val value: T) {
    companion object {
        /**
         * Attempts to create a new [Property] with the given [name] and [value].
         * @param name The name of the property.
         * @param value The value of the property.
         * @return The created [Property].
         * @throws [DynObjectError.UnsupportedPropertyType] if the type of the [value] is not supported.
         */
        fun<T> property(name: PropertyName, value: T): Either<DynObjectError.UnsupportedPropertyType, Property<T>> = either {
            ensure(
                value is String
                        || value is Int
                        || value is Long
                        || value is Float
                        || value is Double
                        || value is Boolean
                        || value is DynObject
                        || value is List<*>
            ) { DynObjectError.UnsupportedPropertyType(name, value!!::class.simpleName ?: "Unknown") }
            if(value is List<*>) {
                ensure(value.all { it is String || it is Int || it is Long || it is Float || it is Double || it is Boolean || it is DynObject }) {
                    DynObjectError.UnsupportedPropertyType(
                        name,
                        "List of ${value.map { it!!::class.simpleName }.distinct().joinToString()}"
                    )
                }
            }
            Property(name, value)
        }

        /**
         * Attempts to cast a [value] of [Any] type into a value of type [T].
         * @return The cast value.
         * @throws [DynObjectError.InvalidCastError] if the cast fails.
         */
        inline fun <reified T> fromAny(value: Any): Either<DynObjectError.InvalidCastError, T> = either {
            ensure(value is T) { DynObjectError.InvalidCastError("$value cannot be cast to type to type ${T::class.simpleName}") }
            value
        }
    }
}