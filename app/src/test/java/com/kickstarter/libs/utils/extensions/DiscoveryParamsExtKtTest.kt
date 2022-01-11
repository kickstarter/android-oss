package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RefTag
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import org.junit.Test

class DiscoveryParamsExtKtTest : KSRobolectricTestCase() {

    private val categories = listOf(
        CategoryFactory.artCategory(),
        CategoryFactory.ceramicsCategory(),
        CategoryFactory.textilesCategory(),
        CategoryFactory.photographyCategory(),
        CategoryFactory.musicCategory(),
        CategoryFactory.bluesCategory(),
        CategoryFactory.worldMusicCategory()
    )

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

    @Test
    fun testDeriveNavigationDrawerData_LoggedOut_DefaultSelected() {
        val data: NavigationDrawerData = DiscoveryParams.builder().build().deriveNavigationDrawerData(
            categories,
            null,
            null
        )
        assertEquals(5, data.sections().size)
        assertEquals(1, data.sections()[0].rows().size)
        assertEquals(1, data.sections()[1].rows().size)
        assertEquals(1, data.sections()[2].rows().size)
        assertEquals(1, data.sections()[3].rows().size)
        assertEquals(1, data.sections()[4].rows().size)
    }

    @Test
    fun testDeriveNavigationDrawerData_LoggedIn_DefaultSelected() {
        val data: NavigationDrawerData = DiscoveryParams.builder().build().deriveNavigationDrawerData(
            categories,
            null,
            UserFactory.user()
        )
        assertEquals(7, data.sections().size)
        assertEquals(1, data.sections()[0].rows().size)
        assertEquals(1, data.sections()[1].rows().size)
        assertEquals(1, data.sections()[2].rows().size)
        assertEquals(1, data.sections()[3].rows().size)
        assertEquals(1, data.sections()[4].rows().size)
        assertEquals(1, data.sections()[5].rows().size)
        assertEquals(1, data.sections()[6].rows().size)
    }

    @Test
    fun testDeriveNavigationDrawerData_LoggedIn_NoRecommendations_DefaultSelected() {
        val data: NavigationDrawerData = DiscoveryParams.builder().build().deriveNavigationDrawerData(
            categories,
            null,
            UserFactory.noRecommendations()
        )
        assertEquals(6, data.sections().size)
        assertEquals(1, data.sections()[0].rows().size)
        assertEquals(1, data.sections()[1].rows().size)
        assertEquals(1, data.sections()[2].rows().size)
        assertEquals(1, data.sections()[3].rows().size)
        assertEquals(1, data.sections()[4].rows().size)
        assertEquals(1, data.sections()[5].rows().size)
    }

    @Test
    fun testDeriveNavigationDrawerData_LoggedIn_Social_DefaultSelected() {
        val data: NavigationDrawerData = DiscoveryParams.builder().build().deriveNavigationDrawerData(
            categories,
            null,
            UserFactory.socialUser()
        )
        assertEquals(8, data.sections().size)
        assertEquals(1, data.sections()[0].rows().size)
        assertEquals(1, data.sections()[1].rows().size)
        assertEquals(1, data.sections()[2].rows().size)
        assertEquals(1, data.sections()[3].rows().size)
        assertEquals(1, data.sections()[4].rows().size)
        assertEquals(1, data.sections()[5].rows().size)
        assertEquals(1, data.sections()[6].rows().size)
        assertEquals(1, data.sections()[7].rows().size)
    }

    @Test
    fun testDeriveNavigationDrawerData_LoggedOut_ArtExpanded() {
        val data: NavigationDrawerData = DiscoveryParams.builder().build().deriveNavigationDrawerData(
            categories,
            CategoryFactory.artCategory(),
            null
        )
        assertEquals(5, data.sections().size)
        assertEquals(1, data.sections()[0].rows().size)
        assertEquals(1, data.sections()[1].rows().size)
        assertEquals(4, data.sections()[2].rows().size)
        assertEquals(1, data.sections()[3].rows().size)
        assertEquals(1, data.sections()[4].rows().size)
    }

    @Test
    fun testDiscoveryParamsPositionFromSort() {
        assertEquals(0, DiscoveryParams.Sort.MAGIC.positionFromSort())
        assertEquals(0, null.positionFromSort())
        assertEquals(1, DiscoveryParams.Sort.POPULAR.positionFromSort())
        assertEquals(2, DiscoveryParams.Sort.NEWEST.positionFromSort())
        assertEquals(3, DiscoveryParams.Sort.ENDING_SOON.positionFromSort())
    }
}
