import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant


class ImageName(private val name: String) {
    val instant: Instant by lazy {

        //"CAM1_804-20200830051609-01.jpg"
        //"CAM1_804-20200830051609-01m.jpg"

        val parts = name.replace("m.", "-").replace(".", "-").split("-")
        val nsec = parts[2].toInt()
        val central = parts[1]
        val year = central.substring(0, 4).toInt()
        val ce = central.substring(4).chunked(2).map { it.toInt() }
        val x = listOf(year) + ce + listOf(nsec)
        val r = LocalDateTime(x[0], x[1], x[2], x[3], x[4], x[5], x[6])
        r.toInstant(tzApp)

    }
}