package com.kickstarter.features.socialshare.ui

import android.content.res.Configuration
import android.graphics.Bitmap
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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.features.socialshare.ui.components.SocialSharePlatformGrid
import com.kickstarter.features.socialshare.ui.components.SocialShareProjectCard
import com.kickstarter.features.socialshare.ui.icons.Vector91
import com.kickstarter.features.socialshare.viewmodel.SocialShareViewModel
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import kotlinx.coroutines.launch

val LocalSocialShareViewModel = staticCompositionLocalOf<SocialShareViewModel> {
    error("No SocialShareViewModel provided — wrap the call site in CompositionLocalProvider(LocalSocialShareViewModel provides vm)")
}

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
    val viewModel = LocalSocialShareViewModel.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.provideErrorAction { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message ?: "Something went wrong")
            }
        }
        viewModel.provideIntentLaunchAction { intent -> onIntentReady(intent) }
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
            containerColor = colors.socialShare.background,
            contentWindowInsets = { WindowInsets(0) },
            dragHandle = null
        ) {
            SocialShareSheetContent(
                shareData = shareData,
                availablePlatforms = uiState.availablePlatforms,
                heroBitmap = uiState.heroBitmap,
                onCardCaptured = viewModel::onCardCaptured,
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
    onDismiss: () -> Unit,
    heroBitmap: Bitmap? = null,
    onCardCaptured: ((Bitmap) -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.socialShare.background)
    ) {
        Image(
            imageVector = Vector91,
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SocialShareDragHandle()
            SocialShareHeader()
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            Box(modifier = Modifier.testTag(SocialShareSheetTestTag.PROJECT_CARD.name)) {
                SocialShareProjectCard(
                    shareData = shareData,
                    heroBitmap = heroBitmap,
                    onCaptured = onCardCaptured
                )
            }
            Spacer(modifier = Modifier.height(dimensions.paddingXLarge))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SocialShareSheetTestTag.PLATFORM_GRID.name)
            ) {
                SocialSharePlatformGrid(
                    platforms = availablePlatforms,
                    onPlatformSelected = onPlatformSelected,
                    onCopyLinkSelected = onCopyLinkSelected
                )
            }
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
        color = colors.socialShare.text,
        modifier = Modifier
            .padding(top = dimensions.paddingMedium)
            .semantics { heading() }
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
                .width(dimensions.socialShareDragHandleWidth)
                .height(dimensions.socialShareDragHandleHeight)
                .background(
                    color = colors.socialShare.dragHandle,
                    shape = RoundedCornerShape(dimensions.socialShareDragHandleRadius)
                )
        )
    }
}

enum class SocialShareSheetTestTag {
    PLATFORM_GRID,
    PROJECT_CARD
}
