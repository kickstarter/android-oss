package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import org.joda.time.DateTime
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class DeepLinkViewModelTest : KSRobolectricTestCase() {
    lateinit var vm: DeepLinkViewModel.DeepLinkViewModel
    private val startBrowser = TestSubscriber<String>()
    private val startDiscoveryActivity = TestSubscriber<Unit>()
    private val startProjectActivity = TestSubscriber<Uri>()
    private val startProjectActivityForCheckout = TestSubscriber<Uri>()
    private val startProjectActivityForComment = TestSubscriber<Uri>()
    private val startProjectActivityForUpdate = TestSubscriber<Uri>()
    private val startProjectActivityToSave = TestSubscriber<Uri>()
    private val startProjectActivityForCommentToUpdate = TestSubscriber<Uri>()
    private val startPreLaunchProjectActivity = TestSubscriber<Project>()
    private val finishDeeplinkActivity = TestSubscriber<Unit>()
    private val disposables = CompositeDisposable()

    fun setUpEnvironment(
        environment: Environment? = null,
        intent: Intent,
        externalCall: CustomNetworkClient? = null
    ) {
        this.vm = DeepLinkViewModel.Factory(
            environment ?: environment(),
            intent,
            externalCall
        ).create(DeepLinkViewModel.DeepLinkViewModel::class.java)

        vm.outputs.startBrowser().subscribe { startBrowser.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startDiscoveryActivity().subscribe { startDiscoveryActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startProjectActivity().subscribe { startProjectActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startProjectActivityForCheckout().subscribe { startProjectActivityForCheckout.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startProjectActivityForComment().subscribe { startProjectActivityForComment.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startProjectActivityForUpdate().subscribe { startProjectActivityForUpdate.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startProjectActivityForCommentToUpdate().subscribe { startProjectActivityForCommentToUpdate.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startProjectActivityToSave().subscribe { startProjectActivityToSave.onNext(it) }.addToDisposable(disposables)
        vm.outputs.finishDeeplinkActivity().subscribe { finishDeeplinkActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startPreLaunchProjectActivity().subscribe { startPreLaunchProjectActivity.onNext(it) }.addToDisposable(disposables)
    }

    @After
    fun clear() {
        disposables.clear()
    }

    @Test
    fun testNonDeepLink_startsBrowser() {
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/comment"

        setUpEnvironment(intent = intentWithData(url))

        startBrowser.assertValue(url)
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testMainPageDeeplink_OpensDiscovery() {
        val url =
            "ksr://www.kickstarter.com/?app_banner=1&ref=nav"

        setUpEnvironment(intent = intentWithData(url))
        startBrowser.assertValue(url)
        startDiscoveryActivity.assertValue(Unit)
        startProjectActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForUpdate.assertNoValues()
        startProjectActivityForCommentToUpdate.assertNoValues()
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
        finishDeeplinkActivity.assertNoValues()
    }

    @Test
    fun testEmailDomain_startsProjectPage() {
        val projectUriAfterRedirection = "https://www.kickstarter.com/projects/lunar1622/the-nasa-approved-tech-watch-with-moon-dust"

        val httpUrl = mock(HttpUrl::class.java)
        val mockedResponse = mock(Response::class.java)
        val mockedRequest = mock(Request::class.java)

        `when`(httpUrl.toString()).thenReturn(projectUriAfterRedirection)
        `when`(mockedResponse.priorResponse).thenReturn(mockedResponse)
        `when`(mockedResponse.request).thenReturn(mockedRequest)
        `when`(mockedResponse.code).thenReturn(302)
        `when`(mockedRequest.url).thenReturn(httpUrl)

        val url =
            "https://clicks.kickstarter.com/f/a/tkHp7b-QTkKgs07EBNX69w~~/AAQRxQA~/RgRnG6LxP0SNaHR0cHM6Ly93d3cua2lja3N0YXJ0ZXIuY29tL3Byb2plY3RzL2x1bmFyMTYyMi90aGUtbmFzYS1hcHByb3ZlZC10ZWNoLXdhdGNoLXdpdGgtbW9vbi1kdXN0P2xpZD13cW13NHc5anpwdjUmcmVmPWtzcl9lbWFpbF9ta3RnX3B3bF8yMDIzLTEwLTI1VwNzcGNCCmU48R05Zac7H7RSGWIuc2FuZ3dpbkBraWNrc3RhcnRlci5jb21YBAAAAFQ~"
        val externalCall = object : CustomNetworkClient {
            override fun obtainUriFromRedirection(uri: Uri): Observable<Response> {
                return Observable.just(mockedResponse)
            }
        }

        setUpEnvironment(intent = intentWithData(url), externalCall = externalCall)
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertValue(Uri.parse(projectUriAfterRedirection))
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testEmailDomain_isNotProject() {
        val discoveryUriAfterRedirection = "https://www.kickstarter.com/discover/advanced?category_id=34&amp;sort=newest"

        val httpUrl = mock(HttpUrl::class.java)
        val mockedResponse = mock(Response::class.java)
        val mockedRequest = mock(Request::class.java)

        `when`(httpUrl.toString()).thenReturn(discoveryUriAfterRedirection)
        `when`(mockedResponse.priorResponse).thenReturn(mockedResponse)
        `when`(mockedResponse.request).thenReturn(mockedRequest)
        `when`(mockedResponse.code).thenReturn(302)
        `when`(mockedRequest.url).thenReturn(httpUrl)

        val url =
            "https://clicks.kickstarter.com/f/a/FCICZcz5yLRUa3P_yQez0Q~~/AAQRxQA~/RgRnQPFeP0TZaHR0cHM6Ly93d3cua2lja3N0YXJ0ZXIuY29tL2Rpc2NvdmVyL2FkdmFuY2VkP2NhdGVnb3J5X2lkPTM0JnNvcnQ9bmV3ZXN0JnNlZWQ9MjgzNDQ0OSZuZXh0X3BhZ2VfY3Vyc29yPSZwYWdlPTElM0ZyZWYlM0RkaXNjb3Zlcnlfb3ZlcmxheSZyZWY9a3NyX2VtYWlsX21rdGdfanVzdGxhdW5jaGVkX0hvcml6b25Gb3JiaWRkZW5XZXN0XzIwMjMtMTEtMjImbGlkPWsyNXFjcDhxNHNwd1cDc3BjQgplVV5sXmVQsuv8UhV0aGViYXNzZHVkZUBnbWFpbC5jb21YBAAAAFQ~"

        val externalCall = object : CustomNetworkClient {
            override fun obtainUriFromRedirection(uri: Uri): Observable<Response> {
                return Observable.just(mockedResponse)
            }
        }
        setUpEnvironment(intent = intentWithData(url), externalCall = externalCall)

        startBrowser.assertNoValues()
        startDiscoveryActivity.assertValue(Unit)
        startProjectActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testProjectPreviewLink_startsBrowser() {
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?token=beepboop"
        setUpEnvironment(intent = intentWithData(url))
        startBrowser.assertValue(url)
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testCheckoutDeepLinkWithRefTag_startsProjectActivity() {
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/pledge?ref=discovery"
        setUpEnvironment(intent = intentWithData(url))
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertValue(Uri.parse(url))
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testCommentsDeepLinkWithRefTag_startsProjectActivityForComments() {
        val url =
            "https://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/comments?ref=discovery"
        setUpEnvironment(intent = intentWithData(url))
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertValue(Uri.parse(url))
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testCommentsDeepLinkWithRefTag_startsProjectActivityForComments_KSR_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUserV2(user)
        val environment = environment().toBuilder()
            .currentUserV2(mockUser)
            .build()
        val url = "ksr://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/comments?ref=discovery"
        setUpEnvironment(environment, intentWithData(url))
        startBrowser.assertNoValues()
        startProjectActivityForComment.assertValue(Uri.parse(url))
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testUpdateDeepLinkWithRefTag_startsProjectActivityForUpdate() {
        val url =
            "https://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/posts/3254626?ref=discovery"
        setUpEnvironment(intent = intentWithData(url))
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForUpdate.assertValue(Uri.parse(url))
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testUpdateDeepLinkWithRefTag_startsProjectActivityForUpdate_KSR_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUserV2(user)
        val environment = environment().toBuilder()
            .currentUserV2(mockUser)
            .build()
        val url = "ksr://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/posts/3254626?ref=discovery"
        setUpEnvironment(environment, intentWithData(url))
        startBrowser.assertNoValues()
        startProjectActivityForUpdate.assertValue(Uri.parse(url))
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testUpdateDeepLinkWithRefTag_startsProjectActivityForCommentsToUpdate() {
        val url =
            "https://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/posts/3254626/comments?ref=discovery"
        setUpEnvironment(intent = intentWithData(url))
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForUpdate.assertNoValues()
        startProjectActivityForCommentToUpdate.assertValue(Uri.parse(url))
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testUpdateDeepLinkWithRefTag_startsProjectActivityForCommentsToUpdate_KSR_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUserV2(user)
        val environment = environment().toBuilder()
            .currentUserV2(mockUser)
            .build()
        val url = "ksr://www.kickstarter.com/projects/fjorden/fjorden-iphone-photography-reinvented/posts/3254626/comments?ref=discovery"
        setUpEnvironment(environment, intentWithData(url))
        startBrowser.assertNoValues()
        startProjectActivityForUpdate.assertNoValues()
        startProjectActivityForCommentToUpdate.assertValue(Uri.parse(url))
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testCheckoutDeepLinkWithoutRefTag_startsProjectActivity() {
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/pledge"
        setUpEnvironment(intent = intentWithData(url))
        val expectedUrl =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/pledge?ref=android_deep_link"
        startProjectActivity.assertNoValues()
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertValue(Uri.parse(expectedUrl))
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testProjectDeepLinkWithRefTag_startsProjectActivity() {
        val mockUser = MockCurrentUserV2()
        val project = ProjectFactory.backedProject().toBuilder().displayPrelaunch(false)
            .deadline(DateTime.now().plusDays(2)).build()

        val environment = environment().toBuilder()
            .apolloClientV2(mockApolloClientForBacking(project))
            .currentUserV2(mockUser)
            .build()
        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?ref=discovery"

        setUpEnvironment(environment, intentWithData(url))

        startProjectActivity.assertValue(Uri.parse(url))
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testProjectDeepLinkWithoutRefTag_startsProjectActivity() {
        val project = ProjectFactory.backedProject().toBuilder().displayPrelaunch(false)
            .deadline(DateTime.now().plusDays(2)).build()

        val environment = environment().toBuilder()
            .apolloClientV2(mockApolloClientForBacking(project))
            .build()

        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap"
        setUpEnvironment(environment, intentWithData(url))
        val expectedUrl =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?ref=android_deep_link"
        startProjectActivity.assertValue(Uri.parse(expectedUrl))
        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testDiscoveryDeepLink_startsDiscoveryActivity() {
        val url = "https://www.kickstarter.com/projects"
        setUpEnvironment(intent = intentWithData(url))
        startDiscoveryActivity.assertValueCount(1)
        startBrowser.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testInAppMessageLink_UpdateUser_refreshUser_HTTPS_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUserV2(user)
        val environment = environment().toBuilder()
            .currentUserV2(mockUser)
            .build()
        val url = "https://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true"
        setUpEnvironment(environment, intentWithData(url))
        startBrowser.assertNoValues()
//        finishDeeplinkActivity.assertValueCount(1)
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testInAppMessageLink_UpdateUser_refreshUser_KSR_schema() {
        val user =
            UserFactory.allTraitsTrue().toBuilder().notifyMobileOfMarketingUpdate(false).build()
        val mockUser = MockCurrentUserV2(user)
        val environment = environment().toBuilder()
            .currentUserV2(mockUser)
            .build()
        val url = "ksr://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true"
        setUpEnvironment(environment, intentWithData(url))
        startBrowser.assertNoValues()
//        finishDeeplinkActivity.assertValueCount(1)
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testInAppMessageLink_NoUser_KSR_schema() {
        val mockUser = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .currentUserV2(mockUser)
            .build()
        val url = "ksr://staging.kickstarter.com/settings/notify_mobile_of_marketing_update/true"
        setUpEnvironment(environment, intentWithData(url))
        startBrowser.assertNoValues()
        finishDeeplinkActivity.assertNoValues()
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testRewardFulfilledDeepLink_HttpsSchema_UserIsBacker() {
        val mockUser = MockCurrentUserV2()
        val project = ProjectFactory.backedProject().toBuilder().deadline(DateTime.now().plusDays(2)).build()
        val backing = requireNotNull(project.backing())

        val environment = environment().toBuilder()
            .apolloClientV2(mockApolloClientForBacking(project))
            .apiClientV2(mockApiSetBacking(backing, true))
            .currentUserV2(mockUser)
            .build()

        val url = "https://staging.kickstarter.com/projects/polymernai/baby-spirits-plush-collection/mark_reward_fulfilled/true"
        setUpEnvironment(environment, intentWithData(url))

        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCommentToUpdate.assertNoValues()
        startProjectActivityForUpdate.assertNoValues()
        finishDeeplinkActivity.assertValueCount(1)
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testRewardFulfilledDeepLink_KsrSchema_UserIsBacker() {
        val mockUser = MockCurrentUserV2()
        val project = ProjectFactory.backedProject().toBuilder().deadline(DateTime.now().plusDays(2)).build()
        val backing = requireNotNull(project.backing())

        val environment = environment().toBuilder()
            .apolloClientV2(mockApolloClientForBacking(project))
            .apiClientV2(mockApiSetBacking(backing, true))
            .currentUserV2(mockUser)
            .build()

        val url = "ksr://staging.kickstarter.com/projects/polymernai/baby-spirits-plush-collection/mark_reward_fulfilled/true"
        setUpEnvironment(environment, intentWithData(url))

        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCommentToUpdate.assertNoValues()
        startProjectActivityForUpdate.assertNoValues()
        finishDeeplinkActivity.assertValueCount(1)
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testRewardFulfilledDeepLink_KsrSchema_UserNotBacker() {
        val mockUser = MockCurrentUserV2()
        val project = ProjectFactory.project()

        val environment = environment().toBuilder()
            .apolloClientV2(mockApolloClientForBacking(project))
            .currentUserV2(mockUser)
            .build()

        val url = "ksr://staging.kickstarter.com/projects/polymernai/baby-spirits-plush-collection/mark_reward_fulfilled/true"
        setUpEnvironment(environment, intentWithData(url))

        startBrowser.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startProjectActivityForCommentToUpdate.assertNoValues()
        startProjectActivityForUpdate.assertNoValues()
        finishDeeplinkActivity.assertValueCount(1)
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testEmailDeepLink() {
        val mockUser = MockCurrentUserV2()
        val environment = environment().toBuilder()
            .currentUserV2(mockUser)
            .build()
        val emails =
            "https://emails.kickstarter.com/ss/c/jbhlvoU_4ViWFpZVbUjED0OpkQHNg7x7JtaKmK7tAlf5HiOJFJwm9QPPHsJjrM5f9t9VpxDuveQHeHob1bSqGauk2heWob9Nvf5D1AgqasWyutgs_WwtPIUhUkX5M3H7U6NCGGIfeY9CvX4ft9BfkMkCE8G15l8dEz1PdFQRek_DMr5D5qq0dR4Qq0kRPKN6snTdVcVJxKhGn6x8t0hegsNVS046-eMTInsXrYvLawE/3c0/o96ZyfWsS1aWJ3l5HgGPcw/h1/07F-qb88bQjr9FC9pH6j4r-zri95lNQI5hMZvl7Z7WM"
        val cliks =
            "https://clicks.kickstarter.com/f/a/Hs4EAU85CJvgLr-uBBByog~~/AAQRxQA~/RgRiXE13P0TUaHR0cHM6Ly93d3cua2lja3N0YXJ0ZXIuY29tL3Byb2plY3RzL21zdDNrL21ha2Vtb3JlbXN0M2s_cmVmPU5ld3NBcHIxNjIxLWVuLWdsb2JhbC1hbGwmdXRtX21lZGl1bT1lbWFpbC1tZ2ImdXRtX3NvdXJjZT1wd2xuZXdzbGV0dGVyJnV0bV9jYW1wYWlnbj1wcm9qZWN0c3dlbG92ZS0wNDE2MjAyMSZ1dG1fY29udGVudD1pbWFnZSZiYW5uZXI9ZmlsbS1uZXdzbGV0dGVyMDFXA3NwY0IKYHh3yHlgkYIOrFIYbGl6YmxhaXJAa2lja3N0YXJ0ZXIuY29tWAQAAABU"
        setUpEnvironment(environment, intentWithData(emails))
        startBrowser.assertValueCount(0)
        startProjectActivityToSave.assertNoValues()
        startPreLaunchProjectActivity.assertNoValues()
    }

    @Test
    fun testProjectSaveLink_startsProjectPageActivity() {
        val project = ProjectFactory.backedProject().toBuilder().displayPrelaunch(false)
            .deadline(DateTime.now().plusDays(2)).build()

        val environment = environment().toBuilder()
            .apolloClientV2(mockApolloClientForBacking(project))
            .build()

        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?save=true"
        val expectedUrl =
            "$url&ref=android_deep_link"

        setUpEnvironment(environment, intentWithData(url))
        startProjectActivityToSave.assertValue(Uri.parse(expectedUrl))
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startBrowser.assertNoValues()
    }

    @Test
    fun testProjectSaveLink_startsPreLaunchProjectPageActivity() {
        val project = ProjectFactory.backedProject().toBuilder().displayPrelaunch(true)
            .deadline(DateTime.now().plusDays(2)).build()

        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        val environment = environment().toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .apolloClientV2(mockApolloClientForBacking(project))
            .build()

        val url =
            "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?save=true"
        val expectedUrl =
            "$url&ref=android_deep_link"

        setUpEnvironment(environment, intentWithData(url))
        startProjectActivityToSave.assertNoValues()
        startDiscoveryActivity.assertNoValues()
        startProjectActivity.assertNoValues()
        startProjectActivityForCheckout.assertNoValues()
        startProjectActivityForComment.assertNoValues()
        startBrowser.assertNoValues()
        finishDeeplinkActivity.assertValueCount(1)
        startPreLaunchProjectActivity.assertValueCount(1)
    }

    private fun mockApiSetBacking(backing: Backing, completed: Boolean): MockApiClientV2 {
        return object : MockApiClientV2() {
            override fun postBacking(
                project: Project,
                backing: Backing,
                checked: Boolean,
            ): Observable<Backing> {
                return Observable.just(backing.toBuilder().completedByBacker(completed).build())
            }
        }
    }

    private fun mockApolloClientForBacking(project: Project): MockApolloClientV2 {
        return object : MockApolloClientV2() {
            override fun getProject(slug: String): Observable<Project> {
                return Observable.just(project.toBuilder().name("leigh283u98").state("successful").build())
            }
        }
    }

    private fun intentWithData(url: String): Intent {
        return Intent()
            .setData(Uri.parse(url))
    }
}
