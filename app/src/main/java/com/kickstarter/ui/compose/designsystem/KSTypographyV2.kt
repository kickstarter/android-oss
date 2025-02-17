package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.kickstarter.R

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun KSTextPreviewV2() {
    KSTheme {
        Column(modifier = Modifier.background(KSTheme.colors.kds_white)) {
            Text(text = "heading2XL", style = KSTheme.typographyV2.heading2XL)
            Text(text = "headingXL", style = KSTheme.typographyV2.headingXL)
            Text(text = "headingLG", style = KSTheme.typographyV2.headingLG)
            Text(text = "headingMD", style = KSTheme.typographyV2.headingMD)
            Text(text = "headingSM", style = KSTheme.typographyV2.headingSM)
            Text(text = "headingXS", style = KSTheme.typographyV2.headingXS)
            Text(text = "bodyXL", style = KSTheme.typographyV2.bodyXL)
            Text(text = "bodyBoldXL", style = KSTheme.typographyV2.bodyBoldXL)
            Text(text = "bodyLG", style = KSTheme.typographyV2.bodyLG)
            Text(text = "bodyBoldLG", style = KSTheme.typographyV2.bodyBoldLG)
            Text(text = "bodyMD", style = KSTheme.typographyV2.bodyMD)
            Text(text = "bodyBoldMD", style = KSTheme.typographyV2.bodyBoldMD)
            Text(text = "bodySM", style = KSTheme.typographyV2.bodySM)
            Text(text = "bodyBoldSM", style = KSTheme.typographyV2.bodyBoldSM)
            Text(text = "bodyXS", style = KSTheme.typographyV2.bodyXS)
            Text(text = "bodyBoldXS", style = KSTheme.typographyV2.bodyBoldXS)
            Text(text = "bodyXXS", style = KSTheme.typographyV2.bodyXXS)
            Text(text = "bodyBoldXXS", style = KSTheme.typographyV2.bodyBoldXXS)
            Text(text = "buttonLabel", style = KSTheme.typographyV2.buttonLabel)
            Text(text = "linkLG", style = KSTheme.typographyV2.linkLG)
            Text(text = "linkMD", style = KSTheme.typographyV2.linkMD)
            Text(text = "linkSM", style = KSTheme.typographyV2.linkSM)
            Text(text = "linkXS", style = KSTheme.typographyV2.linkXS)
            // OLD DESIGN SYSTEM
            Text(text = "title1", style = KSTheme.typographyV2.title1)
            Text(text = "title1Bold", style = KSTheme.typographyV2.title1Bold)
            Text(text = "title2", style = KSTheme.typographyV2.title2)
            Text(text = "titleRewardBold", style = KSTheme.typographyV2.titleRewardBold)
            Text(text = "title2Bold", style = KSTheme.typographyV2.title2Bold)
            Text(text = "headLine", style = KSTheme.typographyV2.headLine)
            Text(text = "body", style = KSTheme.typographyV2.body)
            Text(text = "footNote", style = KSTheme.typographyV2.footNote)
            Text(text = "footNoteMedium", style = KSTheme.typographyV2.footNoteMedium)
            Text(text = "subHeadline", style = KSTheme.typographyV2.subHeadline)
            Text(text = "subHeadlineMedium", style = KSTheme.typographyV2.subHeadlineMedium)
        }
    }
}

@Immutable
data class KSTypographyV2(
    val heading2XL: TextStyle = TextStyle.Default,
    val headingXL: TextStyle = TextStyle.Default,
    val headingLG: TextStyle = TextStyle.Default,
    val headingMD: TextStyle = TextStyle.Default,
    val headingSM: TextStyle = TextStyle.Default,
    val headingXS: TextStyle = TextStyle.Default,
    val bodyXL: TextStyle = TextStyle.Default,
    val bodyBoldXL: TextStyle = TextStyle.Default,
    val bodyLG: TextStyle = TextStyle.Default,
    val bodyBoldLG: TextStyle = TextStyle.Default,
    val bodyMD: TextStyle = TextStyle.Default,
    val bodyBoldMD: TextStyle = TextStyle.Default,
    val bodySM: TextStyle = TextStyle.Default,
    val bodyBoldSM: TextStyle = TextStyle.Default,
    val bodyXS: TextStyle = TextStyle.Default,
    val bodyBoldXS: TextStyle = TextStyle.Default,
    val bodyXXS: TextStyle = TextStyle.Default,
    val bodyBoldXXS: TextStyle = TextStyle.Default,
    val buttonLabel: TextStyle = TextStyle.Default,
    val linkLG: TextStyle = TextStyle.Default,
    val linkMD: TextStyle = TextStyle.Default,
    val linkSM: TextStyle = TextStyle.Default,
    val linkXS: TextStyle = TextStyle.Default,
    // OLD DESIGN SYSTEM
    val title1: TextStyle = TextStyle.Default,
    val title1Bold: TextStyle = TextStyle.Default,
    val title2: TextStyle = TextStyle.Default,
    val title2Bold: TextStyle = TextStyle.Default,
    val titleRewardBold: TextStyle = TextStyle.Default,
    val headLine: TextStyle = TextStyle.Default,
    val body: TextStyle = TextStyle.Default,
    val footNote: TextStyle = TextStyle.Default,
    val footNoteMedium: TextStyle = TextStyle.Default,
    val subHeadline: TextStyle = TextStyle.Default,
    val subHeadlineMedium: TextStyle = TextStyle.Default,
)

