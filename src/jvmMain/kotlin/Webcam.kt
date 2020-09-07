import java.io.File

data class Group(val name: String)
class Webcam(val pathname: File) {
    constructor(pathname: String) : this(File(pathname))

    fun listGroups(): List<Group> {

        val f = pathname.list().filterNotNull().sorted().map { Group(it) }
        return f
    }
}