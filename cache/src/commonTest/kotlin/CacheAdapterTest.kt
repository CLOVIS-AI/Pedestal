package opensavvy.cache

import opensavvy.cache.properties.readingValues
import opensavvy.prepared.runner.kotest.PreparedSpec

class CacheAdapterTest : PreparedSpec({

	readingValues { it }

})
