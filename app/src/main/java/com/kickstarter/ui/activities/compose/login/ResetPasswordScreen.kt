package com.kickstarter.ui.activities.compose.login

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isEmail
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTextInput
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.toolbars.compose.TopToolBar

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ResetPasswordScreenPreview() {
    KSTheme {
        ResetPasswordScreen(
            scaffoldState = rememberScaffoldState(),
            hintText = "Hey, this is some text that could tell you something",
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

enum class ResetPasswordTestTag {
    PAGE_TITLE,
    BACK_BUTTON,
    OPTIONS_ICON,
    PROGRESS_BAR,
    HINT_TEXT,
    EMAIL,
    RESET_PASSWORD_BUTTON
}

@Composable
fun ResetPasswordScreen(
    scaffoldState: ScaffoldState,
    title: String = stringResource(id = R.string.forgot_password_title),
    hintText: String = "",
    initialEmail: String = "",
    onBackClicked: () -> Unit,
    onTermsOfUseClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onCookiePolicyClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    onResetPasswordButtonClicked: (String) -> Unit,
    resetButtonEnabled: Boolean,
    showProgressBar: Boolean
) {

    var emailInput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopToolBar(
                title = title,
                titleColor = KSTheme.colors.kds_support_700,
                titleModifier = Modifier.testTag(ResetPasswordTestTag.PAGE_TITLE.name),
                leftOnClickAction = onBackClicked,
                leftIconColor = KSTheme.colors.kds_support_700,
                leftIconModifier = Modifier.testTag(ResetPasswordTestTag.BACK_BUTTON.name),
                backgroundColor = KSTheme.colors.kds_white,
                right = {
                    IconButton(
                        modifier = Modifier.testTag(ResetPasswordTestTag.OPTIONS_ICON.name),
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
                    KSErrorSnackbar(text = data.message)
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = showProgressBar) {
                KSLinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(ResetPasswordTestTag.PROGRESS_BAR.name)
                )
            }

            Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingLarge))

            if (hintText.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(
                            PaddingValues(
                                start = KSTheme.dimensions.paddingLarge,
                                end = KSTheme.dimensions.paddingLarge
                            )
                        )
                        .testTag(ResetPasswordTestTag.HINT_TEXT.name),
                    text = hintText,
                    style = KSTheme.typography.body2,
                    color = KSTheme.colors.kds_support_700
                )

                Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingLarge))
            }

            KSTextInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        PaddingValues(
                            start = KSTheme.dimensions.paddingLarge,
                            end = KSTheme.dimensions.paddingLarge
                        )
                    )
                    .testTag(ResetPasswordTestTag.EMAIL.name),
                label = stringResource(id = R.string.Email),
                initialValue = initialEmail,
                onValueChanged = { value ->
                    emailInput = value
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                })
            )

            Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingLarge))

            KSPrimaryGreenButton(
                modifier = Modifier
                    .padding(
                        PaddingValues(
                            start = KSTheme.dimensions.paddingLarge,
                            end = KSTheme.dimensions.paddingLarge
                        )
                    )
                    .testTag(ResetPasswordTestTag.RESET_PASSWORD_BUTTON.name),
                onClickAction = { onResetPasswordButtonClicked.invoke(emailInput) },
                text = stringResource(id = R.string.forgot_password_buttons_reset_my_password),
                isEnabled = emailInput.isEmail() && resetButtonEnabled
            )
        }
    }
}
