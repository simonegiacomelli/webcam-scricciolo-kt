import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

internal class WebcamTest {

    @Test
    fun listGroups() {
        val pathname = "./src/jvmTest/resources/test_files/flat_files"
        val target = Webcam(pathname)
        assertEquals(listOf("20200825", "20200827", "20200830"), target.listGroups().map { it.name })
    }
}