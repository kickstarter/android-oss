package com.kickstarter.ui.activities.compose.login

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isEmail
import com.kickstarter.libs.utils.extensions.isValidPassword
import com.kickstarter.ui.compose.designsystem.KSAlertDialogNoHeadline
import com.kickstarter.ui.compose.designsystem.KSClickableText
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHiddenTextInput
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSuccessSnackbar
import com.kickstarter.ui.compose.designsystem.KSTextInput
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.toolbars.compose.TopToolBar

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun LoginScreenPreview() {
    KSTheme {
        LoginScreen(
            "",
            rememberScaffoldState(),
            false,
            {},
            { _, _ -> },
            {},
            {},
            {},
            {},
            {},
            "",
            false,
            {}
        )
    }
}

@Composable
fun LoginScreen(
    prefillEmail: String = "",
    scaffoldState: ScaffoldState,
    isLoading: Boolean,
    onBackClicked: () -> Unit,
    onLoginClicked: (email: String, password: String) -> Unit,
    onTermsOfUseClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onCookiePolicyClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    resetPasswordDialogMessage: String,
    showDialog: Boolean,
    setShowDialog: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var logInButtonEnabled = when {
        email.isEmail() && password.isValidPassword() -> true
        else -> false
    }

    Scaffold(
        topBar = {
            TopToolBar(
                title = stringResource(id = R.string.login_navbar_title),
                titleColor = KSTheme.colors.kds_support_700,
                titleModifier = Modifier.testTag(LoginToutTestTag.PAGE_TITLE.name),
                leftOnClickAction = onBackClicked,
                leftIconColor = KSTheme.colors.kds_support_700,
                leftIconModifier = Modifier.testTag(LoginToutTestTag.BACK_BUTTON.name),
                backgroundColor = KSTheme.colors.kds_white,
                right = {
                    IconButton(
                        modifier = Modifier.testTag(LoginToutTestTag.OPTIONS_ICON.name),
                        onClick = { expanded = !expanded },
                        enabled = true
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(
                                    id = R.string.general_navigation_accessibility_button_help_menu_label
                                ),
                                tint = KSTheme.colors.kds_black
                            )

                            KSLoginDropdownMenu(
                                expanded = expanded,
                                onDismissed = { expanded = !expanded },
                                onTermsOfUseClicked = onTermsOfUseClicked,
                                onPrivacyPolicyClicked = onPrivacyPolicyClicked,
                                onCookiePolicyClicked = onCookiePolicyClicked,
                                onHelpClicked = onHelpClicked
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = scaffoldState.snackbarHostState,
                snackbar = { data ->
                    if (data.actionLabel == "error") {
                        KSErrorSnackbar(text = data.message)
                    } else {
                        KSSuccessSnackbar(text = data.message)
                    }
                }
            )
        },
        scaffoldState = scaffoldState
    ) { padding ->
        Column(
            Modifier
                .background(KSTheme.colors.kds_white)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    PaddingValues(
                        start = dimensions.paddingLarge,
                        end = dimensions.paddingLarge
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                KSLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            KSTextInput(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.email),
                initialValue = prefillEmail,
                onValueChanged = { email = it }
            )

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            KSHiddenTextInput(
                Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.login_placeholder_password),
                onValueChanged = { password = it }
            )

            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

            KSPrimaryGreenButton(
                onClickAction = { onLoginClicked(email, password) },
                text = stringResource(id = R.string.login_buttons_log_in),
                isEnabled = logInButtonEnabled && !isLoading
            )

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            KSClickableText(
                resourceId = R.string.forgot_password_title,
                clickCallback = onForgotPasswordClicked
            )

            if (showDialog) {
                KSAlertDialogNoHeadline(
                    setShowDialog = setShowDialog,
                    bodyText = resetPasswordDialogMessage,
                    rightButtonText = stringResource(id = R.string.login_errors_button_ok)
                )
            }
        }
    }
}
