package com.kickstarter.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.compose.KSTheme.colors

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

            Spacer(modifier = Modifier.height(8.dp))

            KSSwitch(checked = checkedOff, onCheckChanged = { checkedOff = ! checkedOff })

            Spacer(modifier = Modifier.height(8.dp))

            KSSwitch(checked = checkedOn, onCheckChanged = {}, enabled = false)

            Spacer(modifier = Modifier.height(8.dp))

            KSRadioButton(
                selected = radioButtonSelected,
                onClick = { radioButtonSelected = !radioButtonSelected }
            )

            Spacer(modifier = Modifier.height(8.dp))

            KSRadioButton(
                selected = !radioButtonSelected,
                onClick = { radioButtonSelected = ! radioButtonSelected })

            Spacer(modifier = Modifier.height(8.dp))

            KSRadioButton(selected = radioButtonSelected, onClick = {}, enabled = false)

            Spacer(modifier = Modifier.height(8.dp))

            KSCheckbox(checked = checked, onCheckChanged = { checked = ! checked} )

            Spacer(modifier = Modifier.height(8.dp))

            KSCheckbox(checked = checked, onCheckChanged = {}, enabled = false)
        }
    }
}

@Composable
fun KSSwitch(
    checked: Boolean,
    onCheckChanged: ((Boolean) -> Unit),
    enabled: Boolean = true
) {
    Switch(
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