package com.kickstarter.ui.compose.designsystem

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PreviewControls() {
    KSTheme {
        var checkedOn by remember { mutableStateOf(true) }
        var checkedOff by remember { mutableStateOf(false) }
        var radioButtonSelected by remember { mutableStateOf(true) }
        var checked by remember { mutableStateOf(false) }
        Column(Modifier.background(color = colors.kds_support_100)) {
            KSSwitch(checked = checkedOn, onCheckChanged = { checkedOn = !checkedOn })

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSwitch(checked = checkedOff, onCheckChanged = { checkedOff = !checkedOff })

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSwitch(checked = checkedOn, onCheckChanged = {}, enabled = false)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSRadioButton(
                selected = radioButtonSelected,
                onClick = { radioButtonSelected = !radioButtonSelected }
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSRadioButton(
                selected = !radioButtonSelected,
                onClick = { radioButtonSelected = !radioButtonSelected }
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSRadioButton(selected = radioButtonSelected, onClick = {}, enabled = false)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSCheckbox(checked = checked, onCheckChanged = { checked = !checked })

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSCheckbox(checked = checked, onCheckChanged = {}, enabled = false)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSStringDropdown(
                onItemSelected = { _, _ -> },
                items = arrayOf("Coffee", "Soda", "Water")
            )
        }
    }
}

@Composable
fun KSSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckChanged: ((Boolean) -> Unit),
    enabled: Boolean = true
) {
    Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckChanged,
        colors = SwitchDefaults.colors(
            uncheckedThumbColor = colors.kds_support_100,
            uncheckedTrackColor = colors.kds_support_500,
            uncheckedTrackAlpha = 0.38f,
            checkedThumbColor = colors.kds_create_700,
            checkedTrackColor = colors.kds_create_700,
            checkedTrackAlpha = 0.38f,
            disabledCheckedThumbColor = colors.kds_support_300,
            disabledCheckedTrackColor = colors.kds_support_700,
        ),
        enabled = enabled
    )
}

@Composable
fun KSRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(
            selectedColor = colors.kds_create_700,
            unselectedColor = colors.kds_create_700,
            disabledColor = colors.kds_support_300
        ),
        enabled = enabled
    )
}

@Composable
fun KSCheckbox(
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckChanged,
        colors = CheckboxDefaults.colors(
            checkedColor = colors.kds_create_700,
            uncheckedColor = colors.kds_create_700,
            checkmarkColor = colors.kds_white,
            disabledColor = kds_support_500,
        ),
        enabled = enabled
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun KSStringDropdown(
    @SuppressLint("ModifierParameter")
    modifier: Modifier = Modifier.width(dimensions.dropDownStandardWidth),
    items: Array<String>,
    onItemSelected: (Int, String) -> Unit,
    startingItemIndex: Int = 0
) {

    var expanded by remember {
        mutableStateOf(false)
    }
    var selectedItem by remember {
        mutableStateOf(Pair(startingItemIndex, items[startingItemIndex]))
    }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .background(color = colors.kds_white)
                .padding(dimensions.listItemSpacingMediumSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedItem.second,
                color = colors.kds_create_700,
                style = typography.subheadlineMedium,
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                modifier = Modifier
                    .size(dimensions.listItemSpacingMediumSmall)
                    .rotate(if (expanded) 180f else 0f),
                painter = painterResource(
                    id = R.drawable.ic_down,
                ),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = colors.kds_support_400.copy(alpha = 0.8f))
            )
        }

        ExposedDropdownMenu(
            modifier = Modifier.background(color = colors.kds_white),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    modifier = Modifier.background(color = colors.kds_white),
                    onClick = {
                        onItemSelected(index, item)
                        selectedItem = Pair(index, item)
                        expanded = false
                    }
                ) {
                    Text(text = item, style = typography.body2, color = colors.kds_support_700)
                }
            }
        }
    }
}
