package com.kickstarter.ui.views.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSOutlinedButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography


@Preview
@Composable
fun KSImageTextCtaBannerPreview() {
    KSImageTextCtaBanner(
        imageResToDisplay = R.drawable.ic_alert_diamond,
        titleResToDisplay = R.string.project_project_notices_header,
        textResToDisplay = R.string.project_project_notices_notice_intro,
        buttonTextResToDisplay = R.string.project_project_notices_notice_cta,
        textColorRes = R.color.text_primary,
        backgroundColor = colors.backgroundDangerSubtle,
        highlightColorRes = R.color.kds_alert,
    )
}

@Composable
fun KSImageTextCtaBanner(
    imageResToDisplay: Int,
    titleResToDisplay: Int,
    textResToDisplay: Int,
    buttonTextResToDisplay: Int,
    textColorRes: Int,
    backgroundColor: Color,
    highlightColorRes: Int,
    onClickAction: (() -> Unit)? = null
) {
    Row (
        modifier = Modifier
            .background(
                color = colorResource(highlightColorRes),
                shape = RoundedCornerShape(
                    // TODO: YC - Add actual dimensions
                    topStart = 6.dp,
                    topEnd = 6.dp,
                    bottomStart = 6.dp,
                    bottomEnd = 6.dp
                )
            )
            .padding(
                start = dimensions.paddingXSmall
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(
                    // TODO: YC - Add actual dimensions
                    topStart = 0.dp,
                    topEnd = 6.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 6.dp
                )
            )
            .padding(dimensions.paddingMedium)
    ) {
        Image(
            painter = painterResource(id = imageResToDisplay),
            colorFilter = ColorFilter.tint(color = colorResource(highlightColorRes)),
            contentDescription = stringResource(id = titleResToDisplay),
            modifier = Modifier
                .padding(end = dimensionResource(id = R.dimen.grid_2))
                .size(20.dp)
        )

        Column {
            Text(
                text = stringResource(id = titleResToDisplay),
                color = colorResource(id = textColorRes),
                style = typography.subheadlineMedium
            )

            Spacer(modifier = Modifier.height(dimensions.paddingSmall))

            Text(
                text = stringResource(id = textResToDisplay),
                color = colorResource(id = textColorRes),
                style = typography.subheadline
            )

            KSOutlinedButton(
                backgroundColor = backgroundColor,
                text = stringResource(buttonTextResToDisplay),
                onClickAction = { onClickAction?.invoke() }
            )
        }
    }
}
