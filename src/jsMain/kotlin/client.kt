import framework.ApiClient
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLProgressElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event
import kotlin.math.min


fun main() {
    println("v2.0")
    window.onload = ::onload
}

val api = ApiClient(::doRequest)

object page {
    val automaticNext: Boolean get() = automaticCheckBox.checked
    val maskEnabled: Boolean get() = maskCheckBox.checked
    val img by lazy { img("img_tag") }
    val imgDiv by lazy { document.getElementById("img_div") as HTMLSpanElement }
    val days_div by lazy { div("days_div") }
    val progressbar by lazy { document.getElementById("progress_tag") as HTMLProgressElement }
    val automaticCheckBox by lazy { document.getElementById("automatic_tag") as HTMLInputElement }
    private val msecInput by lazy { document.getElementById("automatic_msec") as HTMLInputElement }
    val maskCheckBox by lazy { document.getElementById("mask_images") as HTMLInputElement }
    val prevBtn by lazy { button("prevBtn") }
    val nextBtn by lazy { button("nextBtn") }
    val resetBtn by lazy { button("resetBtn") }
    val deleteBtn by lazy { button("deleteBtn") }
    val intervalMsec: Int get() = msecInput.value.toInt()
}

fun onload(e: Event) {

    val body = document.body!!

    GlobalScope.launch {

        page.days_div.innerHTML = ""

        val resp = api.New(SummaryRequest())
        resp.payload.forEach { apiDay ->
            page.days_div.appendChild(br())
            page.days_div.appendChild(div().also { it.innerHTML = apiDay.name })
            val allDayEvents = mutableListOf<EventShow>()
            fun onDelete(ev: EventShow) {
                println("Ondelete ${ev.event.time}")
                val idx = allDayEvents.indexOf(ev)
                if (idx == -1) return
                allDayEvents.removeAt(idx)
                if (allDayEvents.size == 0) return
                val nextIdx = min(idx, allDayEvents.size - 1)
                GlobalScope.async { allDayEvents[nextIdx].startClick() }
            }
            allDayEvents.addAll(apiDay.events.map { apiEvent ->
                EventShow(apiDay, apiEvent) { onDelete(it) }
            })
            allDayEvents.forEach {
                page.days_div.appendChild(it.btn)
            }

        }

    }
}

suspend fun doRequest(apiName: String, serializedArguments: String): String {
    val url = apiBaseUrl(apiName)
    val resp = window.fetch("$url?$apiArgumentKeyName=$serializedArguments").await()
    return resp.text().await()
}


