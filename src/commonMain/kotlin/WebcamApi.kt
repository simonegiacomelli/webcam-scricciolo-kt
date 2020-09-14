import framework.BasicRpc
import kotlinx.serialization.Serializable

const val apiArgumentKeyName = "serialized_request"
fun apiBaseUrl(apiName: String) = "/api/$apiName"


@Serializable
data class ApiEvent(val name: String, val time: String, val firstFileName: String, val dayFolder: String)

@Serializable
data class ApiDay(val name: String, val events: List<ApiEvent>)

expect class Api : BasicRpc

expect suspend fun Api.summary(): List<ApiDay>
expect suspend fun Api.eventFileList(firstFileName: String): List<String>
expect suspend fun Api.deleteEvent(firstFileName: String): Boolean

fun Api.serverRegisterApi() {
    registerServerHandler(::summary)
    registerServerHandler(::eventFileList)
    registerServerHandler(::deleteEvent)
}
