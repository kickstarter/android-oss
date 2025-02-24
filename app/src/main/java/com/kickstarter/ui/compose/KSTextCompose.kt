package com.kickstarter.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.kds_create_700
import com.kickstarter.ui.compose.designsystem.kds_white

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFF0EAE2, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, backgroundColor = 0X00000000, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TitleTextPreview() {
    KSTheme {
        Column {
            TextH6ExtraBoldTitle(
                stringResource(R.string.Risks_and_challenges),
                Modifier
                    .paddingFromBaseline(
                        top = dimensionResource(id = R.dimen.grid_8),
                        bottom = dimensionResource(id = R.dimen.grid_4)
                    )
                    .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
            )

            TextBody1Title(
                stringResource(id = R.string.profile_settings_newsletter_music_newsletter),
                Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
            )

            TextWithKdsSupport700Bg(
                "Coming soon",
                Modifier
            )

            TextCaptionStyle(
                "56 followers",
                Modifier
            )

            TextCaptionStyleWithStartIcon(
                "Art Books",
                Icons.Filled.LocationOn,
                modifier = Modifier
            )

            TextWithStartIcon(
                text = "wow",
                imageVector = ImageVector.vectorResource(id = R.drawable.icon__check_green),
                modifier = Modifier.padding()
            )
        }
    }
}

@Composable
fun TextH6ExtraBoldTitle(
    text: String,
    modifier: Modifier,
    textColor: Color = colors.kds_support_700
) {
    Text(
        text = text,
        style = typographyV2.subHeadline,
        color = textColor,
        modifier = modifier
    )
}

@Composable
fun TextBody1Title(
    text: String,
    modifier: Modifier,
    textColor: Color = colors.kds_support_700,
    fontSizeUnit: TextUnit = dimensionResource(id = R.dimen.callout).value.sp,
    letterSpacing: TextUnit = 0.0.sp
) {
    Text(
        text = text,
        style = typographyV2.body.copy(
            fontSize = fontSizeUnit
        ),
        color = textColor,
        modifier = modifier,
        letterSpacing = letterSpacing
    )
}

@Composable
fun TextBody2Style(
    text: String,
    modifier: Modifier,
    textColor: Color = colors.kds_support_700
) {
    Text(
        text = text,
        style = typographyV2.bodyMD,
        color = textColor,
        modifier = modifier
    )
}

@Composable
fun TextWithKdsSupport700Bg(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = typographyV2.bodySM,
        color = kds_white,
        modifier = modifier
            .background(kds_create_700)
            .padding(
                horizontal = dimensionResource(id = R.dimen.grid_3),
                vertical = dimensionResource(id = R.dimen.grid_1)
            )
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun TextCaptionStyle(
    text: String,
    modifier: Modifier,
    textColor: Color = colors.kds_support_700
) {
    Text(
        text = text,
        style = typographyV2.body.copy(
            fontSize = dimensionResource(id = R.dimen.caption_1).value.sp,
            fontFamily = FontFamily(
                Font(DeviceFontFamilyName("sans-serif-medium"))
            ),
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        ),
        color = textColor,
        modifier = modifier.wrapContentHeight() // make text center vertical
    )
}

@Composable
fun TextCaptionStyleWithStartIcon(
    text: String,
    imageVector: ImageVector,
    modifier: Modifier,
    tintColor: Color = colors.kds_support_400,
) {
    ConstraintLayout(modifier = modifier) {
        val (
            icon, textElement
        ) = createRefs()
        Icon(
            imageVector = imageVector,
            contentDescription = "null",
            tint = tintColor,
            modifier = Modifier.constrainAs(icon) {
                top.linkTo(textElement.top)
                bottom.linkTo(textElement.bottom)
            }.padding(end = dimensionResource(id = R.dimen.grid_1))
        )

        TextCaptionStyle(
            text,
            Modifier.constrainAs(textElement) {
                start.linkTo(icon.end)
            },
            tintColor
        )
    }
}

@Composable
fun TextCaptionStyleWithStartIcon(
    text: String,
    painter: Painter,
    modifier: Modifier,
    tintColor: Color = colors.kds_support_400
) {
    ConstraintLayout(modifier = modifier) {
        val (
            icon, textElement
        ) = createRefs()
        Icon(
            painter = painter,
            contentDescription = "null",
            tint = tintColor,
            modifier = Modifier.constrainAs(icon) {
                top.linkTo(textElement.top)
                bottom.linkTo(textElement.bottom)
            }.padding(end = dimensionResource(id = R.dimen.grid_1))
        )

        TextCaptionStyle(
            text,
            Modifier.constrainAs(textElement) {
                start.linkTo(icon.end)
            },
            tintColor
        )
    }
}

@Composable
fun TextWithStartIcon(
    text: String,
    imageVector: ImageVector,
    modifier: Modifier,
    style: TextStyle = LocalTextStyle.current,
    iconColor: Color = imageVector.tintColor,
    textColor: Color = colors.kds_support_400,
    iconHeight: Dp = dimensionResource(id = R.dimen.icon),
    iconPadding: Dp = dimensionResource(id = R.dimen.grid_1)
) {
    Row(modifier = modifier) {
        Icon(
            imageVector = imageVector,
            contentDescription = "null",
            tint = iconColor,
            modifier = Modifier.padding(end = iconPadding).height(iconHeight),
        )

        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = text,
            color = textColor,
            style = style
        )
    }
}
