package com.kickstarter.ui.views.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.kickstarter.ui.compose.designsystem.red_02
import com.kickstarter.ui.views.KSModalBottomSheet


@Preview
@Composable
fun KSImageTextCtaBannerPreview() {
    KSImageTextCtaBanner(
        imageResToDisplay = R.drawable.ic_alert_diamond,
        titleResToDisplay = R.string.Add_ons_unavailable,
        textResToDisplay = R.string.Address_confirmed_need_to_change_your_address_before_it_locks,
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
    textColorRes: Int,
    backgroundColor: Color,
    highlightColorRes: Int,
    onClickAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
//            .background(
//                color = red_02,
//                shape = RoundedCornerShape(
//                    // TODO: YC - Add actual dimensions
//                    topStart = 6.dp,
//                    topEnd = 6.dp,
//                    bottomStart = 6.dp,
//                    bottomEnd = 6.dp
//                )
//            )
        ,

        verticalAlignment = Alignment.Top   ,
    ) {

        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp)
                .background(
                    color = backgroundColor
//                    shape = RoundedCornerShape(
//                        // TODO: YC - Add actual dimensions
//                        topStart = 6.dp,
//                        topEnd = 0.dp,
//                        bottomStart = 6.dp,
//                        bottomEnd = 0.dp
//                    )
                )
        )

        Row (
            modifier = Modifier
                .background(
                    color = red_02,
                    shape = RoundedCornerShape(
                        // TODO: YC - Add actual dimensions
                        topStart = 0.dp,
                        topEnd = 6.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 6.dp
                    )
                )
                .padding(all = dimensionResource(id = R.dimen.grid_2))
        ) {
            Image(
                painter = painterResource(id = imageResToDisplay),
                colorFilter = ColorFilter.tint(color = colorResource(highlightColorRes)),
                contentDescription = stringResource(id = titleResToDisplay),
                modifier = Modifier.padding(end = dimensionResource(id = R.dimen.grid_2))
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
                    text = "Learn more",
                    onClickAction = { onClickAction?.invoke() }
                )
            }


        }

    }
}
