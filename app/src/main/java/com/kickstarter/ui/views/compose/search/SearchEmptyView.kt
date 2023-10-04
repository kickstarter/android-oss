package com.kickstarter.ui.views.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchEmptyViewPreview() {
    KSTheme {
        SearchEmptyView()
    }
}

@Composable
fun SearchEmptyView(
    modifier: Modifier = Modifier,
    environment: Environment? = null,
    currentSearchTerm: String = ""
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colors.kds_white),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(dimensions.paddingTripleLarge))

        Text(
            text = stringResource(id = R.string.No_Results),
            style = typography.body,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        environment?.ksString()?.let { ksString ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = dimensions.paddingLarge, end = dimensions.paddingLarge),
                textAlign = TextAlign.Center,
                text = ksString.format(
                    stringResource(id = R.string.We_couldnt_find_anything_for_search_term),
                    "search_term",
                    currentSearchTerm
                ),
                style = typography.body2,
                color = colors.textSecondary
            )
        }
    }
}
