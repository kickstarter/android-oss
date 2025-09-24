package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSTextInputPreview() {
    KSTheme {
        var isError by remember { mutableStateOf(false) }
        var showAssistiveText by remember { mutableStateOf(false) }
        var assistiveText by remember { mutableStateOf("") }

        Column {
            KSTextInput(
                label = "Label",
                onValueChanged = { currentString ->
                    when (currentString) {
                        "cccccc" -> {
                            isError = true
                            showAssistiveText = true
                            assistiveText = "$currentString is not allowed!"
                        }

                        else -> {
                            isError = false
                            showAssistiveText = false
                            assistiveText = ""
                        }
                    }
                },
                isError = isError,
                assistiveText = assistiveText,
                showAssistiveText = showAssistiveText
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingMediumSmall))

            KSHiddenTextInput(label = "Password")
        }
    }
}

@Composable
fun KSTextInput(
    modifier: Modifier = Modifier,
    label: String,
    initialValue: String = "",
    onValueChanged: ((String) -> Unit)? = null,
    isError: Boolean = false,
    assistiveText: String? = null,
    showAssistiveText: Boolean = false,
    hideInput: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    var value by rememberSaveable { mutableStateOf(initialValue) }

    Column {
        TextField(
            modifier = modifier,
            value = value,
            onValueChange = {
                onValueChanged?.invoke(it)
                value = it
            },
            label = { Text(text = label) },
            maxLines = 1,
            shape = RoundedCornerShape(
                topStart = dimensions.radiusMediumSmall,
                topEnd = dimensions.radiusMediumSmall
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.kds_support_200,
                unfocusedContainerColor = colors.kds_support_200,
                disabledContainerColor = colors.kds_support_200,
                errorContainerColor = colors.kds_support_200,
                focusedTextColor = colors.kds_support_700,
                unfocusedTextColor = colors.kds_support_700,
                disabledTextColor = colors.textDisabled,
                focusedLabelColor = colors.kds_create_700,
                unfocusedLabelColor = colors.kds_support_700,
                errorLabelColor = colors.kds_alert,
                focusedIndicatorColor = colors.kds_create_700,
                unfocusedIndicatorColor = colors.kds_support_700,
                errorIndicatorColor = colors.kds_alert,
                cursorColor = colors.kds_create_700,
                errorCursorColor = colors.kds_alert
            ),
            isError = isError,
            trailingIcon = trailingIcon,
            visualTransformation =
            if (hideInput) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )

        if (showAssistiveText) {
            assistiveText?.let {
                Text(
                    modifier = Modifier.padding(
                        start = dimensions.paddingMedium,
                        top = dimensions.assistiveTextTopSpacing
                    ),
                    text = it,
                    color =
                    if (isError) colors.kds_alert
                    else colors.kds_black.copy(alpha = 0.6f),
                    style = typographyV2.bodySM
                )
            }
        }
    }
}

@Composable
fun KSHiddenTextInput(
    modifier: Modifier = Modifier,
    onValueChanged: ((String) -> Unit)? = null,
    label: String,
    initialValue: String = "",
    hideTextByDefault: Boolean = true,
    visibilityOffIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_visibility_off),
    visibilityOnIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_visibility_on),
    offIconContentDescription: String = stringResource(id = R.string.Hide_password),
    onIconContentDescription: String = stringResource(id = R.string.Show_password),
    offIconTint: Color = colors.kds_create_700,
    onIconTint: Color = colors.kds_support_400,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    var showHiddenText by rememberSaveable { mutableStateOf(!hideTextByDefault) }

    KSTextInput(
        modifier = modifier,
        label = label,
        initialValue = initialValue,
        onValueChanged = onValueChanged,
        hideInput = !showHiddenText,
        trailingIcon = {
            IconButton(onClick = {
                showHiddenText = !showHiddenText
            }) {
                Icon(
                    imageVector =
                    if (showHiddenText) visibilityOffIcon
                    else visibilityOnIcon,
                    contentDescription =
                    if (showHiddenText) offIconContentDescription
                    else onIconContentDescription,
                    tint =
                    if (showHiddenText) offIconTint
                    else onIconTint
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}
