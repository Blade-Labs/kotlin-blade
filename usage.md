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
