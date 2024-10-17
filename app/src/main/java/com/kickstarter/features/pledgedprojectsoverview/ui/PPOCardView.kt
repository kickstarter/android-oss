package com.kickstarter.features.pledgedprojectsoverview.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.data.Flag
import com.kickstarter.libs.utils.extensions.format
import com.kickstarter.ui.compose.designsystem.KSAlertBadge
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryRedButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.KSWarningBadge
import com.kickstarter.ui.compose.designsystem.shapes

@Composable
@Preview(showSystemUi = true, showBackground = true, name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showSystemUi = true, showBackground = true, name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PPOCardPreview() {
    KSTheme {
        LazyColumn(
            Modifier
                .background(color = colors.backgroundSurfacePrimary),
            contentPadding = PaddingValues(dimensions.paddingMedium)
        ) {
            item {
                PPOCardView(
                    viewType = PPOCardViewType.CONFIRM_ADDRESS,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    shippingAddress = "Firsty Lasty\n123 First Street, Apt #5678\nLos Angeles, CA 90025-1234\nUnited States",
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address").type("warning").icon("time").build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }
            item {
                PPOCardView(
                    viewType = PPOCardViewType.FIX_PAYMENT,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$50.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address").type("warning").icon("time").build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.AUTHENTICATE_CARD,
                    onCardClick = { },
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$60.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }

            item {
                PPOCardView(
                    viewType = PPOCardViewType.OPEN_SURVEY,
                    onCardClick = {},
                    projectName = "Sugardew Island - Your cozy farm shop let’s pretend this is a longer title let’s pretend this is a longer title",
                    pledgeAmount = "$70.00",
                    creatorName = "Some really really really really really really really long name",
                    sendAMessageClickAction = {},
                    onActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    onProjectPledgeSummaryClick = {},
                    flags = listOf(Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address locks in 7 days").type("warning").icon("time").build(), Flag.builder().message("Address").type("warning").icon("time").build()),
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            }
        }
    }
}

enum class PPOCardViewType {
    CONFIRM_ADDRESS,
    FIX_PAYMENT,
    AUTHENTICATE_CARD,
    OPEN_SURVEY,
    UNKNOWN,
}

enum class PPOCardViewTestTag {
    SHIPPING_ADDRESS_VIEW,
    CONFIRM_ADDRESS_BUTTONS_VIEW,
    FlAG_LIST_VIEW,
}

@Composable
fun PPOCardView(
    viewType: PPOCardViewType,
    onCardClick: () -> Unit,
    onProjectPledgeSummaryClick: () -> Unit,
    projectName: String? = null,
    pledgeAmount: String? = null,
    imageUrl: String? = null,
    imageContentDescription: String? = null,
    creatorName: String? = null,
    sendAMessageClickAction: () -> Unit,
    shippingAddress: String? = null,
    onActionButtonClicked: () -> Unit,
    onSecondaryActionButtonClicked: () -> Unit,
    flags: List<Flag?>? = null,
) {

    BadgedBox(
        badge = { if (isTier1Alert(viewType)) Badge(backgroundColor = colors.backgroundDangerBold) }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = shapes.small,
            contentColor = colors.backgroundSurfacePrimary,
            backgroundColor = colors.backgroundSurfacePrimary,
            border = BorderStroke(width = dimensions.borderThickness, color = colors.borderSubtle),
        ) {
            Column(
                Modifier.clickable { onCardClick.invoke() }
            ) {
                if (!flags.isNullOrEmpty()) {
                    AlertFlagsView(flags = flags)
                }

                ProjectPledgeSummaryView(
                    projectName = projectName,
                    pledgeAmount = pledgeAmount,
                    imageUrl = imageUrl,
                    imageContentDescription = imageContentDescription,
                    onProjectPledgeSummaryClick = onProjectPledgeSummaryClick
                )

                CreatorNameSendMessageView(
                    creatorName = creatorName,
                    sendAMessageClickAction = sendAMessageClickAction
                )

                if (viewType == PPOCardViewType.CONFIRM_ADDRESS && !shippingAddress.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                    ShippingAddressView(
                        shippingAddress = shippingAddress
                    )
                }

                when (viewType) {
                    PPOCardViewType.CONFIRM_ADDRESS -> ConfirmAddressButtonsView(!shippingAddress.isNullOrEmpty(), onActionButtonClicked, onSecondaryActionButtonClicked)
                    PPOCardViewType.FIX_PAYMENT -> FixPaymentButtonView(onActionButtonClicked)
                    PPOCardViewType.AUTHENTICATE_CARD -> AuthenticateCardButtonView(onActionButtonClicked)
                    PPOCardViewType.OPEN_SURVEY -> TakeSurveyButtonView(onActionButtonClicked)
                    PPOCardViewType.UNKNOWN -> {}
                }
            }
        }
    }
}

@Composable
fun ProjectPledgeSummaryView(
    projectName: String? = null,
    pledgeAmount: String? = null,
    imageUrl: String? = null,
    imageContentDescription: String? = null,
    onProjectPledgeSummaryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onProjectPledgeSummaryClick.invoke() }
            .fillMaxWidth()
            .padding(all = dimensions.paddingMediumSmall)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = imageContentDescription,
            modifier = Modifier
                .weight(0.25f)
                .height(dimensions.clickableButtonHeight)
                .clip(shapes.small),
            placeholder = ColorPainter(color = colors.backgroundDisabled),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.width(dimensions.paddingSmall))

        Column(
            modifier = Modifier
                .weight(0.75f)
                .height(dimensions.clickableButtonHeight)
        ) {
            Text(
                text = projectName ?: "",
                color = colors.textPrimary,
                style = typography.footnoteMedium,
                overflow = TextOverflow.Ellipsis,
                minLines = 1,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

            Text(
                text = stringResource(id = R.string.Pledge_amount_pledged).format("pledge_amount", pledgeAmount),
                color = colors.textSecondary,
                style = typography.caption2
            )
        }
    }
}

@Composable
fun CreatorNameSendMessageView(
    creatorName: String? = null,
    sendAMessageClickAction: () -> Unit
) {
    KSDividerLineGrey()

    Row(
        Modifier
            .fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .weight(0.7f)
                .padding(
                    top = dimensions.paddingMediumSmall,
                    bottom = dimensions.paddingMediumSmall,
                    start = dimensions.paddingMediumSmall,
                    end = dimensions.paddingSmall
                )
        ) {
            Text(
                text = stringResource(id = R.string.project_menu_created_by),
                color = colors.textSecondary,
                style = typography.caption2
            )

            Text(
                text = " ${creatorName.orEmpty()}",
                overflow = TextOverflow.Ellipsis,
                color = colors.textSecondary,
                style = typography.caption2Medium,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier
                .weight(0.3f)
                .padding(
                    end = dimensions.paddingMediumSmall,
                    top = dimensions.paddingMediumSmall,
                    bottom = dimensions.paddingMediumSmall
                )
                .clickable { sendAMessageClickAction.invoke() }
        ) {
            Text(
                text = stringResource(id = R.string.Send_a_message),
                color = colors.textAccentGreen,
                style = typography.caption2
            )

            Image(
                modifier = Modifier.size(dimensions.paddingMediumSmall),
                imageVector = ImageVector.vectorResource(id = R.drawable.chevron_right),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = colors.textAccentGreen)
            )
        }
    }

    KSDividerLineGrey()
}

@Composable
fun ShippingAddressView(
    shippingAddress: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensions.paddingSmall)
            .testTag(PPOCardViewTestTag.SHIPPING_ADDRESS_VIEW.name),
    ) {
        Text(
            text = stringResource(id = R.string.Shipping_address),
            modifier = Modifier
                .weight(0.25f)
                .height(dimensions.clickableButtonHeight)
                .clip(shapes.small),
            color = colors.textPrimary,
            style = typography.caption1Medium,
        )

        Spacer(modifier = Modifier.width(dimensions.paddingSmall))

        Text(
            text = shippingAddress ?: "",
            modifier = Modifier
                .weight(0.75f),
            color = colors.textPrimary,
            style = typography.caption1,
            overflow = TextOverflow.Ellipsis,
            minLines = 4,
            maxLines = 6
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertFlagsView(flags: List<Flag?>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(PPOCardViewTestTag.FlAG_LIST_VIEW.name)
            .padding(
                top = dimensions.paddingMediumSmall,
                start = dimensions.paddingMediumSmall,
                end = dimensions.paddingMediumSmall
            ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        flags.forEach {
            val icon =
                when (it?.icon) {
                    "alert" -> ImageVector.vectorResource(id = R.drawable.ic_alert)
                    "time" -> ImageVector.vectorResource(id = R.drawable.ic_clock)
                    else -> null
                }

            when (it?.type) {
                "alert" -> KSAlertBadge(icon = icon, message = it.message)
                "warning" -> KSWarningBadge(icon = icon, message = it.message)
                else -> {}
            }
        }
    }
}

@Composable
fun ConfirmAddressButtonsView(isConfirmButtonEnabled: Boolean, onEditAddressClicked: () -> Unit, onConfirmAddressClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensions.paddingSmall)
            .testTag(PPOCardViewTestTag.CONFIRM_ADDRESS_BUTTONS_VIEW.name)
    ) {
        KSPrimaryBlackButton(
            modifier = Modifier
                .weight(0.5f),
            onClickAction = { onEditAddressClicked.invoke() },
            text = stringResource(id = R.string.Edit),
            isEnabled = true,
            textStyle = typography.buttonText
        )
        Spacer(modifier = Modifier.width(dimensions.paddingSmall))

        KSPrimaryGreenButton(
            modifier = Modifier
                .weight(0.5f),
            onClickAction = { onConfirmAddressClicked.invoke() },
            text = stringResource(id = R.string.Confirm),
            isEnabled = isConfirmButtonEnabled,
            textStyle = typography.buttonText
        )
    }
}

@Composable
fun FixPaymentButtonView(onFixPaymentClicked: () -> Unit) {
    KSSecondaryRedButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        onClickAction = { onFixPaymentClicked.invoke() },
        text = stringResource(id = R.string.Fix_payment),
        isEnabled = true,
        textStyle = typography.buttonText
    )
}

@Composable
fun AuthenticateCardButtonView(onAuthenticateCardClicked: () -> Unit) {
    KSSecondaryRedButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        onClickAction = { onAuthenticateCardClicked.invoke() },
        text = stringResource(id = R.string.Authenticate_card),
        isEnabled = true,
        textStyle = typography.buttonText
    )
}

@Composable
fun TakeSurveyButtonView(onAuthenticateCardClicked: () -> Unit) {
    KSPrimaryGreenButton(
        modifier = Modifier.padding(dimensions.paddingMediumSmall),
        onClickAction = { onAuthenticateCardClicked.invoke() },
        text = stringResource(id = R.string.Take_survey),
        isEnabled = true,
        textStyle = typography.buttonText
    )
}

fun isTier1Alert(viewType: PPOCardViewType): Boolean {
    return when (viewType) {
        PPOCardViewType.CONFIRM_ADDRESS, PPOCardViewType.AUTHENTICATE_CARD, PPOCardViewType.OPEN_SURVEY, PPOCardViewType.FIX_PAYMENT -> true
        else -> false
    }
}
