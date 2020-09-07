import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement

var HTMLButtonElement.onclickExt: suspend () -> Unit
    get() = throw Error()
    set(value) {
        onclick = {
            GlobalScope.async { value() }
        }
    }


fun div() = document.createElement("div") as HTMLDivElement
fun button() = document.createElement("button") as HTMLButtonElement
