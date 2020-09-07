import framework.ApiServer
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.timer
import kotlin.concurrent.withLock


var imageDirectory = "./src/jvmTest/resources/test_files/flat_files"
var workingDirectory = "build/distributions"

fun log(line: String) {
    val m = Clock.System.now().toLocalDateTime(TimeZone.of("Europe/Rome"))
    println("$m $line")
}

object WebcamProvider {
    private var w: Webcam? = null
    val lock: Lock = ReentrantLock()
    val value: Webcam
        get() {
            return lock.withLock {
                if (w == null)
                    w = newWebcam()
                w!!
            }
        }

    private fun newWebcam(): Webcam {
        val start = System.currentTimeMillis()
        log("Loading $imageDirectory")
        val webcam1 = Webcam(imageDirectory)
        val ela = System.currentTimeMillis() - start
        log("newWebcam elapsed $ela")
        return webcam1
    }

    fun refreshOnceInAWhile() {
        timer("WebcamProvider", daemon = true, period = 1000 * 60) {
            val v = newWebcam()
            lock.withLock { w = v }
        }
    }
}

val webcam: Webcam get() = WebcamProvider.value

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        imageDirectory = args[0]
        log("Using imageDirectory=$imageDirectory")
        workingDirectory = "."
    }

    WebcamProvider.refreshOnceInAWhile()

    val apiServer = ApiServer()
    apiServer.registerApi()

    val port = 8090
    val host = "0.0.0.0"
    println("Starting server v1.3 on  $host:$port")

    embeddedServer(Netty, port = port, host = host) {
        routing {
            get("/") {
                call.respondRedirect("/index.html", permanent = false)
            }
            get("/index.html") {
//                call.respondFile(File(folder).resolve("wwwroot").resolve("index.html"))
                val file = File(workingDirectory, "index.html")
                println("file exists ${file.exists()}")
                if (file.exists())
                    call.respondFile(file)
                else {
                    val content = call.resolveResource("index.html")
                    if (content != null)
                        call.respond(content)
                    else
                        log("Content is null")
                }

            }
            get("/api/{class_name}") {
                val className = call.parameters["class_name"]!!
                log("api $className")
                val serializedRequest = call.request.queryParameters[apiArgumentKeyName]!!
                val serializedResponse = apiServer.invoke(className, serializedRequest)
                call.respondText(serializedResponse, ContentType.Text.Plain)
            }
            get("/image") {
                val f = call.request.queryParameters["full_filename"]!!
                println("full_name=$f")
                call.respondFile(File(imageDirectory, f))
            }
            static("/static") {
                files(workingDirectory)
                resources()
            }
        }
    }.start(wait = true)
}

private fun ApiServer.registerApi() {
    register<SummaryRequest, SummaryResponse> { SummaryResponse(webcam.summary()) }
    register<EventRequest, ApiEventSummary> { webcam.eventSummary(it.firstFileName) }
}
