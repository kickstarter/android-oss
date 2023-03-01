package com.kickstarter.ui.activities.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.toHtml
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.KsButton
import com.kickstarter.ui.compose.KsCreatorLayout
import com.kickstarter.ui.compose.ProjectImageFromURl
import com.kickstarter.ui.compose.TextBody2Style
import com.kickstarter.ui.compose.TextCaptionStyle
import com.kickstarter.ui.compose.TextCaptionStyleWithStartIcon
import com.kickstarter.ui.compose.TextH6ExtraBoldTitle
import com.kickstarter.ui.compose.TextWithKdsSupport700Bg
import com.kickstarter.ui.compose.ToolbarIconButton
import com.kickstarter.ui.compose.ToolbarIconToggleButton
import com.kickstarter.ui.compose.TopToolBar
import com.kickstarter.ui.compose.kds_support_500
import com.kickstarter.ui.compose.kds_white

@Preview(widthDp = 300, heightDp = 800)
@Composable
fun PreLaunchProjectPageScreenPreview() {
    MaterialTheme {
        val project = ProjectFactory.backedProject()
        val projectState = remember { mutableStateOf(null) }
        PreLaunchProjectPageScreen(projectState)
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PreLaunchProjectPageScreen(
    projectState: State<Project?>,
    leftOnClickAction: () -> Unit = {},
    rightOnClickAction: () -> Unit = {},
    middleRightClickAction: () -> Unit = {},
    onCreatorLayoutClicked: () -> Unit = {},
    onButtonClicked: () -> Unit = {}
) {
    val project = projectState.value
    Scaffold(
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
                middle = { ToolbarIconButton(icon = Icons.Filled.Share, clickAction = { middleRightClickAction.invoke() }) },
                leftOnClickAction = { leftOnClickAction() }
            )
        }
    ) {
        val screenPadding = dimensionResource(id = R.dimen.activity_horizontal_margin)
        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (
                projectImage, comingSoonBadge, projectName, creatorLayout,
                projectDescription, category, location, buttonCardLayout
            ) = createRefs()

            ProjectImageFromURl(
                imageUrl = (project?.photo()?.full()),
                modifier = Modifier
                    .constrainAs(projectImage) {
                        top.linkTo(parent.top)
                    }
                    .aspectRatio(1.77f)
            )

            TextWithKdsSupport700Bg(
                stringResource(id = R.string.FPO_Coming_soon),
                Modifier.constrainAs(comingSoonBadge) {
                    top.linkTo(projectImage.bottom)
                    bottom.linkTo(projectImage.bottom)
                    start.linkTo(parent.start, screenPadding)
                }
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
            )

            KsCreatorLayout(
                creatorName = project?.creator()?.name() ?: "",
                imageUrl = project?.creator()?.avatar()?.medium() ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(creatorLayout) {
                        top.linkTo(projectName.bottom)
                        start.linkTo(parent.start, screenPadding)
                    }
                    .padding(end = screenPadding)
                    .padding(
                        vertical =
                        dimensionResource(id = R.dimen.grid_1)
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
                        .constrainAs(projectDescription) {
                            top.linkTo(creatorLayout.bottom)
                            start.linkTo(parent.start, screenPadding)
                        }
                        .padding(end = screenPadding)
                        .padding(
                            vertical =
                            dimensionResource(id = R.dimen.grid_1)
                        )
                )
            }

            project?.category()?.name()?.let {
                TextCaptionStyleWithStartIcon(
                    it,
                    ImageVector.vectorResource(id = R.drawable.icon__compass),
                    modifier = Modifier
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
                elevation = dimensionResource(id = R.dimen.grid_2)
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
                        defaultText = stringResource(id = R.string.FPO_Notify_me_on_launch),
                        pressedText = stringResource(id = R.string.Saved),
                        defaultImageVector = ImageVector.vectorResource(id = R.drawable.ic_discovery_heart),
                        pressedImageVector = ImageVector.vectorResource(id = R.drawable.icon__heart),
                        pressedButtonColor = kds_white,
                        pressedTextColor = kds_support_500,
                        onClickAction = { onButtonClicked.invoke() },
                        isChecked = project?.isStarred() ?: false,
                        modifier = Modifier
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

                    project?.watchesCount()?.toString()?.let {
                        TextCaptionStyle(
                            text = stringResource(R.string.FPO_followers, it),
                            modifier = Modifier
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
