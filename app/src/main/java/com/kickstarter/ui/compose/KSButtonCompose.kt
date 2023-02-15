package com.kickstarter.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kickstarter.R

@Preview(showBackground = true)
@Composable
fun KsButtonPreview() {
    MaterialTheme {
        Column(Modifier.background(Color.White).height(IntrinsicSize.Min)) {
            Spacer(modifier = Modifier.height(1.dp))
            KsButton(
                defaultText = "Creator Studio",
                pressedText = "Creator Studio",
                defaultImageVector = ImageVector.vectorResource(id = R.drawable.icon__heart),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun KsButton(
    defaultText: String,
    pressedText: String,
    modifier: Modifier,
    defaultImageVector: ImageVector? = null,
    pressedImageVector: ImageVector? = null,
    defaultButtonColor: Color = kds_support_700,
    pressedButtonColor: Color = kds_support_700,
    defaultButtonBorderColors: Color = kds_support_700,
    pressedButtonBorderColors: Color = kds_support_700,
    defaultTextColor: Color = kds_white,
    pressedTextColor: Color = kds_white,
    isChecked: Boolean = true,
    onClickAction: () -> Unit = {}
) {
    val isCheckedState = remember { mutableStateOf(isChecked) }
    isCheckedState.value = isChecked

    val buttonColor = if (isCheckedState.value) pressedButtonColor else defaultButtonColor
    val buttonText = if (isCheckedState.value) pressedText else defaultText
    val textColor = if (isCheckedState.value) pressedTextColor else defaultTextColor
    val iconImageVector = if (isCheckedState.value) pressedImageVector else defaultImageVector
    val buttonBorderColors = if (isCheckedState.value) {
        pressedButtonBorderColors
    } else {
        defaultButtonBorderColors
    }

    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.fab_radius)),
        border = BorderStroke(1.dp, buttonBorderColors),
        contentPadding = PaddingValues(
            top = dimensionResource(id = R.dimen.grid_2),
            bottom = dimensionResource(id = R.dimen.grid_2)
        ),
        onClick = {
            isCheckedState.value = !isCheckedState.value
            onClickAction()
        }
    ) {
        iconImageVector?.let {
            Icon(
                imageVector = it,
                contentDescription = "null",
                modifier = Modifier
                    .padding(end = dimensionResource(id = R.dimen.grid_1)),
                tint = Color.Unspecified
            )
        }
        Text(
            text = buttonText,
            style = MaterialTheme.typography.body1.copy(
                fontSize = dimensionResource(id = R.dimen.callout).value.sp,
                fontFamily = FontFamily(
                    Font(DeviceFontFamilyName("sans-serif-medium"))
                )
            ),
            color = textColor,
            letterSpacing = 0.0.sp
        )
    }
}
