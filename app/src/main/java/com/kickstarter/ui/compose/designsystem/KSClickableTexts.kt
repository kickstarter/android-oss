package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PreviewKSClickableText() {
    KSTheme {
        Column(
            modifier = Modifier.background(KSTheme.colors.kds_white)
        ) {
            KSClickableText(
                resourceId = R.string.Learn_about_AI_policy_on_Kickstarter
            )
        }
    }
}
@Composable
fun KSClickableText(
    modifier: Modifier = Modifier,
    @StringRes resourceId: Int,
    clickCallback: () -> Unit = {}
) {
    ClickableText(
        text = AnnotatedString(
            text = stringResource(id = resourceId),
            spanStyle = SpanStyle(
                color = KSTheme.colors.kds_create_700,
                textDecoration = TextDecoration.Underline,
            )
        ),
        onClick = {
            clickCallback()
        },
        modifier = modifier
    )
}
