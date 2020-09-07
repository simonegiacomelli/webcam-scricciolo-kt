import framework.HasResponse
import kotlinx.serialization.Serializable

val apiArgumentKeyName = "serialized_request"
fun apiBaseUrl(apiName: String) = "/api/$apiName"

@Serializable
data class LoginRequest(val username: String, val password: String) : HasResponse<LoginResponse>

@Serializable
data class LoginResponse(val success: Boolean, val message: String)
