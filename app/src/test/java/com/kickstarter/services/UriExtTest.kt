package com.kickstarter.services

import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.isCheckoutUri
import com.kickstarter.libs.utils.extensions.isDiscoverCategoriesPath
import com.kickstarter.libs.utils.extensions.isDiscoverPlacesPath
import com.kickstarter.libs.utils.extensions.isDiscoverScopePath
import com.kickstarter.libs.utils.extensions.isDiscoverSortParam
import com.kickstarter.libs.utils.extensions.isKSFavIcon
import com.kickstarter.libs.utils.extensions.isKickstarterUri
import com.kickstarter.libs.utils.extensions.isModalUri
import com.kickstarter.libs.utils.extensions.isNewGuestCheckoutUri
import com.kickstarter.libs.utils.extensions.isProjectCommentUri
import com.kickstarter.libs.utils.extensions.isProjectPreviewUri
import com.kickstarter.libs.utils.extensions.isProjectSurveyUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateCommentsUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateUri
import com.kickstarter.libs.utils.extensions.isProjectUpdatesUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.libs.utils.extensions.isRewardFulfilledDl
import com.kickstarter.libs.utils.extensions.isSettingsUrl
import com.kickstarter.libs.utils.extensions.isSignupUri
import com.kickstarter.libs.utils.extensions.isUserSurveyUri
import com.kickstarter.libs.utils.extensions.isVerificationEmailUrl
import com.kickstarter.libs.utils.extensions.isWebViewUri
import org.junit.Test

class UriExtTest : KSRobolectricTestCase() {
    private val checkoutUri = Uri.parse("https://www.ksr.com/projects/creator/project/pledge")
    private val deepLinkMarketingHttps =
        Uri.parse("https://www.kickstarter.com/settings/notify_mobile_of_marketing_update/true")
    private val deepLinkMarketingHttpsStaging =
        Uri.parse("https://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true")
    private val deepLinkMarketingKsr =
        Uri.parse("ksr://www.kickstarter.com/settings/notify_mobile_of_marketing_update/true")
    private val deepLinkMarketingKsrStaging =
        Uri.parse("ksr://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true")
    private val rewardFulfilledKsr =
        Uri.parse("ksr://www.kickstarter.com/projects/1186238668/skull-graphic-tee/mark_reward_fulfilled/true")
    private val rewardFulfilledStagingKsr =
        Uri.parse("ksr://staging.kickstarter.com/projects/1186238668/skull-graphic-tee/mark_reward_fulfilled/true")
    private val rewardFulfilledHttps =
        Uri.parse("https://www.kickstarter.com/projects/1186238668/skull-graphic-tee/mark_reward_fulfilled/true")
    private val rewardFulfilledStagingHttps =
        Uri.parse("https://staging.kickstarter.com/projects/1186238668/skull-graphic-tee/mark_reward_fulfilled/true")
    private val discoverCategoriesUri = Uri.parse("https://www.ksr.com/discover/categories/art")
    private val discoverScopeUri = Uri.parse("https://www.kickstarter.com/discover/ending-soon")
    private val discoverPlacesUri = Uri.parse("https://www.ksr.com/discover/places/newest")
    private val newGuestCheckoutUri = Uri.parse("https://www.ksr.com/checkouts/1/guest/new")
    private val projectUri = Uri.parse("https://www.ksr.com/projects/creator/project")
    private val signUpUri = Uri.parse("https://www.ksr.com/signup")
    private val verificationEmail = Uri.parse("https://www.ksr.com/profile/verify_email")
    private val projectPreviewUri =
        Uri.parse("https://www.ksr.com/projects/creator/project?token=token")
    private val projectSurveyUri =
        Uri.parse("https://www.ksr.com/projects/creator/project/surveys/survey-param")
    private val updatesUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts")
    private val updateUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts/id")
    private val userSurveyUri = Uri.parse("https://www.ksr.com/users/user-param/surveys/survey-id")
    private val webEndpoint = "https://www.ksr.com"
    private val discoverSortUri = Uri.parse("https://www.kickstarter.com/discover/advanced?sort=ending-soon")

    @Test
    fun testUri_isCheckoutUri() {
        assertTrue(checkoutUri.isCheckoutUri(webEndpoint))
    }

    @Test
    fun testUri_isSettingsURI() {
        assertTrue(deepLinkMarketingHttps.isSettingsUrl())
        assertTrue(deepLinkMarketingKsr.isSettingsUrl())
        assertTrue(deepLinkMarketingHttpsStaging.isSettingsUrl())
        assertTrue(deepLinkMarketingKsrStaging.isSettingsUrl())
    }

    @Test
    fun testUri_isRewardFulfilledDeepLink() {
        assertTrue(rewardFulfilledHttps.isRewardFulfilledDl())
        assertTrue(rewardFulfilledStagingHttps.isRewardFulfilledDl())
        assertTrue(rewardFulfilledKsr.isRewardFulfilledDl())
        assertTrue(rewardFulfilledStagingKsr.isRewardFulfilledDl())
    }

