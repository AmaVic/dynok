import arrow.core.raise.either
import DynObjectBuilder.Companion.newDynObject
import kotlin.test.Test
import kotlin.test.fail

class DynObjectBuilderTest {
    @Test
    fun `Successfully create a valid DynObject`() {
        val expectedObject = DynObject(
            "Customer", mapOf(
                "email" to "george@gmail.com",
                "yearsOfService" to listOf(2020, 2021, 2022),
                "premium" to true,
                "discount" to 0.1f,
                "longVal" to 3L,
                "mainCompany" to DynObject("Company", mapOf("name" to "Main Corp"))
            )
        )

        either {
            newDynObject("Customer") {
                property("email", "george@gmail.com")
                property("yearsOfService", listOf(2020, 2021, 2022))
                property("premium", true)
                property("discount", 0.1f)
                property("longVal", 3L)
                property("mainCompany", newDynObject("Company") {
                    property("name", "Main Corp")
                }.bind())
            }.bind()
        }.map { builtObj ->
            assert(builtObj == expectedObject) { "Expected object to be $expectedObject, but it is $builtObj" }
        }.mapLeft {
            fail("Failed to create DynObject due to: $it")
        }
    }

    @Test
    fun `Fail to create invalid DynObject`() {
        newDynObject("Customer") {
            property("invalidPair", "k" to "v")
        }.map {
            fail("Expected to fail to create DynObject, but it succeeded")
        }
    }
}