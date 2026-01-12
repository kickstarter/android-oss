package com.kickstarter.features.home.ui.compose

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kickstarter.features.home.data.Tab
import com.kickstarter.features.home.data.tabs
import com.kickstarter.ui.compose.designsystem.KSTheme
import kotlin.math.roundToInt

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AppPreview() {
    KSTheme {
        Box(
            modifier = Modifier.background(Color.LightGray)
        ) {
            val nav = rememberNavController()
            FloatingCenterBottomNav(nav)
        }
    }
}

@Composable
private fun FloatingCenterNavItem(
    modifier: Modifier = Modifier,
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val activeColor = KSTheme.colors.navBackgroundHighlight

    val targetColor = when {
        pressed -> KSTheme.colors.navBackgroundTapped
        else -> {
            activeColor.copy(alpha = 0f)
        }
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        )
    )

    Image(
        modifier = modifier
            .clip(RoundedCornerShape(KSTheme.dimensions.navIconPadding))
            .background(animatedBackgroundColor)
            .padding(KSTheme.dimensions.navIconPadding)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        imageVector = tab.icon,
        contentDescription = tab.route,
        colorFilter = if (selected) ColorFilter.tint(KSTheme.colors.navIconSelected)
        else ColorFilter.tint(KSTheme.colors.navIcon)
    )
}

@Composable
fun FloatingCenterBottomNav(
    nav: NavHostController,
) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val activeIndex = tabs.indexOfFirst { it.route == current }.coerceAtLeast(0)

    // - animation offSet state for sliding container
    val indicatorOffset = remember { Animatable(0f) }
    val activeColor = KSTheme.colors.navBackgroundHighlight

    // - State to store the final layout data of the items and the Row itself
    val itemLayouts = remember { mutableStateMapOf<Int, IntOffset>() }
    val itemWidths = remember { mutableStateMapOf<Int, Int>() }
    var rowCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    // - Animates the indicator whenever the active tab/layout/coordinates changes
    LaunchedEffect(activeIndex, itemLayouts.size, rowCoordinates) {
        val rowX = rowCoordinates?.positionInWindow()?.x?.roundToInt() ?: 0
        val itemWindowOffset = itemLayouts[activeIndex] ?: return@LaunchedEffect
        val targetX = (itemWindowOffset.x - rowX).toFloat()

        if (targetX >= 0f) {
            indicatorOffset.animateTo(
                targetValue = targetX,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
            )
        }
    }

    Box(
        Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(horizontal = KSTheme.dimensions.navPadding, vertical = KSTheme.dimensions.paddingLarge),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .width(KSTheme.dimensions.navWidth)
                .dropShadow(
                    shape = RoundedCornerShape(KSTheme.dimensions.navCorner),
                    shadow = Shadow(
                        radius = KSTheme.dimensions.navCorner,
                        spread = KSTheme.dimensions.navShadowXOffset,
                        color = KSTheme.colors.kds_black.copy(alpha = 0.28f),
                        offset = DpOffset(x = KSTheme.dimensions.navShadowXOffset, KSTheme.dimensions.navShadowYOffset)
                    )
                )
                .background(
                    color = KSTheme.colors.navBackground,
                    shape = RoundedCornerShape(KSTheme.dimensions.navCorner)
                )
        ) {

            Box {
                // - Sliding animated container, simulating background of FloatingPillNavItem
                Box(
                    modifier = Modifier
                        .padding(KSTheme.dimensions.navIconPadding)
                        .offset { IntOffset(indicatorOffset.value.toInt(), 0) }
                        .size(KSTheme.dimensions.navIconSize)
                        .background(activeColor, RoundedCornerShape(KSTheme.dimensions.navCornerIcon))
                )

                // - BottomNav Layout
                Row(
                    modifier = Modifier
                        .padding(KSTheme.dimensions.navPadding)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates -> rowCoordinates = coordinates },
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = current == tab.route
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned { coordinates ->
                                    itemLayouts[index] = coordinates.positionInWindow().round()
                                    itemWidths[index] = coordinates.size.width
                                }
                        ) {
                            FloatingCenterNavItem(
                                tab = tab,
                                selected = selected,
                                onClick = {
                                    nav.navigate(tab.route) {
                                        popUpTo(nav.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        if (index < tabs.size - 1) {
                            Spacer(modifier = Modifier.width(KSTheme.dimensions.navBetween))
                        }
                    }
                }
            }
        }
    }
}
