package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
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
    rewardItems: List<Reward>,
    project: Project,
    onItemAddedOrRemoved: (Map<Reward, Int>) -> Unit,
    selectedAddOnsMap: Map<Reward, Int>,
    isLoading: Boolean = false,
    onContinueClicked: () -> Unit
) {
    val addOnCount = getAddOnCount(selectedAddOnsMap)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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
                        conversionAmount = if (project.currentCurrency() == project.currency()) "" else {
                            environment.ksString()?.format(
                                stringResource(R.string.About_reward_amount),
                                "reward_amount",
                                environment.ksCurrency()?.format(
                                    reward.convertedMinimum(),
                                    project,
                                    true,
                                    RoundingMode.HALF_UP,
                                    true
                                )
                            )
                        },
                        shippingAmount = environment.ksCurrency()?.let {
                            it.format(0.0, project)
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

private fun getAddOnCount(selectedAddOnsMap: Map<Reward, Int>): Int {
    var totalAddOnsCount = 0
    selectedAddOnsMap.forEach {
        totalAddOnsCount += it.value
    }
    return totalAddOnsCount
}
