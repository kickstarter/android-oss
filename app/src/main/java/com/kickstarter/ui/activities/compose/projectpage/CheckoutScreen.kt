package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.hrefUrlFromTranslation
import com.kickstarter.libs.utils.extensions.hrefUrlListFromTranslation
import com.kickstarter.libs.utils.extensions.stringsFromHtmlTranslation
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.compose.TextWithClickableLink
import com.kickstarter.ui.activities.compose.login.LoginToutTestTag
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSGreenBadge
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.kds_black
import com.kickstarter.ui.compose.designsystem.kds_support_400
import com.kickstarter.ui.compose.designsystem.kds_support_700
import com.kickstarter.ui.compose.designsystem.kds_white
import okhttp3.internal.cookieToString

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun CheckoutScreenPreview() {
    KSTheme {
        CheckoutScreen(
                isCTAButtonEnabled = true,
                rewardsList = (1..6).map {
                    Pair("Cool Item $it", "$20")
                },
                shippingAmount = "$5",
                initialShippingLocation = "United States",
                totalAmount = "$55",
                totalAmountCurrencyConverted = "About $",
                ctaButtonText = "Pledge",
                initialBonusSupport = "",
                totalBonusSupport = "",
                isPostCampaignBacking = true,
                deliveryDateString = stringResource(id = R.string.Estimated_delivery) + " May 2024",
                onRewardSelectClicked = { }
        )
    }
}

@Composable
fun CheckoutScreen(
        isCTAButtonEnabled: Boolean,
        ctaButtonText: String,
        ksString: KSString? = null,
        rewardsList: List<Pair<String, String>> = listOf(),
        shippingAmount: String = "",
        initialShippingLocation: String? = null,
        totalAmount: String,
        totalAmountCurrencyConverted: String = "",
        initialBonusSupport: String,
        totalBonusSupport: String,
        deliveryDateString: String = "",
        isPostCampaignBacking : Boolean = false,
        onRewardSelectClicked: () -> Unit
) {

    val scrollState = rememberScrollState()

    Scaffold(
            backgroundColor = colors.backgroundAccentGraySubtle,
            modifier = Modifier
                    .background(kds_white)
                    .fillMaxWidth(),
            bottomBar = {
                Column {
                    Surface(
                            modifier = Modifier
                                    .fillMaxWidth(),
                            shape = RoundedCornerShape(
                                    topStart = dimensions.radiusLarge,
                                    topEnd = dimensions.radiusLarge
                            ),
                            color = colors.backgroundSurfacePrimary,
                            elevation = dimensions.elevationLarge,
                    ) {
                        Column(modifier = Modifier
                                .background(colors.kds_white)
                                .padding(bottom = dimensions.paddingMediumLarge, start = dimensions.paddingMediumLarge, end = dimensions.paddingMediumLarge, top = dimensions.paddingMediumLarge)
                        ) {

                            KSPrimaryGreenButton(
                                    modifier = Modifier
                                            .padding(bottom = dimensions.paddingMediumSmall)
                                            .fillMaxWidth(),
                                    onClickAction = onRewardSelectClicked,
                                    isEnabled = isCTAButtonEnabled,
                                    text = ctaButtonText
                            )

                            if (isPostCampaignBacking) {
                                Text(text = "Your payment method will be charged immediately upon pledge. You’ll receive a confirmation email at %{email} when your rewards are ready to fulfill so that you can finalize and pay shipping and tax.", textAlign = TextAlign.Center,
                                        style = typography.caption2, color = colors.kds_support_400)

                                Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))
                            }
                            TermsOfUseClickableText(
                                    onPrivacyPolicyClicked = {},
                                    onCookiePolicyClicked = {},
                                    onTermsOfUseClicked = {}
                            )
                        }
                    }
                }
            },

    ) { padding ->
            LazyColumn {
                   item {
                       Text(

                               modifier = Modifier.padding(start = dimensions.paddingMediumLarge, top = dimensions.paddingMediumLarge),
                               text = "Checkout",
                               style = typography.title3Bold,
                               color = colors.kds_black,
                       )
                       Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))
                   }

                item {
                    Card(
                           modifier = Modifier.padding(start = dimensions.paddingMediumLarge, end = dimensions.paddingMediumLarge),
                            shape = RoundedCornerShape(
                                    bottomStart = dimensions.radiusMediumLarge,
                                    bottomEnd = dimensions.radiusMediumLarge,
                                    topStart = dimensions.radiusMediumLarge,
                                    topEnd = dimensions.radiusMediumLarge
                            ),
                            backgroundColor = colors.kds_support_200,
                    ) {
                        Row(modifier = Modifier.padding(dimensions.paddingSmall)) {
                            Icon(
                                    modifier = Modifier
                                            .padding(start = dimensions.paddingMediumSmall, end = dimensions.paddingLarge)
                                            .align(Alignment.CenterVertically),
                                    painter = painterResource(id = R.drawable.ic_not_a_store),
                                    contentDescription = null,
                                    tint = colors.textAccentGreen
                            )

                            Column {

                                Text(modifier = Modifier.padding(bottom = dimensions.paddingXSmall, top = dimensions.paddingXSmall),
                                        text = stringResource(id = R.string.Kickstarter_is_not_a_store), style = typography.body2Medium, color = colors.kds_support_400)
                                TextWithClickableAccountabilityLink(
                                        padding = dimensions.paddingXSmall,
                                        html = stringResource(id = R.string.Its_a_way_to_bring_creative_projects_to_life_Learn_more_about_accountability),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))
                }

        if (rewardsList.isNotEmpty()) {
            item {
                ItemizedRewardListContainer(
                        ksString = ksString,
                        rewardsList = rewardsList,
                        shippingAmount = shippingAmount,
                        initialShippingLocation = initialShippingLocation,
                        totalAmount = totalAmount,
                        totalAmountCurrencyConverted = totalAmountCurrencyConverted,
                        initialBonusSupport = initialBonusSupport,
                        totalBonusSupport = totalBonusSupport,
                        deliveryDateString = deliveryDateString
                )
            }
        }
                else {
                    item {
                        ItemizedRewardListContainer(
                                totalAmount = totalAmount,
                                totalAmountCurrencyConverted = totalAmountCurrencyConverted,
                                rewardsList = (1..1).map {
                                    Pair(stringResource(id = R.string.Pledge_without_a_reward), totalAmount)
                    },
                                initialBonusSupport = "",
                                totalBonusSupport = ""
                        )
                    }
        }
    }
    }
}


