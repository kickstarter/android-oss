package com.kickstarter.ui.activities.compose.login

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
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
        expanded = expanded,
        onDismissRequest = { onDismissed.invoke() },
        modifier = Modifier.testTag(LoginDropdownTestTag.OPTIONS_MENU.name),
        containerColor = KSTheme.colors.kds_support_100
    ) {
        val itemColors = MenuDefaults.itemColors(
            textColor = KSTheme.colors.kds_support_700
        )

        DropdownMenuItem(
            onClick = {
                onTermsOfUseClicked.invoke()
                onDismissed.invoke()
            },
            colors = itemColors,
            text = {
                Text(
                    modifier = Modifier.testTag(LoginDropdownTestTag.OPTIONS_TERMS.name),
                    text = stringResource(R.string.login_tout_help_sheet_terms)
                )
            }
        )
        DropdownMenuItem(
            onClick = {
                onPrivacyPolicyClicked.invoke()
                onDismissed.invoke()
            },
            colors = itemColors,
            text = {
                Text(
                    modifier = Modifier.testTag(LoginDropdownTestTag.OPTIONS_PRIVACY_POLICY.name),
                    text = stringResource(R.string.login_tout_help_sheet_privacy)
                )
            }
        )
        DropdownMenuItem(
            onClick = {
                onCookiePolicyClicked.invoke()
                onDismissed.invoke()
            },
            colors = itemColors,
            text = {
                Text(
                    modifier = Modifier.testTag(LoginDropdownTestTag.OPTIONS_COOKIE.name),
                    text = stringResource(R.string.login_tout_help_sheet_cookie)
                )
            }
        )
        DropdownMenuItem(
            onClick = {
                onHelpClicked.invoke()
                onDismissed.invoke()
            },
            colors = itemColors,
            text = {
                Text(
                    modifier = Modifier.testTag(LoginDropdownTestTag.OPTIONS_HELP.name),
                    text = stringResource(R.string.general_navigation_buttons_help),
                )
            },
        )
    }
}
