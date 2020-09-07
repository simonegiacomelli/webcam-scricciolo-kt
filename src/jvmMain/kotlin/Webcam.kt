import java.io.File

data class Event(val root: File) {
    val firstFile: File = root.listFiles().filterNotNull().minByOrNull { it.name }!!
    val name: String = root.name
    val time: String = firstFile.name.split("-")[1].substring(8).chunked(2).joinToString(":")
}

data class Day(val root: File) {
    val events = list(root).map { Event(it) }
    val name: String
        get() = root.name.let {
            println(it)
            it.substring(0, 4) + "-" + it.substring(4, 6) + "-" + it.substring(6, 8)
        }.also {
            println(it)
        }
}

data class ApiEvent(val name: String, val time: String)
data class ApiDay(val name: String, val events: List<ApiEvent>)
class Webcam(private val root: File) {
    fun summary(): List<ApiDay> {
        return days.map { day ->
            ApiDay(day.name, day.events.map { event ->
                ApiEvent(event.name, event.time)
            })
        }
    }

    constructor(pathname: String) : this(File(pathname))

    val days = list(root).map { Day(it) }

}

private fun list(file: File) = file.listFiles().filterNotNull().sorted()
