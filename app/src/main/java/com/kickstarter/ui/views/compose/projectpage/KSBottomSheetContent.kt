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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun KSBottomSheetContentPreview() {
    KSTheme {
        KSBottomSheetContent(
            title = "Hello world",
            body = "Body of text",
            onLinkClicked = { },
            onClose = { }
        )
    }
}

@Composable
fun KSBottomSheetContent(
    title: String,
    body: String?,
    onLinkClicked: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(vertical = dimensions.paddingMedium)
    ) {
        Text(
            text = title,
            style = typography.calloutMedium
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        body?.let {
            Text(
                text = body,
                style = typography.callout
            )
        }
        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        val annotation = "http://www.kickstarter.com"
        val text = "Learn more about creator accountability"
        val annotatedText = buildAnnotatedString {
            pushStringAnnotation(
                tag = annotation,
                annotation = text
            )
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(text)
            }
            pop()
        }
        ClickableText(
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

        KSPrimaryBlackButton(
            modifier = Modifier
                .padding(top = dimensions.paddingMedium)
                .fillMaxWidth(),
            onClickAction = { onClose() },
            isEnabled = true,
            text = stringResource(R.string.general_alert_buttons_ok)
        )
    }
}
