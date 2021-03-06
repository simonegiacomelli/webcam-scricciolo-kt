import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLProgressElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event


fun main() {
    println("v2.3")
    window.onload = ::onload
}

val api1 = Api3()

object page {
    var maskSelected: String = "no-mask"
    val automaticNext: Boolean get() = automaticCheckBox.checked
    val img by lazy { img("imgTag") }
    val imgDiv by lazy { document.getElementById("imgDiv") as HTMLSpanElement }
    val days_div by lazy { div("days_div") }
    val progressbar by lazy { document.getElementById("progress_tag") as HTMLProgressElement }
    val automaticCheckBox by lazy { document.getElementById("automatic_tag") as HTMLInputElement }
    private val msecInput by lazy { document.getElementById("automatic_msec") as HTMLInputElement }
    val maskImages by lazy { div("maskImages") }
    val gapInfo by lazy { div("gapInfo") }
    val resetBtn by lazy { button("resetBtn") }
    val prevBtn by lazy { button("prevBtn") }
    val nextBtn by lazy { button("nextBtn") }
    val deleteBtn by lazy { button("deleteBtn") }
    val intervalMsec: Int get() = msecInput.value.toInt()
}

fun onload(e: Event) {

    GlobalScope.launch {

        page.days_div.innerHTML = ""
        val allEvents: MutableList<EventShow> = mutableListOf()

        val days = api1.summary()

        allEvents.addAll(days.flatMap { day ->
            day.events.map { EventShow(day, it, allEvents) }
        })

        allEvents.forEach {
            page.days_div.appendChild(it.btn)
        }


    }
}



