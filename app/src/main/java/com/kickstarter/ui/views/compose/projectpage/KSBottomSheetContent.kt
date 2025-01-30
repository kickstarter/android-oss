package com.kickstarter.ui.views.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

enum class KSBottomSheetContentTestTag() {
    TITLE,
    BODY,
    LINK,
    CLOSE_BUTTON
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun KSBottomSheetContentPreview() {
    KSTheme {
        KSBottomSheetContent(
            title = "Hello world",
            body = "Body of text",
            linkText = "Click to learn more",
            onLinkClicked = { },
            onClose = { }
        )
    }
}

@Composable
fun KSBottomSheetContent(
    title: String,
    body: String,
    linkText: String?,
    onLinkClicked: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(dimensions.paddingLarge)
    ) {
        Text(
            text = title,
            style = typography.title3Bold,
            color = colors.textPrimary,
            modifier = Modifier.testTag(KSBottomSheetContentTestTag.TITLE.name)
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        Text(
            text = body,
            style = typography.callout,
            color = colors.textPrimary,
            modifier = Modifier.testTag(KSBottomSheetContentTestTag.BODY.name)
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))
        if (linkText != null) {
            val annotation = "http://www.kickstarter.com"
            val annotatedText = buildAnnotatedString {
                pushStringAnnotation(
                    tag = annotation,
                    annotation = linkText
                )
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(linkText)
                }
                pop()
            }
            ClickableText(
                modifier = Modifier.testTag(KSBottomSheetContentTestTag.LINK.name),
                text = annotatedText,
                onClick = {
                    annotatedText.getStringAnnotations(
                        tag = annotation, start = it,
                        end = it
                    )
                        .firstOrNull()?.let { annotation ->
                            onLinkClicked()
                        } ?: onLinkClicked()
                }
            )
        }

        KSPrimaryBlackButton(
            modifier = Modifier
                .padding(top = dimensions.paddingMedium)
                .fillMaxWidth()
                .testTag(KSBottomSheetContentTestTag.CLOSE_BUTTON.name),
            onClickAction = { onClose() },
            isEnabled = true,
            text = stringResource(R.string.general_alert_buttons_ok)
        )
    }
}
