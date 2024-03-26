package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.PopupProperties
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import java.math.RoundingMode

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun AddOnsScreenPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = colors.backgroundAccentGraySubtle
        ) { padding ->
            AddOnsScreen(
                modifier = Modifier.padding(padding),
                environment = Environment.Builder().build(),
                lazyColumnListState = rememberLazyListState(),
                countryList = listOf(
                    ShippingRule.builder()
                        .location(Location.builder().displayableName("United States").build())
                        .build(),
                    ShippingRule.builder()
                        .location(Location.builder().displayableName("Japan").build())
                        .build(),
                    ShippingRule.builder()
                        .location(Location.builder().displayableName("Korea").build())
                        .build(),
                    ShippingRule.builder()
                        .location(Location.builder().displayableName("United States").build())
                        .build()
                ),
                shippingSelectorIsGone = false,
                onShippingRuleSelected = {},
                currentShippingRule = ShippingRule.builder().build(),
                rewardItems = (0..10).map {
                    Reward.builder()
                        .title("Item Number $it")
                        .description("This is a description for item $it")
                        .id(it.toLong())
                        .convertedMinimum((100 * (it + 1)).toDouble())
                        .isAvailable(it != 0)
                        .limit(if (it == 0) 1 else 10)
                        .build()
                },
                project =
                Project.builder()
                    .currency("USD")
                    .currentCurrency("USD")
                    .build(),
                onItemAddedOrRemoved = {},
                selectedAddOnsMap = mutableMapOf(),
                onContinueClicked = {}
            )
        }
    }
}

@Composable
fun AddOnsScreen(
    modifier: Modifier,
    environment: Environment,
    lazyColumnListState: LazyListState,
    shippingSelectorIsGone: Boolean,
    currentShippingRule: ShippingRule,
    countryList: List<ShippingRule>,
    onShippingRuleSelected: (ShippingRule) -> Unit,
    rewardItems: List<Reward>,
    project: Project,
    onItemAddedOrRemoved: (Map<Reward, Int>) -> Unit,
    selectedAddOnsMap: Map<Reward, Int>,
    onContinueClicked: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val addOnCount = getAddOnCount(selectedAddOnsMap)

    Scaffold(
        modifier = modifier,
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensions.paddingMediumLarge)
                    ) {
                        KSPrimaryGreenButton(
                            onClickAction = onContinueClicked,
                            text =
                            if (addOnCount == 0) stringResource(id = R.string.Skip_add_ons)
                            else {
                                when {
                                    addOnCount == 1 -> environment.ksString()?.format(
                                        stringResource(R.string.Continue_with_quantity_count_add_ons_one),
                                        "quantity_count",
                                        addOnCount.toString()
                                    ) ?: ""

                                    addOnCount > 1 -> environment.ksString()?.format(
                                        stringResource(R.string.Continue_with_quantity_count_add_ons_many),
                                        "quantity_count",
                                        addOnCount.toString()
                                    ) ?: ""

                                    else -> stringResource(id = R.string.Skip_add_ons)
                                }
                            },
                            isEnabled = true
                        )
                    }
                }
            }
        },
        backgroundColor = colors.backgroundAccentGraySubtle
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(
                    start = dimensions.paddingMedium,
                    end = dimensions.paddingMedium,
                    top = dimensions.paddingMedium
                )
                .padding(paddingValues = padding),
            state = lazyColumnListState
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.Customize_your_reward_with_optional_addons),
                    style = typography.title3Bold,
                    color = colors.textPrimary
                )

                if (!shippingSelectorIsGone) {
                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

                    Text(
                        text = stringResource(id = R.string.Your_shipping_location),
                        style = typography.subheadlineMedium,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                    CountryInputWithDropdown(
                        interactionSource = interactionSource,
                        initialCountryInput = currentShippingRule.location()?.displayableName(),
                        countryList = countryList,
                        onShippingRuleSelected = onShippingRuleSelected
                    )
                }
            }

            items(
                items = rewardItems
            ) { reward ->
                Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                AddOnsContainer(
                    title = reward.title() ?: "",
                    amount = environment.ksCurrency()?.format(
                        reward.minimum(),
                        project,
                        true,
                    ) ?: "",
                    conversionAmount = environment.ksString()?.format(
                        stringResource(R.string.About_reward_amount),
                        "reward_amount",
                        environment.ksCurrency()?.format(
                            reward.convertedMinimum(),
                            project,
                            true,
                            RoundingMode.HALF_UP,
                            true
                        )
                    ),
                    shippingAmount = environment.ksCurrency()?.let {
                        getShippingCost(
                            reward = reward,
                            ksCurrency = it,
                            shippingRules = reward.shippingRules(),
                            selectedShippingRule = currentShippingRule,
                            project = project
                        )
                    },
                    description = reward.description() ?: "",
                    buttonEnabled = reward.isAvailable(),
                    buttonText = stringResource(id = R.string.Add),
                    limit = reward.limit() ?: -1,
                    onItemAddedOrRemoved = { count ->
                        val rewardSelections = mutableMapOf<Reward, Int>()
                        rewardSelections[reward] = count

                        onItemAddedOrRemoved(rewardSelections)
                    },
                    environment = environment,
                    includesList = reward.addOnsItems()?.map {
                        environment.ksString()?.format(
                            "rewards_info_item_quantity_title", it.quantity(),
                            "quantity", it.quantity().toString(),
                            "title", it.item().name()
                        ) ?: ""
                    } ?: listOf(),
                    itemAddOnCount = selectedAddOnsMap[reward] ?: 0
                )
            }

            item {
                Spacer(modifier = Modifier.height(dimensions.paddingDoubleLarge))
            }
        }
    }
}

