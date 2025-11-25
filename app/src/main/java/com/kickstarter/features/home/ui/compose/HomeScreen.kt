@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.kickstarter.features.home.ui.compose

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kickstarter.features.home.data.Tab
import com.kickstarter.features.home.data.tabs
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun AppPreview() {
    KickstarterApp {
        Surface {
            val nav = rememberNavController()
            FloatingCenterPill(nav)
        }
    }
}

@Composable
private fun FloatingPillNavItem(
    modifier: Modifier = Modifier,
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val activeColor = Color(0xFFB6F36C)
    // val inactiveColor = Color.Transparent

    val targetColor = when {
        pressed -> activeColor.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = 200,
            easing = LinearOutSlowInEasing
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(animatedBackgroundColor)
            .padding(8.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.route,
        )
    }
}

@Composable // ---> Animation working quite well but not just yet it.
fun FloatingCenterPill(
    nav: NavHostController,
) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val activeIndex = tabs.indexOfFirst { it.route == current }.coerceAtLeast(0)

    // - animation offSet state for sliding container
    val indicatorOffset = remember { Animatable(0f) }
    val activeColor = Color(0xFFB6F36C)

    // State to store the final layout data of the items and the Row itself
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
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val shadow = Color(0xFFB6F36C)
        Box(
            modifier = Modifier
                .width(152.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = shadow.copy(alpha = 0.12f),
                    spotColor = shadow.copy(alpha = 0.12f),
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // - Using BoxWithConstraints as a canvas to draw the animation
            BoxWithConstraints {
                Timber.d("${this.javaClass} minHeigh: $minHeight")
                Timber.d("${this.javaClass} minWidth: $minWidth")
                Timber.d("${this.javaClass} maxHeigh: $maxHeight")
                Timber.d("${this.javaClass} maxWidth: $maxWidth")

                // - Sliding animated container, simulating background of FloatingPillNavItem
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .offset { IntOffset(indicatorOffset.value.toInt(), 0) }
                        .width(40.dp)
                        .height(40.dp)
                        .background(activeColor, RoundedCornerShape(8.dp))
                )

                // - BottomNav Layout
                Row(
                    modifier = Modifier
                        .padding(8.dp)
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
                            FloatingPillNavItem(
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
                    }
                }
            }
        }
    }
}
