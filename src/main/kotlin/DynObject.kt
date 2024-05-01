import Property.Companion.property
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

typealias ObjectType = String

/**
 * Base [Exception] class for all errors of the DynOK library.
 * @param reason The reason for the error.
 */
sealed class DynObjectError(reason: String? = null): Exception(reason) {
    /**
     * Error that occurs when a property type is not supported.
     * @param propertyName The name of the property that has an unsupported type.
     * @param typeName The name of the unsupported type.
     */
    class UnsupportedPropertyType(propertyName: PropertyName, typeName: String): DynObjectError("Type of Property $propertyName ($typeName) is not supported. Valid types are primitive types, DynObjects, and Lists of any of these.")

    /**
     * Error that occurs when attempting to access a property that does not exist in a [DynObject].
     * @param [name] The name of the property that was not found.
     */
    class PropertyNotFoundError(name: PropertyName): DynObjectError("Property $name not found")

    /**
     * Error that occurs when attempting to cast a value to a type that it cannot be cast to.
     * @param reason The reason for the invalid cast.
     */
    class InvalidCastError(reason: String): DynObjectError("Invalid cast: $reason")

    /**
     * Error that occurs when a serialization error occurs.
     * @param reason The reason for the serialization error.
     */
    class SerializationError(reason: String): DynObjectError("Serialization error: $reason")
}

/**
 * A [DynObject] is a dynamic object to which properties can be added, updated and removed at runtime.
 * [DynObject]s are immutable.
 * @param type The type of the object.
 * @param properties The properties of the object (name-value pairs).
 */
data class DynObject(val type: ObjectType, val properties: PropertyMap = emptyMap()) {
    companion object {
        /**
         * Attempts to create a new [DynObject] with the given [properties].
         * @param type The type of the object.
         * @param properties The properties of the object (name-value pairs).
         * @return The created [DynObject].
         * @throws [DynObjectError.UnsupportedPropertyType] if the type of any property is not supported.
         */
        fun dynObject(type: ObjectType, properties: PropertyMap = emptyMap()): Either<DynObjectError.UnsupportedPropertyType, DynObject> = either {
            properties.forEach { (name, value) ->
                Property.property(name, value).bind()
            }
            DynObject(type, properties)
        }

        /**
         * Attempts to create a [DynObject] from a JSON string.
         * @param json The JSON string to parse.
         * @return The [DynObject] parsed from the JSON string.
         * @throws [DynObjectError.SerializationError] if the JSON string is not a valid representation of a [DynObject].
         */
        fun fromJson(json: String): Either<DynObjectError.SerializationError, DynObject> = either {
            try {
                JSONObject(json).let { jsonObject ->
                    if (!jsonObject.has("type") || !jsonObject.has("properties")) {
                        return DynObjectError.SerializationError("Missing 'type' or 'properties' field").left()
                    }
                    val type = jsonObject.getString("type")
                    val rawProperties = jsonObject.getJSONObject("properties").toMap()
                    val properties = rawProperties.mapValues { prop ->
                        if(prop.value is HashMap<*, *>) {
                            val dynObjRaw: Map<String, Any> =
                                @Suppress("UNCHECKED_CAST")
                                (prop.value as HashMap<String, Any>).toMap()
                            val dynObjJson = JSONObject(dynObjRaw).toString()
                            fromJson(dynObjJson).bind()
                        } else if(prop.value is List<*>){
                            val list = prop.value as List<*>
                            list.map { item ->
                                if(item is HashMap<*, *>) {
                                    val dynObjRaw: Map<String, Any> =
                                        @Suppress("UNCHECKED_CAST")
                                        (item as HashMap<String, Any>).toMap()
                                    val dynObjJson = JSONObject(dynObjRaw).toString()
                                    fromJson(dynObjJson).bind()
                                } else {
                                    item
                                }
                            }
                        } else {
                            prop.value
                        }
                    }
                    DynObject(type, properties)
                }
            } catch (e: JSONException) {
                raise(DynObjectError.SerializationError(e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Creates a new [DynObject] with the given [properties].
     * @param type The type of the object.
     * @param properties The properties of the object (name-value pairs).
     * @return The new [DynObject] with the given properties.
     */
    constructor(type: ObjectType, vararg properties: Pair<PropertyName, Any>): this(type, properties.toMap())

    /**
     * Attempts to retrieve the value of the property with the given [name], and to cast it as [T].
     * @return The value of the property as [T].
     * @throws [DynObjectError.PropertyNotFoundError] if the property does not exist.
     */
    inline fun<reified T> get(name: PropertyName): Either<DynObjectError, T> = either {
        properties[name]?.let { rawValue ->
            Property.fromAny<T>(rawValue).bind()
        } ?: raise(DynObjectError.PropertyNotFoundError(name))
    }

    /**
     * Creates a copy of the [DynObject] with the property [name] added or updated with [value].
     * @param name The name of the property to add or update.
     * @param value The value of the property to add or update.
     * @return The new [DynObject] with the property added or updated.
     * @throws [DynObjectError.UnsupportedPropertyType] if the type of the [value] is not supported.
     */
    fun<T> set(name: PropertyName, value: T): Either<DynObjectError.UnsupportedPropertyType, DynObject> =
        property(name, value).map {
            copy(properties = properties.toMutableMap().apply { put(name, value as Any) }.toMap())
        }

    /**
     * Serializes the [DynObject] into a JSON string. It is represented using a JSON with 2 fields: 'type' which represents the [type] of the object, and 'properties', a JSON object that represents the [properties] of the object.
     * @return The JSON string representation of the [DynObject].
     */
    fun toJson(): String =
        mutableMapOf<String, Any>().let { map ->
        map["type"] = type
        map["properties"] = properties
        JSONObject(map).toString(2)
    }
}