package opensavvy.progress

import kotlin.test.Test
import kotlin.test.assertEquals

class DoneTest {

    @Test
    fun string() {
        assertEquals("Done", done().toString())
    }
}
