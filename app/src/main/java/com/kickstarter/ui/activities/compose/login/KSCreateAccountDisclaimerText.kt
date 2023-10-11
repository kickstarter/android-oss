package com.kickstarter.ui.activities.compose.login

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat
import com.kickstarter.R
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.compose.designsystem.KSTheme

@Composable
fun LogInSignUpClickableDisclaimerText(
    onTermsOfUseClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onCookiePolicyClicked: () -> Unit
) {
    val formattedText = HtmlCompat.fromHtml(
        stringResource(id = R.string.login_tout_disclaimer_agree_to_terms_html),
        0
    ).toString()

    val annotatedLinkString = buildAnnotatedString {
        val termsOfUseString =
            stringResource(id = R.string.login_tout_help_sheet_terms).lowercase()
        val termsOfUseStartIndex = formattedText.indexOf(
            string = termsOfUseString,
            ignoreCase = true
        )
        val termsOfUserEndIndex = termsOfUseStartIndex + termsOfUseString.length

        val privacyPolicyString =
            stringResource(id = R.string.login_tout_help_sheet_privacy).lowercase()
        val privacyPolicyStartIndex = formattedText.indexOf(
            string = privacyPolicyString,
            ignoreCase = true
        )
        val privacyPolicyEndIndex = privacyPolicyStartIndex + privacyPolicyString.length

        val cookiePolicyString =
            stringResource(id = R.string.login_tout_help_sheet_cookie).lowercase()
        val cookiePolicyStartIndex = formattedText.indexOf(
            string = cookiePolicyString,
            ignoreCase = true
        )
        val cookiePolicyEndIndex = cookiePolicyStartIndex + cookiePolicyString.length

        append(formattedText)

        if (termsOfUseStartIndex != -1) {
            addStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline
                ),
                start = termsOfUseStartIndex,
                end = termsOfUserEndIndex
            )

            addStringAnnotation(
                tag = DisclaimerItems.TERMS.name,
                annotation = "",
                start = termsOfUseStartIndex,
                end = termsOfUserEndIndex
            )
        }

        if (privacyPolicyStartIndex != -1) {
            addStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline
                ),
                start = privacyPolicyStartIndex,
                end = privacyPolicyEndIndex
            )

            addStringAnnotation(
                tag = DisclaimerItems.PRIVACY.name,
                annotation = "",
                start = privacyPolicyStartIndex,
                end = privacyPolicyEndIndex
            )
        }

        if (cookiePolicyStartIndex != -1) {
            addStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline
                ),
                start = cookiePolicyStartIndex,
                end = cookiePolicyEndIndex
            )

            addStringAnnotation(
                tag = DisclaimerItems.COOKIES.name,
                annotation = "",
                start = cookiePolicyStartIndex,
                end = cookiePolicyEndIndex
            )
        }
    }

    ClickableText(
        modifier = Modifier.testTag(LoginToutTestTag.TOU_PP_COOKIE_DISCLAIMER.name),
        text = annotatedLinkString,
        style = KSTheme.typography.caption1.copy(
            color = KSTheme.colors.kds_support_400,
            textAlign = TextAlign.Center
        ),
        onClick = { index ->
            annotatedLinkString.getStringAnnotations(index, index)
                .firstOrNull()?.let { annotation ->
                    when (annotation.tag) {
                        DisclaimerItems.TERMS.name -> {
                            onTermsOfUseClicked.invoke()
                        }

                        DisclaimerItems.PRIVACY.name -> {
                            onPrivacyPolicyClicked.invoke()
                        }

                        DisclaimerItems.COOKIES.name -> {
                            onCookiePolicyClicked.invoke()
                        }
                    }
                }
        }
    )
}
