import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ImageNameTest {
    @Test
    fun instant() {
        val target = ImageName("CAM1_804-20200830051609-01.jpg")
        val expected = LocalDateTime(2020, 8, 30, 5, 16, 9, 1).toInstant(tzApp)
        assertEquals(expected, target.instant)
    }

    @Test
    fun instant_m() {
        val target = ImageName("CAM1_804-20200830051609-01m.jpg")
        val expected = LocalDateTime(2020, 8, 30, 5, 16, 9, 1).toInstant(tzApp)
        assertEquals(expected, target.instant)
    }
}