package com.kickstarter.features.home.ui.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kickstarter.features.home.data.Tab
import com.kickstarter.features.home.data.tabs

@Composable
fun FloatingCenterPillM3(nav: NavHostController, shouldShowBottomNav: MutableState<Boolean>) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    Box(
        Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(152.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.LightGray.copy(alpha = 0.5f),
                    spotColor = Color.LightGray.copy(alpha = 0.5f),
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {

            // Needed to remove ripple
            val interactionSource = remember { MutableInteractionSource() }

            NavigationBar(
                modifier = Modifier
                    .height(64.dp)
                    .widthIn(min = 260.dp)
                    .padding(horizontal = 8.dp),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                tabs.forEach { tab ->
                    val selected = current == tab.route

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (tab == Tab.Profile) shouldShowBottomNav.value = false
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.surface,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.indication(
                            interactionSource = interactionSource,
                            indication = null,
                        ),
                        interactionSource = null,
                        icon = {
                            val activeColor = Color(0xFFB6F36C)
                            val inactiveColor = Color.Transparent

                            val targetColor = if (selected) activeColor else inactiveColor

                            // - color property animated for tab icons
                            val animatedBackgroundColor by animateColorAsState(
                                targetValue = targetColor,
                                animationSpec = tween(
                                    durationMillis = 300, // from figma values
                                    easing = EaseInBack
                                ),
                            )

                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(animatedBackgroundColor)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.route
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
