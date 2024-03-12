package com.kickstarter.ui.activities.compose.projectpage

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.ui.compose.designsystem.KSCoralBadge
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSStepper
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.shapes

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun AddOnsContainerPreview() {
    KSTheme {
        AddOnsContainer(
            title = "This Is A Test",
            amount = "$500",
            shippingAmount = "$5 shipping each",
            conversionAmount = "About $500",
            description = "This is just a test, don't worry about it, This is just a test, don't worry about it, This is just a test, don't worry about it, This is just a test, don't worry about it",
            includesList = listOf("this is item 1", "this is item 2", "this is item 3"),
            limit = 10,
            buttonEnabled = true,
            buttonText = "Add",
            environment = Environment.builder().build(),
            onItemAddedOrRemoved = { count ->
            }
        )
    }
}

@Composable
fun AddOnsContainer(
    title: String,
    amount: String,
    shippingAmount: String? = null,
    conversionAmount: String? = null,
    description: String,
    includesList: List<String> = listOf(),
    limit: Int = -1,
    buttonEnabled: Boolean,
    buttonText: String,
    environment: Environment,
    onItemAddedOrRemoved: (count: Int) -> Unit
) {
    var addOnCount by rememberSaveable { mutableStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.kds_white,
        shape = shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.paddingMediumLarge)
        ) {

            Text(text = title, style = typography.title2Bold, color = colors.kds_black)

            Row {
                Text(text = amount, style = typography.callout, color = colors.textAccentGreen)

                if (!shippingAmount.isNullOrEmpty()) {
                    Text(
                        text = getShippingString(LocalContext.current, environment.ksString(), shippingAmount) ?: "",
                        style = typography.callout,
                        color = colors.textAccentGreen
                    )
                }
            }

            if (!conversionAmount.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.padding(top = dimensions.paddingXSmall),
                    text = conversionAmount,
                    style = typography.footnoteMedium,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

            Text(text = description, style = typography.body2, color = colors.textPrimary)

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            KSDividerLineGrey()

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            if (includesList.isNotEmpty()) {
                Text(
                    text = "Includes",
                    style = typography.calloutMedium,
                    color = colors.textSecondary
                )

                includesList.forEachIndexed { index, itemDescription ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))

                        Box(
                            modifier = Modifier
                                .padding(end = dimensions.paddingSmall)
                                .size(dimensions.dottedListDotSize)
                                .background(color = colors.textPrimary, shape = CircleShape),
                        )

                        Text(
                            modifier = Modifier.padding(
                                top = dimensions.paddingXSmall,
                                bottom = dimensions.paddingXSmall
                            ),
                            text = itemDescription,
                            style = typography.body2,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))
                    }

                    if (index != includesList.lastIndex) KSDividerLineGrey()
                }
            }

            if (limit > 0) {
                Spacer(Modifier.height(dimensions.paddingMedium))

                KSCoralBadge(text = "Limit $limit")
            }

            Spacer(Modifier.height(dimensions.paddingLarge))

            when (addOnCount) {
                0 -> {
                    KSPrimaryBlackButton(
                        onClickAction = {
                            addOnCount++
                            onItemAddedOrRemoved(addOnCount)
                        },
                        text = buttonText,
                        isEnabled = buttonEnabled
                    )
                }

                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        KSStepper(
                            onPlusClicked = {
                                addOnCount++
                                onItemAddedOrRemoved(addOnCount)
                            },
                            isPlusEnabled = addOnCount < limit,
                            onMinusClicked = {
                                addOnCount--
                                onItemAddedOrRemoved(addOnCount)
                            },
                            isMinusEnabled = true
                        )

                        Box(
                            modifier = Modifier
                                .border(
                                    width = dimensions.dividerThickness,
                                    color = colors.textSecondary,
                                    shape = shapes.small
                                )
                                .padding(
                                    top = dimensions.paddingSmall,
                                    bottom = dimensions.paddingSmall,
                                    start = dimensions.paddingMedium,
                                    end = dimensions.paddingMedium
                                )
                        ) {
                            Text(
                                text = "$addOnCount",
                                style = typography.callout,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getShippingString(context: Context, ksString: KSString?, shippingAmount: String?): String? {
    if (shippingAmount.isNullOrEmpty() || ksString.isNull()) return ""
    val rewardAndShippingString =
        context.getString(R.string.reward_amount_plus_shipping_cost_each)
    val stringSections = rewardAndShippingString.split("+")
    val shippingString = "+" + stringSections[1]
    val ammountAndShippingString = ksString?.format(
        shippingString,
        "shipping_cost",
        shippingAmount
    )
    return ammountAndShippingString
}
