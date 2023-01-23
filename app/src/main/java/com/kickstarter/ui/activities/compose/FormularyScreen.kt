package com.kickstarter.ui.activities.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun FormularyScreenPreview() {
    MaterialTheme {
        FormularyScreen()
    }
}

@Composable
fun FormularyScreen() {
    Column(
        modifier = Modifier
            .animateContentSize()
            .verticalScroll(rememberScrollState())
            .background(colorResource(id = R.color.kds_white))
            .padding(vertical = dimensionResource(id = R.dimen.grid_2)),
        horizontalAlignment = Alignment.End
    ) {
        var details by remember { mutableStateOf("") }

        TextField(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.grid_3),
                    vertical = dimensionResource(id = R.dimen.grid_1)
                ),
            // TODO: populate from viewModel
            value = "arkariang@gmail.com",
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
            // TODO: populate from viewModel
            value = "https://staging.kickstarter.com/projects/weirdcitygames/leaf-1?ref=section-homepage-view-more-recommendations-p1",
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

        val focusRequester = remember { FocusRequester() }
        OutlinedTextField(
            modifier =
            Modifier
                .focusRequester(focusRequester)
                .padding(
                    horizontal = dimensionResource(id = R.dimen.grid_3),
                    vertical = dimensionResource(id = R.dimen.grid_1)
                )
                .fillMaxWidth(),
            value = details,
            onValueChange = { details = it },
            label = { Text(stringResource(id = R.string.FPO_Details)) },
            placeholder = {
                // TODO: populate from viewModel
                Text("Please provide more details why you are reporting this project")
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = colorResource(id = R.color.kds_create_700),
                focusedLabelColor = colorResource(id = R.color.kds_create_700),
                cursorColor = colorResource(id = R.color.kds_create_700),
            )
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
            onClick = {
                // TODO: call viewModel to mutation
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
