package com.kickstarter.ui.activities.compose.login

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.Divider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.validPassword
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHiddenTextInput
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.toolbars.compose.TopToolBar

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SetPasswordPreview() {
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

enum class SetPasswordScreenTestTag {
    PAGE_TITLE,
    SAVE_BUTTON,
    OPTIONS_ICON,
    PROGRESS_BAR,
    PAGE_DISCLAIMER,
    NEW_PASSWORD_EDIT_TEXT,
    CONFIRM_PASSWORD_EDIT_TEXT,
    WARNING_TEXT,
}

@Composable
fun SetPasswordScreen(
    onSaveButtonClicked: (newPass: String) -> Unit,
    showProgressBar: Boolean,
    headline: String? = stringResource(id = R.string.We_will_be_discontinuing_the_ability_to_log_in_via_FB),
    isFormSubmitting: Boolean,
    onTermsOfUseClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onCookiePolicyClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    scaffoldState: ScaffoldState
) {

    var expanded by remember { mutableStateOf(false) }
    var newPasswordLine1 by rememberSaveable { mutableStateOf("") }
    var newPasswordLine2 by rememberSaveable { mutableStateOf("") }

    val acceptButtonEnabled = when {
        !isFormSubmitting &&
            newPasswordLine1.validPassword() &&
            newPasswordLine2.validPassword() &&
            newPasswordLine1 == newPasswordLine2 -> true

        else -> false
    }

    val warningText = when {
        newPasswordLine1.validPassword() &&
            newPasswordLine2.isNotEmpty() &&
            newPasswordLine2 != newPasswordLine1 -> {
            stringResource(id = R.string.Passwords_matching_message)
        }

        newPasswordLine1.isNotEmpty() && newPasswordLine1.length < 6 -> {
            stringResource(id = R.string.Password_min_length_message)
        }

        else -> ""
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        scaffoldState = scaffoldState,
        topBar = {
            TopToolBar(
                title = stringResource(id = R.string.Set_your_password),
                titleColor = KSTheme.colors.kds_support_700,
                titleModifier = Modifier.testTag(SetPasswordScreenTestTag.PAGE_TITLE.name),
                leftIcon = null,
                leftOnClickAction = null,
                backgroundColor = KSTheme.colors.kds_white,
                right = {
                    IconButton(
                        modifier = Modifier.testTag(SetPasswordScreenTestTag.OPTIONS_ICON.name),
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
        }
    ) { padding ->
        Column(
            Modifier
                .background(KSTheme.colors.kds_white)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            AnimatedVisibility(visible = showProgressBar) {
                KSLinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SetPasswordScreenTestTag.PROGRESS_BAR.name)
                )
            }
            if (headline != null) {
                Text(
                    modifier = Modifier
                        .padding(KSTheme.dimensions.paddingMedium)
                        .testTag(SetPasswordScreenTestTag.PAGE_DISCLAIMER.name),
                    text = headline,
                    style = KSTheme.typography.body2,
                    color = KSTheme.colors.kds_support_700
                )
            }

            Column(
                Modifier
                    .background(color = KSTheme.colors.kds_white)
                    .padding(KSTheme.dimensions.paddingMedium)
                    .fillMaxWidth()
            ) {
                KSHiddenTextInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SetPasswordScreenTestTag.NEW_PASSWORD_EDIT_TEXT.name),
                    onValueChanged = { newPasswordLine1 = it },
                    label = stringResource(id = R.string.New_password),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(
                                focusDirection = FocusDirection.Down
                            )
                        }
                    )
                )

                Spacer(modifier = Modifier.height(KSTheme.dimensions.listItemSpacingMedium))

                KSHiddenTextInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SetPasswordScreenTestTag.CONFIRM_PASSWORD_EDIT_TEXT.name),
                    onValueChanged = { newPasswordLine2 = it },
                    label = stringResource(id = R.string.Confirm_password),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                )

                Divider(color = KSTheme.colors.kds_support_300)

                AnimatedVisibility(visible = warningText.isNotEmpty()) {
                    Text(
                        modifier = Modifier
                            .padding(KSTheme.dimensions.paddingMedium)
                            .testTag(SetPasswordScreenTestTag.WARNING_TEXT.name),
                        text = warningText,
                        style = KSTheme.typography.body2,
                        color = KSTheme.colors.kds_support_700
                    )
                }

                Spacer(modifier = Modifier.height(KSTheme.dimensions.minButtonHeight))

                KSPrimaryGreenButton(
                    modifier = Modifier.testTag(SetPasswordScreenTestTag.SAVE_BUTTON.name),
                    text = stringResource(id = R.string.Save),
                    onClickAction = { onSaveButtonClicked.invoke(newPasswordLine1) },
                    isEnabled = acceptButtonEnabled
                )
            }
        }
    }
}
