package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.services.DiscoveryParams
import org.junit.Test

class DiscoveryParamsExtKtTest : KSRobolectricTestCase() {

    @Test
    fun testRefTag() {
        assertEquals(
            DiscoveryParams.builder().category(CategoryFactory.artCategory()).build().refTag(),
            RefTag.category()
        )
        assertEquals(
            RefTag.category(DiscoveryParams.Sort.POPULAR),
            DiscoveryParams.builder().category(CategoryFactory.artCategory())
                .sort(DiscoveryParams.Sort.POPULAR).build().refTag()
        )
        assertEquals(
            RefTag.city(),
            DiscoveryParams.builder().location(LocationFactory.germany()).build().refTag()
        )
        assertEquals(
            RefTag.recommended(),
            DiscoveryParams.builder().staffPicks(true).build().refTag()
        )
        assertEquals(
            RefTag.recommended(DiscoveryParams.Sort.POPULAR),
            DiscoveryParams.builder().staffPicks(true).sort(DiscoveryParams.Sort.POPULAR)
                .build().refTag()
        )
        assertEquals(
            RefTag.social(),
            DiscoveryParams.builder().social(1).build().refTag()
        )
        assertEquals(
            RefTag.search(),
            DiscoveryParams.builder().term("art").build().refTag()
        )
        assertEquals(
            RefTag.discovery(),
            DiscoveryParams.builder().build().refTag()
        )
    }
}
