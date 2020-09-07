import framework.HasResponse
import kotlinx.serialization.Serializable

const val apiArgumentKeyName = "serialized_request"
fun apiBaseUrl(apiName: String) = "/api/$apiName"

@Serializable
data class LoginRequest(val username: String, val password: String) : HasResponse<LoginResponse>

@Serializable
data class LoginResponse(val success: Boolean, val message: String)


@Serializable
data class SummaryRequest(val ignore: String = "") : HasResponse<SummaryResponse>


@Serializable
data class SummaryResponse(val payload: List<ApiDay>) : HasResponse<SummaryResponse>

@Serializable
data class ApiEvent(val name: String, val time: String)

@Serializable
data class ApiDay(val name: String, val events: List<ApiEvent>)

