package com.kickstarter.ui.activities.compose

import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kickstarter.ui.activities.compose.login.LoginScreen
import com.kickstarter.ui.activities.compose.login.LoginTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class LoginScreenTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

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
    private val pageTitle = composeTestRule.onNodeWithTag(LoginTestTag.PAGE_TITLE.name)
    private val backButton = composeTestRule.onNodeWithTag(LoginTestTag.BACK_BUTTON.name)
    private val optionsIcon = composeTestRule.onNodeWithTag(LoginTestTag.OPTIONS_ICON.name)
    private val loading = composeTestRule.onNodeWithTag(LoginTestTag.LOADING.name)
    private val email = composeTestRule.onNodeWithTag(LoginTestTag.EMAIL.name)
    private val password = composeTestRule.onNodeWithTag(LoginTestTag.PASSWORD.name)
    private val loginButton = composeTestRule.onNodeWithTag(LoginTestTag.LOGIN_BUTTON.name)
    private val forgotPasswordText =
        composeTestRule.onNodeWithTag(LoginTestTag.FORGOT_PASSWORD_TEXT.name)

    @Test
    fun testComponentsVisible() {
        composeTestRule.setContent {
            KSTheme {
                LoginScreen(
                    scaffoldState = rememberScaffoldState(),
                    isLoading = false,
                    onBackClicked = {  },
                    onLoginClicked = { _, _ -> },
                    onTermsOfUseClicked = {  },
                    onPrivacyPolicyClicked = {  },
                    onCookiePolicyClicked = {  },
                    onHelpClicked = {  },
                    onForgotPasswordClicked = {  },
                    resetPasswordDialogMessage = "",
                    showDialog = false,
                    setShowDialog = { _ -> }
                )
            }
        }

        pageTitle.assertIsDisplayed()
        val titleText = context.getString(R.string.login_navbar_title)
        pageTitle.assertTextEquals(titleText)

        backButton.assertIsDisplayed()
        optionsIcon.assertIsDisplayed()
        optionsMenu.assertDoesNotExist()
        loading.assertDoesNotExist()
        email.assertIsDisplayed()
        password.assertIsDisplayed()
        loginButton.assertIsDisplayed()
        loginButton.assertIsNotEnabled()

        forgotPasswordText.assertIsDisplayed()
        val forgotPasswordString = context.getString(R.string.forgot_password_title)
        forgotPasswordText.assertTextEquals(forgotPasswordString)
    }

    @Test
    fun testLoginButtonEnable() {
        composeTestRule.setContent {
            KSTheme {
                LoginScreen(
                    scaffoldState = rememberScaffoldState(),
                    isLoading = false,
                    onBackClicked = {  },
                    onLoginClicked = { _, _ -> },
                    onTermsOfUseClicked = {  },
                    onPrivacyPolicyClicked = {  },
                    onCookiePolicyClicked = {  },
                    onHelpClicked = {  },
                    onForgotPasswordClicked = {  },
                    resetPasswordDialogMessage = "",
                    showDialog = false,
                    setShowDialog = { _ -> }
                )
            }
        }

        loginButton.assertIsNotEnabled()

        email.performTextInput("notAvalidEmail")
        password.performTextInput("12345")

        loginButton.assertIsNotEnabled()
        email.performTextClearance()
        password.performTextClearance()

        email.performTextInput("valid@email.com")
        password.performTextInput("123456")

        loginButton.assertIsEnabled()
    }

    @Test
    fun testClickableObjects() {
        var termsClickedCount = 0
        var privacyClickedCount = 0
        var cookieClickedCount = 0
        var helpClickedCount = 0
        var backClickedCount = 0
        var forgotPasswordClickedCount = 0
        composeTestRule.setContent {
            KSTheme {
                LoginScreen(
                    scaffoldState = rememberScaffoldState(),
                    isLoading = false,
                    onBackClicked = { backClickedCount++ },
                    onLoginClicked = { _, _ -> },
                    onTermsOfUseClicked = { termsClickedCount++ },
                    onPrivacyPolicyClicked = { privacyClickedCount++ },
                    onCookiePolicyClicked = { cookieClickedCount++ },
                    onHelpClicked = { helpClickedCount++ },
                    onForgotPasswordClicked = { forgotPasswordClickedCount++ },
                    resetPasswordDialogMessage = "",
                    showDialog = false,
                    setShowDialog = { }
                )
            }
        }

        backButton.performClick()
        assertEquals(backClickedCount, 1)

        optionsIcon.performClick()
        optionsMenu.assertIsDisplayed()
        optionsTerms.assertIsDisplayed()
        optionsPrivacyPolicy.assertIsDisplayed()
        optionsCookie.assertIsDisplayed()
        optionsHelp.assertIsDisplayed()

        optionsTerms.performClick()
        assertEquals(termsClickedCount, 1)
        optionsMenu.assertDoesNotExist()

        optionsIcon.performClick()
        optionsPrivacyPolicy.performClick()
        assertEquals(privacyClickedCount, 1)
        optionsMenu.assertDoesNotExist()

        optionsIcon.performClick()
        optionsCookie.performClick()
        assertEquals(cookieClickedCount, 1)
        optionsMenu.assertDoesNotExist()

        optionsIcon.performClick()
        optionsHelp.performClick()
        assertEquals(helpClickedCount, 1)
        optionsMenu.assertDoesNotExist()

        forgotPasswordText.performClick()
        assertEquals(forgotPasswordClickedCount, 1)
    }

    @Test
    fun testLoadingDisplay() {
        composeTestRule.setContent {
            KSTheme {
                var isloading by remember { mutableStateOf(false) }
                LoginScreen(
                    scaffoldState = rememberScaffoldState(),
                    isLoading = isloading,
                    onBackClicked = { isloading = !isloading },
                    onLoginClicked = { _, _ -> },
                    onTermsOfUseClicked = {  },
                    onPrivacyPolicyClicked = {  },
                    onCookiePolicyClicked = {  },
                    onHelpClicked = {  },
                    onForgotPasswordClicked = {  },
                    resetPasswordDialogMessage = "",
                    showDialog = false,
                    setShowDialog = {}
                )
            }
        }

        loading.assertDoesNotExist()
        backButton.performClick()
        loading.assertIsDisplayed()
        backButton.performClick()
        loading.assertDoesNotExist()
    }
}
