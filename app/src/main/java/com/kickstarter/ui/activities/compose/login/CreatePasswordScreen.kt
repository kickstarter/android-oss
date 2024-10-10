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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.kickstarter.libs.utils.extensions.validPassword
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHiddenTextInput
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.toolbars.compose.TopToolBar

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun CreatePasswordScreenPreview() {
    KSTheme {
        CreatePasswordScreen(
            onBackClicked = {},
            onAcceptButtonClicked = {},
            showProgressBar = false,
            scaffoldState = rememberScaffoldState()
        )
    }
}

enum class CreatePasswordScreenTestTag {
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
fun CreatePasswordScreen(
    onBackClicked: () -> Unit,
    onAcceptButtonClicked: (newPass: String) -> Unit,
    showProgressBar: Boolean,
    scaffoldState: ScaffoldState
) {

    var newPasswordLine1 by rememberSaveable { mutableStateOf("") }
    var newPasswordLine2 by rememberSaveable { mutableStateOf("") }

    val acceptButtonEnabled = when {
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
                title = stringResource(id = R.string.Create_password),
                titleColor = KSTheme.colors.kds_support_700,
                titleModifier = Modifier.testTag(CreatePasswordScreenTestTag.PAGE_TITLE.name),
                leftOnClickAction = onBackClicked,
                leftIconColor = KSTheme.colors.kds_support_700,
                leftIconModifier = Modifier.testTag(CreatePasswordScreenTestTag.BACK_BUTTON.name),
                backgroundColor = KSTheme.colors.kds_white,
                right = {
                    IconButton(
                        modifier = Modifier.testTag(CreatePasswordScreenTestTag.SAVE_BUTTON.name),
                        onClick = {
                            onAcceptButtonClicked.invoke(
                                newPasswordLine1
                            )
                        },
                        enabled = acceptButtonEnabled
                    ) {
                        Image(
                            modifier = Modifier.testTag(CreatePasswordScreenTestTag.SAVE_IMAGE.name),
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
                        .testTag(CreatePasswordScreenTestTag.PROGRESS_BAR.name)
                )
            }

            Text(
                modifier = Modifier
                    .padding(KSTheme.dimensions.paddingMedium)
                    .testTag(CreatePasswordScreenTestTag.PAGE_DISCLAIMER.name),
                text = stringResource(
                    id = R.string.Well_ask_you_to_sign_back_into_the_Kickstarter_app_once_youve_changed_your_password
                ),
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
                        .testTag(CreatePasswordScreenTestTag.NEW_PASSWORD_EDIT_TEXT.name),
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
                        .testTag(CreatePasswordScreenTestTag.CONFIRM_PASSWORD_EDIT_TEXT.name),
                    onValueChanged = { newPasswordLine2 = it },
                    label = stringResource(id = R.string.Confirm_password),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                )
            }

            Divider(color = KSTheme.colors.kds_support_300)

            AnimatedVisibility(visible = warningText.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(KSTheme.dimensions.paddingMedium)
                        .testTag(CreatePasswordScreenTestTag.WARNING_TEXT.name),
                    text = warningText,
                    style = KSTheme.typography.body2,
                    color = KSTheme.colors.kds_support_700
                )
            }
        }
    }
}
