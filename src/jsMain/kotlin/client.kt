import framework.ApiClient
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.Node
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.html.button
import kotlinx.serialization.InternalSerializationApi

@InternalSerializationApi
fun main() {
    window.onload = {
        GlobalScope.async {
            document.body?.sayHello()
        }
    }
}

suspend fun doRequest(apiName: String, serializedArguments: String): String {
    val url = apiBaseUrl(apiName)
    val resp = window.fetch("$url?$apiArgumentKeyName=$serializedArguments").await()
    return resp.text().await()
}

@InternalSerializationApi
suspend fun Node.sayHello() {
    val api = ApiClient(::doRequest)
    val resp = api.New(LoginRequest("simo", "simo"))
    append {
        div { //comm
            +"Hello from JS 2"
        }
        button {
            +"refresh"
        }
    }
    if (true) {
        append {
            div { //comm
                +"yepa=$resp "
            }
        }
    }
}
