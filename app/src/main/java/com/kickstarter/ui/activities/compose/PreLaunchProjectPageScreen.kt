package com.kickstarter.ui.activities.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.toHtml
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.COMING_SOON_BADGE
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.CREATOR_LAYOUT
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_CATEGORY_NAME
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_DESCRIPTION
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_FOLLOWERS
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_IMAGE
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_LOCATION_NAME
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_NAME
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_SAVE_BUTTON
import com.kickstarter.ui.compose.ProjectImageFromURl
import com.kickstarter.ui.compose.TextBody2Style
import com.kickstarter.ui.compose.TextCaptionStyle
import com.kickstarter.ui.compose.TextCaptionStyleWithStartIcon
import com.kickstarter.ui.compose.TextH6ExtraBoldTitle
import com.kickstarter.ui.compose.TextWithKdsSupport700Bg
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KsButton
import com.kickstarter.ui.toolbars.compose.ToolbarIconButton
import com.kickstarter.ui.toolbars.compose.ToolbarIconToggleButton
import com.kickstarter.ui.toolbars.compose.TopToolBar
import com.kickstarter.ui.views.compose.KsCreatorLayout

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PreLaunchProjectPageScreenPreview() {
    KSTheme {
        val project = ProjectFactory.backedProject()
        val projectState = remember { mutableStateOf(null) }
        PreLaunchProjectPageScreen(projectState)
    }
}

