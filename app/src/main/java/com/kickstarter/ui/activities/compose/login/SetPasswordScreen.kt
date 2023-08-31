package com.kickstarter.ui.activities.compose.login

import android.content.res.Configuration
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SetPasswordPreview() {

}

@Composable
fun SetPasswordScreen(
        onBackClicked: () -> Unit,
        onAcceptButtonClicked: (currentPass: String, newPass: String) -> Unit,
        showProgressBar: Boolean,
        scaffoldState: ScaffoldState
) {

}