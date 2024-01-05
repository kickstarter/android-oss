package com.kickstarter.ui.views.compose

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.CircleImageFromURl
import com.kickstarter.ui.compose.TextBody2Style
import com.kickstarter.ui.compose.TextCaptionStyle
import com.kickstarter.ui.compose.designsystem.KSTheme

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFF0EAE2, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, backgroundColor = 0X00000000, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KsCreatorLayoutPreview() {
    KSTheme {
        Column {
            KsCreatorLayout("Creator Studio", "http://goo.gl/gEgYUd")
        }
    }
}

@Composable
fun KsCreatorLayout(
    creatorName: String?,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.clickable { onClickAction() }) {
        CircleImageFromURl(
            imageUrl = (imageUrl),
            modifier = Modifier.size(dimensionResource(id = R.dimen.project_avatar_height))
        )
        Column(modifier = Modifier.padding(start = dimensionResource(id = R.dimen.grid_1))) {
            TextCaptionStyle(
                text = stringResource(R.string.project_menu_created_by),
                modifier = Modifier
            )
            TextBody2Style(text = creatorName.orEmpty(), modifier = Modifier)
        }
    }
}
