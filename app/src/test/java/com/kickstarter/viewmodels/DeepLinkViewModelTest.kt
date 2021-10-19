package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.UserFactory
import org.junit.Test
import rx.observers.TestSubscriber

class DeepLinkViewModelTest : KSRobolectricTestCase() {
    lateinit var vm: DeepLinkViewModel.ViewModel
    private val startBrowser = TestSubscriber<String>()
    private val startDiscoveryActivity = TestSubscriber<Void>()
    private val startProjectActivity = TestSubscriber<Uri>()
    private val startProjectActivityForCheckout = TestSubscriber<Uri>()
    private val startProjectActivityForComment = TestSubscriber<Uri>()
    private val startProjectActivityForUpdate = TestSubscriber<Uri>()
    private val startProjectActivityForCommentToUpdate = TestSubscriber<Uri>()
    private val finishDeeplinkActivity = TestSubscriber<Void>()

    fun setUpEnvironment() {
        vm = DeepLinkViewModel.ViewModel(environment())
        vm.outputs.startBrowser().subscribe(startBrowser)
        vm.outputs.startDiscoveryActivity().subscribe(startDiscoveryActivity)
        vm.outputs.startProjectActivity().subscribe(startProjectActivity)
        vm.outputs.startProjectActivityForCheckout().subscribe(startProjectActivityForCheckout)
        vm.outputs.startProjectActivityForComment().subscribe(startProjectActivityForComment)
        vm.outputs.startProjectActivityForUpdate().subscribe(startProjectActivityForUpdate)
        vm.outputs.startProjectActivityForCommentToUpdate().subscribe(startProjectActivityForCommentToUpdate)
        vm.outputs.finishDeeplinkActivity().subscribe(finishDeeplinkActivity)
    }

    fun setUpEnvironment(environment: Environment) {
        vm = DeepLinkViewModel.ViewModel(environment)
        vm.outputs.startBrowser().subscribe(startBrowser)
        vm.outputs.startDiscoveryActivity().subscribe(startDiscoveryActivity)
        vm.outputs.startProjectActivity().subscribe(startProjectActivity)
        vm.outputs.startProjectActivityForCheckout().subscribe(startProjectActivityForCheckout)
        vm.outputs.startProjectActivityForComment().subscribe(startProjectActivityForComment)
        vm.outputs.startProjectActivityForUpdate().subscribe(startProjectActivityForUpdate)
        vm.outputs.startProjectActivityForCommentToUpdate().subscribe(startProjectActivityForCommentToUpdate)
        vm.outputs.finishDeeplinkActivity().subscribe(finishDeeplinkActivity)
    }

    @Test
    fun testNonDeepLink_startsBrowser() {
        setUpEnvironment()
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/comment"
        vm.intent(intentWithData(url))
        startBrowser.assertValue(url)
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
    }

    @Test
    fun testProjectPreviewLink_startsBrowser() {
        setUpEnvironment()
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?token=beepboop"
        vm.intent(intentWithData(url))
        startBrowser.assertValue(url)
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
    }

    @Test
    fun testCheckoutDeepLinkWithRefTag_startsProjectActivity() {
        setUpEnvironment()
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/pledge?ref=discovery"
        vm.intent(intentWithData(url))
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertValue(Uri.parse(url))
    }

    @Test
    fun testCommentsDeepLinkWithRefTag_startsProjectActivityForComments() {
        setUpEnvironment()
        val url =
            "https://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/comments?ref=discovery"
        vm.intent(intentWithData(url))
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertValue(Uri.parse(url))
    }

