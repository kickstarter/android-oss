package com.kickstarter.features.socialshare.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.features.socialshare.ui.components.SocialSharePlatformGrid
import com.kickstarter.features.socialshare.ui.components.SocialShareProjectCard
import com.kickstarter.features.socialshare.ui.icons.Vector91
import com.kickstarter.features.socialshare.viewmodel.SocialShareViewModel
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SocialShareSheetPreview() {
    KSTheme {
        SocialShareSheetContent(
            shareData = SocialShareData(
                projectName = "Ringo Move - The Ultimate Workout Bottle",
                projectUrl = "https://www.kickstarter.com",
                imageUrl = "",
                creatorName = "Ringo"
            ),
            availablePlatforms = SocialSharePlatform.entries,
            onPlatformSelected = {},
            onCopyLinkSelected = {},
            onDismiss = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialShareSheet(
    shareData: SocialShareData,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onIntentReady: (android.content.Intent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current
    val environment = remember { context.getEnvironment() } ?: return

    val factory = remember(shareData) {
        SocialShareViewModel.Factory(
            environment = environment,
            context = context.applicationContext,
            shareData = shareData
        )
    }
    // key = projectUrl ensures a fresh ViewModel (and fresh image cache job) each
    // time the sheet is opened for a different project, even within the same Activity.
    val viewModel: SocialShareViewModel = viewModel(key = shareData.projectUrl, factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.provideIntentLaunchAction { intent -> onIntentReady(intent) }
        viewModel.provideErrorAction { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message ?: "Something went wrong")
            }
        }
    }

    LaunchedEffect(uiState.copiedToClipboard) {
        if (uiState.copiedToClipboard) {
            snackbarHostState.showSnackbar("Link copied!")
            viewModel.onCopiedToastShown()
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) sheetState.show() else sheetState.hide()
    }

    // Animate the sheet down first, then notify the parent to remove it from composition.
    val smoothDismiss: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(topStart = dimensions.radiusMediumLarge, topEnd = dimensions.radiusMediumLarge),
            containerColor = Color(0xFF05CE78),
            contentWindowInsets = { WindowInsets(0) },
            dragHandle = { SocialShareDragHandle() }
        ) {
            SocialShareSheetContent(
                shareData = shareData,
                availablePlatforms = uiState.availablePlatforms,
                onPlatformSelected = { viewModel.onPlatformSelected(it) },
                onCopyLinkSelected = { viewModel.onCopyLinkClicked() },
                onDismiss = smoothDismiss
            )
        }
    }
}

@Composable
private fun SocialShareSheetContent(
    shareData: SocialShareData,
    availablePlatforms: List<SocialSharePlatform>,
    onPlatformSelected: (SocialSharePlatform) -> Unit,
    onCopyLinkSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF05CE78))
    ) {
        Image(
            imageVector = Vector91,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SocialShareHeader()
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            SocialShareProjectCard(shareData = shareData)
            SocialSharePlatformGrid(
                platforms = availablePlatforms,
                onPlatformSelected = onPlatformSelected,
                onCopyLinkSelected = onCopyLinkSelected
            )
            Spacer(modifier = Modifier.height(dimensions.paddingLarge))
        }
    }
}

@Composable
private fun SocialShareHeader() {
    Text(
        text = "Share project",
        style = KSTheme.typographyV2.headingLG.copy(
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.38.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        ),
        color = colors.textPrimary,
        modifier = Modifier.padding(top = dimensions.paddingMedium)
    )
}

@Composable
private fun SocialShareDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensions.paddingSmall, bottom = dimensions.paddingXSmall),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

enum class SocialShareSheetTestTag {
    CLOSE_BUTTON,
    PLATFORM_GRID,
    PROJECT_CARD
}
