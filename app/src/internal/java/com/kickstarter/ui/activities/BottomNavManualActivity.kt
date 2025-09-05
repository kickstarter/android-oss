package com.kickstarter.ui.activities

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.kickstarter.features.search.ui.SearchAndFilterActivity
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.KickstarterApp

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
                            NavigationBar {
                                @Composable
                                fun Item(tab: Tab, icon: ImageVector, label: String) =
                                    NavigationBarItem(
                                        selected = currentTab == tab,
                                        onClick = { if (currentTab != tab) ctx.launchTab(tab) },
                                        icon = { Icon(icon, null) },
                                        label = { Text(label) },
                                        alwaysShowLabel = false
                                    )

                                Item(Tab.HOME, Icons.Default.Home, "Home")
                                Item(Tab.SEARCH, Icons.Default.Search, "Search")
                                Item(Tab.PROFILE, Icons.Default.Person, "Profile")
                            }
                        }
                    ) { inner ->
                        Box(
                            Modifier.fillMaxSize().padding(inner),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Home")
                        }
                    }
                }
            }
        }
    }
}
