package com.kickstarter.ui.activities.compose

import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.activities.compose.login.LoginDropdownTestTag
import com.kickstarter.ui.activities.compose.login.ResetPasswordScreen
import com.kickstarter.ui.activities.compose.login.ResetPasswordTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class ResetPasswordScreenTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val pageTitle = composeTestRule.onNodeWithTag(ResetPasswordTestTag.PAGE_TITLE.name)
    private val backButton = composeTestRule.onNodeWithTag(ResetPasswordTestTag.BACK_BUTTON.name)
    private val optionsIcon = composeTestRule.onNodeWithTag(ResetPasswordTestTag.OPTIONS_ICON.name)
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
    private val progressBar = composeTestRule.onNodeWithTag(ResetPasswordTestTag.PROGRESS_BAR.name)
    private val hintText = composeTestRule.onNodeWithTag(ResetPasswordTestTag.HINT_TEXT.name)
    private val email = composeTestRule.onNodeWithTag(ResetPasswordTestTag.EMAIL.name)
    private val resetPasswordButton =
        composeTestRule.onNodeWithTag(ResetPasswordTestTag.RESET_PASSWORD_BUTTON.name)

    @Test
    fun testBlankScreen() {
        composeTestRule.setContent {
            KSTheme {
                ResetPasswordScreen(
                    scaffoldState = rememberScaffoldState(),
                    onBackClicked = { },
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    onResetPasswordButtonClicked = { },
                    resetButtonEnabled = true,
                    showProgressBar = false
                )
            }
        }
        val titleString = context.getString(R.string.forgot_password_title)
        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(titleString)

        backButton.assertIsDisplayed()
        optionsIcon.assertIsDisplayed()
        optionsMenu.assertDoesNotExist()
        optionsTerms.assertDoesNotExist()
        optionsPrivacyPolicy.assertDoesNotExist()
        optionsCookie.assertDoesNotExist()
        optionsHelp.assertDoesNotExist()
        progressBar.assertDoesNotExist()
        hintText.assertDoesNotExist()
        email.assertIsDisplayed()
        resetPasswordButton.assertIsDisplayed()
        resetPasswordButton.assertIsNotEnabled()
    }

    @Test
    fun testTopBarAndMenu() {
        var backCLickedCount = 0
        var termsClickedCount = 0
        var privacyClickedCount = 0
        var cookieClickedCount = 0
        var helpClickedCount = 0

        composeTestRule.setContent {
            KSTheme {
                ResetPasswordScreen(
                    scaffoldState = rememberScaffoldState(),
                    onBackClicked = { backCLickedCount++ },
                    onTermsOfUseClicked = { termsClickedCount++ },
                    onPrivacyPolicyClicked = { privacyClickedCount++ },
                    onCookiePolicyClicked = { cookieClickedCount++ },
                    onHelpClicked = { helpClickedCount++ },
                    onResetPasswordButtonClicked = { },
                    resetButtonEnabled = true,
                    showProgressBar = false
                )
            }
        }

        backButton.performClick()
        assertEquals(backCLickedCount, 1)

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

    @Test
    fun testInitializedScreen() {
        composeTestRule.setContent {
            KSTheme {
                ResetPasswordScreen(
                    scaffoldState = rememberScaffoldState(),
                    title = "TestTitle",
                    hintText = "TestHint",
                    initialEmail = "test@test.test",
                    onBackClicked = { },
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    onResetPasswordButtonClicked = { },
                    resetButtonEnabled = true,
                    showProgressBar = false
                )
            }
        }

        pageTitle.assertTextEquals("TestTitle")
        hintText.assertIsDisplayed()
        hintText.assertTextEquals("TestHint")
        assertEquals(
            email.fetchSemanticsNode().config[SemanticsProperties.EditableText].text,
            "test@test.test"
        )
    }

    @Test
    fun testButtonBehaviour() {
        var clickCount = 0
        composeTestRule.setContent {
            KSTheme {
                ResetPasswordScreen(
                    scaffoldState = rememberScaffoldState(),
                    onBackClicked = { },
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    onResetPasswordButtonClicked = { clickCount++ },
                    resetButtonEnabled = true,
                    showProgressBar = false
                )
            }
        }

        resetPasswordButton.assertIsNotEnabled()

        email.performTextInput("notAValidEmail")

        resetPasswordButton.assertIsNotEnabled()

        email.performTextClearance()
        email.performTextInput("anything@everywhere.allatonce")

        resetPasswordButton.assertIsEnabled()
        resetPasswordButton.performClick()
        assertEquals(clickCount, 1)
    }
}
