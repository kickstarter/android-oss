package com.kickstarter.ui.activities.compose

import OnboardingScreen
import OnboardingScreenTestTags
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class OnboardingFlowScreenTest : KSRobolectricTestCase() {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val closeButton = composeTestRule.onNodeWithTag(OnboardingScreenTestTags.CLOSE_BUTTON, useUnmergedTree = true)
    private val pageTitle = composeTestRule.onNodeWithTag(OnboardingScreenTestTags.PAGE_TITLE, useUnmergedTree = true)
    private val pageDescription = composeTestRule.onNodeWithTag(OnboardingScreenTestTags.PAGE_DESCRIPTION, useUnmergedTree = true)
    private val pageAnimation = composeTestRule.onNodeWithTag(OnboardingScreenTestTags.PAGE_ANIMATION, useUnmergedTree = true)
    private val primaryButton = composeTestRule.onNodeWithTag(OnboardingScreenTestTags.PRIMARY_BUTTON, useUnmergedTree = true)
    private val secondaryButton = composeTestRule.onNodeWithTag(OnboardingScreenTestTags.SECONDARY_BUTTON, useUnmergedTree = true)
    private val progressBar = composeTestRule.onNodeWithTag(OnboardingScreenTestTags.PROGRESS_BAR, useUnmergedTree = true)

    private var onboardingCompletedCalled = false
    private var onboardingCancelledCalled = false
    private var turnOnNotificationsCalled = false
    private var allowTrackingCalled = false
    private var signupOrLoginCalled = false

    private val welcomePageTitleText by lazy { context.getString(R.string.onboarding_welcome_to_kickstarter_title) }
    private val welcomePageDescriptionText by lazy { context.getString(R.string.onboarding_welcome_to_kickstarter_subtitle) }
    private val welcomePageButtonText by lazy { context.getString(R.string.project_checkout_navigation_next) }

    private val saveProjectsPageTitleText by lazy { context.getString(R.string.onboarding_save_projects_for_later_title) }
    private val saveProjectsPageDescriptionText by lazy { context.getString(R.string.onboarding_save_projects_for_later_subtitle) }
    private val saveProjectsPageButtonText by lazy { context.getString(R.string.project_checkout_navigation_next) }

    private val notificationsPageTitleText by lazy { context.getString(R.string.onboarding_stay_in_the_know_title) }
    private val notificationsPageDescriptionText by lazy { context.getString(R.string.onboarding_stay_in_the_know_subtitle) }
    private val notificationsPageButtonText by lazy { context.getString(R.string.Get_notified) }
    private val notificationsPageSecondaryButtonText by lazy { context.getString(R.string.Not_right_now) }

    private val activityTrackingPageTitleText by lazy { context.getString(R.string.onboarding_personalize_your_experience_title) }
    private val activityTrackingPageDescriptionText by lazy { context.getString(R.string.onboarding_personalize_your_experience_subtitle) }
    private val activityTrackingPageButtonText by lazy { context.getString(R.string.Use_personalization) }
    private val activityTrackingPageSecondaryButtonText by lazy { context.getString(R.string.Not_right_now) }

    private val loginOrSignupPageTitleText by lazy { context.getString(R.string.onboarding_join_the_community_title) }
    private val loginOrSignupPageDescriptionText by lazy { context.getString(R.string.onboarding_join_the_community_subtitle) }
    private val loginOrSignupPageButtonText by lazy { context.getString(R.string.Sign_up_or_log_in) }
    private val loginOrSignupPageSecondaryButtonText by lazy { context.getString(R.string.Explore_the_app) }

    private fun setupOnboardingScreen(isUserLoggedIn: Boolean = false, deviceNeedsNotificationPermissions: Boolean = true) {
        onboardingCompletedCalled = false
        onboardingCancelledCalled = false
        turnOnNotificationsCalled = false
        allowTrackingCalled = false
        signupOrLoginCalled = false

        composeTestRule.setContent {
            KSTheme {
                OnboardingScreen(
                    isUserLoggedIn = isUserLoggedIn,
                    deviceNeedsNotificationPermissions = deviceNeedsNotificationPermissions,
                    onboardingCompleted = { onboardingCompletedCalled = true },
                    onboardingCancelled = { onboardingCancelledCalled = true },
                    turnOnNotifications = { turnOnNotificationsCalled = true },
                    allowTracking = { allowTrackingCalled = true },
                    signupOrLogin = { signupOrLoginCalled = true }
                )
            }
        }
    }

    @Test
    fun `Test welcome page UI displays correctly`() {
        setupOnboardingScreen()

        closeButton.assertIsDisplayed()
        pageAnimation.assertIsDisplayed()
        progressBar.assertIsDisplayed()
        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(welcomePageTitleText)
        pageDescription.assertIsDisplayed()
        pageDescription.assertTextEquals(welcomePageDescriptionText)
        primaryButton.assertIsDisplayed()
        composeTestRule.onNodeWithText(welcomePageButtonText)
            .assertExists()

        secondaryButton.assertDoesNotExist() // No secondary button on the welcome page
    }

    @Test
    fun `Test welcome page next button click navigates to save projects page`() {
        setupOnboardingScreen()

        primaryButton.performClick()

        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(saveProjectsPageTitleText)
        pageDescription.assertIsDisplayed()
        pageDescription.assertTextEquals(saveProjectsPageDescriptionText)
        composeTestRule.onNodeWithText(saveProjectsPageButtonText)
            .assertExists()
        secondaryButton.assertDoesNotExist() // No secondary button on the save projects page
    }

    @Test
    fun `Test close button invokes onboardingCancelled`() {
        setupOnboardingScreen()
        closeButton.performClick()
        assertTrue(onboardingCancelledCalled)
    }

    @Test
    fun `Test save projects page next button click skips notifications page if device doesn't need notification permissions`() {
        setupOnboardingScreen(deviceNeedsNotificationPermissions = false)

        primaryButton.performClick() // Welcome -> Save
        Thread.sleep(500)
        primaryButton.performClick() // Save -> Activity Tracking

        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(activityTrackingPageTitleText)
        pageDescription.assertIsDisplayed()
        pageDescription.assertTextEquals(activityTrackingPageDescriptionText)
        composeTestRule.onNodeWithText(activityTrackingPageButtonText)
            .assertExists()
        composeTestRule.onNodeWithText(activityTrackingPageSecondaryButtonText)
            .assertExists()
    }

    @Test
    fun `Test notifications page primary button click invokes turnOnNotifications`() {
        setupOnboardingScreen()

        primaryButton.performClick() // Welcome -> Save
        Thread.sleep(500)
        primaryButton.performClick() // Save -> Notifications
        Thread.sleep(500)

        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(notificationsPageTitleText)
        pageDescription.assertIsDisplayed()
        pageDescription.assertTextEquals(notificationsPageDescriptionText)
        primaryButton.assertIsDisplayed()
        composeTestRule.onNodeWithText(notificationsPageButtonText)
            .assertExists()
        composeTestRule.onNodeWithText(notificationsPageSecondaryButtonText)
            .assertExists()

        primaryButton.performClick()
        assertTrue(turnOnNotificationsCalled)
    }

    @Test
    fun `Test activity tracking page primary button click invokes allowTracking`() {
        setupOnboardingScreen()

        primaryButton.performClick() // Welcome -> Save
        Thread.sleep(500)
        primaryButton.performClick() // Save -> Notifications
        Thread.sleep(500)
        secondaryButton.performClick() // Notifications -> Activity Tracking
        Thread.sleep(500)

        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(activityTrackingPageTitleText)
        pageDescription.assertIsDisplayed()
        pageDescription.assertTextEquals(activityTrackingPageDescriptionText)
        primaryButton.assertIsDisplayed()
        composeTestRule.onNodeWithText(activityTrackingPageButtonText)
            .assertExists()
        composeTestRule.onNodeWithText(activityTrackingPageSecondaryButtonText)
            .assertExists()

        primaryButton.performClick()
        assertTrue(allowTrackingCalled)
    }

    @Test
    fun `Test activity tracking page secondary button click navigates to loginOrSignup page if user is not logged in`() {
        setupOnboardingScreen(isUserLoggedIn = false)

        primaryButton.performClick() // Welcome -> Save
        Thread.sleep(500)
        primaryButton.performClick() // Save -> Notifications
        Thread.sleep(500)
        secondaryButton.performClick() // Notifications -> Activity Tracking
        Thread.sleep(500)
        secondaryButton.performClick() // Activity Tracking -> Login/Signup
        Thread.sleep(500)

        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(loginOrSignupPageTitleText)
        pageDescription.assertIsDisplayed()
        pageDescription.assertTextEquals(loginOrSignupPageDescriptionText)
        primaryButton.assertIsDisplayed()
        composeTestRule.onNodeWithText(loginOrSignupPageButtonText)
            .assertExists()
        composeTestRule.onNodeWithText(loginOrSignupPageSecondaryButtonText)
            .assertExists()

        // Assert that onboardingCompleted callback was NOT called
        assertFalse("onboardingCompleted callback should NOT have been called", onboardingCompletedCalled)
    }

    @Test
    fun `Test activity tracking page secondary button click navigates to Discovery page if user is already logged in`() {
        setupOnboardingScreen(isUserLoggedIn = true)

        // Navigate to Notifications Page (Welcome -> Save -> Notifications)
        primaryButton.performClick() // Welcome -> Save
        Thread.sleep(500)
        primaryButton.performClick() // Save -> Notifications
        Thread.sleep(500)
        secondaryButton.performClick() // Notifications -> Activity Tracking
        Thread.sleep(500)
        secondaryButton.performClick() // Activity Tracking -> Login/Signup
        Thread.sleep(500)

        // Assert that signupOrLogin callback was NOT called
        assertFalse("signupOrLogin callback should NOT have been called", signupOrLoginCalled)
        // Assert that onboardingCompleted callback was called
        assertTrue("onboardingCompleted callback should have been called", onboardingCompletedCalled)
    }
}
