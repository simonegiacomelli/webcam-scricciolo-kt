import java.io.File

data class Event(val root: File) {
    val firstFile: File = root.listFiles().filterNotNull().minByOrNull { it.name }!!
    val name: String = root.name
    val time: String = firstFile.name.split("-")[1].substring(8).chunked(2).joinToString(":")
}

data class Day(val root: File) {
    val events = list(root).map { Event(it) }
    val name: String get() = root.name
}

class Webcam(private val root: File) {
    constructor(pathname: String) : this(File(pathname))

    val days = list(root).map { Day(it) }

}

private fun list(file: File) = file.listFiles().filterNotNull().sorted()
