package com.kickstarter.ui.activities.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.viewmodels.ReportProjectViewModel
import io.reactivex.Observable

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun FormularyScreenPreview() {
    MaterialTheme {

        val inputs = object : ReportProjectViewModel.Inputs {
            override fun createFlagging() {}
            override fun inputDetails(s: String) {}
            override fun kind(kind: String) {}
        }

        val outputs = object : ReportProjectViewModel.Outputs {
            override fun projectUrl(): Observable<String> = Observable.empty()
            override fun email(): Observable<String> = Observable.empty()
            override fun finish(): Observable<Pair<Boolean, String>> = Observable.empty()
            override fun progressBarIsVisible(): Observable<Boolean> = Observable.empty()
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
    if (outputs.finish().subscribeAsState(initial = Pair(false, "")).value.first) {
        callback()
    }

    Column(
        modifier = Modifier
            .animateContentSize()
            .verticalScroll(rememberScrollState())
            .background(colorResource(id = R.color.kds_white)),
        horizontalAlignment = Alignment.End
    ) {

        if (outputs.progressBarIsVisible().subscribeAsState(initial = false).value) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(id = R.color.kds_create_700),
                backgroundColor = colorResource(id = R.color.kds_create_300)
            )
        } else {
            Spacer(
                modifier = Modifier
                    .height(ProgressIndicatorDefaults.StrokeWidth)
                    .background(colorResource(id = R.color.kds_white))
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
            label = { Text(stringResource(id = R.string.email)) },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = colorResource(id = R.color.kds_create_700),
                focusedLabelColor = colorResource(id = R.color.kds_create_700)
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
                    text = stringResource(id = R.string.FPO_Project_url)
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = colorResource(id = R.color.kds_create_700),
                focusedLabelColor = colorResource(id = R.color.kds_create_700)
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
            label = { Text(stringResource(id = R.string.FPO_Details)) },
            placeholder = {
                Text(stringResource(id = R.string.FPO_Details_placeholder))
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = colorResource(id = R.color.kds_create_700),
                focusedLabelColor = colorResource(id = R.color.kds_create_700),
                cursorColor = colorResource(id = R.color.kds_create_700),
            ),
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Button(
            modifier = Modifier
                .padding(horizontal = dimensionResource(id = R.dimen.grid_3)),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.kds_create_700),
                contentColor = colorResource(id = R.color.kds_white)
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
