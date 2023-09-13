package com.kickstarter.ui.activities.compose.login

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHeadsupSnackbar
import com.kickstarter.ui.compose.designsystem.KSLinearProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryGreyButton
import com.kickstarter.ui.compose.designsystem.KSSuccessSnackbar
import com.kickstarter.ui.compose.designsystem.KSTextInput
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.toolbars.compose.TopToolBar

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun TwoFactorScreenPreview() {
    KSTheme {
        TwoFactorScreen(
            rememberScaffoldState(),
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            false
        )
    }
}

@Composable
fun TwoFactorScreen(
    scaffoldState: ScaffoldState,
    onBackClicked: () -> Unit,
    onTermsOfUseClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onCookiePolicyClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    onResendClicked: () -> Unit,
    onSubmitClicked: (String) -> Unit,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }
    var submitEnabled = code.isNotEmpty()
    Scaffold(
        topBar = {
            TopToolBar(
                title = stringResource(id = R.string.two_factor_title),
                titleColor = colors.kds_support_700,
                titleModifier = Modifier.testTag(LoginToutTestTag.PAGE_TITLE.name),
                leftOnClickAction = onBackClicked,
                leftIconColor = colors.kds_support_700,
                leftIconModifier = Modifier.testTag(LoginToutTestTag.BACK_BUTTON.name),
                backgroundColor = colors.kds_white,
                right = {
                    IconButton(
                        modifier = Modifier.testTag(LoginToutTestTag.OPTIONS_ICON.name),
                        onClick = { expanded = !expanded },
                        enabled = true
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(
                                    id = R.string.general_navigation_accessibility_button_help_menu_label
                                ),
                                tint = colors.kds_black
                            )

                            KSLoginDropdownMenu(
                                expanded = expanded,
                                onDismissed = { expanded = !expanded },
                                onTermsOfUseClicked = onTermsOfUseClicked,
                                onPrivacyPolicyClicked = onPrivacyPolicyClicked,
                                onCookiePolicyClicked = onCookiePolicyClicked,
                                onHelpClicked = onHelpClicked
                            )
                        }
                    }
                }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                hostState = scaffoldState.snackbarHostState,
                snackbar = { data ->
                    if (data.actionLabel == "error"){
                        KSErrorSnackbar(text = data.message)
                    } else {
                        KSSuccessSnackbar(text = data.message)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .background(colors.kds_white)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    PaddingValues(
                        start = dimensions.paddingLarge,
                        end = dimensions.paddingLarge
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                KSLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(dimensions.paddingDoubleLarge))

            Text(
                text = stringResource(id = R.string.two_factor_message),
                style = typography.subheadline,
                color = colors.kds_support_700
            )

            Spacer(modifier = Modifier.height(dimensions.paddingLarge))

            KSTextInput(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(id = R.string.two_factor_code_placeholder),
                onValueChanged = { code = it }
            )

            Spacer(modifier = Modifier.height(dimensions.paddingXLarge))

            Row {
                KSSecondaryGreyButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onClickAction = onResendClicked,
                    isEnabled = true,
                    text = stringResource(id = R.string.two_factor_buttons_resend),
                )

                Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))

                KSPrimaryGreenButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onClickAction = { onSubmitClicked(code) },
                    text = stringResource(id = R.string.two_factor_buttons_submit),
                    isEnabled = submitEnabled
                )
            }
        }
    }
}