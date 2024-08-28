package com.kickstarter.ui.views.compose.checkout

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
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
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ShippingSelectorPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->

            val interactionSource = remember {
                MutableInteractionSource()
            }

            ShippingSelector(
                modifier = Modifier
                    .padding(paddingValues = padding),
                interactionSource = interactionSource,
                currentShippingRule = ShippingRuleFactory.usShippingRule(),
                countryList = listOf(ShippingRuleFactory.usShippingRule(), ShippingRuleFactory.germanyShippingRule()),
                onShippingRuleSelected = {}
            )
        }
    }
}

@Composable
fun ShippingSelector(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    currentShippingRule: ShippingRule,
    countryList: List<ShippingRule>,
    onShippingRuleSelected: (ShippingRule) -> Unit,
) {
    Column(modifier = modifier) {
        // Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        Text(
            text = stringResource(id = R.string.Your_shipping_location),
            style = typography.subheadlineMedium,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingSmall))

        CountryInputWithDropdown(
            interactionSource = interactionSource,
            initialCountryInput = currentShippingRule.location()?.displayableName(),
            countryList = countryList,
            onShippingRuleSelected = onShippingRuleSelected
        )
        // Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingSmall))
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

    var countryInput by remember(key1 = initialCountryInput) {
        mutableStateOf(initialCountryInput ?: "")
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
