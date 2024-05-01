import DynObjectBuilder.Companion.newDynObject
import arrow.core.raise.either

fun main() {
    either {
        // Create a new DynObject with primitive, DynObject and List properties
        val customer = newDynObject("Customer") {
            property("id", 0L)
            property("email", "george@green.fr")
            property("premium", true)
            property("yearsOfService", listOf(2020, 2021, 2022))
            property("discount", 0.1f)
            property("employer", newDynObject("Company") {
                property("name", "Main Corp")
            }.bind())
            property("ownedCompanies", listOf(
                newDynObject("Company") {
                    property("name", "Acme")
                }.bind(),
                newDynObject("Company") {
                    property("name", "Globex")
                }.bind()
            ))
        }.bind()

        // Retrieve the values of properties
        val customerEmail = customer.get<String>("email").bind()
        val customerPremium = customer.get<Boolean>("premium").bind()
        val customerEmployerName = customer.get<DynObject>("employer").bind().get<String>("name").bind()

        // Update the value of a property (produces a copy with updated property)
        val updatedCustomer = customer.set("email", "newemail@gmail.com")

        // JSON Serialization
        val json = customer.toJson()
        println(json)

        // JSON De-Serialization
        val dynObjJson = """
            {
              "type": "Customer",
              "properties": {
                "premium": true,
                "yearsOfService": [
                  2020,
                  2021,
                  2022
                ],
                "ownedCompanies": [
                  {
                    "type": "Company",
                    "properties": {"name": "Acme"}
                  },
                  {
                    "type": "Company",
                    "properties": {"name": "Globex"}
                  }
                ],
                "discount": 0.1,
                "employer": {
                  "type": "Company",
                  "properties": {"name": "Main Corp"}
                },
                "id": 0,
                "email": "george@green.fr"
              }
            }
        """

        val loadedFromJson: DynObject = DynObject.fromJson(dynObjJson).bind()
    }.mapLeft { error ->
        println("Error: $error")
    }
}