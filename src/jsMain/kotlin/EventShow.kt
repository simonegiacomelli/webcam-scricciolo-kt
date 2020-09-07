import fragment.HotkeyWindow
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.w3c.dom.HTMLButtonElement
import kotlin.math.ceil

class EventShow(val day: ApiDay, val event: ApiEvent, val btn: HTMLButtonElement) {

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
        }
    }

    var handle: Int? = null
    var imageIndex = -1
    var files: List<String> = emptyList()

    var allFiles: List<String> = emptyList()

    suspend fun buttonClick() {
        highLightButton()
        setCurrentShowTo(this)
        setupEvents()

        allFiles = api.New(EventRequest(event.firstFileName)).files
        fromBeginning()
    }

    private fun fromBeginning() {
        updateFiles()
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
        page.deleteBtn.onclickExt = { deleteEvent() }
    }

    private suspend fun deleteEvent() {
        if (!window.confirm("Are you sure?"))
            return
        clearTimeout()
        api.New(ApiDeleteEvent(event.firstFileName))
        btn.remove()
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
        btn.classList.add("selected_btn")
        btn.blur()
    }

}