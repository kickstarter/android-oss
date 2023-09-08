package com.kickstarter.ui.activities.compose.login

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isNotEmptyAndAtLeast6Chars
import com.kickstarter.ui.compose.designsystem.KSButton
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
                onBackClicked = {},
                onAcceptButtonClicked = {},
                showProgressBar = false,
                email = "test@test.com",
                isFormSubmitting = false,
                scaffoldState = rememberScaffoldState()
        )
    }
}

enum class SetPasswordScreenTestTag {
    BACK_BUTTON,
    PAGE_TITLE,
    SAVE_BUTTON,
    SAVE_IMAGE,
    PROGRESS_BAR,
    PAGE_DISCLAIMER,
    NEW_PASSWORD_EDIT_TEXT,
    CONFIRM_PASSWORD_EDIT_TEXT,
    WARNING_TEXT
}

@Composable
fun SetPasswordScreen(
        onBackClicked: () -> Unit,
        onAcceptButtonClicked: (newPass: String) -> Unit,
        showProgressBar: Boolean,
        email: String,
        isFormSubmitting: Boolean,
        scaffoldState: ScaffoldState
) {

    var newPasswordLine1 by rememberSaveable { mutableStateOf("") }
    var newPasswordLine2 by rememberSaveable { mutableStateOf("") }

    val acceptButtonEnabled = when {
        !isFormSubmitting &&
        newPasswordLine1.isNotEmptyAndAtLeast6Chars() &&
                newPasswordLine2.isNotEmptyAndAtLeast6Chars() &&
                newPasswordLine1 == newPasswordLine2 -> true

        else -> false
    }

    val warningText = when {
        newPasswordLine1.isNotEmptyAndAtLeast6Chars() &&
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
            scaffoldState = scaffoldState,
            topBar = {
                TopToolBar(
                        title = stringResource(id = R.string.Set_your_password),
                        titleColor = KSTheme.colors.kds_support_700,
                        titleModifier = Modifier.testTag(SetPasswordScreenTestTag.PAGE_TITLE.name),
                        leftIcon = null,
                        leftOnClickAction = null,
                        leftIconColor = KSTheme.colors.kds_support_700,
                        leftIconModifier = Modifier.testTag(SetPasswordScreenTestTag.BACK_BUTTON.name),
                        backgroundColor = KSTheme.colors.kds_white,
                        right = {
                            IconButton(
                                    modifier = Modifier.testTag(SetPasswordScreenTestTag.SAVE_BUTTON.name),
                                    onClick = {
                                        onAcceptButtonClicked.invoke(
                                                newPasswordLine1
                                        )
                                    },
                                    enabled = acceptButtonEnabled
                            ) {
                                Image(
                                        modifier = Modifier.testTag(SetPasswordScreenTestTag.SAVE_IMAGE.name),
                                        painter = painterResource(id = R.drawable.icon__check),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(
                                                color =
                                                if (acceptButtonEnabled) KSTheme.colors.kds_create_700
                                                else KSTheme.colors.kds_support_300
                                        )
                                )
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
                        .background(KSTheme.colors.kds_support_100)
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
            Text(
                    modifier = Modifier
                            .padding(KSTheme.dimensions.paddingMedium)
                            .testTag(SetPasswordScreenTestTag.PAGE_DISCLAIMER.name),
                    text = stringResource(R.string.We_will_be_discontinuing_the_ability_to_log_in_via_FB, email),
                    style = KSTheme.typography.body2,
                    color = KSTheme.colors.kds_support_700
            )

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

                Spacer(modifier = Modifier.height(KSTheme.dimensions.listItemSpacingMedium))

                KSPrimaryGreenButton(
                        text = "Set Password",
                        onClickAction = { onAcceptButtonClicked.invoke(newPasswordLine1) },
                        isEnabled = acceptButtonEnabled
                )
            }
        }
    }
}