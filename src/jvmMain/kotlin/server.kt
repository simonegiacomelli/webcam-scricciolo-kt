import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*

fun HTML.index() {
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            +"Hello from Ktor"
        }
        div {
            id = "root"
        }
        script(src = "/static/output.js") {}
    }
}

fun main() {
    embeddedServer(Netty, port = 8071, host = "0.0.0.0") {
        routing {
            get("/") {
                println("ciao")
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            static("/static") {
                files("build/distributions")
                resources()
            }
        }
    }.start(wait = true)
}