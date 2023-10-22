package opensavvy.progress

import kotlin.test.Test
import kotlin.test.assertEquals

class UnquantifiedLoadingTest {

    @Test
    fun string() {
        assertEquals("Loading", loading().toString())
    }

}
