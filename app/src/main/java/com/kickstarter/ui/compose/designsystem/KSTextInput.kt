package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

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

            Spacer(modifier = Modifier.height(12.dp))

            KSHiddenTextInput(label = "Password")
        }
    }
}

@Composable
fun KSTextInput(
    modifier: Modifier = Modifier,
    label: String,
    onValueChanged: ((String) -> Unit)? = null,
    isError: Boolean = false,
    assistiveText: String? = null,
    showAssistiveText: Boolean = false,
    hideInput: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var value by remember { mutableStateOf("") }

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
            textStyle = typography.callout,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = colors.kds_support_200,
                errorLabelColor = colors.kds_alert,
                errorIndicatorColor = colors.kds_alert,
                unfocusedLabelColor = colors.kds_support_700,
                unfocusedIndicatorColor = colors.kds_support_700,
                focusedLabelColor = colors.kds_create_700,
                focusedIndicatorColor = colors.kds_create_700,
                cursorColor = colors.kds_create_700,
                errorCursorColor = colors.kds_alert
            ),
            isError = isError,
            trailingIcon = trailingIcon,
            visualTransformation =
            if (hideInput) PasswordVisualTransformation()
            else VisualTransformation.None
        )

        if (showAssistiveText) {
            assistiveText?.let {
                Text(
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                    text = it,
                    color =
                    if (isError) colors.kds_alert
                    else colors.kds_black.copy(alpha = 0.6f),
                    style = typography.caption1
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
    hideTextByDefault: Boolean = true,
    visibilityOffIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_visibility_off),
    visibilityOnIcon: ImageVector = ImageVector.vectorResource(id = R.drawable.ic_visibility_on),
    offIconContentDescription: String = stringResource(id = R.string.Hide_password),
    onIconContentDescription: String = stringResource(id = R.string.Show_password),
    offIconTint: Color = colors.kds_create_700,
    onIconTint: Color = colors.kds_support_400
) {
    var showHiddenText by remember { mutableStateOf(!hideTextByDefault) }

    KSTextInput(
        modifier = modifier,
        label = label,
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
        }
    )
}
