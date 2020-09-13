package framework

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmName
import kotlin.reflect.KSuspendFunction0
import kotlin.reflect.KSuspendFunction1
import kotlin.reflect.KSuspendFunction2

open class BasicRpc {
    val json = Json { allowStructuredMapKeys = true }

    val handlers = mutableMapOf<String, suspend (String) -> String>()

    inline fun <reified Req : Any, reified Res : Any>
            setHandler(
        apiName: String, crossinline handler: suspend (req: Req) -> Res
    ) {
        if (handlers.containsKey(apiName))
            throw Exception("Api [$apiName] already defined. Is it an overloaded method?")
        val h: suspend (String) -> String = { serialized_request ->
            val request: Req = json.decodeFromString(serialized_request)
            val response = handler(request)
            val serialized_response = json.encodeToString(response)
            serialized_response
        }
        handlers[apiName] = h
    }

    @JvmName("registerServerHandler2")
    inline fun <reified P1 : Any, reified P2 : Any, reified Res : Any, reified Req : Pair<P1, P2>>
            registerServerHandler(func: KSuspendFunction2<P1, P2, Res>) {
        val handler: suspend (req: Req) -> Res = { func(it.first, it.second) }
        setHandler(func.name, handler)
    }

    @JvmName("registerServerHandler1")
    inline fun <reified Req : Any, reified Res : Any>
            registerServerHandler(func: KSuspendFunction1<Req, Res>) {
        val handler: suspend (req: Req) -> Res = { func(it) }
        setHandler(func.name, handler)
    }

    @JvmName("registerServerHandler0")
    inline fun <reified Res : Any>
            registerServerHandler(func: KSuspendFunction0<Res>) {
        val handler: suspend (Unit) -> Res = { func() }
        setHandler(func.name, handler)
    }

    suspend inline fun <reified Req, reified Res> cli(apiName: String, request: Req): Res {
        val ser = json.encodeToString(request)
        val resStr = doRequest(apiName, ser)
        val res = json.decodeFromString<Res>(resStr)
        return res
    }


    suspend inline fun <reified P1 : Any, reified P2 : Any, reified Res : Any>
            clientInvoke(func: KSuspendFunction2<P1, P2, Res>, p1: P1, p2: P2): Res {
        println("clientInvoke ${func.name}")
        return cli(func.name, Pair(p1, p2))
    }

    suspend inline fun <reified P1 : Any, reified Res : Any>
            clientInvoke(func: KSuspendFunction1<P1, Res>, p1: P1): Res {
        println("clientInvoke ${func.name}")
        return cli(func.name, p1)
    }

    suspend inline fun <reified Res : Any>
            clientInvoke(func: KSuspendFunction0<Res>): Res {
        println("clientInvoke ${func.name}")
        return cli(func.name, Unit)
    }


    open suspend fun doRequest(apiName: String, serializedArguments: String): String {
        throw NotImplementedError()
    }

}