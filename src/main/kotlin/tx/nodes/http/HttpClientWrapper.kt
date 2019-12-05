package tx.nodes.http

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import tx.nodes.API.ApiDhtDetail
import tx.nodes.API.ApiGetResponse
import tx.nodes.API.PostSnippet

class HttpClientWrapper {
    val masterNodeReference = "masternode" // ! this works thanks to docker compose

    private val httpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
    }
    suspend fun masternodeConnectGet(): HttpClientCall {
        return httpClient.call("http://$masterNodeReference/connect") {
            method = HttpMethod.Get
        }
    }

    suspend fun ping(node: String = masterNodeReference): Boolean {
        return try {
            httpClient.get<HttpStatusCode>("http://$node/api/ping/") == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun postSnippetTo(data: PostSnippet, target: String) {
        httpClient.post<HttpStatusCode>("http://$target") {
            contentType(ContentType.Application.Json)
            body = data
        }
    }

    suspend fun postDhtUpdateTo(apiDhtUpdate: ApiDhtDetail, target: String) {
        httpClient.post<Any>("http://$target/dht") {
            contentType(ContentType.Application.Json)
            body = apiDhtUpdate
        }
    }

    suspend fun getTransfer(ip: String, dataId: Int): ApiGetResponse {
        return httpClient.get("http://$ip/?id=$dataId")
    }
}