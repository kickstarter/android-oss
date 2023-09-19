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
import com.kickstarter.ui.activities.compose.login.TwoFactorScreen
import com.kickstarter.ui.activities.compose.login.TwoFactorScreenTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class TwoFactorScreenTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val backButton = composeTestRule.onNodeWithTag(TwoFactorScreenTestTag.BACK_BUTTON.name)
    private val pageTitle =
        composeTestRule.onNodeWithTag(TwoFactorScreenTestTag.PAGE_TITLE.name)
    private val optionsIcon =
        composeTestRule.onNodeWithTag(
            TwoFactorScreenTestTag.OPTIONS_ICON.name,
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

    private val loading = composeTestRule.onNodeWithTag(TwoFactorScreenTestTag.LOADING.name)
    private val headline = composeTestRule.onNodeWithTag(TwoFactorScreenTestTag.HEADLINE.name)
    private val code = composeTestRule.onNodeWithTag(TwoFactorScreenTestTag.CODE.name)
    private val resend = composeTestRule.onNodeWithTag(TwoFactorScreenTestTag.RESEND.name)
    private val submit = composeTestRule.onNodeWithTag(TwoFactorScreenTestTag.SUBMIT.name)

    @Test
    fun testComponentsVisible() {
        composeTestRule.setContent {
            KSTheme {
                TwoFactorScreen(
                    scaffoldState = rememberScaffoldState(),
                    onBackClicked = {},
                    onTermsOfUseClicked = {},
                    onPrivacyPolicyClicked = {},
                    onCookiePolicyClicked = {},
                    onHelpClicked = {},
                    onResendClicked = {},
                    onSubmitClicked = {},
                    isLoading = false
                )
            }
        }

        backButton.assertIsDisplayed()

        val titleText = context.getString(R.string.two_factor_title)
        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(titleText)

        optionsIcon.assertIsDisplayed()
        optionsMenu.assertDoesNotExist()
        loading.assertDoesNotExist()

        val headlineText = context.getString(R.string.two_factor_message)
        headline.assertIsDisplayed()
        headline.assertTextEquals(headlineText)

        code.assertIsDisplayed()
        resend.assertIsDisplayed()
        submit.assertIsDisplayed()
        submit.assertIsNotEnabled()
    }

    @Test
    fun testSubmitEnabledCondition() {
        composeTestRule.setContent {
            KSTheme {
                TwoFactorScreen(
                    scaffoldState = rememberScaffoldState(),
                    onBackClicked = {},
                    onTermsOfUseClicked = {},
                    onPrivacyPolicyClicked = {},
                    onCookiePolicyClicked = {},
                    onHelpClicked = {},
                    onResendClicked = {},
                    onSubmitClicked = {},
                    isLoading = false
                )
            }
        }

        submit.assertIsNotEnabled()
        code.performTextInput("testing")
        submit.assertIsEnabled()
        code.performTextClearance()
        submit.assertIsNotEnabled()
    }

    @Test
    fun testClickActions() {
        var backClickedCount = 0
        var termsClickedCount = 0
        var privacyClickedCount = 0
        var cookieClickedCount = 0
        var helpClickedCount = 0
        var resendClickedCount = 0
        var submitClickedCount = 0
        composeTestRule.setContent {
            KSTheme {
                TwoFactorScreen(
                    scaffoldState = rememberScaffoldState(),
                    onBackClicked = { backClickedCount++ },
                    onTermsOfUseClicked = { termsClickedCount++ },
                    onPrivacyPolicyClicked = { privacyClickedCount++ },
                    onCookiePolicyClicked = { cookieClickedCount++ },
                    onHelpClicked = { helpClickedCount++ },
                    onResendClicked = { resendClickedCount++ },
                    onSubmitClicked = { code -> submitClickedCount++ },
                    isLoading = false
                )
            }
        }

        backButton.performClick()
        assertEquals(backClickedCount, 1)

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

        resend.performClick()
        assertEquals(resendClickedCount, 1)

        code.performTextInput("anything")
        submit.performClick()
        assertEquals(submitClickedCount, 1)
    }

    @Test
    fun testLoading() {
        composeTestRule.setContent {
            var showLoading by remember { mutableStateOf(false) }
            KSTheme {
                TwoFactorScreen(
                    scaffoldState = rememberScaffoldState(),
                    onBackClicked = { showLoading = !showLoading },
                    onTermsOfUseClicked = {},
                    onPrivacyPolicyClicked = {},
                    onCookiePolicyClicked = {},
                    onHelpClicked = {},
                    onResendClicked = {},
                    onSubmitClicked = {},
                    isLoading = showLoading
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
