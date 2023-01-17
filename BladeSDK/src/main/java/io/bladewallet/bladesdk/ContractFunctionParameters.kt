package io.bladewallet.bladesdk

import com.google.gson.Gson
import java.math.BigInteger
import java.util.Base64


class ContractFunctionParameters {
    private var params: MutableList<ContractFunctionParameter> = mutableListOf()
    private val gson = Gson()

    fun addAddress(value: String): ContractFunctionParameters {
        params.add(ContractFunctionParameter("address", listOf(value)))
        return this
    }

    fun addAddressArray(value: List<String>): ContractFunctionParameters {
        params.add(ContractFunctionParameter("address[]", value))
        return this
    }

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

    fun addUInt8(value: UInt): ContractFunctionParameters {
        params.add(ContractFunctionParameter("uint8", listOf(value.toString())))
        return this
    }

    fun addUInt64(value: ULong): ContractFunctionParameters {
        params.add(ContractFunctionParameter("uint64", listOf(value.toString())))
        return this
    }

    fun addUInt64Array(value: List<ULong>): ContractFunctionParameters {
        params.add(ContractFunctionParameter("uint64[]", value.map{ it.toString() }))
        return this
    }

    fun addInt64(value: Long): ContractFunctionParameters {
        params.add(ContractFunctionParameter("int64", listOf(value.toString())));
        return this
    }

    fun addUInt256(value: BigInteger): ContractFunctionParameters {
        params.add(ContractFunctionParameter("uint256", listOf(value.toString())));
        return this
    }

    fun addUInt256Array(value: List<BigInteger>): ContractFunctionParameters {
        params.add(ContractFunctionParameter("uint256[]", value.map { it.toString() }));
        return this
    }

    fun addTuple(value: ContractFunctionParameters): ContractFunctionParameters {
        params.add(ContractFunctionParameter("tuple", listOf( value.encode())));
        return this
    }

    fun addTupleArray(value: List<ContractFunctionParameters>): ContractFunctionParameters {
        params.add(ContractFunctionParameter("tuple[]", value.map{it.encode()}))
        return this
    }

    fun addString(value: String): ContractFunctionParameters {
        params.add(ContractFunctionParameter("string", listOf(value)));
        return this
    }

    fun addStringArray(value: List<String>): ContractFunctionParameters {
        params.add(ContractFunctionParameter("string[]", value));
        return this
    }

    fun encode(): String {
        try {
            return gson.toJson(params).replace("\\", "\\\\");
        } catch (error: Exception) {
            println(error)
        }
        return "";
    }

}