    @Test
    fun testCommentsDeepLinkWithRefTag_startsProjectActivityForComments_KSR_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
            .currentUser(mockUser)
            .build()
        setUpEnvironment(environment)
        val url = "ksr://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/comments?ref=discovery"
        vm.intent(intentWithData(url))
        startBrowser.assertNoValues()
        finishDeeplinkActivity.assertValueCount(1)
        startProjectActivityForComment.assertValue(Uri.parse(url))
    }

    @Test
    fun testUpdateDeepLinkWithRefTag_startsProjectActivityForUpdate() {
        setUpEnvironment()
        val url =
            "https://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/posts/3254626?ref=discovery"
        vm.intent(intentWithData(url))
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForUpdate.assertValue(Uri.parse(url))
    }

    @Test
    fun testUpdateDeepLinkWithRefTag_startsProjectActivityForUpdate_KSR_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
            .currentUser(mockUser)
            .build()
        setUpEnvironment(environment)
        val url = "ksr://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/posts/3254626?ref=discovery"
        vm.intent(intentWithData(url))
        startBrowser.assertNoValues()
        startProjectActivityForUpdate.assertValue(Uri.parse(url))
        finishDeeplinkActivity.assertValueCount(1)
    }

    @Test
    fun testUpdateDeepLinkWithRefTag_startsProjectActivityForCommentsToUpdate() {
        setUpEnvironment()
        val url =
            "https://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/posts/3254626/comments?ref=discovery"
        vm.intent(intentWithData(url))
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForUpdate.assertNoValues()
        startProjectActivityForCommentToUpdate.assertValue(Uri.parse(url))
    }

    @Test
    fun testUpdateDeepLinkWithRefTag_startsProjectActivityForCommentsToUpdate_KSR_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
            .currentUser(mockUser)
            .build()
        setUpEnvironment(environment)
        val url = "ksr://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/posts/3254626/comments?ref=discovery"
        vm.intent(intentWithData(url))
        startBrowser.assertNoValues()
        startProjectActivityForUpdate.assertNoValues()
        startProjectActivityForCommentToUpdate.assertValue(Uri.parse(url))
        finishDeeplinkActivity.assertValueCount(1)
    }

    @Test
    fun testCheckoutDeepLinkWithoutRefTag_startsProjectActivity() {
        setUpEnvironment()
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/pledge"
        vm.intent(intentWithData(url))
        val expectedUrl =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/pledge?ref=android_deep_link"
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertValue(Uri.parse(expectedUrl))
    }

    @Test
    fun testProjectDeepLinkWithRefTag_startsProjectActivity() {
        setUpEnvironment()
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?ref=discovery"
        vm.intent(intentWithData(url))
        startProjectActivity.assertValue(Uri.parse(url))
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
    }

    @Test
    fun testProjectDeepLinkWithoutRefTag_startsProjectActivity() {
        setUpEnvironment()
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap"
        vm.intent(intentWithData(url))
        val expectedUrl =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?ref=android_deep_link"
        startProjectActivity.assertValue(Uri.parse(expectedUrl))
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
    }

    @Test
    fun testDiscoveryDeepLink_startsDiscoveryActivity() {
        setUpEnvironment()
        val url = "https://www.kickstarter.com/projects"
        vm.intent(intentWithData(url))
        startDiscoveryActivity.assertValueCount(1)
        startBrowser.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
    }

    @Test
    fun testInAppMessageLink_UpdateUser_refreshUser_HTTPS_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
            .currentUser(mockUser)
            .build()
        setUpEnvironment(environment)
        val url = "https://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true"
        vm.intent(intentWithData(url))
        startBrowser.assertNoValues()
        finishDeeplinkActivity.assertValueCount(1)
    }

    @Test
    fun testInAppMessageLink_UpdateUser_refreshUser_KSR_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUser(user)
        val environment = environment().toBuilder()
            .currentUser(mockUser)
            .build()
        setUpEnvironment(environment)
        val url = "ksr://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true"
        vm.intent(intentWithData(url))
        startBrowser.assertNoValues()
        finishDeeplinkActivity.assertValueCount(1)
    }

    @Test
    fun testInAppMessageLink_NoUser_KSR_schema() {
        val mockUser = MockCurrentUser()
        val environment = environment().toBuilder()
            .currentUser(mockUser)
            .build()
        setUpEnvironment(environment)
        val url = "ksr://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true"
        vm.intent(intentWithData(url))
        startBrowser.assertNoValues()
        finishDeeplinkActivity.assertNoValues()
    }

    @Test
    fun testEmailDeepLink() {
        val mockUser = MockCurrentUser()
        val environment = environment().toBuilder()
            .currentUser(mockUser)
            .build()
        setUpEnvironment(environment)
        val emails =
            "https://emails.kickstarter.com/ss/c/jbhlvoU_4ViWFpZVbUjED0OpkQHNg7x7JtaKmK7tAlf5HiOJFJwm9QPPHsJjrM5f9t9VpxDuveQHeHob1bSqGauk2heWob9Nvf5D1AgqasWyutgs_WwtPIUhUkX5M3H7U6NCGGIfeY9CvX4ft9BfkMkCE8G15l8dEz1PdFQRek_DMr5D5qq0dR4Qq0kRPKN6snTdVcVJxKhGn6x8t0hegsNVS046-eMTInsXrYvLawE/3c0/o96ZyfWsS1aWJ3l5HgGPcw/h1/07F-qb88bQjr9FC9pH6j4r-zri95lNQI5hMZvl7Z7WM"
        val cliks =
            "https://clicks.kickstarter.com/f/a/Hs4EAU85CJvgLr-uBBByog~~/AAQRxQA~/RgRiXE13P0TUaHR0cHM6Ly93d3cua2lja3N0YXJ0ZXIuY29tL3Byb2plY3RzL21zdDNrL21ha2Vtb3JlbXN0M2s_cmVmPU5ld3NBcHIxNjIxLWVuLWdsb2JhbC1hbGwmdXRtX21lZGl1bT1lbWFpbC1tZ2ImdXRtX3NvdXJjZT1wd2xuZXdzbGV0dGVyJnV0bV9jYW1wYWlnbj1wcm9qZWN0c3dlbG92ZS0wNDE2MjAyMSZ1dG1fY29udGVudD1pbWFnZSZiYW5uZXI9ZmlsbS1uZXdzbGV0dGVyMDFXA3NwY0IKYHh3yHlgkYIOrFIYbGl6YmxhaXJAa2lja3N0YXJ0ZXIuY29tWAQAAABU"
        vm.intent(intentWithData(emails))
        startBrowser.assertValueCount(1)
    }

    private fun intentWithData(url: String): Intent {
        return Intent()
            .setData(Uri.parse(url))
    }
}
