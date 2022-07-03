package opensavvy.backbone

import kotlin.test.Test
import kotlin.test.assertEquals

class DataTest {

	@Test
	fun percent() {
		val loadingNoInfo = Data.Status.Loading.Basic()
		assertEquals(null, loadingNoInfo.progression)
		assertEquals(null, loadingNoInfo.percent)
		assertEquals("Loading.Basic", loadingNoInfo.toString())

		val loadingStart = Data.Status.Loading.Basic(0f)
		assertEquals(0f, loadingStart.progression)
		assertEquals(0, loadingStart.percent)

		val loadingThird = Data.Status.Loading.Basic(0.33f)
		assertEquals(0.33f, loadingThird.progression)
		assertEquals(33, loadingThird.percent)
		assertEquals("Loading.Basic(progression = 0.33)", loadingThird.toString())

		val loadingDone = Data.Status.Loading.Basic(1.0f)
		assertEquals(1.0f, loadingDone.progression)
		assertEquals(100, loadingDone.percent)
	}
}
