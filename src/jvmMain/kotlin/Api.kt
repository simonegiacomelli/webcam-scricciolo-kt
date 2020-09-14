actual class Api3

actual suspend fun Api3.summary(): List<ApiDay> {
    return webcam.summary()
}

actual suspend fun Api3.eventFileList(firstFileName: String): List<String> {
    return webcam.eventFileList(firstFileName)
}

actual suspend fun Api3.deleteEvent(firstFileName: String) {
    webcam.deleteEvent(firstFileName)
}
