package com.kickstarter.ui.activities.compose.login

import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class SignupScreenKtTest : KSRobolectricTestCase() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val backButton =
        composeTestRule.onNodeWithTag(SignupScreenTestTag.BACK_BUTTON.name)
    private val pageTitle =
        composeTestRule.onNodeWithTag(SignupScreenTestTag.PAGE_TITLE.name)
    private val signupButton =
        composeTestRule.onNodeWithTag(SignupScreenTestTag.SIGNUP_BUTTON.name)

    private val progressBar =
        composeTestRule.onNodeWithTag(SignupScreenTestTag.PROGRESS_BAR.name)
    private val newsletterOptInText =
        composeTestRule.onNodeWithTag(SignupScreenTestTag.NEWSLETTER_OPT_IN_TEXT.name)
    private val newsletterOptInSwitch =
        composeTestRule.onNodeWithTag(SignupScreenTestTag.NEWSLETTER_OPT_IN_SWITCH.name)
    private val nameEditText =
        composeTestRule.onNodeWithTag(SignupScreenTestTag.NAME_EDIT_TEXT.name)
    private val emailEditText =
        composeTestRule.onNodeWithTag(SignupScreenTestTag.EMAIL_EDIT_TEXT.name)
    private val passwordEditText =
        composeTestRule.onNodeWithTag(SignupScreenTestTag.PASSWORD_EDIT_TEXT.name)

    private val optionsIcon =
        composeTestRule.onNodeWithTag(
            SignupScreenTestTag.OPTIONS_ICON.name,
        )

    private val optionsMenu = composeTestRule.onNodeWithTag(LoginDropdownTestTag.OPTIONS_MENU.name)
    private val optionsTerms =
        composeTestRule.onNodeWithTag(
            LoginDropdownTestTag.OPTIONS_TERMS.name,
            useUnmergedTree = true
        )
    private val optionsPrivacyPolicy = composeTestRule.onNodeWithTag(
        LoginDropdownTestTag.OPTIONS_PRIVACY_POLICY.name,
        useUnmergedTree = true
    )
    private val optionsCookie =
        composeTestRule.onNodeWithTag(
            LoginDropdownTestTag.OPTIONS_COOKIE.name,
            useUnmergedTree = true
        )
    private val optionsHelp =
        composeTestRule.onNodeWithTag(
            LoginDropdownTestTag.OPTIONS_HELP.name,
            useUnmergedTree = true
        )

    @Test
    fun testComponentsVisible() {
        composeTestRule.setContent {
            KSTheme {
                SignupScreen(
                    onBackClicked = {},
                    onSignupButtonClicked = { _, _, _, _ -> },
                    showProgressBar = false,
                    isFormSubmitting = false,
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        backButton.assertIsDisplayed()

        val titleText = context.resources.getString(R.string.Sign_up)
        pageTitle.assertTextEquals(titleText)
        pageTitle.assertIsDisplayed()

        emailEditText.assertIsDisplayed()
        nameEditText.assertIsDisplayed()
        passwordEditText.assertIsDisplayed()
        progressBar.assertDoesNotExist()

        val newsletterOptInString =
            context.resources.getString(R.string.signup_newsletter_full_opt_out)
        newsletterOptInText.assertTextEquals(newsletterOptInString)
        newsletterOptInText.assertIsDisplayed()

        newsletterOptInSwitch.assertIsDisplayed()
        newsletterOptInSwitch.assertIsEnabled()
    }

    @Test
    fun testSignupButton() {
        var signupButtonClicked = 0

        composeTestRule.setContent {
            KSTheme {
                SignupScreen(
                    onBackClicked = {},
                    onSignupButtonClicked = { _, _, _, _ -> signupButtonClicked++ },
                    showProgressBar = false,
                    isFormSubmitting = false,
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        emailEditText.assertIsDisplayed()
        nameEditText.assertIsDisplayed()
        passwordEditText.assertIsDisplayed()
        progressBar.assertDoesNotExist()
        signupButton.assertIsNotEnabled()
        signupButton.performClick()
        assertEquals(signupButtonClicked, 0)

        emailEditText.performTextInput("leigh@gmail.com")
        nameEditText.performTextInput("test")
        passwordEditText.performTextInput("sfdgdfgdgdhghgd")
        signupButton.assertIsEnabled()

        signupButton.performClick()
        assertEquals(signupButtonClicked, 1)
    }

    @Test
    fun testSendNewsletterSwitch() {
        var sendNewsletter = false
        var signupButtonClicked = 0

        composeTestRule.setContent {
            KSTheme {
                SignupScreen(
                    onBackClicked = {},
                    onSignupButtonClicked = { _, _, _, sendNewsletterClick ->
                        sendNewsletter = sendNewsletterClick
                        signupButtonClicked++
                    },
                    showProgressBar = false,
                    isFormSubmitting = false,
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        backButton.assertIsDisplayed()

        val titleText = context.resources.getString(R.string.Sign_up)
        pageTitle.assertTextEquals(titleText)
        pageTitle.assertIsDisplayed()

        emailEditText.assertIsDisplayed()
        nameEditText.assertIsDisplayed()
        passwordEditText.assertIsDisplayed()
        progressBar.assertDoesNotExist()

        val newsletterOptInString =
            context.resources.getString(R.string.signup_newsletter_full_opt_out)
        newsletterOptInText.assertTextEquals(newsletterOptInString)
        newsletterOptInText.assertIsDisplayed()

        newsletterOptInSwitch.assertIsDisplayed()
        newsletterOptInSwitch.assertIsEnabled()
        newsletterOptInSwitch.assertIsToggleable()

        emailEditText.performTextInput("leigh@gmail.com")
        nameEditText.performTextInput("test")
        passwordEditText.performTextInput("sfdgdfgdgdhghgd")
        signupButton.assertIsEnabled()

        newsletterOptInSwitch.performTouchInput { swipeRight() }
        signupButton.performClick()
        assertEquals(signupButtonClicked, 1)
        assertEquals(sendNewsletter, true)

        newsletterOptInSwitch.performTouchInput { swipeLeft() }
        signupButton.performClick()
        assertEquals(sendNewsletter, false)
    }

    fun testBackButton() {
        var backClickedCount = 0

        composeTestRule.setContent {
            KSTheme {
                SignupScreen(
                    onBackClicked = { backClickedCount++ },
                    onSignupButtonClicked = { _, _, _, _ -> },
                    showProgressBar = false,
                    isFormSubmitting = false,
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        backButton.assertIsDisplayed()
        backButton.performClick()
        assertEquals(1, backClickedCount)
    }

    fun testProgressBar() {
        var backClickedCount = 0

        composeTestRule.setContent {
            KSTheme {
                SignupScreen(
                    onBackClicked = { backClickedCount++ },
                    onSignupButtonClicked = { _, _, _, _ -> },
                    showProgressBar = false,
                    isFormSubmitting = false,
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        progressBar.assertDoesNotExist()
        signupButton.assertIsNotEnabled()
        signupButton.performClick()

        emailEditText.performTextInput("leigh@gmail.com")
        nameEditText.performTextInput("test")
        passwordEditText.performTextInput("sfdgdfgdgdhghgd")
        signupButton.assertIsEnabled()

        signupButton.performClick()
        progressBar.assertExists()
        progressBar.assertIsDisplayed()
    }

    @Test
    fun testDropDownButtonClicks() {
        var termsClickedCount = 0
        var privacyClickedCount = 0
        var cookieClickedCount = 0
        var helpClickedCount = 0

        composeTestRule.setContent {
            KSTheme {
                SignupScreen(
                    onBackClicked = { },
                    onSignupButtonClicked = { _, _, _, _ -> },
                    showProgressBar = false,
                    isFormSubmitting = false,
                    onTermsOfUseClicked = { termsClickedCount++ },
                    onPrivacyPolicyClicked = { privacyClickedCount++ },
                    onCookiePolicyClicked = { cookieClickedCount++ },
                    onHelpClicked = { helpClickedCount++ },
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        optionsIcon.performClick()
        optionsMenu.assertIsDisplayed()

        optionsTerms.performClick()
        optionsMenu.assertDoesNotExist()
        assertEquals(termsClickedCount, 1)

        optionsIcon.performClick()
        optionsPrivacyPolicy.performClick()
        assertEquals(privacyClickedCount, 1)

        optionsIcon.performClick()
        optionsCookie.performClick()
        assertEquals(cookieClickedCount, 1)

        optionsIcon.performClick()
        optionsHelp.performClick()
        assertEquals(helpClickedCount, 1)
    }
}
