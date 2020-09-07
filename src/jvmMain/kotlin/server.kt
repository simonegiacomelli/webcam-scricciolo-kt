import framework.ApiServer
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*
import kotlinx.serialization.InternalSerializationApi

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

@InternalSerializationApi
fun main() {
    val apiServer = ApiServer()
    apiServer.register<LoginRequest, LoginResponse> {
        LoginResponse(true, "hello ${it.username}")
    }

    val port = 8071
    val host = "0.0.0.0"
    println("Starting server on  $host:$port")
    embeddedServer(Netty, port = port, host = host) {
        routing {
            get("/") {
                println("ciao")
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            get("/api/{class_name}") {
                val className = call.parameters["class_name"]!!
                val serializedRequest = call.request.queryParameters[apiArgumentKeyName]!!
                val serializedResponse = apiServer.invoke(className, serializedRequest)
                call.respondText(serializedResponse, ContentType.Text.Plain)
            }
            static("/static") {
                files("build/distributions")
                resources()
            }
        }
    }.start(wait = true)
}