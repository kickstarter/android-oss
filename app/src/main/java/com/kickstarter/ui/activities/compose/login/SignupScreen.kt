package com.kickstarter.ui.activities.compose.login

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.kickstarter.libs.utils.extensions.isEmail
import com.kickstarter.libs.utils.extensions.isNotEmptyAndAtLeast6Chars
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHiddenTextInput
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSwitch
import com.kickstarter.ui.compose.designsystem.KSTextInput
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.toolbars.compose.TopToolBar


@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SignupPreview() {
    KSTheme {
        SignupScreen(
                onBackClicked = {},
                onSignupButtonClicked = {_, _, _, _-> },
                showProgressBar = true,
                isFormSubmitting = false,
                onTermsOfUseClicked = { },
                onPrivacyPolicyClicked = { },
                onCookiePolicyClicked = { },
                onHelpClicked = { },
                scaffoldState = rememberScaffoldState()
        )
    }
}
enum class SignupScreenTestTag {
    PAGE_TITLE,
    NAME_EDIT_TEXT,
    EMAIL_EDIT_TEXT,
    BACK_BUTTON,
    OPTIONS_ICON,
    PROGRESS_BAR,
    PASSWORD_EDIT_TEXT,
    NEWSLETTER_OPT_IN_TEXT,
    NEWSLETTER_OPT_IN_SWITCH,
    SIGNUP_BUTTON
}

@Composable
fun SignupScreen(
        onBackClicked : () -> Unit,
        onSignupButtonClicked: (name: String, email: String, password: String, receiveNewsLetter: Boolean) -> Unit,
        showProgressBar: Boolean,
        isFormSubmitting: Boolean,
        onTermsOfUseClicked: () -> Unit,
        onPrivacyPolicyClicked: () -> Unit,
        onCookiePolicyClicked: () -> Unit,
        onHelpClicked: () -> Unit,
        scaffoldState: ScaffoldState
) {

    var expanded by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }

    var password by rememberSaveable { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    var receiveNewsletterChecked by remember { mutableStateOf(false) }

    val signUpButtonEnabled = when {
        name.isNotEmpty() &&
                email.isEmail() &&
                !isFormSubmitting &&
                password.isNotEmptyAndAtLeast6Chars() -> true

        else -> false
    }

    Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopToolBar(
                        title = stringResource(id = R.string.Sign_up),
                        titleColor = KSTheme.colors.kds_support_700,
                        titleModifier = Modifier.testTag(SignupScreenTestTag.PAGE_TITLE.name),
                        leftOnClickAction = onBackClicked,
                        leftIconColor = KSTheme.colors.kds_support_700,
                        leftIconModifier = Modifier.testTag(SignupScreenTestTag.BACK_BUTTON.name),
                        backgroundColor = KSTheme.colors.kds_white,
                        right = {
                            IconButton(
                                    modifier = Modifier.testTag(SignupScreenTestTag.OPTIONS_ICON.name),
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
                                .testTag(SignupScreenTestTag.PROGRESS_BAR.name)
                )
            }

            Column(
                    Modifier
                            .background(color = KSTheme.colors.kds_white)
                            .padding(KSTheme.dimensions.paddingMedium)
                            .fillMaxWidth()

            ) {
                KSTextInput(
                        modifier = Modifier
                                .fillMaxWidth()
                                .testTag(SignupScreenTestTag.NAME_EDIT_TEXT.name),
                        onValueChanged = { name = it },
                        label = stringResource(id = R.string.Name),
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

                KSTextInput(
                        modifier = Modifier
                                .fillMaxWidth()
                                .testTag(SignupScreenTestTag.EMAIL_EDIT_TEXT.name),
                        onValueChanged = { email = it },
                        label = stringResource(id = R.string.login_placeholder_email),
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
                                .testTag(SignupScreenTestTag.PASSWORD_EDIT_TEXT.name),
                        onValueChanged = { password = it },
                        label = stringResource(id = R.string.signup_input_fields_password_min_characters),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                        )
                )

                Spacer(modifier = Modifier.height(KSTheme.dimensions.listItemSpacingMedium))

                Row(
                        modifier = Modifier
                                .background(color = KSTheme.colors.kds_white)
                ) {

                    Text(
                            modifier = Modifier
                                    .weight(1f)
                                    .testTag(SignupScreenTestTag.NEWSLETTER_OPT_IN_TEXT.name),
                            text = stringResource(id = R.string.signup_newsletter_full_opt_out),
                            style = KSTheme.typography.caption2,
                            color = KSTheme.colors.kds_support_700
                    )

                    KSSwitch(
                            modifier = Modifier.testTag(SignupScreenTestTag.NEWSLETTER_OPT_IN_SWITCH.name),
                            checked = receiveNewsletterChecked,
                            onCheckChanged = {
                                receiveNewsletterChecked = it
                            },
                            enabled = true
                    )
                }

                Spacer(modifier = Modifier.height(KSTheme.dimensions.listItemSpacingMedium))

                KSPrimaryGreenButton(
                        modifier = Modifier.testTag(SignupScreenTestTag.SIGNUP_BUTTON.name),
                        text = stringResource(id = R.string.Sign_up),
                        onClickAction = {
                            onSignupButtonClicked.invoke(
                                    name,
                                    email,
                                    password,
                                    receiveNewsletterChecked
                            )
                        },
                        isEnabled = signUpButtonEnabled
                )

                Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingMedium))

                LogInSignUpClickableDisclaimerText(
                        onTermsOfUseClicked = onTermsOfUseClicked,
                        onPrivacyPolicyClicked = onPrivacyPolicyClicked,
                        onCookiePolicyClicked = onCookiePolicyClicked
                )

                Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingDoubleLarge))

            }
        }
    }
}