val LocalKSCustomTypographyV2 = staticCompositionLocalOf {
    KSTypographyV2()
}
val interFontFamily = FontFamily(
    Font(R.font.inter_variable, FontWeight.Thin), // 100
    Font(R.font.inter_variable, FontWeight.Light), // 300
    Font(R.font.inter_variable, FontWeight.Normal), // 400
    Font(R.font.inter_variable, FontWeight.Medium), // 500
    Font(R.font.inter_variable, FontWeight.SemiBold), // 600
    Font(R.font.inter_variable, FontWeight.Bold), // 700
    Font(R.font.inter_variable, FontWeight.ExtraBold) // 800
)

var interFontFamilyItalic = FontFamily(
    Font(R.font.inter_variable_italic, FontWeight.Thin), // 100
    Font(R.font.inter_variable_italic, FontWeight.Light), // 300
    Font(R.font.inter_variable_italic, FontWeight.Normal), // 400
    Font(R.font.inter_variable_italic, FontWeight.Medium), // 500
    Font(R.font.inter_variable_italic, FontWeight.SemiBold), // 600
    Font(R.font.inter_variable_italic, FontWeight.Bold), // 700
    Font(R.font.inter_variable_italic, FontWeight.ExtraBold) // 800
)

val KSCustomTypographyV2 = KSTypographyV2(
    /*
     * NEW DESIGN SYSTEM
     */
    heading2XL = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.8).sp,
        color = kds_support_700
    ),
    headingXL = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.67).sp,
        color = kds_support_700
    ),
    headingLG = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.37).sp,
        color = kds_support_700
    ),
    headingMD = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.32).sp,
        color = kds_support_700
    ),
    headingSM = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.15).sp,
        color = kds_support_700
    ),
    headingXS = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.15).sp,
        color = kds_support_700
    ),
    bodyXL = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = kds_support_700
    ),
    bodyBoldXL = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = kds_support_700
    ),
    bodyLG = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = kds_support_700
    ),
    bodyBoldLG = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = kds_support_700
    ),
    bodyMD = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = kds_support_700
    ),
    bodyBoldMD = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = kds_support_700
    ),
    bodySM = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = kds_support_700
    ),
    bodyBoldSM = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = kds_support_700
    ),
    bodyXS = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = kds_support_700
    ),
    bodyBoldXS = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = kds_support_700
    ),
    bodyXXS = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        color = kds_support_700
    ),
    bodyBoldXXS = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        color = kds_support_700
    ),
    buttonLabel = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.4).sp,
        color = kds_support_700
    ),
    linkLG = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.4).sp,
        textDecoration = TextDecoration.Underline,
        color = kds_support_700
    ),
    linkMD = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.4).sp,
        textDecoration = TextDecoration.Underline,
        color = kds_support_700
    ),
    linkSM = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        textDecoration = TextDecoration.Underline,
        color = kds_support_700
    ),
    linkXS = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        textDecoration = TextDecoration.Underline,
        color = kds_support_700
    ),

    /*
     * OLD DESIGN SYSTEM
     * Not matches with new design system so some values were changed to match with new design system.
     * For any doubt check with design team.
     */
    title1 = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = kds_support_700
    ),
    title1Bold = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.85).sp,
        color = kds_support_700
    ),
    title2 = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        color = kds_support_700
    ),
    title2Bold = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.75).sp,
        color = kds_support_700
    ),
    titleRewardBold = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = kds_support_700
    ),
    headLine = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.5).sp,
        color = kds_support_700
    ),
    body = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
        color = kds_support_700
    ),
    footNote = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.sp,
        color = kds_support_700
    ),
    footNoteMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = (-0.23).sp,
        color = kds_support_700
    ),
    subHeadline = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
        color = kds_support_700
    ),
    subHeadlineMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 17.sp,
        letterSpacing = (-0.34).sp,
        color = kds_support_700
    )
)
