package com.kickstarter.ui.activities.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.text.HtmlCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.activities.compose.login.LoginDropdownTestTag
import com.kickstarter.ui.activities.compose.login.LoginToutScreen
import com.kickstarter.ui.activities.compose.login.LoginToutTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class LoginToutScreenTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val backButton = composeTestRule.onNodeWithTag(LoginToutTestTag.BACK_BUTTON.name)
    private val title = composeTestRule.onNodeWithTag(LoginToutTestTag.PAGE_TITLE.name)
    private val optionsIcon = composeTestRule.onNodeWithTag(LoginToutTestTag.OPTIONS_ICON.name)
    private val optionsMenu = composeTestRule.onNodeWithTag(LoginDropdownTestTag.OPTIONS_MENU.name)
    private val optionsTerms =
        composeTestRule.onNodeWithTag(LoginDropdownTestTag.OPTIONS_TERMS.name, useUnmergedTree = true)
    private val optionsPrivacyPolicy = composeTestRule.onNodeWithTag(
        LoginDropdownTestTag.OPTIONS_PRIVACY_POLICY.name,
        useUnmergedTree = true
    )
    private val optionsCookie =
        composeTestRule.onNodeWithTag(LoginDropdownTestTag.OPTIONS_COOKIE.name, useUnmergedTree = true)
    private val optionsHelp =
        composeTestRule.onNodeWithTag(LoginDropdownTestTag.OPTIONS_HELP.name, useUnmergedTree = true)
    private val kSLogo = composeTestRule.onNodeWithTag(LoginToutTestTag.KS_LOGO.name)
    private val logoTitle = composeTestRule.onNodeWithTag(LoginToutTestTag.LOGO_TITLE.name)
    private val facebookButton =
        composeTestRule.onNodeWithTag(LoginToutTestTag.FACEBOOK_BUTTON.name)
    private val facebookDisclaimer =
        composeTestRule.onNodeWithTag(LoginToutTestTag.FACEBOOK_DISCLAIMER.name)
    private val emailLogInButton =
        composeTestRule.onNodeWithTag(LoginToutTestTag.EMAIL_LOG_IN_BUTTON.name)
    private val emailSignUpButton =
        composeTestRule.onNodeWithTag(LoginToutTestTag.EMAIL_SIGN_UP_BUTTON.name)
    private val touPpCookieDisclaimer =
        composeTestRule.onNodeWithTag(LoginToutTestTag.TOU_PP_COOKIE_DISCLAIMER.name)

    @Test
    fun testComponentsVisible() {
        composeTestRule.setContent {
            KSTheme {
                LoginToutScreen(
                    onBackClicked = { },
                    onFacebookButtonClicked = { },
                    onEmailLoginClicked = { },
                    onEmailSignupClicked = { },
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { }
                )
            }
        }

        val titleText = context.resources.getString(R.string.login_tout_navbar_title)
        title.assertTextEquals(titleText)

        val logoTitleText =
            context.resources.getString(R.string.discovery_onboarding_title_bring_creative_projects_to_life)
        logoTitle.assertTextEquals(logoTitleText)

        val facebookDisclaimerText =
            context.resources.getString(R.string.Facebook_login_disclaimer_update)
        facebookDisclaimer.assertTextEquals(facebookDisclaimerText)

        val touPpCookieDisclaimerText =
            context.resources.getString(R.string.login_tout_disclaimer_agree_to_terms_html)
        val formattedText = HtmlCompat.fromHtml(
            touPpCookieDisclaimerText,
            0
        ).toString()
        touPpCookieDisclaimer.assertTextEquals(formattedText)

        backButton.assertIsDisplayed()
        title.assertIsDisplayed()
        optionsIcon.assertIsDisplayed()
        optionsMenu.assertDoesNotExist()
        kSLogo.assertIsDisplayed()
        logoTitle.assertIsDisplayed()
        facebookButton.assertIsDisplayed()
        facebookDisclaimer.assertIsDisplayed()
        emailLogInButton.assertIsDisplayed()
        emailSignUpButton.assertIsDisplayed()
        touPpCookieDisclaimer.assertIsDisplayed()
    }

    @Test
    fun testOptionsMenuGetsDisplayed() {
        composeTestRule.setContent {
            KSTheme {
                LoginToutScreen(
                    onBackClicked = { },
                    onFacebookButtonClicked = { },
                    onEmailLoginClicked = { },
                    onEmailSignupClicked = { },
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { }
                )
            }
        }

        optionsMenu.assertDoesNotExist()
        optionsIcon.performClick()
        optionsMenu.assertIsDisplayed()

        optionsTerms.assertIsDisplayed()
        val optionsTermsText = context.resources.getString(R.string.login_tout_help_sheet_terms)
        optionsTerms.assertTextEquals(optionsTermsText)

        optionsPrivacyPolicy.assertIsDisplayed()
        val optionsPrivacyPolicyText =
            context.resources.getString(R.string.login_tout_help_sheet_privacy)
        optionsPrivacyPolicy.assertTextEquals(optionsPrivacyPolicyText)

        optionsCookie.assertIsDisplayed()
        val optionsCookieText = context.resources.getString(R.string.login_tout_help_sheet_cookie)
        optionsCookie.assertTextEquals(optionsCookieText)

        optionsHelp.assertIsDisplayed()
        val optionsHelpText = context.resources.getString(R.string.general_navigation_buttons_help)
        optionsHelp.assertTextEquals(optionsHelpText)

        optionsHelp.performClick()
        optionsMenu.assertDoesNotExist()
    }

    @Test
    fun testClickActionsWork() {
        var backClickedCount = 0
        var facebookClickedCount = 0
        var emailLoginClickedCount = 0
        var emailSignUpClickedCount = 0
        var termsOfUseClickedCount = 0
        var privacyPolicyClickedCount = 0
        var cookiePolicyClickedCount = 0
        var helpClickedCount = 0

        composeTestRule.setContent {
            KSTheme {
                LoginToutScreen(
                    onBackClicked = { backClickedCount++ },
                    onFacebookButtonClicked = { facebookClickedCount++ },
                    onEmailLoginClicked = { emailLoginClickedCount++ },
                    onEmailSignupClicked = { emailSignUpClickedCount++ },
                    onTermsOfUseClicked = { termsOfUseClickedCount++ },
                    onPrivacyPolicyClicked = { privacyPolicyClickedCount++ },
                    onCookiePolicyClicked = { cookiePolicyClickedCount++ },
                    onHelpClicked = { helpClickedCount++ }
                )
            }
        }

        backButton.performClick()
        assertEquals(backClickedCount, 1)

        facebookButton.performClick()
        assertEquals(facebookClickedCount, 1)

        emailLogInButton.performClick()
        assertEquals(emailLoginClickedCount, 1)

        emailSignUpButton.performClick()
        assertEquals(emailSignUpClickedCount, 1)

        optionsIcon.performClick()
        optionsTerms.performClick()
        assertEquals(termsOfUseClickedCount, 1)

        optionsIcon.performClick()
        optionsPrivacyPolicy.performClick()
        assertEquals(privacyPolicyClickedCount, 1)

        optionsIcon.performClick()
        optionsCookie.performClick()
        assertEquals(cookiePolicyClickedCount, 1)

        optionsIcon.performClick()
        optionsHelp.performClick()
        assertEquals(helpClickedCount, 1)

        // TODO find a way to click hyperlinks in touPpCookieDisclaimer text reliably to test
    }
}
