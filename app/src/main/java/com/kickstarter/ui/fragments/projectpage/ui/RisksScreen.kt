package com.kickstarter.ui.fragments.projectpage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R

/**
 * Screen representing the UI loading on [com.kickstarter.ui.fragments.projectpage.ProjectRiskFragment]
 * built in compose
 *
 * @param riskDescState mutable state holding the risks description state, this is the only dynamic piece
 * for this UI.
 *
 * @param callback callback attached to an onClick event
 */
@Composable
fun RisksScreen(
    riskDescState: State<String>,
    callback: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.Risks_and_challenges),
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            modifier = Modifier
                .paddingFromBaseline(
                    top = dimensionResource(id = R.dimen.grid_8),
                    bottom = dimensionResource(id = R.dimen.grid_4)
                )
                .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
        )
        Text(
            text = riskDescState.value,
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .paddingFromBaseline(
                    top = dimensionResource(id = R.dimen.grid_3)
                )
                .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.grid_3),
                    vertical = dimensionResource(id = R.dimen.grid_3)
                )
                .background(
                    color = colorResource(id = R.color.kds_support_300)
                )
                .height(1.dp)
        )
        ClickableText(
            text = AnnotatedString(
                text = stringResource(id = R.string.Learn_about_accountability_on_Kickstarter),
                spanStyle = SpanStyle(
                    color = colorResource(id = R.color.kds_create_700),
                    textDecoration = TextDecoration.Underline,
                )
            ),
            onClick = {
                callback()
            },
            modifier = Modifier
                .paddingFromBaseline(
                    top = dimensionResource(id = R.dimen.grid_3),
                    bottom = dimensionResource(id = R.dimen.grid_5)
                )
                .padding(horizontal = dimensionResource(id = R.dimen.grid_3))
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0EAE2)
@Composable
fun ProjectRisksPreview() {
    MaterialTheme {
        // - Mock state for the preview
        val desc = stringResource(id = R.string.risk_description)
        val riskDesc = remember { mutableStateOf(desc) }
        RisksScreen(
            riskDescState = riskDesc,
            callback = {}
        )
    }
}
