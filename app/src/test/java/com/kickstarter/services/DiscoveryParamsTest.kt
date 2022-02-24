package com.kickstarter.services

import android.net.Uri
import com.kickstarter.services.DiscoveryParams.Companion.builder
import com.kickstarter.services.DiscoveryParams.Companion.fromUri
import com.kickstarter.mock.factories.CategoryFactory.bluesCategory
import com.kickstarter.mock.factories.CategoryFactory.gamesCategory
import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class DiscoveryParamsTest : KSRobolectricTestCase() {
    @Test
    fun testFromUri_discoverRoot() {
        val uri = Uri.parse("https://www.kickstarter.com/discover")
        assertEquals(builder().build(), fromUri(uri))
    }

    @Test
    fun testFromUri_backed() {
        val uri = Uri.parse("https://www.kickstarter.com/discover/advanced?backed=1")
        assertEquals(builder().backed(1).build(), fromUri(uri))
    }

    @Test
    fun testFromUri_categories() {
        val params = builder().categoryParam("music").build()
        val categoryUri = Uri.parse("https://www.kickstarter.com/discover/categories/music")
        assertEquals(params, fromUri(categoryUri))
        val advancedUri =
            Uri.parse("https://www.kickstarter.com/discover/advanced?category_id=music")
        assertEquals(params, fromUri(advancedUri))
    }

    @Test
    fun testFromUri_filters() {
        val allParams = builder()
            .recommended(true)
            .social(1)
            .staffPicks(true)
            .starred(1)
            .build()
        val allParamsUri = Uri.parse(
            "https://www.kickstarter.com/discover/advanced?recommended=true&social=1&staff_picks=true&starred=1"
        )
        assertEquals(allParams, fromUri(allParamsUri))
        val recommendedParams = builder().recommended(true).build()
        val recommendedUri =
            Uri.parse("https://www.kickstarter.com/discover/advanced?recommended=true")
        assertEquals(recommendedParams, fromUri(recommendedUri))
        val socialParams = builder().social(1).build()
        val socialUri = Uri.parse("https://www.kickstarter.com/discover/advanced?social=1")
        assertEquals(socialParams, fromUri(socialUri))
        val staffPickParams = builder().staffPicks(true).build()
        val staffPicksUri =
            Uri.parse("https://www.kickstarter.com/discover/advanced?staff_picks=true")
        assertEquals(staffPickParams, fromUri(staffPicksUri))
        val starredParams = builder().starred(1).build()
        val starredUri = Uri.parse("https://www.kickstarter.com/discover/advanced?starred=1")
        assertEquals(starredParams, fromUri(starredUri))
    }

    @Test
    fun testFromUri_locations() {
        val params = builder().locationParam("sydney-au").build()
        val placesUri = Uri.parse("https://www.kickstarter.com/discover/places/sydney-au")
        assertEquals(params, fromUri(placesUri))
        val advancedUri =
            Uri.parse("https://www.kickstarter.com/discover/advanced?woe_id=sydney-au")
        assertEquals(params, fromUri(advancedUri))
    }

    @Test
    fun testFromUri_collections() {
        val staffPicksParams = builder().staffPicks(true).build()
        val staffPicksUri = Uri.parse("https://www.kickstarter.com/discover/advanced?staff_picks=1")
        assertEquals(staffPicksParams, fromUri(staffPicksUri))
        val recommendedParams = builder().recommended(true).build()
        val recommendedUri =
            Uri.parse("https://www.kickstarter.com/discover/advanced?recommended=1")
        assertEquals(recommendedParams, fromUri(recommendedUri))
        val starredParams = builder().starred(1).build()
        val starredUri = Uri.parse("https://www.kickstarter.com/discover/advanced?starred=1")
        assertEquals(starredParams, fromUri(starredUri))
        val socialParams = builder().social(1).build()
        val socialUri = Uri.parse("https://www.kickstarter.com/discover/advanced?social=1")
        assertEquals(socialParams, fromUri(socialUri))
    }

    @Test
    fun testFromUri_customScopes() {
        val endingSoonParams = builder().sort(DiscoveryParams.Sort.ENDING_SOON).build()
        val endingSoonUri = Uri.parse("https://www.kickstarter.com/discover/ending-soon")
        assertEquals(endingSoonParams, fromUri(endingSoonUri))
        val newestParams = builder().sort(DiscoveryParams.Sort.NEWEST).staffPicks(true).build()
        val newestUri = Uri.parse("https://www.kickstarter.com/discover/newest")
        assertEquals(newestParams, fromUri(newestUri))
        val popularParams = builder().sort(DiscoveryParams.Sort.POPULAR).build()
        val popularUri = Uri.parse("https://www.kickstarter.com/discover/popular")
        assertEquals(popularParams, fromUri(popularUri))
        val recentlyLaunchedParams = builder().sort(DiscoveryParams.Sort.NEWEST).build()
        val recentlyLaunchedUri =
            Uri.parse("https://www.kickstarter.com/discover/recently-launched")
        assertEquals(recentlyLaunchedParams, fromUri(recentlyLaunchedUri))
        val smallProjectsParams = builder().pledged(0).build()
        val smallProjectsUri = Uri.parse("https://www.kickstarter.com/discover/small-projects")
        assertEquals(smallProjectsParams, fromUri(smallProjectsUri))
        val socialParams = builder().social(0).build()
        val socialUri = Uri.parse("https://www.kickstarter.com/discover/social")
        assertEquals(socialParams, fromUri(socialUri))
        val successfulParams =
            builder().sort(DiscoveryParams.Sort.ENDING_SOON).state(DiscoveryParams.State.SUCCESSFUL)
                .build()
        val successfulUri = Uri.parse("https://www.kickstarter.com/discover/successful")
        assertEquals(successfulParams, fromUri(successfulUri))
    }

    @Test
    fun testFromUri_pagination() {
        val params = builder().page(5).perPage(21).build()
        val uri = Uri.parse("https://www.kickstarter.com/discover/advanced?page=5&per_page=21")
        assertEquals(params, fromUri(uri))
    }

    @Test
    fun testFromUri_sort() {
        val params = builder().sort(DiscoveryParams.Sort.POPULAR).build()
        val uri = Uri.parse("https://www.kickstarter.com/discover/advanced?sort=popularity")
        assertEquals(params, fromUri(uri))
    }

    @Test
    fun testFromUri_term() {
        val params = builder().term("skull graphic tee").build()
        val advancedUri =
            Uri.parse("https://www.kickstarter.com/discover/advanced?term=skull+graphic+tee")
        assertEquals(params, fromUri(advancedUri))
        val searchUri =
            Uri.parse("https://www.kickstarter.com/projects/search?term=skull+graphic+tee")
        assertEquals(params, fromUri(searchUri))
    }

    @Test
    fun testShouldIncludeFeatured() {
        val nonRootCategory = bluesCategory()
        val nonRootParams = builder().category(nonRootCategory).build()
        assertEquals(false, nonRootParams.shouldIncludeFeatured())
        val rootCategory = gamesCategory()
        val rootParams = builder().category(rootCategory).build()
        assertEquals(true, rootParams.shouldIncludeFeatured())
    }
}