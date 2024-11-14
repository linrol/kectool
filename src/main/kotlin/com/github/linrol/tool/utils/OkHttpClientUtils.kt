package com.github.linrol.tool.utils

import com.intellij.openapi.diagnostic.logger
import okhttp3.*
import java.util.concurrent.TimeUnit

class OkHttpClientUtils {

    private var client = OkHttpClient()

    private val logger = logger<OkHttpClientUtils>()

    private fun <T> request(request: Request, callback: (ResponseBody) -> T): T {
        client.newCall(request).execute().let {
            if (it.isSuccessful) {
                return callback(it.body())
            } else {
                val error = "request url[${request.url()}] response error: ${it.code()} - ${it.message()}"
                logger.error(error)
                throw RuntimeException(error)
            }
        }
    }

    fun <T> get(url: String, callback: (ResponseBody) -> T): T {
        val request = Request.Builder().url(url).get().build()
        return request(request, callback)
    }

    fun <T> get(url: String, headers: Headers, callback: (ResponseBody) -> T): T {
        val request = Request.Builder().url(url).headers(headers).get().build()
        return request(request, callback)
    }

    fun <T> post(url: String, body: RequestBody, callback: (ResponseBody) -> T): T {
        val request = Request.Builder().url(url).post(body).build()
        return request(request, callback)
    }

    fun <T> post(url: String, headers: Headers, body: RequestBody, callback: (ResponseBody) -> T): T {
        val request = Request.Builder().url(url).headers(headers).post(body).build()
        return request(request, callback)
    }

    fun connectTimeout(timeout: Long, unit: TimeUnit) : OkHttpClientUtils {
        this.client = client.newBuilder().connectTimeout(timeout, unit).readTimeout(timeout, unit).writeTimeout(timeout, unit).build()
        return this
    }
}