import framework.BasicRpc
import kotlinx.browser.window

import kotlinx.coroutines.await

actual class Api3 : BasicRpc() {
    override suspend fun clientRequest(apiName: String, serializedArguments: String): String {

        //val url = "/api1/$apiName"
        val url = apiBaseUrl(apiName)
        console.log("sending fetch to $url")
        val resp = window.fetch("$url?$apiArgumentKeyName=$serializedArguments").await()
        val response = resp.text().await().split("\n\n", limit = 2)
        if (response[0] == "success=1")
            return response[1]
        throw Exception(response[1])
    }

    actual suspend fun summary(): List<ApiDay> {
        return clientInvoke(::summary)
    }
}


actual suspend fun Api3.eventFileList(firstFileName: String): List<String> {
    return clientInvoke(::eventFileList, firstFileName)
}

actual suspend fun Api3.deleteEvent(firstFileName: String) {
    return clientInvoke(::deleteEvent, firstFileName)
}