package framework

import kotlinx.serialization.serializer

class ApiServer {
    val handlers = mutableMapOf<String, (String) -> String>()

    inline fun <reified Req : HasResponse<Res>, reified Res : Any> register(crossinline handler: (req: Req) -> Res) {
        val h: (String) -> String = { serialized_request ->
            val request = json.decodeFromString(Req::class.serializer(), serialized_request)
            val response = handler(request)
            val serialized_response = json.encodeToString(Res::class.serializer(), response)
            serialized_response
        }
        handlers[Req::class.simpleName!!] = h
    }

    fun invoke(requestClassName: String, serialized_request: String): String {
        return handlers[requestClassName]!!(serialized_request)
    }
}