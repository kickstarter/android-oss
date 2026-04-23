package com.kickstarter.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.libs.featureflag.StatsigClientType
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.KSCoralBadge
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSSwitch
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.toolbars.compose.TopToolBar
import com.kickstarter.utils.WindowInsetsUtil
import java.util.Locale

class StatsigFeatureGatesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val statsigClient = requireNotNull(applicationContext.getEnvironment()?.statsigClient())

        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        WindowInsetsUtil.manageEdgeToEdge(window, rootView)

        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            KickstarterApp(useDarkTheme = isDarkTheme) {
                StatsigFeatureGatesScreen(
                    statsigClient = statsigClient,
                    onBackClicked = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}

data class GateUiState(
    val gateKey: StatsigGateKey,
    val currentValue: Boolean,
    val isOverridden: Boolean,
)

private fun loadGateStates(client: StatsigClientType): List<GateUiState> {
    val overriddenGates = client.getAllOverrides().gates
    return StatsigGateKey.entries.map { gate ->
        GateUiState(
            gateKey = gate,
            currentValue = client.checkGate(gate.key),
            isOverridden = overriddenGates.containsKey(gate.key),
        )
    }
}

@Composable
fun StatsigFeatureGatesScreen(
    statsigClient: StatsigClientType,
    onBackClicked: () -> Unit,
) {
    var gateStates by remember { mutableStateOf(loadGateStates(statsigClient)) }

    fun refresh() {
        gateStates = loadGateStates(statsigClient)
    }

    Scaffold(
        topBar = {
            TopToolBar(
                title = "Feature Gates",
                titleColor = colors.kds_black,
                leftIconColor = colors.kds_black,
                leftOnClickAction = onBackClicked,
                backgroundColor = colors.backgroundSurfaceSecondary,
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundSurfacePrimary)
                .padding(padding),
            contentPadding = PaddingValues(dimensions.paddingSmall),
        ) {
            items(gateStates, key = { it.gateKey.key }) { state ->
                GateRow(
                    state = state,
                    onToggle = { newValue ->
                        statsigClient.overrideGate(state.gateKey.key, newValue)
                        refresh()
                    },
                    onReset = {
                        statsigClient.removeGateOverride(state.gateKey.key)
                        refresh()
                    }
                )
                KSDividerLineGrey()
            }
        }
    }
}

@Composable
private fun GateRow(
    state: GateUiState,
    onToggle: (Boolean) -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.paddingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.gateKey.key
                    .split('_')
                    .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } },
                style = typographyV2.bodyBoldMD,
                color = colors.kds_support_700,
            )
            Text(
                text = state.gateKey.key,
                style = typographyV2.bodyXS,
                color = colors.kds_support_400,
            )
            if (state.isOverridden) {
                Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KSCoralBadge(text = "OVERRIDDEN")
                    Spacer(modifier = Modifier.width(dimensions.paddingSmall))
                    Text(
                        text = "Reset",
                        style = typographyV2.linkSM,
                        color = colors.kds_create_700,
                        modifier = Modifier.clickable { onReset() },
                    )
                }
            }
        }
        KSSwitch(
            checked = state.currentValue,
            onCheckChanged = onToggle,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GateRowOverriddenPreview() {
    KSTheme {
        GateRow(
            state = GateUiState(
                gateKey = StatsigGateKey.ANDROID_VIDEO_FEED,
                currentValue = true,
                isOverridden = true,
            ),
            onToggle = {},
            onReset = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GateRowLivePreview() {
    KSTheme {
        GateRow(
            state = GateUiState(
                gateKey = StatsigGateKey.ANDROID_VIDEO_FEED,
                currentValue = false,
                isOverridden = false,
            ),
            onToggle = {},
            onReset = {},
        )
    }
}
