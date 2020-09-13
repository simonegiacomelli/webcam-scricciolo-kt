import framework.BasicRpc
import kotlinx.serialization.Serializable

const val apiArgumentKeyName = "serialized_request"
fun apiBaseUrl(apiName: String) = "/api/$apiName"


@Serializable
data class ApiEvent(val name: String, val time: String, val firstFileName: String, val dayFolder: String)

@Serializable
data class ApiDay(val name: String, val events: List<ApiEvent>)

expect class Api1 : BasicRpc

expect suspend fun Api1.divideByTwo(firstParam: Int): Double
expect suspend fun Api1.divide(dividend: Double, divisor: Double): Double
expect suspend fun Api1.sumNumbers(a: Int, b: Int): Int
expect suspend fun Api1.externalEcho(s: String): String

expect suspend fun Api1.summary(): List<ApiDay>
expect suspend fun Api1.eventFileList(firstFileName: String) :List<String>
expect suspend fun Api1.deleteEvent(firstFileName: String) :Boolean

