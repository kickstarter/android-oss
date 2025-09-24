package com.kickstarter.ui.activities.compose

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.viewmodels.ReportProjectViewModel
import io.reactivex.Observable

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FormularyScreenPreview() {
    KSTheme {

        val inputs = object : ReportProjectViewModel.Inputs {
            override fun createFlagging() {}
            override fun inputDetails(s: String) {}
            override fun kind(kind: String) {}
            override fun openExternalBrowser(tag: String) {}
        }

        val outputs = object : ReportProjectViewModel.Outputs {
            override fun projectUrl(): Observable<String> = Observable.empty()
            override fun email(): Observable<String> = Observable.empty()
            override fun finish(): Observable<ReportProjectViewModel.ReportProjectViewModel.NavigationResult> = Observable.empty()
            override fun progressBarIsVisible(): Observable<Boolean> = Observable.empty()
            override fun openExternalBrowserWithUrl(): Observable<String> = Observable.empty()
        }

        FormularyScreen(
            callback = {},
            inputs = inputs,
            outputs = outputs
        )
    }
}

@Composable
fun FormularyScreen(
    callback: () -> Unit = {},
    inputs: ReportProjectViewModel.Inputs,
    outputs: ReportProjectViewModel.Outputs
) {
    if (outputs.finish().subscribeAsState(initial = ReportProjectViewModel.ReportProjectViewModel.NavigationResult(false, "")).value.hasFinished) {
        callback()
    }

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .animateContentSize()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(colors.kds_white),
        horizontalAlignment = Alignment.End
    ) {

        if (outputs.progressBarIsVisible().subscribeAsState(initial = false).value) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = colors.kds_create_700,
                trackColor = colors.kds_create_300,
                gapSize = 0.dp,
                strokeCap = StrokeCap.Butt
            )
        } else {
            Spacer(
                modifier = Modifier
                    .height(dimensions.linearProgressBarHeight)
                    .background(colors.kds_white)
            )
        }

        TextField(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.grid_3),
                    vertical = dimensionResource(id = R.dimen.grid_3)
                ),
            value = outputs.email().subscribeAsState(initial = "").value,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.Email)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.kds_support_200,
                unfocusedContainerColor = colors.kds_support_200,
                disabledContainerColor = colors.kds_support_200,
                errorContainerColor = colors.kds_support_200,
                focusedTextColor = colors.kds_support_700,
                unfocusedTextColor = colors.kds_support_700,
                disabledTextColor = colors.textDisabled,
                focusedLabelColor = colors.kds_create_700,
                unfocusedLabelColor = colors.kds_support_700,
                errorLabelColor = colors.kds_alert,
                focusedIndicatorColor = colors.kds_create_700,
                unfocusedIndicatorColor = colors.kds_support_700,
                errorIndicatorColor = colors.kds_alert,
                cursorColor = colors.kds_create_700,
                errorCursorColor = colors.kds_alert
            )
        )

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.grid_3),
                    vertical = dimensionResource(id = R.dimen.grid_1)
                ),
            value = outputs.projectUrl().subscribeAsState(initial = "").value,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = stringResource(id = R.string.Project_url)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.kds_support_200,
                unfocusedContainerColor = colors.kds_support_200,
                disabledContainerColor = colors.kds_support_200,
                errorContainerColor = colors.kds_support_200,
                focusedTextColor = colors.kds_support_700,
                unfocusedTextColor = colors.kds_support_700,
                disabledTextColor = colors.textDisabled,
                focusedLabelColor = colors.kds_create_700,
                unfocusedLabelColor = colors.kds_support_700,
                errorLabelColor = colors.kds_alert,
                focusedIndicatorColor = colors.kds_create_700,
                unfocusedIndicatorColor = colors.kds_support_700,
                errorIndicatorColor = colors.kds_alert,
                cursorColor = colors.kds_create_700,
                errorCursorColor = colors.kds_alert
            )
        )

        var details by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        OutlinedTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.grid_3),
                    vertical = dimensionResource(id = R.dimen.grid_1)
                )
                .fillMaxWidth(),
            value = details,
            onValueChange = {
                inputs.inputDetails(it)
                details = it
            },
            label = { Text(stringResource(id = R.string.Tell_us_more_details)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.kds_support_200,
                unfocusedContainerColor = colors.kds_support_200,
                disabledContainerColor = colors.kds_support_200,
                errorContainerColor = colors.kds_support_200,
                focusedTextColor = colors.kds_support_700,
                unfocusedTextColor = colors.kds_support_700,
                disabledTextColor = colors.textDisabled,
                focusedLabelColor = colors.kds_create_700,
                unfocusedLabelColor = colors.kds_support_700,
                errorLabelColor = colors.kds_alert,
                focusedIndicatorColor = colors.kds_create_700,
                unfocusedIndicatorColor = colors.kds_support_700,
                errorIndicatorColor = colors.kds_alert,
                cursorColor = colors.kds_create_700,
                errorCursorColor = colors.kds_alert
            ),
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Button(
            modifier = Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.grid_3)),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.kds_create_700,
                contentColor = colors.kds_white
            ),
            enabled = details.isNotEmpty(),
            onClick = {
                inputs.createFlagging()
            }
        ) {
            Text(
                modifier = Modifier
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.grid_2)
                    ),
                text = stringResource(id = R.string.Send)
            )
        }
    }
}
