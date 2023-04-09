package opensavvy.progress

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SimpleLoadingImplementationTest {

    @Test
    fun normalized0() {
        assertEquals(0.0, loading(0.0).normalized)
    }

    @Test
    fun normalizedThird() {
        assertEquals(0.33, loading(0.33).normalized)
    }

    @Test
    fun normalized1() {
        assertEquals(1.0, loading(1.0).normalized)
    }

    @Test
    fun normalizedIllegalValues() {
        assertFails { loading(-1.0) }
        assertFails { loading(1.01) }
        assertFails { loading(1.0000001) }
        assertFails { loading(-0.000001) }
        assertFails { loading(Double.MAX_VALUE) }
        assertFails { loading(Double.NEGATIVE_INFINITY) }
        assertFails { loading(Double.POSITIVE_INFINITY) }
    }

    @Test
    fun percent0() {
        assertEquals(0, loading(0.0).percent)
    }

    @Test
    fun percentThird() {
        assertEquals(33, loading(0.33).percent)
    }

    @Test
    fun percent100() {
        assertEquals(100, loading(1.0).percent)
    }

    @Test
    fun string() {
        assertEquals("Loading(0%)", loading(0.0).toString())
        assertEquals("Loading(20%)", loading(0.2).toString())
        assertEquals("Loading(99%)", loading(0.99).toString())
        assertEquals("Loading(100%)", loading(1.0).toString())
    }

    @Test
    fun hash() {
        val set = hashSetOf(
            done(),
            loading(0.0),
            loading(0.7),
            loading(0.9),
            loading(1.0),
        )

        assertContains(set, done())
        assertContains(set, loading(0.0))
        assertContains(set, loading(0.7))
        assertContains(set, loading(0.9))
        assertContains(set, loading(1.0))
    }
}
