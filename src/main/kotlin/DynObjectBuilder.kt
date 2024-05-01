import arrow.core.Either
import arrow.core.raise.either

/**
 * Provides a DSL to create new [DynObject] instances.
 */
class DynObjectBuilder {
    private var objetType: String? = null
    private val properties: MutablePropertyMap = mutableMapOf()

    companion object {
        /**
         * Attempts to create a new [DynObject].
         * @param type The type of the object.
         * @param init The initialization block for the object.
         * @return The created [DynObject].
         * @throws [DynObjectError.UnsupportedPropertyType] if the type of any property is not supported.
         */
        fun newDynObject(type: String, init: DynObjectBuilder.() -> Unit): Either<DynObjectError, DynObject> = either {
            val builder = DynObjectBuilder()
            builder.objetType = type
            builder.init()
            DynObject.dynObject(builder.objetType!!, builder.properties).bind()
        }
    }

    /**
     * Adds or update the value of a property to the [DynObject].
     * @param name The name of the property.
     * @param value The (new or updated) value of the property.
     */
    fun property(name: PropertyName, value: Any) {
        properties[name] = value
    }
}