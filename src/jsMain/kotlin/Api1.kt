import framework.BasicRpc
import kotlinx.browser.window

import kotlinx.coroutines.await

actual class Api1 : BasicRpc() {
    override suspend fun doRequest(apiName: String, serializedArguments: String): String {

        //val url = "/api1/$apiName"
        val url = apiBaseUrl(apiName)
        console.log("sending fetch to $url")
        val resp = window.fetch("$url?$apiArgumentKeyName=$serializedArguments").await()
        val response = resp.text().await().split("\n\n", limit = 2)
        if (response[0] == "success=1")
            return response[1]
        throw Exception(response[1])
    }

}

actual suspend fun Api1.divideByTwo(firstParam: Int): Double {
    return clientInvoke(::divideByTwo, firstParam)
}

actual suspend fun Api1.sumNumbers(a: Int, b: Int): Int {
    return clientInvoke(::sumNumbers, a, b)
}

actual suspend fun Api1.externalEcho(s: String): String {
    return clientInvoke(::externalEcho, s)
}

actual suspend fun Api1.divide(dividend: Double, divisor: Double): Double {
    return clientInvoke(::divide, dividend, divisor)
}

actual suspend fun Api1.summary(): List<ApiDay> {
    return clientInvoke(::summary)
}

actual suspend fun Api1.eventFileList(firstFileName: String): List<String> {
    return clientInvoke(::eventFileList, firstFileName)
}

actual suspend fun Api1.deleteEvent(firstFileName: String): Boolean {
    return clientInvoke(::deleteEvent, firstFileName)
}