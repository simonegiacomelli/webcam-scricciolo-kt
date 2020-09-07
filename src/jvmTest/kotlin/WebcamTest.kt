import java.io.File
import kotlin.test.*

internal class WebcamTest {
    val pathname = "./src/jvmTest/resources/test_files/flat_files"

    object Temp {
        private val directories = mutableListOf<File>()
        fun dir(skip: Boolean = false): File {
            val res = createTempDir()
            if (!skip) directories.add(res)
            return res
        }

        fun clean() {
            directories.forEach {
                it.deleteRecursively()
            }
        }
    }

    @AfterTest
    fun afterTest() {
        Temp.clean()
    }

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

    @Test
    fun api_summary() {
        val target = Webcam(pathname)
        val summary = target.summary()
        assertEquals("2020-08-25", summary.first().name)
        assertEquals("05:16:36", summary.last().events.last().time)
        assertEquals("20200830", summary.last().events.last().dayFolder)
    }

    @Test
    fun api_eventSummary() {
        val target = Webcam(pathname)
        val expected = listOf(
            "CAM1_805-20200830051636-01.jpg",
            "CAM1_805-20200830051637-00.jpg",
            "CAM1_805-20200830051637-01.jpg",
            "CAM1_805-20200830051638-00.jpg"
        )
        assertEquals(expected, target.eventSummary("CAM1_805-20200830051636-01.jpg").files)
    }

    @Test
    fun api_deleteEvent() {
        val root = Temp.dir()
//        println("file://$root")
        File(pathname).copyRecursively(root)
        val folder = root.resolve("20200830/804")
        assertTrue(folder.exists())
        val target = Webcam(root)
        target.deleteEvent("CAM1_804-20200830051609-01.jpg")
        assertFalse(folder.exists())
        val day = target.summary().first { it.name == "2020-08-30" }
        assertEquals(0, day.events.count { it.name == "804" })
    }
}