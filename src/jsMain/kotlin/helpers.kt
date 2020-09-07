import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
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


fun br() = document.createElement("br")
fun div() = document.createElement("div") as HTMLDivElement
fun div(elementId: String) = document.getElementById(elementId) as HTMLDivElement
fun button(elementId: String) = document.getElementById(elementId) as HTMLButtonElement
fun img(elementId: String) = document.getElementById(elementId) as HTMLImageElement
fun button() = document.createElement("button") as HTMLButtonElement

class LazyJs<T : Any>(private val inner: suspend () -> T) {
    private var value: T? = null
    suspend fun get(): T {
        if (value == null)
            value = inner()
        return this.value!!

    }
}