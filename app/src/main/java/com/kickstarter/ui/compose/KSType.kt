package com.kickstarter.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun KSTextPreview() {
    Column(modifier = Modifier.background(kds_white)) {
        Text(text = "Title1", style = title1)
        Text(text = "Title1Bold", style = title1Bold)
        Text(text = "Title2", style = title2)
        Text(text = "Title2Bold", style = title2Bold)
        Text(text = "Title3", style = title3)
        Text(text = "Title3Bold", style = title3Bold)
        Text(text = "headline", style = headline)
        Text(text = "body", style = body)
        Text(text = "callout", style = callout)
        Text(text = "calloutMedium", style = calloutMedium)
        Text(text = "subheadline", style = subheadline)
        Text(text = "subheadlineMedium", style = subheadlineMedium)
        Text(text = "BUTTON", style = buttonText)
        Text(text = "Body2", style = body2)
        Text(text = "Body2Medium", style = body2Medium)
        Text(text = "Footnote", style = footnote)
        Text(text = "FootnoteMedium", style = footnoteMedium)
        Text(text = "Caption1", style = caption1)
        Text(text = "Caption1Medium", style = caption1Medium)
        Text(text = "Caption2", style = caption2)
        Text(text = "Caption2Medium", style = caption2Medium)
    }
}

val title1 = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 28.sp,
    lineHeight = 34.sp,
    color = kds_support_700
)

val title1Bold = TextStyle(
    fontWeight = FontWeight(700),
    fontSize = 28.sp,
    lineHeight = 34.sp,
    color = kds_support_700
)

val title2 = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 22.sp,
    lineHeight = 26.sp,
    color = kds_support_700
)

val title2Bold = TextStyle(
    fontWeight = FontWeight(700),
    fontSize = 22.sp,
    lineHeight = 26.sp,
    color = kds_support_700
)

val title3 = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 20.sp,
    lineHeight = 25.sp,
    color = kds_support_700
)

val title3Bold = TextStyle(
    fontWeight = FontWeight(700),
    fontSize = 20.sp,
    lineHeight = 25.sp,
    color = kds_support_700
)

val headline = TextStyle(
    fontWeight = FontWeight(500),
    fontSize = 18.sp,
    lineHeight = 21.sp,
    color = kds_support_700,
    letterSpacing = 0.25.sp
)

val body = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 18.sp,
    lineHeight = 21.sp,
    color = kds_support_700,
    letterSpacing = 0.25.sp
)

val callout = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 16.sp,
    lineHeight = 22.sp,
    color = kds_support_700,
    letterSpacing = 0.25.sp
)

val calloutMedium = TextStyle(
    fontWeight = FontWeight(500),
    fontSize = 16.sp,
    lineHeight = 21.sp,
    color = kds_support_700,
    letterSpacing = 0.25.sp
)

val subheadline = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 15.sp,
    lineHeight = 18.sp,
    color = kds_support_700,
    letterSpacing = 0.25.sp
)

val subheadlineMedium = TextStyle(
    fontWeight = FontWeight(500),
    fontSize = 15.sp,
    lineHeight = 18.sp,
    color = kds_support_700,
    letterSpacing = 0.25.sp
)

val buttonText = TextStyle(
    fontWeight = FontWeight(500),
    fontSize = 14.sp,
    lineHeight = 16.sp,
    color = kds_create_700,
    letterSpacing = 1.25.sp
)

val body2 = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 14.sp,
    lineHeight = 20.sp,
    color = kds_support_700,
    letterSpacing = 0.25.sp
)

val body2Medium = TextStyle(
    fontWeight = FontWeight(500),
    fontSize = 14.sp,
    lineHeight = 16.sp,
    color = kds_support_700,
    letterSpacing = 0.25.sp
)

val footnote = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 13.sp,
    lineHeight = 15.sp,
    color = kds_support_700,
    letterSpacing = 0.1.sp
)

val footnoteMedium = TextStyle(
    fontWeight = FontWeight(500),
    fontSize = 13.sp,
    lineHeight = 15.sp,
    color = kds_support_700,
    letterSpacing = 0.1.sp
)

val caption1 = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 12.sp,
    lineHeight = 16.sp,
    color = kds_support_700,
    letterSpacing = 0.4.sp
)

val caption1Medium = TextStyle(
    fontWeight = FontWeight(500),
    fontSize = 12.sp,
    lineHeight = 14.sp,
    color = kds_support_700,
    letterSpacing = 0.4.sp
)

val caption2 = TextStyle(
    fontWeight = FontWeight(400),
    fontSize = 11.sp,
    lineHeight = 13.sp,
    color = kds_support_700,
    letterSpacing = 0.4.sp
)

val caption2Medium = TextStyle(
    fontWeight = FontWeight(500),
    fontSize = 11.sp,
    lineHeight = 13.sp,
    color = kds_support_700,
    letterSpacing = 0.4.sp
)