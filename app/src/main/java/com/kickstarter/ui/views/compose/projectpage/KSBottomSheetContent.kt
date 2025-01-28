package com.kickstarter.ui.views.compose.projectpage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSOutlinedButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.kds_white

@Composable
fun KSBottomSheetContent(
    text: String?,
    onLinkClicked: () -> Unit,
    onClose: () -> Unit,
) {
    Column {
        Text(
            text = "Kickstarter has restricted this creator",
            style = typography.subheadlineMedium
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        text?.let {
            Text(
                text = text,
                style = typography.subheadline
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
                    color = colors.kds_create_700,
                )
            ) {
                append(text)
            }
            pop()
        }
        ClickableText(
            modifier = Modifier.padding(bottom = dimensions.paddingMedium),
            text = annotatedText,
            style = TextStyle(
                fontWeight = FontWeight(400),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
                color = colors.kds_support_400
            ),
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

        KSOutlinedButton(
            onClickAction = { onClose() },
            backgroundColor = kds_white,
            text = stringResource(R.string.general_alert_buttons_ok)
        )
    }
}
