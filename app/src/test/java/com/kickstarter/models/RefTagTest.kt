package com.kickstarter.models

import com.kickstarter.libs.RefTag
import com.kickstarter.services.DiscoveryParams
import junit.framework.TestCase
import org.junit.Test

class RefTagTest : TestCase() {

    @Test
    fun testEquals_whenTagSame_shouldReturnTrue() {
        val reftag1 = RefTag.builder().build()
        val reftag2 = RefTag.builder().build()

        assertEquals(reftag1, reftag2)
    }

    @Test
    fun testEquals_whenTagDifferent_shouldReturnFalse() {
        val reftag1 = RefTag.builder().tag("aTag").build()
        val reftag2 = RefTag.builder().build()

        assertFalse(reftag1 == reftag2)
    }

    @Test
    fun testDefaultInit() {
        val reftag = RefTag.builder().tag("aTag").build()

        assertEquals(reftag.tag(), "aTag")
    }

    @Test
    fun testDefaultToBuilder() {
        val reftag = RefTag.builder().build().toBuilder().tag("thisIsATag").build()
        assertEquals(reftag.tag(), "thisIsATag")
    }

    @Test
    fun testFrom_shouldReturnReftag() {
        val reftag = RefTag.from("anotherTag")

        assertTrue(reftag is RefTag)
        assertEquals(reftag.tag(), "anotherTag")
    }

    @Test
    fun testActivity_returnsActivityRefTag() {
        val activityRefTag = RefTag.activity()

        assertTrue(activityRefTag is RefTag)
        assertEquals(activityRefTag.tag(), "activity")
    }

    @Test
    fun testActivitySample_returnsActivitySampleRefTag() {
        val activitySampleRefTag = RefTag.activitySample()

        assertTrue(activitySampleRefTag is RefTag)
        assertEquals(activitySampleRefTag.tag(), "discovery_activity_sample")
    }

    @Test
    fun testCategory_returnsCategoryRefTag() {
        val categoryRefTag = RefTag.category()

        assertTrue(categoryRefTag is RefTag)
        assertEquals(categoryRefTag.tag(), "category")
    }

    @Test
    fun testCategoryWithSort_returnsCategoryWithSortRefTag() {
        val categoryWithSortRefTag = RefTag.category(DiscoveryParams.Sort.POPULAR)

        assertTrue(categoryWithSortRefTag is RefTag)
        assertEquals(categoryWithSortRefTag.tag(), "category_popular")
    }

    @Test
    fun testCategoryFeatured_returnsCategroryFeaturedRefTag() {
        val categoryFeatured = RefTag.categoryFeatured()

        assertTrue(categoryFeatured is RefTag)
        assertEquals(categoryFeatured.tag(), "category_featured")
    }

    @Test
    fun testCity_returnsCityRefTag() {
        val cityRefTag = RefTag.city()

        assertTrue(cityRefTag is RefTag)
        assertEquals(cityRefTag.tag(), "city")
    }

    @Test
    fun testCollection_returnsCollectionRefTag() {
        val collectRefTag = RefTag.collection(31)

        assertTrue(collectRefTag is RefTag)
        assertEquals(collectRefTag.tag(), "android_project_collection_tag_31")
    }

    @Test
    fun testDashboard_returnsDashboardRefTag() {
        val dashboardRefTag = RefTag.dashboard()

        assertTrue(dashboardRefTag is RefTag)
        assertEquals(dashboardRefTag.tag(), "dashboard")
    }

    @Test
    fun testDeeplink_returnsDeeplinkRefTag() {
        val deeplinkRefTag = RefTag.deepLink()

        assertTrue(deeplinkRefTag is RefTag)
        assertEquals(deeplinkRefTag.tag(), "android_deep_link")
    }

    @Test
    fun testDiscovery_returnsDiscoveryRefTag() {
        val discoveryRefTag = RefTag.discovery()

        assertTrue(discoveryRefTag is RefTag)
        assertEquals(discoveryRefTag.tag(), "discovery")
    }

    @Test
    fun testPledgeInfo_returnsPledgeInfoRefTag() {
        val pledgeInfoRefTag = RefTag.pledgeInfo()

        assertTrue(pledgeInfoRefTag is RefTag)
        assertEquals(pledgeInfoRefTag.tag(), "pledge_info")
    }

