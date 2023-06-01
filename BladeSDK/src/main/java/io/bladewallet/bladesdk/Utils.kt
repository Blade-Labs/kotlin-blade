package io.bladewallet.bladesdk

import android.content.Context
import com.fingerprintjs.android.fpjs_pro.Configuration
import com.fingerprintjs.android.fpjs_pro.Error
import com.fingerprintjs.android.fpjs_pro.FingerprintJSFactory
import com.fingerprintjs.android.fpjs_pro.FingerprintJSProResponse
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.google.gson.Gson


internal suspend fun getRemoteConfig(apiKey: String, network: String, dAppCode: String, sdkVersion: String, bladeEnv: BladeEnv): RemoteConfig = withContext(Dispatchers.IO) {
    val url: String = if (bladeEnv === BladeEnv.Prod) {
        "https://rest.prod.bladewallet.io/openapi/v7/sdk/config"
    } else {
        "https://rest.ci.bladewallet.io/openapi/v7/sdk/config"
    }

    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"

    connection.setRequestProperty("x-sdk-token", apiKey)
    connection.setRequestProperty("x-network", network.uppercase())
    connection.setRequestProperty("x-dapp-code", dAppCode)
    connection.setRequestProperty("x-sdk-version", sdkVersion)
    connection.setRequestProperty("content-type", "application/json")

    val responseCode = connection.responseCode
    val reader = if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader(InputStreamReader(connection.inputStream))
    } else {
        BufferedReader(InputStreamReader(connection.errorStream))
    }
    val response = StringBuilder()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        response.append(line)
    }
    reader.close()
    if (responseCode == HttpURLConnection.HTTP_OK) {
        val jsonString = response.toString()
        try {
            val remoteConfig = Gson().fromJson(jsonString, RemoteConfig::class.java)
            return@withContext remoteConfig
        } catch (error: Exception) {
            throw Exception("${error}. Data: `${jsonString}`")
        }
    } else {
        throw Exception(response.toString())
    }
}

internal suspend fun getVisitorId(apiKey: String, context: Context): String = suspendCoroutine { continuation ->
    val factory = FingerprintJSFactory(context)
    val configuration = Configuration(
        apiKey= apiKey,
        region = Configuration.Region.EU,
        endpointUrl = "https://identity.bladewallet.io"
    )

    val fingerprintClient = factory.createInstance(configuration)
    fingerprintClient.getVisitorId(fun(result: FingerprintJSProResponse) {
        continuation.resume(result.visitorId)
    }, fun(error: Error) {
        continuation.resumeWithException(java.lang.Exception("$error"))
    })
}
