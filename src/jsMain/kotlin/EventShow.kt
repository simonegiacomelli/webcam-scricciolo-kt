import fragment.HotkeyWindow
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.datetime.*
import org.w3c.dom.HTMLElement
import kotlin.math.ceil
import kotlin.math.min

class EventShow(
    val day: ApiDay,
    val event: ApiEvent,
    val allEvents: MutableList<EventShow>
) {

    val btn = button().also {
        it.innerHTML = event.time
        it.onclickExt = { startClick() }
    }

    companion object {
        var show: EventShow? = null
        fun setCurrentShowTo(s: EventShow) {
            show?.removeHighlight()
            show = s
        }

        init {
            handleKeyboard()
        }

        private fun handleKeyboard() {
            println("Handling keybord hotkey")
            HotkeyWindow.add("RIGHT") { show?.nextClick(10) }
            HotkeyWindow.add("LEFT") { show?.nextClick(-10) }
            HotkeyWindow.add("D") { GlobalScope.async { show?.deleteEvent() } }
            HotkeyWindow.add("R") { GlobalScope.async { show?.fromBeginning() } }
        }
    }

    var handle: Int? = null
    var imageIndex = -1
    var files: List<String> = emptyList()

    var allFiles: List<String> = emptyList()

    private suspend fun startClick() {
        allFiles = api1.eventFileList(event.firstFileName)
        highLightButton()
        fixAllButtonsVisibility()
        updateGapInfo()
        setCurrentShowTo(this)
        setupEvents()
        fromBeginning()
    }

    private fun fromBeginning() {
        updateFiles()
        imageIndex = -1
        nextClick()
    }

    private fun updateFiles() {
        val masks = allFiles.filter { it.endsWith("m.jpg") }.map { it.removeSuffix("m.jpg") }.toSet()
        files = when (page.maskSelected) {
            "no-mask" -> allFiles.filter { !it.endsWith("m.jpg") }
            "mask" -> allFiles.filter { it.endsWith("m.jpg") }
            "paired" -> {
                allFiles.filter { masks.contains(it.removeSuffix("m.jpg").removeSuffix(".jpg")) }
            }
            "activ" -> {
                allFiles.filter {
                    !it.endsWith("m.jpg") &&
                            masks.contains(it.removeSuffix(".jpg"))
                }
            }
            "both" -> allFiles
            else -> throw Exception("Selezione maschera non riconosciuta ${page.maskSelected}")
        }
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

    private fun nextClick(offset: Int = 1) {
        println("next $offset")
        showImage(offset)
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
        page.imgDiv.innerHTML = "${day.name} ${imageIndex + 1}/${files.size} | all-files=${allFiles.size}"
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
        page.maskImages.onclick = {
            val t = it.target
            if (t != null) {
                val e = t as HTMLElement
                highLightElement(e)
                page.maskSelected = e.innerHTML
            }
            fromBeginning()
        }
        page.resetBtn.onclick = { fromBeginning() }
        page.deleteBtn.onclickExt = { deleteEvent() }
    }

    private suspend fun deleteEvent() {
        if (!window.confirm("Are you sure?"))
            return
        clearTimeout()
        api1.deleteEvent(event.firstFileName)
        btn.remove()
        onDelete()
    }

    private fun onDelete() {
        val idx = allEvents.indexOf(this)
        allEvents.removeAt(idx)
        if (allEvents.isEmpty()) return
        val nextIdx = min(idx, allEvents.size - 1)
        GlobalScope.async { allEvents[nextIdx].startClick() }
    }

    private fun fixAllButtonsVisibility() {
        val idx = allEvents.indexOf(this)
        allEvents.forEachIndexed { i, e ->
            e.btn.visible = i >= idx - 2 && i <= idx + 2
        }
    }


    private fun updateGapInfo() {
        val idx = allEvents.indexOf(this)
        val info = StringBuilder()
        get(idx - 1)?.let {
            val p = gap(it.event.lastInstant, this.event.firstInstant)
            info.append("<-- $p")
        }
        val p = gap(event.firstInstant, event.lastInstant)
        info.append("<br>--= $p<br>")
        get(idx + 1)?.let {
            val p = gap(this.event.lastInstant, it.event.firstInstant)
            info.append("--> $p")
        }
        info.append("<br>")
        page.gapInfo.innerHTML = info.toString()
    }

    private fun gap(a: String, b: String): String {
        val s = a.toInstant()
        val e = b.toInstant()
        val d = s.periodUntil(e, TimeZone.UTC)
        val res = d.toString()
        val sb = mutableListOf<String>()

        fun append(element: Int, spec: String): MutableList<String> {
            val s2 = if (element > 1) "${spec}s" else spec
            sb.add("$element $s2")
            return sb
        }
        d.apply {
            fun allNotPositive() =
                years <= 0 && months <= 0 && days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0 && nanoseconds <= 0 &&
                        (years or months or days or hours or minutes != 0 || seconds or nanoseconds != 0L)
            sb.apply {

                val sign = if (allNotPositive()) {
                    -1
                } else 1
                if (years != 0) append(years * sign, "year")
                if (months != 0) append(months * sign, "month")
                if (days != 0) append(days * sign, "day")
                if (hours != 0) append(hours * sign, "hour")
                if (minutes != 0) append(minutes * sign, "minute")
                if (seconds != 0L) append(seconds.toInt() * sign, "second")
                if (sb.size == 0) sb.add("0 seconds")
            }
        }
        return sb.joinToString(", ")
    }

    private fun get(i: Int): EventShow? {
        return if (i < allEvents.size && i > -1) allEvents[i] else null
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

    private fun removeHighlight() {
        btn.classList.remove("selected_btn")
    }

    private fun highLightButton() {
        highLightElement(btn)
    }

    private fun highLightElement(element: HTMLElement) {
        element.classList.add("selected_btn")
        element.blur()
    }

}

