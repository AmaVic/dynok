# Overview
[![Build](https://github.com/AmaVic/dynok/actions/workflows/build.yml/badge.svg)](https://github.com/AmaVic/dynok/actions/workflows/build.yml) [![Test](https://github.com/AmaVic/dynok/actions/workflows/test.yml/badge.svg)](https://github.com/AmaVic/dynok/actions/workflows/test.yml) [![Doc](https://github.com/AmaVic/dynok/actions/workflows/doc.yml/badge.svg)](https://github.com/AmaVic/dynok/actions/workflows/doc.yml) ![Static Badge](https://img.shields.io/badge/version-0.0.1-github)


**DynOK** stands for "**Dyn**amic **O**bject **K**otlin". It is a pure Kotlin library that allows to create objects with properties that are defined at runtime, similarly to javascript objects. 
It is possible to dynamically retrieve, add and modify the properties of a dynamic object. You can also serialize and deserialize dynamic objects from and to JSON.

The library adopts a functional programming style and relies on the [Arrow](https://arrow-kt.io) library.

## Demonstration
```kotlin
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
```
