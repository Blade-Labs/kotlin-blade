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
import io.bladewallet.bladesdk.models.BladeEnv
import io.bladewallet.bladesdk.models.RemoteConfig
import java.io.IOException


internal suspend fun getRemoteConfig(dAppCode: String, sdkVersion: String, bladeEnv: BladeEnv): RemoteConfig = withContext(Dispatchers.IO) {
    val url: String
    val fallbackConfig = RemoteConfig(fpApiKey = "")

    if (bladeEnv === BladeEnv.Prod) {
        url = "https://rest.prod.bladewallet.io/dapi/v8/public/sdk/config"
        fallbackConfig.fpApiKey = "Li4RsMbgPldpOVfWjnaF"
        throw Exception("Prod env not available for v1.0.0 now")
    } else {
        url = "https://dapi.bld-dev.bladewallet.io/dapi/public/v8/sdk/config"
        fallbackConfig.fpApiKey = "0fScXqpS7MzpCl9HgEsI"
    }

    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

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
                return@withContext Gson().fromJson(jsonString, RemoteConfig::class.java)
            } catch (error: Exception) {
                // throw Exception("${error}. Data: `${jsonString}`")
                return@withContext fallbackConfig
            }
        } else {
            return@withContext fallbackConfig
        }
    } catch (e: IOException) {
        return@withContext fallbackConfig
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
