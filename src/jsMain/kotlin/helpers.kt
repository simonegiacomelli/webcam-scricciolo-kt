import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement

var HTMLButtonElement.onclickExt: suspend () -> Unit
    get() = throw Error()
    set(value) {
        onclick = {
            GlobalScope.async { value() }
        }
    }


fun div() = document.createElement("div") as HTMLDivElement
fun div(elementId: String) = document.getElementById(elementId) as HTMLDivElement
fun img(elementId: String) = document.getElementById(elementId) as HTMLImageElement
fun button() = document.createElement("button") as HTMLButtonElement
