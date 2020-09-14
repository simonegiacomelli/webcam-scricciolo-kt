import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
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
    val api1 = Api()
    api1.registerApi()

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
//            val path = "/api1/{api_name}"
            val path = apiBaseUrl("{api_name}")
            get(path) {
                try {
                    val apiName = call.parameters["api_name"]!!
                    log("api1 $apiName")
                    val serializedResponse = api1.serverDispatch(apiName, call.request.queryParameters[apiArgumentKeyName]!!)
                    call.respondText("success=1\n\n$serializedResponse", ContentType.Text.Plain)
                } catch (ex: Exception) {
                    val text = "success=0\n\n${ex.stackTraceToString()}"
                    println("handling exception [[$text]] ")
                    call.respondText(
                        text = text,
                        status = HttpStatusCode.InternalServerError,
                        contentType = ContentType.Text.Plain
                    )
                }
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