enum class PreLaunchProjectPageScreenTestTag() {
    PROJECT_IMAGE,
    COMING_SOON_BADGE,
    PROJECT_NAME,
    CREATOR_LAYOUT,
    PROJECT_DESCRIPTION,
    PROJECT_CATEGORY_NAME,
    PROJECT_LOCATION_NAME,
    PROJECT_SAVE_BUTTON,
    PROJECT_FOLLOWERS
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PreLaunchProjectPageScreen(
    projectState: State<Project?>,
    leftOnClickAction: () -> Unit = {},
    rightOnClickAction: () -> Unit = {},
    middleRightClickAction: () -> Unit = {},
    onCreatorLayoutClicked: () -> Unit = {},
    onButtonClicked: () -> Unit = {},
    numberOfFollowers: String? = null
) {
    val project = projectState.value
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopToolBar(
                right = {
                    ToolbarIconToggleButton(
                        icon = ImageVector.vectorResource(id = R.drawable.icon__heart_outline),
                        checkedImageVector = ImageVector.vectorResource(id = R.drawable.icon__heart),
                        clickAction = { rightOnClickAction.invoke() },
                        initialState = project?.isStarred() ?: false
                    )
                },
                middle = {
                    ToolbarIconButton(
                        icon = Icons.Filled.Share,
                        clickAction = { middleRightClickAction.invoke() }
                    )
                },
                leftOnClickAction = { leftOnClickAction() }
            )
        }
    ) {
        val screenPadding = dimensionResource(id = R.dimen.activity_horizontal_margin)
        ConstraintLayout(
            modifier = Modifier.fillMaxSize().background(colors.kds_support_100)
        ) {
            val (
                projectImage, comingSoonBadge, projectName, creatorLayout,
                projectDescription, category, location, buttonCardLayout
            ) = createRefs()

            ProjectImageFromURl(
                imageUrl = (project?.photo()?.full()),
                modifier = Modifier
                    .testTag(PROJECT_IMAGE.name)
                    .constrainAs(projectImage) {
                        top.linkTo(parent.top)
                    }
                    .aspectRatio(1.77f)
            )

            TextWithKdsSupport700Bg(
                stringResource(id = R.string.Coming_soon),
                Modifier
                    .constrainAs(comingSoonBadge) {
                        top.linkTo(projectImage.bottom)
                        bottom.linkTo(projectImage.bottom)
                        start.linkTo(parent.start, screenPadding)
                    }
                    .testTag(COMING_SOON_BADGE.name)
            )

            val projectNameAlpha = if (project?.name().isNullOrBlank()) 0f else 1f
            TextH6ExtraBoldTitle(
                text = project?.name().orEmpty(),
                modifier = Modifier
                    .alpha(projectNameAlpha)
                    .constrainAs(projectName) {
                        top.linkTo(comingSoonBadge.bottom)
                        start.linkTo(parent.start, screenPadding)
                    }
                    .padding(end = screenPadding)
                    .testTag(PROJECT_NAME.name)
            )

            KsCreatorLayout(
                creatorName = project?.creator()?.name() ?: "",
                imageUrl = project?.creator()?.avatar()?.medium() ?: "",
                modifier = Modifier
                    .testTag(CREATOR_LAYOUT.name)
                    .fillMaxWidth()
                    .constrainAs(creatorLayout) {
                        top.linkTo(projectName.bottom)
                        start.linkTo(parent.start, screenPadding)
                    }
                    .padding(end = screenPadding)
                    .padding(
                        vertical = dimensionResource(id = R.dimen.grid_1)
                    ),
                onClickAction = {
                    onCreatorLayoutClicked.invoke()
                }
            )

            project?.blurb()?.let {
                TextBody2Style(
                    text = it.toHtml().toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(PROJECT_DESCRIPTION.name)
                        .constrainAs(projectDescription) {
                            top.linkTo(creatorLayout.bottom)
                            start.linkTo(parent.start, screenPadding)
                        }
                        .padding(end = screenPadding)
                        .padding(
                            vertical = dimensionResource(id = R.dimen.grid_1)
                        )
                )
            }

            project?.category()?.name()?.let {
                TextCaptionStyleWithStartIcon(
                    it,
                    painterResource(R.drawable.icon__compass),
                    modifier = Modifier
                        .testTag(PROJECT_CATEGORY_NAME.name)
                        .constrainAs(category) {
                            top.linkTo(projectDescription.bottom)
                            start.linkTo(parent.start, screenPadding)
                        }
                        .padding(end = screenPadding)
                        .padding(vertical = dimensionResource(id = R.dimen.grid_2))
                )
            }

            project?.location()?.name()?.let {
                val locationPadding = dimensionResource(id = R.dimen.grid_5)
                TextCaptionStyleWithStartIcon(
                    it,
                    Icons.Filled.LocationOn,
                    modifier = Modifier
                        .testTag(PROJECT_LOCATION_NAME.name)
                        .constrainAs(location) {
                            start.linkTo(category.end, locationPadding)
                            top.linkTo(category.top)
                            bottom.linkTo(category.bottom)
                        }
                        .padding(end = screenPadding)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(buttonCardLayout) {
                        bottom.linkTo(parent.bottom)
                    },
                shape = RoundedCornerShape(
                    topStart = dimensionResource(id = R.dimen.grid_5),
                    topEnd = dimensionResource(id = R.dimen.grid_5)
                ),
                elevation = dimensionResource(id = R.dimen.grid_2),
                backgroundColor = colors.kds_white
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .padding(
                            vertical = dimensionResource(id = R.dimen.grid_3)
                        )
                        .fillMaxWidth()
                ) {
                    val (button, text) = createRefs()
                    KsButton(
                        defaultText = stringResource(id = R.string.Notify_me_on_launch),
                        pressedText = stringResource(id = R.string.Saved),
                        defaultImageVector = ImageVector.vectorResource(id = R.drawable.ic_discovery_heart),
                        pressedImageVector = ImageVector.vectorResource(id = R.drawable.icon__heart),
                        pressedButtonColor = colors.kds_white,
                        pressedTextColor = colors.kds_support_500,
                        onClickAction = { onButtonClicked.invoke() },
                        isChecked = project?.isStarred() ?: false,
                        modifier = Modifier
                            .testTag(PROJECT_SAVE_BUTTON.name)
                            .constrainAs(button) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start, screenPadding)
                                end.linkTo(parent.end, screenPadding)
                            }
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.grid_4)
                            )
                            .padding(top = dimensionResource(id = R.dimen.grid_5_half))
                            .fillMaxWidth()
                    )

                    numberOfFollowers?.let {
                        TextCaptionStyle(
                            text = it,
                            modifier = Modifier
                                .testTag(PROJECT_FOLLOWERS.name)
                                .constrainAs(text) {
                                    top.linkTo(button.bottom, margin = 8.dp)
                                    start.linkTo(parent.start, screenPadding)
                                    end.linkTo(parent.end, screenPadding)
                                }
                                .padding(bottom = dimensionResource(id = R.dimen.grid_3))
                        )
                    }
                }
            }
        }
    }
}
