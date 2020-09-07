import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.fail

internal class WebcamTest {
    val pathname = "./src/jvmTest/resources/test_files/flat_files"

    @Test
    fun listDays() {
        val target = Webcam(pathname)
        assertEquals(listOf("2020-08-25", "2020-08-27", "2020-08-30"), target.days.map { it.name })
    }

    @Test
    fun listDay_events() {
        val target = Webcam(pathname)
        val day = target.days.first { it.name == "2020-08-30" }
        assertEquals(listOf("804", "805"), day.events.map { it.name })
    }

    @Test
    fun listDay_days_twiceCalled_shouldBeSameReference() {
        val target = Webcam(pathname)
        assertSame(target.days, target.days)
    }

    @Test
    fun listDay_events_twiceCalled_shouldBeSameReference() {
        val target = Webcam(pathname)
        assertSame(target.days.first().events, target.days.first().events)
    }

    @Test
    fun list_event_time() {
        val target = Webcam(pathname)
        val day = target.days.first { it.name == "2020-08-30" }
        val event = day.events.associateBy { it.name }
        val e804 = event["804"] ?: fail("Should exist")
        assertEquals("CAM1_804-20200830051609-01.jpg", e804.firstFile.name)
        assertEquals("05:16:09", e804.time)
        val e805 = event["805"] ?: fail("Should exist")
        assertEquals("CAM1_805-20200830051636-01.jpg", e805.firstFile.name)
        assertEquals("05:16:36", e805.time)
    }

}