package com.kickstarter.ui.activities.compose.projectpage

import CollectionPlan
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.acceptedCardType
import com.kickstarter.libs.utils.extensions.hrefUrlFromTranslation
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.stringsFromHtmlTranslation
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.getCardTypeDrawable
import com.kickstarter.models.extensions.isFromPaymentSheet
import com.kickstarter.type.CreditCardTypes
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.compose.login.LoginToutTestTag
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSRadioButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.kds_white
import com.kickstarter.ui.compose.designsystem.shapes
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.views.compose.checkout.ItemizedRewardListContainer
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun CheckoutScreenPreview() {
    KSTheme {
        CheckoutScreen(
            rewardsList = (1..6).map {
                Pair("Cool Item $it", "$20")
            },
            environment = Environment.Builder().build(),
            shippingAmount = 4.0,
            selectedReward = RewardFactory.rewardWithShipping(),
            currentShippingRule = ShippingRule.builder().build(),
            totalAmount = 60.0,
            totalBonusSupport = 5.0,
            storedCards = listOf(
                StoredCardFactory.visa(), StoredCardFactory.discoverCard(), StoredCardFactory.visa()
            ),
            project =
            Project.builder()
                .currency("USD")
                .currentCurrency("USD")
                .state(Project.STATE_LIVE)
                .availableCardTypes(
                    listOf(
                        CreditCardTypes.AMEX.rawValue,
                        CreditCardTypes.MASTERCARD.rawValue,
                        CreditCardTypes.VISA.rawValue
                    )
                )
                .build(),
            email = "example@example.com",
            pledgeReason = PledgeReason.PLEDGE,
            rewardsHaveShippables = true,
            onPledgeCtaClicked = { },
            newPaymentMethodClicked = { },
            onDisclaimerItemClicked = {},
            onAccountabilityLinkClicked = {},
            onChangedPaymentMethod = {},
            isPlotEnabled = false
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun CheckoutScreenIsPlotEnabledPreview() {
    KSTheme {
        CheckoutScreen(
            rewardsList = (1..6).map {
                Pair("Cool Item $it", "$20")
            },
            environment = Environment.Builder().build(),
            shippingAmount = 4.0,
            selectedReward = RewardFactory.rewardWithShipping(),
            currentShippingRule = ShippingRule.builder().build(),
            totalAmount = 60.0,
            totalBonusSupport = 5.0,
            storedCards = listOf(
                StoredCardFactory.visa(), StoredCardFactory.discoverCard(), StoredCardFactory.visa()
            ),
            project =
            Project.builder()
                .currency("USD")
                .currentCurrency("USD")
                .state(Project.STATE_LIVE)
                .availableCardTypes(
                    listOf(
                        CreditCardTypes.AMEX.rawValue,
                        CreditCardTypes.MASTERCARD.rawValue,
                        CreditCardTypes.VISA.rawValue
                    )
                )
                .build(),
            email = "example@example.com",
            pledgeReason = PledgeReason.PLEDGE,
            rewardsHaveShippables = true,
            onPledgeCtaClicked = { },
            newPaymentMethodClicked = { },
            onDisclaimerItemClicked = {},
            onAccountabilityLinkClicked = {},
            onChangedPaymentMethod = {},
            isPlotEnabled = true
        )
    }
}
@Composable
fun CheckoutScreen(
    storedCards: List<StoredCard> = listOf(),
    environment: Environment,
    selectedReward: Reward? = null,
    project: Project,
    email: String?,
    ksString: KSString? = null,
    selectedRewardsAndAddOns: List<Reward> = listOf(),
    rewardsList: List<Pair<String, String>> = listOf(),
    shippingAmount: Double = 0.0,
    pledgeReason: PledgeReason,
    totalAmount: Double,
    currentShippingRule: ShippingRule?,
    totalBonusSupport: Double = 0.0,
    rewardsHaveShippables: Boolean,
    isLoading: Boolean = false,
    onPledgeCtaClicked: (selectedCard: StoredCard?) -> Unit,
    newPaymentMethodClicked: () -> Unit,
    onDisclaimerItemClicked: (disclaimerItem: DisclaimerItems) -> Unit,
    onAccountabilityLinkClicked: () -> Unit,
    onChangedPaymentMethod: (StoredCard?) -> Unit = {},
    isPlotEnabled: Boolean
) {
    val selectedOption = remember {
        mutableStateOf(
            storedCards.firstOrNull {
                project.acceptedCardType(it.type()) || it.isFromPaymentSheet()
            }
        )
    }

    val onOptionSelected: (StoredCard?) -> Unit = {
        selectedOption.value = it
        onChangedPaymentMethod.invoke(it)
    }

    val totalAmountString = environment.ksCurrency()?.let {
        RewardViewUtils.styleCurrency(
            totalAmount,
            project,
            it
        ).toString()
    } ?: ""

    // - After adding new payment method, selected card should be updated to the newly added
    UpdateSelectedCardIfNewCardAdded(
        remember { mutableStateOf(storedCards.size) },
        storedCards,
        onOptionSelected
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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
                        Column(
                            modifier = Modifier
                                .background(colors.kds_white)
                                .padding(
                                    bottom = dimensions.paddingMediumLarge,
                                    start = dimensions.paddingMediumLarge,
                                    end = dimensions.paddingMediumLarge,
                                    top = dimensions.paddingMediumLarge
                                )
                        ) {

                            KSPrimaryGreenButton(
                                modifier = Modifier
                                    .padding(bottom = dimensions.paddingMediumSmall)
                                    .fillMaxWidth(),
                                onClickAction = { onPledgeCtaClicked(selectedOption.value) },
                                isEnabled = project.acceptedCardType(selectedOption.value?.type()) || selectedOption.value?.isFromPaymentSheet() ?: false,
                                text = if (pledgeReason == PledgeReason.PLEDGE || pledgeReason == PledgeReason.LATE_PLEDGE) stringResource(
                                    id = R.string.Pledge
                                ) + " $totalAmountString" else stringResource(
                                    id = R.string.Confirm
                                )
                            )

                            val formattedEmailDisclaimerString = ksString?.let {
                                email?.let { email ->
                                    ksString.format(
                                        stringResource(id = R.string.Your_payment_method_will_be_charged_immediately),
                                        "user_email",
                                        email
                                    )
                                }
                            }

                            if (!formattedEmailDisclaimerString.isNullOrEmpty() && project.isInPostCampaignPledgingPhase() == true) {
                                Text(
                                    text = formattedEmailDisclaimerString,
                                    textAlign = TextAlign.Center,
                                    style = typography.caption2,
                                    color = colors.kds_support_400
                                )
                            }

                            Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                            TermsOfUseClickableText(
                                onPrivacyPolicyClicked = {
                                    onDisclaimerItemClicked.invoke(DisclaimerItems.PRIVACY)
                                },
                                onCookiePolicyClicked = {
                                    onDisclaimerItemClicked.invoke(DisclaimerItems.COOKIES)
                                },
                                onTermsOfUseClicked = {
                                    onDisclaimerItemClicked.invoke(DisclaimerItems.TERMS)
                                }
                            )
                        }
                    }
                }
            }
        ) { padding ->

            val totalAmountConvertedString =
                if (project.currentCurrency() == project.currency()) "" else {
                    environment.ksCurrency()?.formatWithUserPreference(
                        totalAmount,
                        project,
                        RoundingMode.UP,
                        2
                    ) ?: ""
                }

            val shippingAmountString = environment.ksCurrency()?.let {
                RewardViewUtils.styleCurrency(
                    shippingAmount,
                    project,
                    it
                ).toString()
            } ?: ""

            val initialBonusSupportString = environment.ksCurrency()?.let {
                RewardViewUtils.styleCurrency(
                    0.0,
                    project,
                    it
                ).toString()
            } ?: ""

            val totalBonusSupportString = environment.ksCurrency()?.let {
                RewardViewUtils.styleCurrency(
                    totalBonusSupport,
                    project,
                    it
                ).toString()
            } ?: ""

            val aboutTotalString =
                if (totalAmountConvertedString.isEmpty()) "" else environment.ksString()?.format(
                    stringResource(id = R.string.About_reward_amount),
                    "reward_amount",
                    totalAmountConvertedString
                ) ?: "About $totalAmountConvertedString"

            val shippingLocation = currentShippingRule?.location()?.displayableName() ?: ""

            val deliveryDateString = if (selectedReward?.estimatedDeliveryOn().isNotNull()) {
                stringResource(id = R.string.Estimated_delivery) + " " + DateTimeUtils.estimatedDeliveryOn(
                    requireNotNull(
                        selectedReward?.estimatedDeliveryOn()
                    )
                )
            } else {
                ""
            }
            Column(
                modifier = Modifier
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {

                Text(
                    modifier = Modifier.padding(
                        start = dimensions.paddingMediumLarge,
                        top = dimensions.paddingMediumLarge
                    ),
                    text = stringResource(id = R.string.Checkout),
                    style = typography.title3Bold,
                    color = colors.kds_black,
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))
                if (isPlotEnabled) {
                    Text(
                        modifier = Modifier.padding(
                            start = dimensions.paddingMediumLarge,
                            end = dimensions.paddingMediumLarge
                        ),
                        text = stringResource(id = R.string.fpo_collection_plan),
                        style = typography.headline,
                        color = colors.kds_black,
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                    CollectionPlan(isEligible = true)
                    Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))
                    Text(
                        modifier = Modifier.padding(
                            start = dimensions.paddingMediumLarge,
                            end = dimensions.paddingMediumLarge
                        ),
                        text = stringResource(id = R.string.fpo_payment),
                        style = typography.headline,
                        color = colors.kds_black,
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))
                }
                storedCards.forEachIndexed { index, card ->
                    val isAvailable =
                        project.acceptedCardType(card.type()) || card.isFromPaymentSheet()
                    Card(
                        backgroundColor = colors.kds_white,
                        modifier = Modifier
                            .padding(
                                start = dimensions.paddingMedium,
                                end = dimensions.paddingMedium
                            )
                            .fillMaxWidth()
                            .selectableGroup()
                            .selectable(
                                enabled = isAvailable,
                                selected = if (index == 0) true else false,
                                onClick = {
                                    onOptionSelected(card)
                                }
                            )
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(
                                        top = dimensions.paddingSmall,
                                        bottom = dimensions.paddingSmall
                                    )
                            ) {

                                KSRadioButton(
                                    selected = card == selectedOption.value,
                                    onClick = { onOptionSelected(card) },
                                    enabled = isAvailable
                                )

                                KSCardElement(card = card, environment.ksString(), isAvailable)
                            }

                            if (!isAvailable) {
                                Text(
                                    modifier = Modifier.padding(
                                        start = dimensions.paddingDoubleLarge,
                                        end = dimensions.paddingMediumLarge,
                                        bottom = dimensions.paddingSmall
                                    ),
                                    style = typography.caption1Medium,
                                    color = colors.kds_alert,
                                    text = stringResource(id = R.string.This_project_has_a_set_currency_that_cant_process_this_option)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
                }

                Card(
                    backgroundColor = colors.kds_white,
                    modifier = Modifier
                        .padding(start = dimensions.paddingMedium, end = dimensions.paddingMedium)
                        .clickable { newPaymentMethodClicked.invoke() }
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            top = dimensions.paddingMedium,
                            bottom = dimensions.paddingMedium
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_rounded),
                            contentDescription = "",
                            tint = colors.textAccentGreen,
                            modifier = Modifier.background(
                                color = colors.kds_create_700.copy(alpha = 0.2f),
                                CircleShape
                            )
                        )

                        Text(
                            modifier = Modifier.padding(start = dimensions.paddingSmall),
                            color = colors.textAccentGreen,
                            style = typography.subheadlineMedium,
                            text = stringResource(id = R.string.New_payment_method)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.paddingLarge))

                Card(
                    modifier = Modifier.padding(
                        start = dimensions.paddingMedium,
                        end = dimensions.paddingMedium
                    ),
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
                                .padding(
                                    start = dimensions.paddingMediumSmall,
                                    end = dimensions.paddingLarge
                                )
                                .align(Alignment.CenterVertically),
                            painter = painterResource(id = R.drawable.ic_not_a_store),
                            contentDescription = null,
                            tint = colors.textAccentGreen
                        )

                        Column {

                            Text(
                                modifier = Modifier.padding(
                                    bottom = dimensions.paddingXSmall,
                                    top = dimensions.paddingXSmall
                                ),
                                text = stringResource(id = R.string.Kickstarter_is_not_a_store),
                                style = typography.body2Medium,
                                color = colors.kds_support_400
                            )
                            TextWithClickableAccountabilityLink(
                                padding = dimensions.paddingXSmall,
                                html = stringResource(id = R.string.Its_a_way_to_bring_creative_projects_to_life_Learn_more_about_accountability),
                                onClickCallback = {
                                    onAccountabilityLinkClicked.invoke()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                val resourceString =
                    stringResource(R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline_and_receive_proof_of_pledge)
                val disclaimerText = environment.ksString()?.format(
                    resourceString,
                    "total", totalAmountString,
                    "project_deadline", project.deadline()?.let { DateTimeUtils.longDate(it) }
                ) ?: ""
                val plotDisclaimerText =
                    stringResource(R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline_and_receive_proof_of_pledge)
                val isNoReward = selectedReward?.let { RewardUtils.isNoReward(it) } ?: false
                if (!isNoReward) {
                    ItemizedRewardListContainer(
                        ksString = ksString,
                        rewardsList = rewardsList,
                        shippingAmount = shippingAmount,
                        shippingAmountString = shippingAmountString,
                        initialShippingLocation = shippingLocation,
                        totalAmount = totalAmountString,
                        totalAmountCurrencyConverted = aboutTotalString,
                        initialBonusSupport = initialBonusSupportString,
                        totalBonusSupport = totalBonusSupportString,
                        deliveryDateString = deliveryDateString,
                        rewardsHaveShippables = rewardsHaveShippables,
                        disclaimerText = if (isPlotEnabled) plotDisclaimerText else disclaimerText,
                        plotSelected = false
                    )
                } else {
                    // - For noReward, totalAmount = bonusAmount as there is no reward
                    ItemizedRewardListContainer(
                        totalAmount = totalAmountString,
                        totalAmountCurrencyConverted = aboutTotalString,
                        initialBonusSupport = initialBonusSupportString,
                        totalBonusSupport = totalAmountString,
                        shippingAmount = shippingAmount,
                        disclaimerText = if (isPlotEnabled) plotDisclaimerText else disclaimerText,
                        plotSelected = false
                    )
                }

                if (environment.ksCurrency().isNotNull() && environment.ksString()
                    .isNotNull() && currentShippingRule.isNotNull()
                ) {
                    val estimatedShippingRangeString =
                        RewardViewUtils.getEstimatedShippingCostString(
                            context = LocalContext.current,
                            ksCurrency = environment.ksCurrency()!!,
                            ksString = environment.ksString()!!,
                            project = project,
                            rewards = selectedRewardsAndAddOns,
                            selectedShippingRule = currentShippingRule!!,
                            multipleQuantitiesAllowed = false,
                            useUserPreference = false,
                            useAbout = false
                        )

                    val estimatedShippingRangeConversionString =
                        if (project.currentCurrency() == project.currency()) null
                        else {
                            RewardViewUtils.getEstimatedShippingCostString(
                                context = LocalContext.current,
                                ksCurrency = environment.ksCurrency()!!,
                                ksString = environment.ksString()!!,
                                project = project,
                                rewards = selectedRewardsAndAddOns,
                                selectedShippingRule = currentShippingRule,
                                multipleQuantitiesAllowed = false,
                                useUserPreference = true,
                                useAbout = true
                            )
                        }

                    if (estimatedShippingRangeString.isNotEmpty()) {
                        KSEstimatedShippingCheckoutView(
                            estimatedShippingRange = estimatedShippingRangeString,
                            estimatedShippingRangeConversion = estimatedShippingRangeConversionString
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.backgroundAccentGraySubtle.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                KSCircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun UpdateSelectedCardIfNewCardAdded(
    index: MutableState<Int>,
    storedCards: List<StoredCard>,
    onOptionSelected: (StoredCard?) -> Unit
) {
    if (index.value != storedCards.size && storedCards.isNotEmpty()) {
        onOptionSelected(storedCards.first())
        index.value = storedCards.size
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

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSCardElementPreview() {
    KSTheme {
        KSCardElement(
            StoredCardFactory.visa(), Environment.builder().build().ksString(), true
        )
    }
}

@Composable
fun KSCardElement(card: StoredCard, ksString: KSString?, isAvailable: Boolean) {
    val sdf = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

    val expirationString = ksString?.let {
        card.expiration()?.let { expiration ->
            ksString.format(
                stringResource(id = R.string.Credit_card_expiration),
                "expiration_date", sdf.format(expiration).toString()
            )
        }
    }

    val lastFourString = ksString?.let {
        card.lastFourDigits()?.let { lastFour ->
            ksString.format(
                stringResource(id = R.string.payment_method_last_four),
                "last_four", lastFour
            )
        }
    }

    Row {
        Image(
            modifier = Modifier
                .padding(end = dimensions.paddingMediumSmall)
                .align(Alignment.CenterVertically)
                .width(dimensions.storedCardImageWidth)
                .height(dimensions.storedCardImageHeight),
            contentDescription = card.type()?.let { StoredCard.issuer(it) },
            alpha = if (isAvailable) 1.0f else .5f,
            painter = painterResource(id = card.getCardTypeDrawable()),
        )

        Column(
            modifier = Modifier.align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.Center
        ) {
            if (!lastFourString.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.padding(end = dimensions.paddingMediumLarge),
                    color = if (isAvailable) colors.kds_support_700 else colors.kds_support_400,
                    style = typography.body2Medium,
                    text = lastFourString
                )
            }

            if (!expirationString.isNullOrEmpty()) {
                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(
                        top = dimensions.paddingXSmall,
                        end = dimensions.paddingMediumLarge
                    ),
                    style = typography.caption2Medium,
                    color = if (isAvailable) colors.kds_support_700 else colors.kds_support_400,
                    text = expirationString
                )
            }
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSEstimatedShippingCheckoutViewPreview() {
    KSTheme {
        KSEstimatedShippingCheckoutView(
            estimatedShippingRange = "€5-€10",
            estimatedShippingRangeConversion = "About $6-$11"
        )
    }
}

@Composable
fun KSEstimatedShippingCheckoutView(
    estimatedShippingRange: String,
    estimatedShippingRangeConversion: String?,
) {
    Card(
        modifier = Modifier.padding(dimensions.paddingMediumLarge),
        shape = shapes.medium,
        backgroundColor = colors.backgroundSurfacePrimary
    ) {
        // TODO: Get these strings translations in place
        Row {
            Column(modifier = Modifier.weight(0.6f)) {
                Text(
                    modifier = Modifier.padding(
                        start = dimensions.paddingLarge,
                        top = dimensions.paddingMedium
                    ),
                    text = stringResource(id = R.string.Estimated_Shipping),
                    style = typography.calloutMedium,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                Text(
                    modifier = Modifier.padding(
                        start = dimensions.paddingLarge,
                        bottom = dimensions.paddingMedium
                    ),
                    text = stringResource(id = R.string.This_is_meant_to_give_you),
                    style = typography.caption2,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(dimensions.paddingMedium))

            Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
                Text(
                    modifier = Modifier.padding(
                        end = dimensions.paddingLarge,
                        top = dimensions.paddingMedium
                    ),
                    text = estimatedShippingRange,
                    style = typography.calloutMedium,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                if (!estimatedShippingRangeConversion.isNullOrEmpty()) {
                    Text(
                        modifier = Modifier.padding(
                            end = dimensions.paddingLarge,
                            bottom = dimensions.paddingMedium
                        ),
                        text = estimatedShippingRangeConversion,
                        style = typography.caption1,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
