import framework.ServerRpc
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

const val apiArgumentKeyName = "serialized_request"
fun apiBaseUrl(apiName: String) = "/api/$apiName"


@Serializable
data class ApiEvent(
    val name: String,
    val time: String,
    val firstFileName: String,
    val dayFolder: String,
    val firstInstant: String,
    val lastInstant: String
)

@Serializable
data class ApiDay(val name: String, val events: List<ApiEvent>)

expect class Api3

expect suspend fun Api3.summary(): List<ApiDay>
expect suspend fun Api3.eventFileList(firstFileName: String): List<String>
expect suspend fun Api3.deleteEvent(firstFileName: String)

fun ServerRpc<Api3>.serverRegisterApi() {
    registerServerHandler(Api3::summary)
    registerServerHandler(Api3::eventFileList)
    registerServerHandler(Api3::deleteEvent)
}
