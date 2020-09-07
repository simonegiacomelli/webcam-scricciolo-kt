import framework.ApiClient
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.events.Event


fun main() {
    println("v1.4")
    window.onload = ::onload
}

val api = ApiClient(::doRequest)

fun onload(e: Event) {

    val body = document.body!!

    GlobalScope.launch {


        val resp = api.New(SummaryRequest())
        resp.payload.forEach { day ->
            val container = div()
            container.appendChild(div().also { it.innerHTML = day.name })
            day.events.forEach { event ->
                container.appendChild(button().also {
                    it.innerHTML = event.time
                    it.onclickExt = { eventButtonClick(event) }
                })
            }

            body.appendChild(container)
        }
    }
}

suspend fun doRequest(apiName: String, serializedArguments: String): String {
    val url = apiBaseUrl(apiName)
    val resp = window.fetch("$url?$apiArgumentKeyName=$serializedArguments").await()
    return resp.text().await()
}


suspend fun eventButtonClick(event: ApiEvent) {
    println(event.time)
}