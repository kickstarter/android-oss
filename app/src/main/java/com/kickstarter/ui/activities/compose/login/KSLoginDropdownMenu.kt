package com.kickstarter.ui.activities.compose.login

import androidx.compose.foundation.background
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme

enum class LoginDropdownTestTag {
    OPTIONS_MENU,
    OPTIONS_TERMS,
    OPTIONS_PRIVACY_POLICY,
    OPTIONS_COOKIE,
    OPTIONS_HELP
}

@Composable
fun KSLoginDropdownMenu(
    expanded: Boolean,
    onDismissed: () -> Unit,
    onTermsOfUseClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onCookiePolicyClicked: () -> Unit,
    onHelpClicked: () -> Unit

) {
    DropdownMenu(
        modifier = Modifier
            .background(color = KSTheme.colors.kds_support_100)
            .testTag(LoginDropdownTestTag.OPTIONS_MENU.name),
        expanded = expanded,
        onDismissRequest = { onDismissed.invoke() }
    ) {
        DropdownMenuItem(
            onClick = {
                onTermsOfUseClicked.invoke()
                onDismissed.invoke()
            }
        ) {
            Text(
                modifier = Modifier.testTag(LoginDropdownTestTag.OPTIONS_TERMS.name),
                text = stringResource(id = R.string.login_tout_help_sheet_terms),
                color = KSTheme.colors.kds_support_700
            )
        }
        DropdownMenuItem(
            onClick = {
                onPrivacyPolicyClicked.invoke()
                onDismissed.invoke()
            }
        ) {
            Text(
                modifier = Modifier.testTag(LoginDropdownTestTag.OPTIONS_PRIVACY_POLICY.name),
                text = stringResource(id = R.string.login_tout_help_sheet_privacy),
                color = KSTheme.colors.kds_support_700
            )
        }
        DropdownMenuItem(
            onClick = {
                onCookiePolicyClicked.invoke()
                onDismissed.invoke()
            }
        ) {
            Text(
                modifier = Modifier.testTag(LoginDropdownTestTag.OPTIONS_COOKIE.name),
                text = stringResource(id = R.string.login_tout_help_sheet_cookie),
                color = KSTheme.colors.kds_support_700
            )
        }
        DropdownMenuItem(
            onClick = {
                onHelpClicked.invoke()
                onDismissed.invoke()
            }
        ) {
            Text(
                modifier = Modifier.testTag(LoginDropdownTestTag.OPTIONS_HELP.name),
                text = stringResource(id = R.string.general_navigation_buttons_help),
                color = KSTheme.colors.kds_support_700
            )
        }
    }
}