    @Test
    fun testUri_isDiscoverCategoriesPath() {
        assertTrue(discoverCategoriesUri.isDiscoverCategoriesPath())
        assertFalse(discoverPlacesUri.isDiscoverCategoriesPath())
    }

    @Test
    fun testUri_isDiscoverPlacesPath() {
        assertTrue(discoverPlacesUri.isDiscoverPlacesPath())
        assertFalse(discoverCategoriesUri.isDiscoverPlacesPath())
    }

    @Test
    fun testUri_isDiscoverScopePath() {
        assertTrue(discoverScopeUri.isDiscoverScopePath("ending-soon"))
    }

    @Test
    fun testUri_isKickstarterUri() {
        val ksrUri = Uri.parse("https://www.ksr.com/discover")
        val uri = Uri.parse("https://www.hello-world.org/goodbye")
        assertTrue(ksrUri.isKickstarterUri(webEndpoint))
        assertFalse(uri.isKickstarterUri(webEndpoint))
    }

    @Test
    fun testUri_isWebViewUri() {
        val ksrUri = Uri.parse("https://www.ksr.com/project")
        val uri = Uri.parse("https://www.hello-world.org/goodbye")
        val ksrGraphUri = Uri.parse("https://www.ksr.com/graph")
        val graphUri = Uri.parse("https://www.hello-world.org/graph")
        val favIconUri = Uri.parse("https://www.ksr.com/favicon.ico")
        assertTrue(ksrUri.isWebViewUri(webEndpoint))
        assertFalse(uri.isWebViewUri(webEndpoint))
        assertTrue(ksrGraphUri.isWebViewUri(webEndpoint))
        assertFalse(graphUri.isWebViewUri(webEndpoint))
        assertFalse(favIconUri.isWebViewUri(webEndpoint))
    }

    @Test
    fun testUri_isKSFavIcon() {
        val ksrUri = Uri.parse("https://www.ksr.com/favicon.ico")
        val uri = Uri.parse("https://www.hello-world.org/goodbye")
        assertTrue(ksrUri.isKSFavIcon(webEndpoint))
        assertFalse(uri.isKSFavIcon(webEndpoint))
    }

    @Test
    fun testUri_isModalUri() {
        val modalUri = Uri.parse("https://www.ksr.com/project?modal=true")
        assertTrue(modalUri.isModalUri(webEndpoint))
        assertFalse(projectUri.isModalUri(webEndpoint))
    }

    @Test
    fun testUri_isNewGuestCheckoutUri() {
        assertTrue(newGuestCheckoutUri.isNewGuestCheckoutUri(webEndpoint))
    }

    @Test
    fun testUri_isProjectSurveyUri() {
        assertTrue(projectSurveyUri.isProjectSurveyUri(webEndpoint))
        assertFalse(userSurveyUri.isProjectSurveyUri(webEndpoint))
    }

    @Test
    fun testUri_isProjectUpdateCommentsUri() {
        val updateCommentsUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts/id/comments")
        assertTrue(updateCommentsUri.isProjectUpdateCommentsUri(webEndpoint))
        assertFalse(updatesUri.isProjectUpdateCommentsUri(webEndpoint))
    }

    @Test
    fun testUri_isProjectCommentsUri() {
        val commentsUri = Uri.parse("https://www.ksr.com/projects/creator/project/comments")
        assertTrue(commentsUri.isProjectCommentUri(webEndpoint))
    }

    @Test
    fun testUri_isProjectUpdateUri() {
        assertTrue(updateUri.isProjectUpdateUri(webEndpoint))
        assertFalse(updatesUri.isProjectUpdateUri(webEndpoint))
    }

    @Test
    fun testUri_isProjectUpdatesUri() {
        assertTrue(updatesUri.isProjectUpdatesUri(webEndpoint))
        assertFalse(updateUri.isProjectUpdatesUri(webEndpoint))
    }

    @Test
    fun testUri_isProjectUri() {
        assertTrue(projectUri.isProjectUri(webEndpoint))
        assertTrue(projectPreviewUri.isProjectUri(webEndpoint))
        assertFalse(updateUri.isProjectUri(webEndpoint))
    }

    @Test
    fun testUri_isProjectPreviewUri() {
        assertTrue(projectPreviewUri.isProjectPreviewUri(webEndpoint))
        assertFalse(projectUri.isProjectPreviewUri(webEndpoint))
    }

    @Test
    fun testUri_isUserSurveyUri() {
        assertTrue(userSurveyUri.isUserSurveyUri(webEndpoint))
        assertFalse(projectSurveyUri.isUserSurveyUri(webEndpoint))
    }

    @Test
    fun testUri_isSignupUri() {
        assertFalse(projectUri.isSignupUri(webEndpoint))
        assertTrue(signUpUri.isSignupUri(webEndpoint))
    }

    @Test
    fun testUri_isVerificationEmailUrl() {
        assertFalse(projectUri.isVerificationEmailUrl())
        assertTrue(verificationEmail.isVerificationEmailUrl())
    }

    @Test
    fun testUri_isDiscoverySortUri() {
        assertTrue(discoverSortUri.isDiscoverSortParam())
    }
}
