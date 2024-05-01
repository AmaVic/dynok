import arrow.core.raise.either
import org.json.JSONObject
import kotlin.test.Test
import kotlin.test.fail

class DynObjectTest {
    @Test
    fun `Successfully create a DynObject`() {
        either {
            val customer = DynObject(
                "Customer", mapOf(
                    "email" to "george@sunnyvale.com",
                    "premium" to true,
                    "id" to 0L,
                )
            )

            assert(customer.type == "Customer") { "Expected type to be 'Customer', but it is ${customer.type}" }
            val email: String = customer.get<String>("email").bind()
            val premium: Boolean = customer.get<Boolean>("premium").bind()
            val id: Long = customer.get<Long>("id").bind()
            assert(email == "george@sunnyvale.com") { "Expected email to be 'george@sunnyvale.com', but it is $email" }
            assert(premium) { "Expected premium to be true, but it is $premium" }
            assert(id == 0L) { "Expected id to be 0, but it is $id" }
        }.mapLeft {
            fail("Failed to create DynObject due to: $it")
        }
    }

    @Test
    fun `Fail to retrieve unknown Property`() {
        val dynObject = DynObject("Customer", mapOf("email" to "george@sunnyvale.com"))
        dynObject.get<Any>("unknown")
            .map {
                fail("Expected to fail to retrieve unknown property, but it succeeded")
            }
    }

    @Test
    fun `Successfully set existing DynObject Property value`() {
        val dynObject = DynObject("Customer", mapOf("email" to "george@sunnyvale.com"))
        dynObject.set("email", "jim@sunnyvale.com")
            .map { updatedObj ->
                updatedObj.get<String>("email")
                    .map {
                        assert(it == "jim@sunnyvale.com") { "Expected email to be 'jim@sunnyvale.com', but it is $it)" }
                    }.mapLeft {
                        fail("Failed to set property value due to: $it")
                    }
            }
    }

    @Test
    fun `Fail to set property value (invalid type)`() {
        val dynObject = DynObject("Customer", mapOf("email" to "george@gmail.com"))
        dynObject.set("email", 1 to 2).map {
            fail("Expected to fail to set property value, but it succeeded")
        }
    }

    @Test
    fun `Successfully set new DynObject Property`() {
        val dynObject = DynObject("Customer", "premium" to true)
        dynObject.set("email", "jim@sunnyvale.com").map { updatedObj ->
            updatedObj.get<String>("email")
            .map {
                assert(it == "jim@sunnyvale.com") { "Expected email to be 'jim@sunnyvale.com', but it is $it)" }
            }.mapLeft {
                fail("Failed to set property value due to: $it")
            }
        }
    }

    @Test
    fun `Successfully serialize a DynObject into JSON`() {
        val dynObject = DynObject(
            "Customer", mapOf(
                "email" to "jim@tpb.com",
                "premium" to true,
                "company" to DynObject(
                    "Company", mapOf(
                        "name" to "Sunnyvale Trailer Park"
                    )
                )
            )
        )

        val jsonString = dynObject.toJson()
        val jsonObject = JSONObject(jsonString)

        try {
            assert(jsonObject.getString("type") == "Customer") { "Expected type to be 'Customer', but it is ${jsonObject.getString("type")}" }
            val properties = jsonObject.getJSONObject("properties")
            assert(properties.getString("email") == "jim@tpb.com") { "Expected email to be 'jim@tpb.com', but it is ${properties.getString("email")}" }
            assert(properties.getBoolean("premium")) { "Expected premium to be true, but it is ${properties.getBoolean("premium")}" }
            val companyObject = properties.getJSONObject("company")
            assert(companyObject.getString("type") == "Company") { "Expected company type to be 'Company', but it is ${companyObject.getString("type")}" }
            val companyProperties = companyObject.getJSONObject("properties")
            assert(companyProperties.getString("name") == "Sunnyvale Trailer Park") { "Expected company name to be 'Sunnyvale Trailer Park', but it is ${companyProperties.getString("name")}" }
        } catch (e: Exception) {
            fail("Failed to serialize DynObject into JSON due to: ${e.message}")
        }
    }

    @Test
    fun `Successfully deserialize a DynObject from JSON`() {
        val obj = DynObject(
            "Customer",
            "emails" to listOf("michel@pourclaude.be", "mich@mich.be"),
            "yearsOfService" to listOf(2020, 2021),
            "gender" to "male",
            "companies" to listOf(
                DynObject("Company", "name" to "ComiCo Charleroi"),
                DynObject("Company", "name" to "ComiCo Bruxelles")
            ),
            "manager" to DynObject("Employee", "name" to "George")
        )
        val json = obj.toJson()
        DynObject.fromJson(json)
            .map {
                assert(it == obj) { "Expected deserialized object to be equal to original object, but it is not" }
            }.mapLeft {
                fail("Failed to deserialize DynObject from JSON due to: $it")
            }
    }

    @Test
    fun `Fail to deserialize a DynObject from an invalid JSON (invalid format)`() {
        val json = """
            {
                "email": jim@g.com",
            
        """

        DynObject.fromJson(json)
            .map {
                fail("Expected to fail to deserialize DynObject from invalid JSON, but it succeeded")
            }
    }

    @Test
    fun `Fail to deserialize a DynObject from an invalid JSON (missing type)`() {
        val json = """
            {
                "properties": {
                    "email": "george@gmail.com"
                }
            }
        """
        DynObject.fromJson(json)
            .map {
                fail("Expected to fail to deserialize DynObject from invalid JSON, but it succeeded")
            }
    }

    @Test
    fun `Fail to deserialize a DynObject from an invalid JSON (missing properties)`() {
        val json = """
            {
                "type": "Customer"
            }
        """
        DynObject.fromJson(json)
            .map {
                fail("Expected to fail to deserialize DynObject from invalid JSON, but it succeeded")
            }
    }
}