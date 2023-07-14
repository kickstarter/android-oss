package com.kickstarter.ui.activities.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isNotEmptyAndAtLeast6Chars
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSTextInput
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.toolbars.compose.TopToolBar

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ChangePasswordPreview() {
    KSTheme {
        ChangePasswordScreen(
            onBackClicked = { },
            onAcceptButtonClicked = { _, _ -> },
            showProgressBar = false
        )
    }
}

@Composable
fun ChangePasswordScreen(
    onBackClicked: () -> Unit,
    onAcceptButtonClicked: (currentPass: String, newPass: String) -> Unit,
    showProgressBar: Boolean
) {
    Column(
        Modifier
            .background(colors.kds_support_100)
            .fillMaxSize()
    ) {
        var currentPassword by remember { mutableStateOf("") }
        var newPasswordLine1 by remember { mutableStateOf("") }
        var newPasswordLine2 by remember { mutableStateOf("") }

        val acceptButtonEnabled = when {
            currentPassword.isNotEmptyAndAtLeast6Chars()
                    && newPasswordLine1.isNotEmptyAndAtLeast6Chars()
                    && newPasswordLine2.isNotEmptyAndAtLeast6Chars()
                    && newPasswordLine1 == newPasswordLine2 -> true

            else -> false
        }

        val warningText = when {
            newPasswordLine1.isNotEmptyAndAtLeast6Chars()
                    && newPasswordLine2.isNotEmpty()
                    && newPasswordLine2 != newPasswordLine1 -> {
                stringResource(id = R.string.Passwords_matching_message)
            }

            newPasswordLine1.isNotEmpty() && newPasswordLine1.length < 6 -> {
                stringResource(id = R.string.Password_min_length_message)
            }

            else -> ""
        }

        TopToolBar(
            title = stringResource(id = R.string.Change_password),
            titleColor = colors.kds_support_700,
            leftOnClickAction = onBackClicked,
            leftIconColor = colors.kds_support_700,
            backgroundColor = colors.kds_white,
            right = {
                IconButton(
                    onClick = { onAcceptButtonClicked.invoke(currentPassword, newPasswordLine1) },
                    enabled = acceptButtonEnabled
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon__check),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            color =
                            if (acceptButtonEnabled) colors.kds_create_700
                            else colors.kds_support_300
                        )
                    )
                }
            }
        )

        if (showProgressBar) {
            KSLinearProgressIndicator()
        }

        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(
                id = R.string.Well_ask_you_to_sign_back_into_the_Kickstarter_app_once_youve_changed_your_password
            ),
            style = typography.body2,
            color = colors.kds_support_700
        )

        Column(
            Modifier
                .background(color = colors.kds_white)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            var showCurrentPassword by remember { mutableStateOf(false) }
            KSTextInput(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.Current_password),
                onValueChanged = {
                    currentPassword = it
                },
                hideInput = !showCurrentPassword,
                trailingIcon = {
                    IconButton(onClick = {
                        showCurrentPassword = !showCurrentPassword
                    }) {
                        Icon(
                            imageVector =
                                if (showCurrentPassword) Icons.Filled.VisibilityOff
                                else Icons.Filled.Visibility,
                            contentDescription =
                                if (showCurrentPassword)
                                    stringResource(id = R.string.Hide_password)
                                else
                                    stringResource(id = R.string.Show_password),
                            tint =
                                if (showCurrentPassword) colors.kds_create_700
                                else colors.kds_support_400
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            var showPasswordLine1 by remember { mutableStateOf(false) }

            KSTextInput(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.New_password),
                onValueChanged = {
                    newPasswordLine1 = it
                },
                hideInput = !showPasswordLine1,
                trailingIcon = {
                    IconButton(onClick = {
                        showPasswordLine1 = !showPasswordLine1
                    }) {
                        Icon(
                            imageVector =
                                if (showPasswordLine1) Icons.Filled.VisibilityOff
                                else Icons.Filled.Visibility,
                            contentDescription =
                                if (showPasswordLine1)
                                    stringResource(id = R.string.Hide_password)
                                else
                                    stringResource(id = R.string.Show_password),
                            tint =
                                if (showPasswordLine1) colors.kds_create_700
                                else colors.kds_support_400
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            var showPasswordLine2 by remember { mutableStateOf(false) }

            KSTextInput(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.Confirm_password),
                onValueChanged = {
                    newPasswordLine2 = it
                },
                hideInput = !showPasswordLine2,
                trailingIcon = {
                    IconButton(onClick = {
                        showPasswordLine2 = !showPasswordLine2
                    }) {
                        Icon(
                            imageVector =
                                if (showPasswordLine2) Icons.Filled.VisibilityOff
                                else Icons.Filled.Visibility,
                            contentDescription =
                                if (showPasswordLine2)
                                    stringResource(id = R.string.Hide_password)
                                else
                                    stringResource(id = R.string.Show_password),
                            tint =
                                if (showPasswordLine2) colors.kds_create_700
                                else colors.kds_support_400
                        )
                    }
                }
            )
        }

        Divider(color = colors.kds_support_300)

        Text(
            modifier = Modifier.padding(16.dp),
            text = warningText,
            style = typography.body2,
            color = colors.kds_support_700
        )
    }
}