package com.kickstarter.ui.views.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.kickstarter.R

@Preview
@Composable
fun KSImageTextBannerPreview() {
    KSImageTextBanner(
        imageResToDisplay = R.drawable.ic_alert,
        textResToDisplay = R.string.After_September_5_2023_this_Dashboard_feature_will_only_be_available_on_our_website,
        textColorRes = R.color.kds_white,
        backgroundColorRes = R.color.kds_alert
    )
}

@Composable
fun KSImageTextBanner(
    imageResToDisplay: Int,
    textResToDisplay: Int,
    textColorRes: Int,
    backgroundColorRes: Int,
    onClickAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = backgroundColorRes))
            .padding(all = dimensionResource(id = R.dimen.grid_2))
            .clickable(enabled = onClickAction != null) { onClickAction?.invoke() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = imageResToDisplay),
            contentDescription = stringResource(id = textResToDisplay),
            Modifier.padding(end = dimensionResource(id = R.dimen.grid_2))
        )
        Text(
            text = stringResource(id = textResToDisplay),
            color = colorResource(id = textColorRes),
            fontSize = dimensionResource(id = R.dimen.subheadline).value.sp
        )
    }
}
