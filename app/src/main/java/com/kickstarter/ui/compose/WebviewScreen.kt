package com.kickstarter.ui.compose

import android.content.res.Configuration
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.kickstarter.R
import com.kickstarter.services.RequestHandler
import com.kickstarter.ui.activities.compose.login.LoginToutTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.toolbars.compose.TopToolBar
import com.kickstarter.ui.views.KSWebView

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun WebViewScreenPreview() {
    KSTheme {
        WebViewScreen(
            onBackButtonClicked = {},
            toolbarTitle = "This is a title",
        ) {
            IconButton(
                modifier = Modifier.testTag(LoginToutTestTag.OPTIONS_ICON.name),
                onClick = { },
                enabled = true
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(
                        id = R.string.general_navigation_accessibility_button_help_menu_label
                    ),
                    tint = KSTheme.colors.icon
                )
            }
        }
    }
}

@Composable
fun WebViewScreen(
    onBackButtonClicked: () -> Unit = {},
    toolbarTitle: String = "",
    url: String? = null,
    requestHandlers: List<RequestHandler> = listOf(),
    delegate: KSWebView.Delegate? = null,
    right: @Composable () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopToolBar(
                title = toolbarTitle,
                titleColor = KSTheme.colors.textPrimary,
                leftOnClickAction = onBackButtonClicked,
                leftIconColor = KSTheme.colors.icon,
                backgroundColor = KSTheme.colors.backgroundSurfacePrimary,
                right = right
            )
        },
    ) { padding ->
        var webview: KSWebView? = null
        AndroidView(
            factory = {
                KSWebView(it).apply {
                    webview = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    delegate?.let { del ->
                        this.setDelegate(del)
                    }
                    this.registerRequestHandlers(requestHandlers)
//                    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING) && isInDarkTheme) {
//                        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
//                            WebSettingsCompat.setAlgorithmicDarkeningAllowed(this.getSettings(), true)
//                        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
//                            @Suppress("DEPRECATION")
//                            WebSettingsCompat.setForceDark(this.getSettings(), WebSettingsCompat.FORCE_DARK_ON)
//                        }
//                    }
                }
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            update = {
                it.loadUrl(url)
            }
        )
        BackHandler {
            if (webview?.canGoBack() == true) {
                webview?.goBack()
            } else {
                onBackButtonClicked.invoke()
            }
        }
    }
}
