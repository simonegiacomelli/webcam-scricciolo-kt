import java.io.File

data class Event(val root: File, val day: Day) {

    private val ff: File?
    private val lf: File?

    init {
        val l = root.listFiles().filterNotNull().sorted()
        ff = l.firstOrNull()
        lf = l.lastOrNull()
    }

    val firstFile: File get() = ff!!
    private val lastFile: File get() = lf!!
    val valid: Boolean get() = ff != null
    val name: String = root.name
    val time: String by lazy { firstFile.name.split("-")[1].substring(8).chunked(2).joinToString(":") }
    fun toApiEvent(day: Day): ApiEvent {
        return ApiEvent(
            name,
            time,
            firstFile.name,
            day.root.name,
            ImageName(firstFile.name).instant.toString(),
            ImageName(lastFile.name).instant.toString()
        )
    }
}

data class Day(val root: File) {
    val events = list(root).map { Event(it, this) }.filter { it.valid }.toMutableList()
    val name: String
        get() = root.name.let {
            it.substring(0, 4) + "-" + it.substring(4, 6) + "-" + it.substring(6, 8)
        }.also {
        }
}

class Webcam(private val root: File) {
    fun summary(): List<ApiDay> {
        return days.map { day ->
            ApiDay(day.name, day.events.map { event -> event.toApiEvent(day) })
        }
    }

    val fileMap by lazy { days.flatMap { it.events }.associateBy { it.firstFile.name } }

    fun eventFileList(filename: String): List<String> {
        val eventRoot = fileMap[filename] ?: error("event not found for file $filename")
        val list = eventRoot.root.list().filterNotNull().sorted()
        return list
    }

    fun deleteEvent(filename: String) {
        val event = fileMap[filename] ?: error("event not found for file $filename")
        event.root.deleteRecursively()
        val day = event.day
        day.events.remove(event)
        if (day.events.isEmpty()) {
            days.remove(day)
            if (day.root.list().isEmpty())
                day.root.delete()
        }
    }

    constructor(pathname: String) : this(File(pathname))

    val days = list(root).map { Day(it) }.toMutableList()

}

private fun list(file: File) = file.listFiles().filterNotNull().sorted()