    @Test
    fun testProjectShare_returnsProjectShareRefTag() {
        val projectShare = RefTag.projectShare()

        assertTrue(projectShare is RefTag)
        assertEquals(projectShare.tag(), "android_project_share")
    }

    @Test
    fun testPush_returnsPushRefTag() {
        val pushRefTag = RefTag.push()

        assertTrue(pushRefTag is RefTag)
        assertEquals(pushRefTag.tag(), "push")
    }

    @Test
    fun testRecommended_returnsRecommendedRefTag() {
        val recommendedRefTag = RefTag.recommended()

        assertTrue(recommendedRefTag is RefTag)
        assertEquals(recommendedRefTag.tag(), "recommended")
    }

    @Test
    fun testRecommendedWithSort_returnsRecommendedWithSortRefTag() {
        val recommendedWithSortRefTag = RefTag.recommended(DiscoveryParams.Sort.DISTANCE)

        assertTrue(recommendedWithSortRefTag is RefTag)
        assertEquals(recommendedWithSortRefTag.tag(), "recommended_distance")
    }

    @Test
    fun testSearch_returnsSearchRefTag() {
        val searchRefTag = RefTag.search()

        assertTrue(searchRefTag is RefTag)
        assertEquals(searchRefTag.tag(), "search")
    }

    @Test
    fun testSearchFeatured_returnsSearchFeaturedRefTag() {
        val searchFeaturedRefTag = RefTag.searchFeatured()

        assertTrue(searchFeaturedRefTag is RefTag)
        assertEquals(searchFeaturedRefTag.tag(), "search_featured")
    }

    @Test
    fun testSearchPopular_returnsSearchPopularRefTag() {
        val searchPopularRefTag = RefTag.searchPopular()

        assertTrue(searchPopularRefTag is RefTag)
        assertEquals(searchPopularRefTag.tag(), "search_popular_title_view")
    }

    @Test
    fun testSearchPopularFeatured_returnsSearchPopularFeaturedRefTag() {
        val searchPopularFeaturedRefTag = RefTag.searchPopularFeatured()

        assertTrue(searchPopularFeaturedRefTag is RefTag)
        assertEquals(searchPopularFeaturedRefTag.tag(), "search_popular_featured")
    }

    @Test
    fun testSocial_returnsSocialRefTag() {
        val socialRefTag = RefTag.social()

        assertTrue(socialRefTag is RefTag)
        assertEquals(socialRefTag.tag(), "social")
    }

    @Test
    fun testSurvey_returnsSurveyRefTag() {
        val surveyRefTag = RefTag.survey()

        assertTrue(surveyRefTag is RefTag)
        assertEquals(surveyRefTag.tag(), "survey")
    }

    @Test
    fun testThanks_returnsThanksRefTag() {
        val thanksRefTag = RefTag.thanks()

        assertTrue(thanksRefTag is RefTag)
        assertEquals(thanksRefTag.tag(), "thanks")
    }

    @Test
    fun testThanksFacebookShare_returnsThanksFacebookShareRefTag() {
        val thanksFacebookShareRefTag = RefTag.thanksFacebookShare()

        assertTrue(thanksFacebookShareRefTag is RefTag)
        assertEquals(thanksFacebookShareRefTag.tag(), "android_thanks_facebook_share")
    }

    @Test
    fun testThanksTwitterShare_returnsThanksTwitterShareRefTag() {
        val thanksTwitterShareRefTag = RefTag.thanksTwitterShare()

        assertTrue(thanksTwitterShareRefTag is RefTag)
        assertEquals(thanksTwitterShareRefTag.tag(), "android_thanks_twitter_share")
    }

    @Test
    fun testThanksShare_returnsThanksShareRefTag() {
        val thanksShareRefTag = RefTag.thanksShare()

        assertTrue(thanksShareRefTag is RefTag)
        assertEquals(thanksShareRefTag.tag(), "android_thanks_share")
    }

    @Test
    fun testUpdate_returnsUpdateRefTag() {
        val updateRefTag = RefTag.update()

        assertTrue(updateRefTag is RefTag)
        assertEquals(updateRefTag.tag(), "update")
    }

    @Test
    fun testUpdateShare_returnsUpdateShareRefTag() {
        val updateShareRefTag = RefTag.updateShare()

        assertTrue(updateShareRefTag is RefTag)
        assertEquals(updateShareRefTag.tag(), "android_update_share")
    }
}