private fun getAddOnCount(selectedAddOnsMap: Map<Reward, Int>): Int {
    var totalAddOnsCount = 0
    selectedAddOnsMap.forEach {
        totalAddOnsCount += it.value
    }
    return totalAddOnsCount
}
private fun getShippingCost(
    reward: Reward,
    ksCurrency: KSCurrency,
    shippingRules: List<ShippingRule>?,
    project: Project,
    selectedShippingRule: ShippingRule
): String {
    return if (shippingRules.isNullOrEmpty()) {
        ""
    } else if (!RewardUtils.isDigital(reward) && RewardUtils.isShippable(reward) && !RewardUtils.isLocalPickup(reward)) {
        var cost = 0.0
        shippingRules.filter {
            it.location()?.id() == selectedShippingRule.location()?.id()
        }.map {
            cost += it.cost()
        }
        if (cost > 0) ksCurrency.format(cost, project)
        else ""
    } else {
        ""
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CountryInputWithDropdown(
    interactionSource: MutableInteractionSource,
    initialCountryInput: String? = null,
    countryList: List<ShippingRule>,
    onShippingRuleSelected: (ShippingRule) -> Unit
) {
    var countryListExpanded by remember {
        mutableStateOf(false)
    }

    var countryInput by remember {
        mutableStateOf(initialCountryInput ?: "United States")
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { countryListExpanded = false }
            ),
    ) {
        Box(contentAlignment = Alignment.TopStart) {
            BasicTextField(
                modifier = Modifier
                    .background(color = colors.backgroundSurfacePrimary)
                    .fillMaxWidth(0.6f),
                value = countryInput,
                onValueChange = {
                    countryInput = it
                    countryListExpanded = true
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                textStyle = typography.subheadlineMedium.copy(color = colors.textAccentGreenBold),
                singleLine = false
            ) { innerTextField ->
                TextFieldDefaults.TextFieldDecorationBox(
                    value = countryInput,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = false,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(
                        start = dimensions.paddingMedium,
                        top = dimensions.paddingSmall,
                        bottom = dimensions.paddingSmall,
                        end = dimensions.paddingMedium
                    ),
                )
            }

            val shouldShowDropdown: Boolean = when {
                countryListExpanded && countryInput.isNotEmpty() -> {
                    countryList.filter {
                        it.location()?.displayableName()?.lowercase()
                            ?.contains(countryInput.lowercase()) ?: false
                    }.isNotEmpty()
                }

                else -> countryListExpanded
            }

            DropdownMenu(
                expanded = shouldShowDropdown,
                onDismissRequest = { },
                modifier = Modifier
                    .width(
                        dimensions.countryInputWidth
                    )
                    .heightIn(dimensions.none, dimensions.dropDownStandardWidth),
                properties = PopupProperties(focusable = false)
            ) {
                if (countryInput.isNotEmpty()) {
                    countryList.filter {
                        it.location()?.displayableName()?.lowercase()
                            ?.contains(countryInput.lowercase()) ?: false
                    }.take(3).forEach { rule ->
                        DropdownMenuItem(
                            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
                            onClick = {
                                countryInput =
                                    rule.location()?.displayableName() ?: ""
                                countryListExpanded = false
                                focusManager.clearFocus()
                                onShippingRuleSelected(rule)
                            }
                        ) {
                            Text(
                                text = rule.location()?.displayableName() ?: "",
                                style = typography.subheadlineMedium,
                                color = colors.textAccentGreenBold
                            )
                        }
                    }
                } else {
                    countryList.take(5).forEach { rule ->
                        DropdownMenuItem(
                            modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
                            onClick = {
                                countryInput =
                                    rule.location()?.displayableName() ?: ""
                                countryListExpanded = false
                                focusManager.clearFocus()
                                onShippingRuleSelected(rule)
                            }
                        ) {
                            Text(
                                text = rule.location()?.displayableName() ?: "",
                                style = typography.subheadlineMedium,
                                color = colors.textAccentGreenBold
                            )
                        }
                    }
                }
            }
        }
    }
}
