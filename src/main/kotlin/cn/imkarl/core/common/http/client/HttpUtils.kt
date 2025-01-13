package cn.imkarl.core.common.http.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*

object HttpUtils {

    private fun createHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 10000
            }
        }
    }


    /**
     * 获取重定向后的地址
     */
    suspend fun getRedirectUrl(url: String, maxRedirectCount: Int = 10): String? {
        return getRedirectUrl(url, maxRedirectCount, 0)
    }
    private suspend fun getRedirectUrl(url: String, maxRedirectCount: Int, count: Int): String? {
        var url = url.trim()
        val resp: HttpResponse = request(HttpMethod.Head, url)
        if (resp.status === HttpStatusCode.Found
            || resp.status === HttpStatusCode.TemporaryRedirect
            || resp.status === HttpStatusCode.PermanentRedirect) {
            var newUrl = resp.headers["Location"]
            if (newUrl.isNullOrBlank()) {
                return url
            }
            newUrl = newUrl.trim()
            if (newUrl.startsWith("//")) {
                val p = url.substring(0, 4)
                var last = url.substring(4, 5)
                if (last == ":") {
                    last = ""
                }
                newUrl = "$p$last:$newUrl"
            }
            // 限制重定向次数
            if (count >= maxRedirectCount) {
                return url
            }
            return getRedirectUrl(newUrl)
        }
        return url
    }



    @JvmOverloads
    @JvmStatic
    suspend fun request(
        method: HttpMethod,
        url: String,
        headers: Map<String, String> = emptyMap(),

        params: Map<String, Any>? = null,
        body: String? = null,

        timeout: Int = 5000
    ): HttpResponse {
        val block: HttpRequestBuilder.() -> Unit = {
            this.timeout {
                this.requestTimeoutMillis = timeout.toLong()
                this.connectTimeoutMillis = timeout.toLong()
                this.socketTimeoutMillis = timeout.toLong()
            }
            headers.forEach { header ->
                this.header(header.key, header.value)
            }

            if (!body.isNullOrBlank()) {
                this.setBody(body)
            }
            if (body.isNullOrBlank() || method == HttpMethod.Get) {
                params?.forEach { (key, value) ->
                    this.parameter(key, value)
                }
            }
        }
        return when (method) {
            HttpMethod.Get -> createHttpClient().get(url, block)
            HttpMethod.Post -> createHttpClient().post(url, block)
            HttpMethod.Put -> createHttpClient().put(url, block)
            HttpMethod.Patch -> createHttpClient().patch(url, block)
            HttpMethod.Delete -> createHttpClient().delete(url, block)
            HttpMethod.Head -> createHttpClient().head(url, block)
            HttpMethod.Options -> createHttpClient().options(url, block)
            else -> throw UnsupportedOperationException("Unsupported HTTP method: $method")
        }
    }



    @JvmStatic
    suspend fun post(url: String, body: String = ""): HttpResponse {
        return request(
            method = HttpMethod.Post,
            url = url,
            body = body,
        )
    }

    @JvmStatic
    suspend fun post(url: String, params: Map<String, Any>): HttpResponse {
        return request(
            method = HttpMethod.Post,
            url = url,
            params = params,
        )
    }


    @JvmStatic
    suspend fun get(url: String, body: String = ""): HttpResponse {
        return request(
            method = HttpMethod.Get,
            url = url,
            body = body,
        )
    }

    @JvmStatic
    suspend fun get(url: String, params: Map<String, Any>): HttpResponse {
        return request(
            method = HttpMethod.Get,
            url = url,
            params = params,
        )
    }

}

suspend fun HttpResponse.ungzip(): ByteReadChannel {
    return bodyAsChannel().let { GZip.decode(it) }
}

