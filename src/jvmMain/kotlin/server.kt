import framework.ApiServer
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File


val pathname = "./src/jvmTest/resources/test_files/flat_files"
val webcam by lazy { Webcam(pathname) }
fun main() {

    val apiServer = ApiServer()
    apiServer.registerApi()

    val port = 8071
    val host = "0.0.0.0"
    println("Starting server on  $host:$port")
    val folder = "build/distributions"
    embeddedServer(Netty, port = port, host = host) {
        routing {
            get("/") {
                println("ciao")
                call.respondRedirect("/index.html", permanent = false)
            }
            get("/index.html") {
                call.respondFile(File(folder).resolve("wwwroot").resolve("index.html"))
            }
            get("/api/{class_name}") {
                val className = call.parameters["class_name"]!!
                val serializedRequest = call.request.queryParameters[apiArgumentKeyName]!!
                val serializedResponse = apiServer.invoke(className, serializedRequest)
                call.respondText(serializedResponse, ContentType.Text.Plain)
            }
            static("/static") {
                files(folder)
                resources()
            }
        }
    }.start(wait = true)
}

private fun ApiServer.registerApi() {
    register<SummaryRequest, SummaryResponse> { SummaryResponse(webcam.summary()) }
    register<EventRequest, ApiEventSummary> { webcam.eventSummary(it.firstFileName) }
}
