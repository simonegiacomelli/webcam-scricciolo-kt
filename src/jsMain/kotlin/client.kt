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
val img by lazy { img("img_tag") }

fun onload(e: Event) {

    val body = document.body!!

    GlobalScope.launch {

        val container = div("days_div")
        container.innerHTML = ""

        val resp = api.New(SummaryRequest())
        resp.payload.forEach { day ->
            container.appendChild(div().also { it.innerHTML = day.name })
            day.events.forEach { event ->
                container.appendChild(button().also {
                    it.innerHTML = event.time
                    it.onclickExt = { eventButtonClick(day, event) }
                })
            }

        }

    }
}

suspend fun doRequest(apiName: String, serializedArguments: String): String {
    val url = apiBaseUrl(apiName)
    val resp = window.fetch("$url?$apiArgumentKeyName=$serializedArguments").await()
    return resp.text().await()
}


suspend fun eventButtonClick(day: ApiDay, event: ApiEvent) {
    println("eventButtonClick")
    val resp = api.New(EventRequest(event.firstFileName))

    resp.files.forEach {
        println(it)
    }
    val first = resp.files.first()
    img.src = "/image?full_filename=${event.dayFolder}/${event.name}/$first"
    console.log("Loading ${img.src}")
    img.onload = { console.log("onload for $first") }
}