package be.vamaralds.dynok

import arrow.core.raise.either
import be.vamaralds.be.vamaralds.dynok.DynObject
import be.vamaralds.be.vamaralds.dynok.DynObjectError
import be.vamaralds.be.vamaralds.dynok.Property
import be.vamaralds.be.vamaralds.dynok.Property.Companion.property
import kotlin.test.Test
import kotlin.test.fail

class PropertyTest {
    @Test
    fun `Successfully create a valid Property of each supported Type`() {
        either<DynObjectError.UnsupportedPropertyType, Unit> {
            property("name", "George")
            property("age", 42)
            property("height", 1.85)
            property("isAlive", true)
            property("company", DynObject("Company", "name" to "Acme", "location" to "USA"))
            property("years", listOf(2020, 2021))
            property("companies", listOf(
                DynObject("Company", "name" to "Acme", "location" to "USA"),
                DynObject("Company", "name" to "Globex", "location" to "USA")
            ))
        }.mapLeft {
            fail("Failed to create property due to: $it")
        }
    }

    @Test
    fun `Fail to create an invalid Property`() {
        property("name", "h" to "i")
            .map {
                fail("Expected to fail to create property, but it succeeded")
            }
    }

    @Test
    fun `Fail to create an invalid List Property`() {
        property("names", listOf("Jim" to "Lahey", "Randy" to "Bobandy"))
            .map {
                fail("Expected to fail to create property, but it succeeded")
            }
    }

    @Test
    fun `Successfully make valid cast`() {
        Property.fromAny<String>("George")
            .mapLeft {
                fail("Cast should have succeeded but it failed due to: $it")
            }
    }

    @Test
    fun `Fail to make invalid cast`() {
        Property.fromAny<Int>("George")
            .map {
                fail("Cast should have failed but it succeeded")
            }
    }
}