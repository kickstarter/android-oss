package com.kickstarter.features.home.ui.compose

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kickstarter.features.home.data.Tab
import com.kickstarter.features.home.data.TabIcon
import com.kickstarter.ui.compose.KSCircleImage
import com.kickstarter.ui.compose.designsystem.KSTheme

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FloatingCenterBottomNavLoggedOutPreview() {
    KSTheme {
        Box(
            modifier = Modifier.background(Color.LightGray)
        ) {
            val nav = rememberNavController()
            val tabs = listOf<Tab>(Tab.Home, Tab.Search, Tab.LogIn)
            FloatingCenterBottomNav(nav, tabs)
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FloatingCenterBottomNavLoggedInPreview() {
    KSTheme {
        Box(
            modifier = Modifier.background(Color.LightGray)
        ) {
            val nav = rememberNavController()
            val tabs = listOf<Tab>(Tab.Home, Tab.Search, Tab.Profile(""))
            FloatingCenterBottomNav(nav, tabs)
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

    val baseModifier = modifier
        .clip(RoundedCornerShape(KSTheme.dimensions.navIconPadding))
        .background(animatedBackgroundColor)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )

    val colorFilter =
        if (selected) ColorFilter.tint(KSTheme.colors.navIconSelected)
        else ColorFilter.tint(KSTheme.colors.navIcon)

    when (tab.icon) {
        is TabIcon.Static -> {
            Image(
                modifier = baseModifier
                    .padding(KSTheme.dimensions.navIconPadding),
                imageVector = tab.icon.vector,
                contentDescription = tab.route,
                colorFilter = colorFilter
            )
        }
        is TabIcon.Dynamic -> {
            KSCircleImage(
                url = tab.icon.url,
                contentDescription = tab.route,
                modifier = baseModifier
                    .padding(KSTheme.dimensions.navIconPadding / 2)
                    .sizeIn(maxWidth = KSTheme.dimensions.navAvatarSize, maxHeight = KSTheme.dimensions.navAvatarSize)
                    .border(
                        width = KSTheme.dimensions.strokeWidth,
                        color = KSTheme.colors.navIconBorderAvatar,
                        shape = CircleShape
                    )

            )
        }
        is TabIcon.Resource -> {
            Image(
                modifier = baseModifier
                    .padding(KSTheme.dimensions.navIconPadding),
                painter = painterResource(id = tab.icon.id),
                contentDescription = tab.route,
                colorFilter = colorFilter
            )
        }
    }
}

@Composable
fun FloatingCenterBottomNav(
    nav: NavHostController,
    tabs: List<Tab> = listOf(Tab.Home, Tab.Search, Tab.LogIn)
) {
    // TODO: move navigation references to parent, FloatingCenterBottomNav should only have a callback for ie onTabSelected: (Tab) -> Unit
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val activeIndex = remember(current, tabs) {
        tabs.indexOfFirst { it.route == current }.coerceAtLeast(0)
    }

    // - animation offSet X for sliding container
    val indicatorOffset = remember { Animatable(0f) }
    // - coordinates directory data sample: Index 0 (Home) is at 40.0f || Index 1 (Search) is at 120.0f ...
    val tabsXCoordinate = remember { mutableStateMapOf<Int, Float>() }

    LaunchedEffect(activeIndex, tabsXCoordinate.size) {
        tabsXCoordinate[activeIndex]?.let { targetX ->
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
                        color = KSTheme.colors.navBoxShadow.copy(alpha = 0.28f),
                        offset = DpOffset(x = KSTheme.dimensions.navShadowXOffset, KSTheme.dimensions.navShadowYOffset)
                    )
                )
                .background(
                    color = KSTheme.colors.navBackground,
                    shape = RoundedCornerShape(KSTheme.dimensions.navCorner)
                )
        ) {

            // - Layout of sliding animated container simulating background of FloatingPillNavItem
            Box(
                modifier = Modifier
                    .padding(KSTheme.dimensions.navIconPadding)
                    .size(KSTheme.dimensions.navIconSize)
                    .graphicsLayer { // - animated container x-offset
                        translationX = indicatorOffset.value
                    }
                    .background(
                        color = KSTheme.colors.navBackgroundHighlight,
                        shape = RoundedCornerShape(KSTheme.dimensions.navCornerIcon)
                    )
            )

            // - BottomNav Layout
            Row(
                modifier = Modifier
                    .padding(KSTheme.dimensions.navPadding)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = current == tab.route
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coordinates ->
                                val xPos = coordinates.positionInParent().x
                                if (tabsXCoordinate[index] != xPos) {
                                    tabsXCoordinate[index] = xPos
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingCenterNavItem(
                            modifier = Modifier.sizeIn(maxWidth = 40.dp),
                            tab = tab,
                            selected = selected,
                            onClick = {
                                // TODO: should be a callback floating Nav should have no knowledge of navigation graph.
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
