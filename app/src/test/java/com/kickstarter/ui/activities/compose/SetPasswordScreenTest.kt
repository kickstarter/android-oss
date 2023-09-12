package com.kickstarter.ui.activities.compose

import androidx.compose.material.rememberScaffoldState
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
import com.kickstarter.ui.activities.compose.login.SetPasswordScreen
import com.kickstarter.ui.activities.compose.login.SetPasswordScreenTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class SetPasswordScreenTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val pageTitle =
        composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.PAGE_TITLE.name)
    private val saveButton =
        composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.SAVE_BUTTON.name)
    private val optionsIcon =
        composeTestRule.onNodeWithTag(
            SetPasswordScreenTestTag.OPTIONS_ICON.name,
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

    private val progressBar =
        composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.PROGRESS_BAR.name)
    private val pageDisclaimer =
        composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.PAGE_DISCLAIMER.name)
    private val newPasswordEditText =
        composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.NEW_PASSWORD_EDIT_TEXT.name)
    private val confirmPasswordEditText =
        composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.CONFIRM_PASSWORD_EDIT_TEXT.name)
    private val warningText =
        composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.WARNING_TEXT.name)

    @Test
    fun testComponentsVisible() {
        composeTestRule.setContent {
            KSTheme {
                SetPasswordScreen(
                    onSaveButtonClicked = {},
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

        val titleText = context.resources.getString(R.string.Set_your_password)
        pageTitle.assertTextEquals(titleText)
        pageTitle.assertIsDisplayed()

        saveButton.assertIsDisplayed()
        saveButton.assertIsNotEnabled()
        optionsIcon.assertIsDisplayed()
        progressBar.assertDoesNotExist()

        val pageDisclaimerText = context.getString(R.string.We_will_be_discontinuing_the_ability_to_log_in_via_FB)
        pageDisclaimer.assertTextEquals(pageDisclaimerText)
        pageDisclaimer.assertIsDisplayed()

        newPasswordEditText.assertIsDisplayed()
        confirmPasswordEditText.assertIsDisplayed()
        warningText.assertDoesNotExist()
    }

    @Test
    fun testEditTextConditions() {
        composeTestRule.setContent {
            KSTheme {
                SetPasswordScreen(
                    onSaveButtonClicked = {},
                    showProgressBar = false,
                    headline = "test@test.com",
                    isFormSubmitting = false,
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        // Nothing entered, save button and warning text should be disabled/not shown
        saveButton.assertIsNotEnabled()
        warningText.assertDoesNotExist()

        // If both match save button should be enabled, no warning text
        newPasswordEditText.performTextInput("password")
        confirmPasswordEditText.performTextInput("password")
        saveButton.assertIsEnabled()
        warningText.assertDoesNotExist()

        newPasswordEditText.performTextClearance()
        confirmPasswordEditText.performTextClearance()

        // If the new password field is less than 6 characters, warning is shown, button disabled
        newPasswordEditText.performTextInput("passw")
        saveButton.assertIsNotEnabled()
        val warningTextText1 = context.resources.getString(R.string.Password_min_length_message)
        warningText.assertTextEquals(warningTextText1)
        warningText.assertIsDisplayed()

        newPasswordEditText.performTextClearance()
        confirmPasswordEditText.performTextClearance()

        // If the new password field has a valid password, but the confirm does not match warning is
        // shown, save button is disabled
        newPasswordEditText.performTextInput("password")
        confirmPasswordEditText.performTextInput("passwo")
        saveButton.assertIsNotEnabled()
        val warningTextText2 = context.resources.getString(R.string.Passwords_matching_message)
        warningText.assertTextEquals(warningTextText2)
        warningText.assertIsDisplayed()
    }

    @Test
    fun testDropDownButtonClicks() {
        var termsClickedCount = 0
        var privacyClickedCount = 0
        var cookieClickedCount = 0
        var helpClickedCount = 0

        composeTestRule.setContent {
            KSTheme {
                SetPasswordScreen(
                    onSaveButtonClicked = { },
                    showProgressBar = false,
                    headline = "test@test.com",
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

    @Test
    fun testSetPasswordButtonClicks() {
        var acceptButtonClickedCount = 0

        composeTestRule.setContent {
            KSTheme {
                SetPasswordScreen(
                    onSaveButtonClicked = { acceptButtonClickedCount++ },
                    showProgressBar = false,
                    headline = "test@test.com",
                    isFormSubmitting = false,
                    onTermsOfUseClicked = { },
                    onPrivacyPolicyClicked = { },
                    onCookiePolicyClicked = { },
                    onHelpClicked = { },
                    scaffoldState = rememberScaffoldState()
                )
            }
        }
        newPasswordEditText.performTextInput("this_is_a_passw")
        confirmPasswordEditText.performTextInput("this_is_a_password")
        saveButton.assertIsNotEnabled()

        newPasswordEditText.performTextClearance()
        confirmPasswordEditText.performTextClearance()

        newPasswordEditText.performTextInput("this_is_a_password")
        confirmPasswordEditText.performTextInput("this_is_a_password")
        saveButton.assertIsEnabled()
        saveButton.performClick()
        assertEquals(acceptButtonClickedCount, 1)
    }
}
