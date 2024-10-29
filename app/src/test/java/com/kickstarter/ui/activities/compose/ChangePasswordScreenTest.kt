package com.kickstarter.ui.activities.compose

import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class ChangePasswordScreenTest : KSRobolectricTestCase() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val backButton =
        composeTestRule.onNodeWithTag(ChangePasswordScreenTestTag.BACK_BUTTON.name)

    private val acceptButton =
        composeTestRule.onNodeWithTag(ChangePasswordScreenTestTag.ACCEPT_BUTTON.name)

    private val currentPasswordEditText =
        composeTestRule.onNodeWithTag(ChangePasswordScreenTestTag.CURRENT_PASSWORD.name)

    private val newPasswordLine1EditText =
        composeTestRule.onNodeWithTag(ChangePasswordScreenTestTag.NEW_PASSWORD_1.name)

    private val newPasswordLine2EditText =
        composeTestRule.onNodeWithTag(ChangePasswordScreenTestTag.NEW_PASSWORD_2.name)

    private val warningText =
        composeTestRule.onNodeWithTag(ChangePasswordScreenTestTag.WARNING_TEXT.name)

    private val pageTitle =
        composeTestRule.onNodeWithTag(ChangePasswordScreenTestTag.PAGE_TITLE.name)

    private val progressBar = composeTestRule.onNodeWithTag(ChangePasswordScreenTestTag.PROGRESS_BAR.name)

    private val subtitle = composeTestRule.onNodeWithTag(ChangePasswordScreenTestTag.SUBTITLE.name)

    @Test
    fun `test screen init`() {
        composeTestRule.setContent {
            KSTheme {
                ChangePasswordScreen(
                    onBackClicked = { },
                    onAcceptButtonClicked = { _, _ -> },
                    showProgressBar = false,
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        val pageTitleText = context.getString(R.string.Change_password)
        val changePasswordDescriptionText = context.getString(R.string.Well_ask_you_to_sign_back_into_the_Kickstarter_app_once_youve_changed_your_password)

        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(pageTitleText)
        subtitle.assertIsDisplayed()
        subtitle.assertTextEquals(changePasswordDescriptionText)
        backButton.assertIsDisplayed()
        acceptButton.assertIsDisplayed()
        acceptButton.assertIsNotEnabled()
        progressBar.assertDoesNotExist()
        newPasswordLine1EditText.assertIsDisplayed()
        newPasswordLine2EditText.assertIsDisplayed()
        currentPasswordEditText.assertIsDisplayed()
        warningText.isNotDisplayed()
    }

    @Test
    fun `test back button clicks`() {
        composeTestRule.setContent {
            KSTheme {
                ChangePasswordScreen(
                    onBackClicked = { },
                    onAcceptButtonClicked = { _, _ -> },
                    showProgressBar = false,
                    scaffoldState = rememberScaffoldState()
                )
            }
        }
    }

    @Test
    fun `when passwords not long enough or matching, accept button disabled and warning text displayed`() {
        composeTestRule.setContent {
            KSTheme {
                ChangePasswordScreen(
                    onBackClicked = { },
                    onAcceptButtonClicked = { _, _ -> },
                    showProgressBar = false,
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        val passwordMismatchWarningText = context.getString(R.string.Passwords_matching_message)
        val passwordLengthWarningText = context.getString(R.string.Password_min_length_message)

        // current password too short, button should be disabled but no warning text
        currentPasswordEditText.performTextInput("pass")
        newPasswordLine1EditText.performTextInput("password1")
        newPasswordLine2EditText.performTextInput("password1")

        acceptButton.assertIsNotEnabled()
        warningText.isNotDisplayed()

        currentPasswordEditText.performTextClearance()
        newPasswordLine1EditText.performTextClearance()
        newPasswordLine2EditText.performTextClearance()

        // only one new password field filled out, button should be disabled but no warning text
        currentPasswordEditText.performTextInput("password")
        newPasswordLine1EditText.performTextInput("password1")

        acceptButton.assertIsNotEnabled()
        warningText.isNotDisplayed()

        currentPasswordEditText.performTextClearance()
        newPasswordLine1EditText.performTextClearance()
        newPasswordLine2EditText.performTextClearance()

        // new passwords not long enough, button should be disabled and no warning text
        currentPasswordEditText.performTextInput("password")
        newPasswordLine1EditText.performTextInput("pass")
        newPasswordLine2EditText.performTextInput("password2")

        acceptButton.assertIsNotEnabled()
        warningText.isDisplayed()
        warningText.assertTextEquals(passwordLengthWarningText)

        currentPasswordEditText.performTextClearance()
        newPasswordLine1EditText.performTextClearance()
        newPasswordLine2EditText.performTextClearance()

        // passwords are long enough but don't match, button should be disabled and warning text shows
        currentPasswordEditText.performTextInput("password")
        newPasswordLine1EditText.performTextInput("password1")
        newPasswordLine2EditText.performTextInput("password2")

        acceptButton.assertIsNotEnabled()
        warningText.isDisplayed()
        warningText.assertTextEquals(passwordMismatchWarningText)
    }

    @Test
    fun `when passwords valid and matching, accept button enabled and no warning text visible`() {
        var acceptButtonClickedCount = 0
        composeTestRule.setContent {
            KSTheme {
                ChangePasswordScreen(
                    onBackClicked = { },
                    onAcceptButtonClicked = { _, _ -> acceptButtonClickedCount++ },
                    showProgressBar = false,
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        // valid password and matching
        currentPasswordEditText.performTextInput("password")
        newPasswordLine1EditText.performTextInput("passwordA")
        newPasswordLine2EditText.performTextInput("passwordA")

        acceptButton.assertIsEnabled()
        warningText.isNotDisplayed()
        acceptButton.performClick()
        assertEquals(1, acceptButtonClickedCount)
    }
}
