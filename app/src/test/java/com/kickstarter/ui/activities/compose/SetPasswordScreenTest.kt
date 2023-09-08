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
import com.kickstarter.ui.activities.compose.login.SetPasswordScreen
import com.kickstarter.ui.activities.compose.login.SetPasswordScreenTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class SetPasswordScreenTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val backButton =
            composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.BACK_BUTTON.name)
    private val pageTitle =
            composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.PAGE_TITLE.name)
    private val saveButton =
            composeTestRule.onNodeWithTag(SetPasswordScreenTestTag.SAVE_BUTTON.name)
    private val saveImage =
            composeTestRule.onNodeWithTag(
                    SetPasswordScreenTestTag.SAVE_IMAGE.name,
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
                        onBackClicked = {},
                        onAcceptButtonClicked = {},
                        showProgressBar = false,
                        email = "test@test.com",
                        isFormSubmitting = false,
                        scaffoldState = rememberScaffoldState()
                )
            }
        }

        backButton.assertIsDisplayed()

        val titleText = context.resources.getString(R.string.Set_your_password)
        pageTitle.assertTextEquals(titleText)
        pageTitle.assertIsDisplayed()

        saveButton.assertIsDisplayed()
        saveButton.assertIsNotEnabled()
        saveImage.assertIsDisplayed()
        progressBar.assertDoesNotExist()

        val pageDisclaimerText = context.getString(R.string.We_will_be_discontinuing_the_ability_to_log_in_via_FB, "test@test.com")
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
                        onBackClicked = {},
                        onAcceptButtonClicked = {},
                        showProgressBar = false,
                        email = "test@test.com",
                        isFormSubmitting = false,
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
    fun testButtonClicks() {
        var backClickedCount = 0
        var acceptButtonClickedCount = 0
        composeTestRule.setContent {
            KSTheme {
                SetPasswordScreen(
                        onBackClicked = { backClickedCount++ },
                        onAcceptButtonClicked = { acceptButtonClickedCount++ },
                        showProgressBar = false,
                        email = "test@test.com",
                        isFormSubmitting = false,
                        scaffoldState = rememberScaffoldState()
                )
            }
        }

        backButton.performClick()
        assertEquals(1, backClickedCount)

        newPasswordEditText.performTextInput("password")
        confirmPasswordEditText.performTextInput("password")
        saveButton.performClick()
        assertEquals(1, acceptButtonClickedCount)
    }
}