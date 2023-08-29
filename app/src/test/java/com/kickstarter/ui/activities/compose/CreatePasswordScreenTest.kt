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
import com.kickstarter.ui.activities.compose.login.CreatePasswordScreen
import com.kickstarter.ui.activities.compose.login.CreatePasswordScreenTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class CreatePasswordScreenTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val backButton =
        composeTestRule.onNodeWithTag(CreatePasswordScreenTestTag.BACK_BUTTON.name)
    private val pageTitle =
        composeTestRule.onNodeWithTag(CreatePasswordScreenTestTag.PAGE_TITLE.name)
    private val saveButton =
        composeTestRule.onNodeWithTag(CreatePasswordScreenTestTag.SAVE_BUTTON.name)
    private val saveImage =
        composeTestRule.onNodeWithTag(
            CreatePasswordScreenTestTag.SAVE_IMAGE.name,
            useUnmergedTree = true
        )
    private val progressBar =
        composeTestRule.onNodeWithTag(CreatePasswordScreenTestTag.PROGRESS_BAR.name)
    private val pageDisclaimer =
        composeTestRule.onNodeWithTag(CreatePasswordScreenTestTag.PAGE_DISCLAIMER.name)
    private val newPasswordEditText =
        composeTestRule.onNodeWithTag(CreatePasswordScreenTestTag.NEW_PASSWORD_EDIT_TEXT.name)
    private val confirmPasswordEditText =
        composeTestRule.onNodeWithTag(CreatePasswordScreenTestTag.CONFIRM_PASSWORD_EDIT_TEXT.name)
    private val warningText =
        composeTestRule.onNodeWithTag(CreatePasswordScreenTestTag.WARNING_TEXT.name)

    @Test
    fun testComponentsVisible() {
        composeTestRule.setContent {
            KSTheme {
                CreatePasswordScreen(
                    onBackClicked = {},
                    onAcceptButtonClicked = {},
                    showProgressBar = false,
                    scaffoldState = rememberScaffoldState()
                )
            }
        }

        backButton.assertIsDisplayed()

        val titleText = context.resources.getString(R.string.Create_password)
        pageTitle.assertTextEquals(titleText)
        pageTitle.assertIsDisplayed()

        saveButton.assertIsDisplayed()
        saveButton.assertIsNotEnabled()
        saveImage.assertIsDisplayed()
        progressBar.assertDoesNotExist()

        val pageDisclaimerText =
            context.resources.getString(R.string.Well_ask_you_to_sign_back_into_the_Kickstarter_app_once_youve_changed_your_password)
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
                CreatePasswordScreen(
                    onBackClicked = {},
                    onAcceptButtonClicked = {},
                    showProgressBar = false,
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
                CreatePasswordScreen(
                    onBackClicked = { backClickedCount++ },
                    onAcceptButtonClicked = { acceptButtonClickedCount++ },
                    showProgressBar = false,
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