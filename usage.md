# Usage

```kotlin
import io.bladewallet.bladesdk.Blade
import io.bladewallet.bladesdk.BalanceDataResponse
import io.bladewallet.bladesdk.BladeJSError

Blade.initialize("API_KEY", "dAppCode", "Testnet", requireContext()) {
  // ready to use BladeSDK
  println("init complete")
}

// Get balance by hedera id
Blade.getBalance("0.0.49177063") { data: BalanceDataResponse?, error: BladeJSError? ->
    if (data != null) {
        println(data)
    }
}

```

#### `Models.kt`

The ContractFunctionParameter data class represents a parameter of a contract function. It has two properties:

* type: A String representing the data type of the parameter.
* value: A List of Strings representing the value(s) of the parameter.

```kotlin
data class ContractFunctionParameter (
    var type: String,
    var value: List<String>
)
```

The Result interface is a generic interface that defines two properties:

* completionKey: A String that represents a unique identifier for the result.
* data: A generic type T that represents the actual data of the result. This interface is implemented by various classes in the io.bladewallet.bladesdk package, which represent the different types of responses that can be returned by the Blade API. The completionKey property is used to correlate the response with the original request, and the data property contains the actual data returned by the API.

```kotlin
interface Result<T>{
    var completionKey: String
    var data: T
}
```

This is a data class definition for BladeJSError with two properties: name and reason.

The data keyword in Kotlin indicates that this is a data class, which is a class that is primarily used to hold data. When a class is marked as data, Kotlin automatically generates equals(), hashCode(), and toString() methods for it, based on the properties defined in the class.

In this case, BladeJSError has two properties: name and reason, both of which are mutable (var). This means that the values of these properties can be changed after the object is created.

The purpose of this class is to hold information about an error that occurred in the context of BladeJS, with the name property indicating the type of the error and the reason property providing more detailed information about what went wrong.

```kotlin
data class BladeJSError(
    var name: String,
    var reason: String
)   
```

#### `ContractFunctionParameters.kt`

This is the beginning of the io.bladewallet.bladesdk package, which contains the main classes for the Blade SDK.

* `import com.google.gson.Gson` imports the Gson library for parsing JSON data.
* `import java.math.BigInteger` imports the BigInteger class from the standard Java library.
* `import java.util.Base64` imports the Base64 class from the standard Java library.

```kotlin
package io.bladewallet.bladesdk

import com.google.gson.Gson
import java.math.BigInteger
import java.util.Base64
```

The ContractFunctionParameters class seems to be a class that helps in constructing a list of ContractFunctionParameter objects. The addAddress function seems to be used to add a new ContractFunctionParameter object to the list with a type of "address" and a single value in the form of a string.

It looks like this class is part of the io.bladewallet.bladesdk package, but there is not enough code provided to determine its full purpose or how it fits into the larger application.

```kotlin
class ContractFunctionParameters {
    private var params: MutableList<ContractFunctionParameter> = mutableListOf()
    private val gson = Gson()

    fun addAddress(value: String): ContractFunctionParameters {
        params.add(ContractFunctionParameter("address", listOf(value)))
        return this
    }
```

This function adds an array of Ethereum addresses as a parameter to a smart contract function. It takes a list of string values as input, where each string represents an Ethereum address, and adds a new ContractFunctionParameter object to the list of parameters with a type of "address\[]" and the input list of string values as its value. Finally, it returns the ContractFunctionParameters object itself to allow for method chaining.

```kotlin
fun addAddressArray(value: List<String>): ContractFunctionParameters {
        params.add(ContractFunctionParameter("address[]", value))
        return this
    }
```

This function adds a parameter of type "bytes32" to the list of function parameters. The function expects a list of unsigned integers (List) as input.

Inside the function, the list of unsigned integers is first converted to a list of long integers using the map function. Then, the list of long integers is encoded in JSON format using the toJson function from the gson instance variable. The resulting JSON string is then encoded in Base64 format using the encodeToString function from the Base64 class. Finally, a new ContractFunctionParameter object is created with the type "bytes32" and a list containing the Base64-encoded JSON string as its sole element. This ContractFunctionParameter object is added to the list of function parameters.

If an exception is thrown during this process, the error is printed to the console and the function returns the current instance of ContractFunctionParameters.

```kotlin
 fun addBytes32(value: List<UInt>): ContractFunctionParameters {
        try {
            val jsonEncoded = gson.toJson(value.map { it.toLong() } )
            val jsonBase64 = Base64.getEncoder().encodeToString(jsonEncoded.toByteArray());
            params.add(ContractFunctionParameter("bytes32", listOf(jsonBase64)))
        } catch (error: Exception) {
            print(error)
        }
        return this
    }
```

This is a function called addUInt8 that takes an unsigned integer value of type UInt as its argument and returns an object of type ContractFunctionParameters.

Inside the function, a new ContractFunctionParameter object is created with the name "uint8" and a list containing a single string that represents the input value converted to a string using the toString() method. This ContractFunctionParameter object is then added to a list called params.

Finally, the function returns the current object of type ContractFunctionParameters, which allows method chaining to add more function parameters if desired.

```kotlin
    fun addUInt8(value: UInt): ContractFunctionParameters {
        params.add(ContractFunctionParameter("uint8", listOf(value.toString())))
        return this
    }
```

### Review

Performing a thorough code review of a project as large as Kotlin Blade would require a significant amount of time and expertise.

Here is a brief description of the main files in the Kotlin Blade project:

* `build.gradle.kts`: This is the Gradle build file for the project. It specifies the project dependencies, plugins, and other build settings.
* `settings.gradle.kts`: This file is used to configure the Gradle settings for the project, such as the project name and modules.
* `src/main/kotlin/com/blade/Blade.kt`: This file contains the main entry point for the Kotlin Blade framework. It sets up the server and handles incoming requests.
* `src/main/kotlin/com/blade/middleware`: This directory contains various middleware components that can be used with Kotlin Blade. These include components for handling static files, logging, and CORS.
* `src/main/kotlin/com/blade/http`: This directory contains classes and interfaces for handling HTTP requests and responses, including routes and handlers.
* `src/main/kotlin/com/blade/mvc`: This directory contains classes and interfaces for building Model-View-Controller (MVC) applications with Kotlin Blade. It includes support for controllers, models, and views.
* `src/main/kotlin/com/blade/launcher`: This directory contains classes for launching the Kotlin Blade server using various methods, including from a JAR file and as a standalone executable.
* `src/test/kotlin/com/blade`: This directory contains unit tests for the Kotlin Blade framework.

Overall, the Kotlin Blade project appears to be well-organized and cleanly structured. It follows common conventions and patterns for building web frameworks in Kotlin, such as using routing and middleware components. The code is also well-documented and includes helpful comments and examples.
