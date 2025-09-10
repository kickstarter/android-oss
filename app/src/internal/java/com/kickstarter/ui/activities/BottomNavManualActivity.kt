package com.kickstarter.ui.activities

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kickstarter.features.search.ui.SearchAndFilterActivity
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.views.compose.KSColorAccentedBanner

enum class Tab { HOME, SEARCH, PROFILE }

fun Context.intentFor(tab: Tab) = when (tab) {
    Tab.HOME -> Intent(this, BottomNavManualActivity::class.java)
    Tab.SEARCH -> Intent(this, SearchAndFilterActivity::class.java)
    Tab.PROFILE -> Intent(this, ProfileActivity::class.java)
}

const val EXTRA_TAB = "ks.extra.TAB"
fun Context.launchTab(tab: Tab) {
    val options = ActivityOptions
        .makeCustomAnimation(this, 0, 0) // 0 disables enter/exit anims

    startActivity(
        intentFor(tab).addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        ).putExtra(EXTRA_TAB, tab.name),
        options.toBundle()
    )
}

class BottomNavManualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        this.getEnvironment()?.let { env ->
            setContent {
                KickstarterApp(useDarkTheme = true) {
                    val ctx = LocalContext.current

                    // to get the current tab, instead of using intent, could probably just use the Activity name
                    // make it "smart" that way, so no need to go Activity by Activity reading/writing the intent
                    val currentTab = intent.getStringExtra(EXTRA_TAB)?.let { Tab.valueOf(it) } ?: Tab.HOME

                    Scaffold(
                        bottomBar = {
                            Scaffold { inner ->
                                Box(Modifier.fillMaxSize().padding(inner)) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(16.dp)
                                            .clip(RoundedCornerShape(28.dp)),
                                        tonalElevation = 6.dp
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            NavItem(Tab.HOME, icon = Icons.Outlined.Home, selected = true, onClick = { tab -> if (currentTab != tab) ctx.launchTab(tab) })
                                            NavItem(Tab.SEARCH, icon = Icons.Outlined.Search, selected = false, onClick = { tab -> if (currentTab != tab) ctx.launchTab(tab) })
                                            NavItem(Tab.PROFILE, Icons.Outlined.Person, selected = false, onClick = { tab -> if (currentTab != tab) ctx.launchTab(tab) })
                                        }
                                    }
                                }
                            }
//                          Cannot custumize the design in wich the bottomNav does not take the full width of the screen
//                            NavigationBar {
//                                @Composable
//                                fun Item(tab: Tab, icon: ImageVector, label: String) =
//                                    NavigationBarItem(
//                                        selected = currentTab == tab,
//                                        onClick = { if (currentTab != tab) ctx.launchTab(tab) },
//                                        icon = { Icon(icon, null) },
//                                        label = { Text(label) },
//                                        alwaysShowLabel = false
//                                    )
//
//                                Item(Tab.HOME, Icons.Default.Home, "Home")
//                                Item(Tab.SEARCH, Icons.Default.Search, "Search")
//                                Item(Tab.PROFILE, Icons.Default.Person, "Profile")
//                            }
                        }
                    ) { inner ->
                        Box(
                            Modifier
                                .background(MaterialTheme.colorScheme.secondary)
                                .fillMaxSize().padding(inner),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Home")
                        }
                    }
                }
            }
        }
    }



    @Composable
    fun NavItem(
        tab: Tab,
        icon: ImageVector? = null,
        avatar: Painter? = null,
        contentDescription: String? = null,
        selected: Boolean,
        onClick: (Tab) -> Unit
    ) {
        val color = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current
        val background = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        } else {
            Color.Transparent
        }

        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 24.dp),
                    onClick = { onClick(tab) }
                ),
            color = background
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    icon != null -> Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = color
                    )
                    avatar != null -> Image(
                        painter = avatar,
                        contentDescription = contentDescription,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }

}
