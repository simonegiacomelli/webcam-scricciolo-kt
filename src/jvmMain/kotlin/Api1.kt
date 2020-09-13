import framework.BasicRpc

actual class Api1 : BasicRpc() {


    suspend fun dispatch(apiName: String, serialized_request: String): String {
        println("dispatch $apiName ser=$serialized_request")
        val suspendFunction1 = handlers[apiName]
        if (suspendFunction1 == null) {
            val message = "Did you register [$apiName] api?"
            println(message)
            throw Exception(message)
        }
        println("susp=$suspendFunction1")
        return suspendFunction1!!(serialized_request)
    }

    fun registerApi() {
        println("registerApi")
        registerServerHandler(::divideByTwo)
        registerServerHandler(::sumNumbers)
        registerServerHandler(::externalEcho)
        registerServerHandler(::divide)
        registerServerHandler(::summary)
        registerServerHandler(::eventFileList)
        registerServerHandler(::deleteEvent)
    }


}


actual suspend fun Api1.divideByTwo(firstParam: Int): Double {
    return firstParam.toDouble() / 2
}

actual suspend fun Api1.sumNumbers(a: Int, b: Int): Int {
    return a + b
}

actual suspend fun Api1.externalEcho(s: String): String {
    return "echo for [$s]"
}

actual suspend fun Api1.divide(dividend: Double, divisor: Double): Double {
    return dividend / divisor
}

actual suspend fun Api1.summary(): List<ApiDay> {
    return webcam.summary()
}

actual suspend fun Api1.eventFileList(firstFileName: String): List<String> {
    return webcam.eventFileList(firstFileName)
}

actual suspend fun Api1.deleteEvent(firstFileName: String): Boolean {
    webcam.deleteEvent(firstFileName)
    return true
}