@Composable
fun TermsOfUseClickableText(
        onTermsOfUseClicked: () -> Unit,
        onPrivacyPolicyClicked: () -> Unit,
        onCookiePolicyClicked: () -> Unit
) {
    val formattedText = HtmlCompat.fromHtml(
            stringResource(id = R.string.By_pledging_you_agree_to_Kickstarters_Terms_of_Use_Privacy_Policy_and_Cookie_Policy),
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
                            color = colors.textAccentGreen
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
                            color = colors.textAccentGreen
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
                            color = colors.textAccentGreen
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
            style = typography.caption2.copy(
                    color = colors.kds_support_400,
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

@Composable
fun TextWithClickableAccountabilityLink(
        padding: Dp,
        html: String,
        onClickCallback: (String?) -> Unit = {}
) {

    val stringList = html.stringsFromHtmlTranslation()
    val annotation = html.hrefUrlFromTranslation()
    if (stringList.size == 3) {
        val annotatedText = buildAnnotatedString {
            append(stringList.first())
            pushStringAnnotation(
                    tag = annotation,
                    annotation = stringList[1]
            )
            withStyle(
                    style = SpanStyle(
                            color = colors.kds_create_700,
                    )
            ) {
                append(stringList[1])
            }
            append(stringList.last())

            pop()
        }

        ClickableText(
                modifier = Modifier.padding(bottom = padding),
                text = annotatedText,
                style = TextStyle(
                        fontWeight = FontWeight(400),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        color = colors.kds_support_400
                ),
                onClick = {
                    annotatedText.getStringAnnotations(
                            tag = annotation, start = it,
                            end = it
                    )
                            .firstOrNull()?.let { annotation ->
                                onClickCallback(annotation.tag)
                            } ?: onClickCallback(null)
                }
        )
    }
}




