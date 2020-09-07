import framework.ApiClient
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLProgressElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event
import kotlin.math.ceil


fun main() {
    println("v1.5")
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
    val intervalMsec: Int get() = msecInput.value.toInt()
}

fun onload(e: Event) {

    val body = document.body!!

    GlobalScope.launch {

        page.days_div.innerHTML = ""

        val resp = api.New(SummaryRequest())
        resp.payload.forEach { day ->
            page.days_div.appendChild(br())
            page.days_div.appendChild(div().also { it.innerHTML = day.name })
            day.events.forEach { event ->
                page.days_div.appendChild(button().also {
                    it.innerHTML = event.time
                    val es = EventShow(day, event)
                    it.onclickExt = { es.buttonClick() }
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


class EventShow(val day: ApiDay, val event: ApiEvent) {

    companion object {
        var show: EventShow? = null
        fun setCurrentShowTo(s: EventShow) {
            show = s
        }
    }

    var handle: Int? = null
    var imageIndex = -1
    var files: List<String> = emptyList()
    var allFiles: List<String> = emptyList()

    suspend fun buttonClick() {
        println("eventButtonClick")
        setCurrentShowTo(this)
        setupEvents()

        allFiles = api.New(EventRequest(event.firstFileName)).files
        updateFiles()
        fromBeginning()
    }

    private fun fromBeginning() {
        imageIndex = -1
        nextClick()
    }

    private fun updateFiles() {
        files = if (page.maskEnabled) allFiles else allFiles.filter { !it.endsWith("m.jpg") }
    }

    fun tick() {
        if (show != this) {
            println("Show changed! was ${event.time}")
            return
        }
        if (!page.automaticNext)
            return

        if (showImage(1))
            setTimeout()
    }

    private fun nextClick() {
        showImage(1)
        setTimeout()
    }

    private fun showImage(offset: Int): Boolean {

        val idx = imageIndex + offset
        if (idx < 0 || idx >= files.size)
            return false
        imageIndex = idx

        val filename = files[idx]
        val fullName = "${event.dayFolder}/${event.name}/$filename"
        page.img.src = "/image?full_filename=$fullName"
        page.progressbar.value = ceil((imageIndex + 1).toDouble() / files.size * 100)
        page.imgDiv.innerHTML = "${event.time} ${imageIndex + 1}/${files.size}"
        debugLoad(fullName, idx)
        return true
    }

    private fun debugLoad(fullName: String, idx: Int) {
        console.log("$fullName Loading")
        page.img.onload = { console.log("$fullName Loaded OK ${idx + 1}/${files.size}") }
    }

    private fun setupEvents() {
        page.progressbar.apply {
            onclick = fun(e) {
                val valueClicked = e.offsetX * this.max / this.offsetWidth / 100;
                val x = files.size * valueClicked
                imageIndex = ceil(x).toInt() - 1
                println("valueClicked=$valueClicked x=$x imageIndex=$imageIndex")
                showImage(0)
            }
        }

        page.prevBtn.onclick = { showImage(-1) }
        page.nextBtn.onclick = { nextClick() }
        page.maskCheckBox.onclick = { fromBeginning() }
        page.resetBtn.onclick = { fromBeginning() }
    }


    private fun setTimeout() {
        clearTimeout()
        if (!page.automaticNext)
            return
        handle = window.setTimeout({ tick() }, page.intervalMsec)
    }

    private fun clearTimeout() {
        val h = handle ?: return
        window.clearTimeout(h)
        handle = null
    }

}