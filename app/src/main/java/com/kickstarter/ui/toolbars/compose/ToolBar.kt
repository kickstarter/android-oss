package com.kickstarter.ui.toolbars.compose

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSBetaBadge
import com.kickstarter.ui.compose.designsystem.KSGreenBadge
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ToolBarPreview() {
    KSTheme {
        TopToolBar(
            right = { ToolbarIconButton(icon = ImageVector.vectorResource(id = R.drawable.icon__heart)) },
            middle = { ToolbarIconToggleButton(icon = ImageVector.vectorResource(id = R.drawable.icon__heart)) }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ToolBarPreviewWithText() {
    KSTheme {
        TopToolBar(
            title = "Toolbar",
            right = { ToolbarIconButton(icon = ImageVector.vectorResource(id = R.drawable.icon__heart)) },
            middle = { ToolbarIconToggleButton(icon = ImageVector.vectorResource(id = R.drawable.icon__heart)) },
            showBetaPill = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopToolBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color? = null,
    titleModifier: Modifier = Modifier,
    leftIcon: ImageVector? = Icons.Filled.ArrowBack,
    leftOnClickAction: (() -> Unit)? = {},
    leftIconColor: Color? = null,
    leftIconModifier: Modifier = Modifier,
    right: @Composable () -> Unit = {},
    middle: @Composable () -> Unit = {},
    backgroundColor: Color? = null,
    showBetaPill: Boolean = false,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            title?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = titleModifier,
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = titleColor ?: colors.kds_support_700,
                        style = KSTheme.typographyV2.title2,
                    )
                    if (showBetaPill) {
                        Spacer(modifier = Modifier.width(dimensions.paddingSmall))

                        KSBetaBadge()
                    }
                }
            }
        },
        navigationIcon = {
            if (leftIcon != null && leftOnClickAction != null) {
                IconButton(
                    modifier = leftIconModifier,
                    onClick = { leftOnClickAction() }
                ) {
                    Icon(
                        imageVector = leftIcon,
                        contentDescription = stringResource(id = R.string.Back),
                        tint = leftIconColor ?: colors.kds_black
                    )
                }
            } else {
            }
        },
        actions = {
            middle()
            right()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor ?: colors.kds_white,
            titleContentColor = titleColor ?: colors.kds_support_700,
            navigationIconContentColor = leftIconColor ?: colors.kds_black,
            actionIconContentColor = colors.kds_black
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopToolBarBetaIcon(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color? = null,
    titleModifier: Modifier = Modifier,
    leftIcon: ImageVector? = Icons.Filled.ArrowBack,
    leftOnClickAction: (() -> Unit)? = {},
    leftIconColor: Color? = null,
    leftIconModifier: Modifier = Modifier,
    right: @Composable () -> Unit = {},
    middle: @Composable () -> Unit = {},
    backgroundColor: Color? = null,
    showBetaPill: Boolean = false,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            title?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = titleModifier,
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = titleColor ?: colors.kds_support_700,
                        style = KSTheme.typographyV2.title2,
                    )

                    KSGreenBadge(
                        text = "Beta"
                    )
                }
            }
        },
        navigationIcon = {
            if (leftIcon != null && leftOnClickAction != null) {
                IconButton(
                    modifier = leftIconModifier,
                    onClick = { leftOnClickAction() }
                ) {
                    Icon(
                        imageVector = leftIcon,
                        contentDescription = stringResource(id = R.string.Back),
                        tint = leftIconColor ?: colors.kds_black
                    )
                }
            } else {
            }
        },
        actions = {
            middle()
            right()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor ?: colors.kds_white,
            titleContentColor = titleColor ?: colors.kds_support_700,
            navigationIconContentColor = leftIconColor ?: colors.kds_black,
            actionIconContentColor = colors.kds_black
        )
    )
}

@Composable
fun ToolbarIconButton(icon: ImageVector, clickAction: () -> Unit = {}) =
    IconButton(onClick = { clickAction.invoke() }) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )
    }

@Composable
fun ToolbarIconToggleButton(
    icon: ImageVector,
    checkedImageVector: ImageVector? = null,
    clickAction: () -> Unit = {},
    initialState: Boolean = false,
    initialStateTintColor: Color = colors.kds_black,
    onCheckClicked: Color = colors.kds_celebrate_700
) {
    val state = remember { mutableStateOf(initialState) }
    state.value = initialState
    return IconToggleButton(checked = state.value, onCheckedChange = {
        clickAction.invoke()
        state.value = it
    }) {
        val tint = animateColorAsState(if (state.value) onCheckClicked else initialStateTintColor)
        val imageVector = if (state.value) checkedImageVector ?: icon else icon

        Icon(imageVector = imageVector, contentDescription = null, tint = tint.value)
    }
}
