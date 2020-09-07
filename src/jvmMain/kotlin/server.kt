import framework.ApiServer
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import io.netty.handler.codec.DefaultHeaders
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.util.*


var imageDirectory = "./src/jvmTest/resources/test_files/flat_files"
var workingDirectory = "build/distributions"

fun log(line: String) {
    val m = Clock.System.now().toLocalDateTime(TimeZone.of("Europe/Rome"))
    println("$m $line")
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
        install(io.ktor.features.DefaultHeaders) {
            header("WWW-Authenticate", "Basic")
        }
        routing {
            get("/") {
                call.respondRedirect("/index.html", permanent = false)
            }
            get("/index.html") {
                if (!authorized(this)) {
                    call.respond(HttpStatusCode.Unauthorized, "not authorized")
                    return@get
                }
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

val authFile = File(".auth.txt")
fun authorized(ctx: PipelineContext<Unit, ApplicationCall>): Boolean {
    if (!authFile.exists()) return true
    val authRaw = ctx.context.request.header("Authorization").orEmpty().split(" ")
    if (authRaw.size != 2 || authRaw[0] != "Basic")
        return false
    val authDecoded = String(Base64.getDecoder().decode(authRaw[1]))
    return authDecoded == authFile.readText()

}

private fun ApiServer.registerApi() {
    register<SummaryRequest, SummaryResponse> { SummaryResponse(webcam.summary()) }
    register<EventRequest, ApiEventSummary> { webcam.eventSummary(it.firstFileName) }
    register<ApiDeleteEvent, ApiDeleteResponse> { webcam.deleteEvent(it.firstFileName); ApiDeleteResponse() }
}
