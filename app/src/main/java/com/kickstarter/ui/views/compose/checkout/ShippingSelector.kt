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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.PopupProperties
import com.kickstarter.R
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ShippingSelectorPreview() {
    KSTheme {
        Scaffold(
            containerColor = KSTheme.colors.backgroundAccentGraySubtle
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
        Text(
            text = stringResource(id = R.string.Your_shipping_location),
            style = typographyV2.subHeadlineMedium,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(KSTheme.dimensions.paddingSmall))

        CountryInputWithDropdown(
            interactionSource = interactionSource,
            initialCountryInput = currentShippingRule.location()?.displayableName(),
            countryList = countryList,
            onShippingRuleSelected = onShippingRuleSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryInputWithDropdown(
    interactionSource: MutableInteractionSource,
    initialCountryInput: String? = null,
    countryList: List<ShippingRule>,
    onShippingRuleSelected: (ShippingRule) -> Unit
) {
    var countryListExpanded by remember { mutableStateOf(false) }
    var countryInput by remember(key1 = initialCountryInput) { mutableStateOf(initialCountryInput ?: "") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.clickable(interactionSource = interactionSource, indication = null) {
            countryListExpanded = false
        }
    ) {
        Box(contentAlignment = Alignment.TopStart) {
            BasicTextField(
                modifier = Modifier.fillMaxWidth(0.6f),
                value = countryInput,
                onValueChange = { countryInput = it; countryListExpanded = true },
                textStyle = typographyV2.subHeadlineMedium.copy(color = colors.textAccentGreenBold),
                singleLine = false
            ) { innerTextField ->
                TextFieldDefaults.DecorationBox(
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.backgroundSurfacePrimary,
                        unfocusedContainerColor = colors.backgroundSurfacePrimary,
                        disabledContainerColor = colors.backgroundSurfacePrimary,
                        errorContainerColor = colors.backgroundSurfacePrimary,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }

            val filtered = if (countryInput.isNotEmpty()) {
                countryList.filter {
                    it.location()?.displayableName()?.lowercase()?.contains(countryInput.lowercase()) == true
                }.take(3)
            } else {
                countryList.take(5)
            }

            val shouldShowDropdown = countryListExpanded && filtered.isNotEmpty()

            DropdownMenu(
                expanded = shouldShowDropdown,
                onDismissRequest = { countryListExpanded = false },
                modifier = Modifier
                    .width(dimensions.countryInputWidth)
                    .heightIn(dimensions.none, dimensions.dropDownStandardWidth),
                properties = PopupProperties(focusable = false)
            ) {
                filtered.forEach { rule ->
                    DropdownMenuItem(
                        modifier = Modifier.background(color = colors.backgroundSurfacePrimary),
                        onClick = {
                            countryInput = rule.location()?.displayableName().orEmpty()
                            countryListExpanded = false
                            focusManager.clearFocus()
                            onShippingRuleSelected(rule)
                        },
                        text = {
                            Text(
                                text = rule.location()?.displayableName().orEmpty(),
                                style = typographyV2.subHeadlineMedium,
                                color = colors.textAccentGreenBold
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = colors.textAccentGreenBold
                        )
                    )
                }
            }
        }
    }
}
