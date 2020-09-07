package framework

import kotlinx.serialization.*
import kotlinx.serialization.json.Json


interface HasResponse<Res>

val json = Json { allowStructuredMapKeys = true }

class ApiClient(val doRequest: suspend (apiName: String, serializedArguments: String) -> String) {
    suspend inline fun <reified Req : HasResponse<Res>, reified Res : Any> New(request: Req): Res {
        val serialized_request = json.encodeToString(Req::class.serializer(), request)
        val responseStr = doRequest(Req::class.simpleName!!, serialized_request)
        return json.decodeFromString(Res::class.serializer(), responseStr)
    }
}
