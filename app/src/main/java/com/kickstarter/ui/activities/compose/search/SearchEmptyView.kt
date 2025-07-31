package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.ui.compose.designsystem.KSButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchEmptyViewPreviewWithFilter() {
    KSTheme {
        SearchEmptyView()
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchEmptyViewPreviewWithoutFilter() {
    KSTheme {
        SearchEmptyView(
            currentSearchTerm = "cat",
            activeFilters = true
        )
    }
}

@Composable
fun SearchEmptyView(
    modifier: Modifier = Modifier,
    environment: Environment? = null,
    currentSearchTerm: String = "",
    onClick: () -> Unit = {},
    activeFilters: Boolean = false
) {

    var title = if (currentSearchTerm.isNotEmpty()) {
        environment?.ksString()?.format(
            stringResource(id = R.string.No_results_for),
            "query",
            currentSearchTerm
        ) ?: stringResource(id = R.string.No_Results)
    } else {
        stringResource(id = R.string.No_Results)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colors.kds_white),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(dimensions.paddingTripleLarge))

        Text(
            text = title,
            style = typographyV2.headingXL,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        val text = if (activeFilters) {
            stringResource(id = R.string.Try_rephrasing_your_search_or_adjusting_the_filters)
        } else {
            stringResource(id = R.string.Try_rephrasing_your_search)
        }

        Text(
            text = text,
            style = typographyV2.body,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = dimensions.paddingSmall),
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        if (activeFilters) {
            KSButton(
                backgroundColor = colors.kds_black,
                textColor = colors.kds_white,
                onClickAction = {
                    onClick()
                },
                shape = RoundedCornerShape(size = KSTheme.dimensions.radiusExtraSmall),
                text = stringResource(id = R.string.Remove_all_filters),
                textStyle = typographyV2.buttonLabel,
                isEnabled = true,
                shouldWrapContentWidth = true
            )
        }
    }
}
