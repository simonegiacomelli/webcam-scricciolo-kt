import framework.BasicRpc

actual class Api : BasicRpc()

actual suspend fun Api.summary(): List<ApiDay> {
    return webcam.summary()
}

actual suspend fun Api.eventFileList(firstFileName: String): List<String> {
    return webcam.eventFileList(firstFileName)
}

actual suspend fun Api.deleteEvent(firstFileName: String) {
    webcam.deleteEvent(firstFileName)
}
