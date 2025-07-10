import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSButton
import com.kickstarter.ui.compose.designsystem.KSButtonType
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

data class OnboardingPageData(
    val title: String,
    val description: String,
    val animationRes: Int,
    val buttonText: String,
    val secondaryButtonText: String? = null
)

@Composable
@Preview
fun OnboardingScreenPreview() {
    OnboardingScreen()
}

@Composable
fun OnboardingScreen() {
    val pages = listOf(
        OnboardingPageData(
            title = stringResource(R.string.onboarding_welcome_to_kickstarter_title),
            description = stringResource(R.string.onboarding_welcome_to_kickstarter_subtitle),
            animationRes = R.raw.android_onboarding_flow_welcome,
            buttonText = stringResource(R.string.project_checkout_navigation_next)
        ),
        OnboardingPageData(
            title = stringResource(R.string.onboarding_save_projects_for_later_title),
            description = stringResource(R.string.onboarding_save_projects_for_later_subtitle),
            animationRes = R.raw.android_onboarding_flow_save_projects,
            buttonText = stringResource(R.string.project_checkout_navigation_next)
        ),
        OnboardingPageData(
            title = stringResource(R.string.onboarding_stay_in_the_know_title),
            description = stringResource(R.string.onboarding_stay_in_the_know_subtitle),
            animationRes = R.raw.android_onboarding_flow_enable_notifications,
            buttonText = stringResource(R.string.Get_notified),
            secondaryButtonText = stringResource(R.string.Not_right_now),
        ),
        OnboardingPageData(
            title = stringResource(R.string.onboarding_personalize_your_experience_title),
            description = stringResource(R.string.onboarding_personalize_your_experience_subtitle),
            animationRes = R.raw.android_onboarding_flow_activity_tracking,
            buttonText = stringResource(R.string.Use_personalization),
            secondaryButtonText = stringResource(R.string.Not_right_now),
        ),
        OnboardingPageData(
            title = stringResource(R.string.onboarding_join_the_community_title),
            description = stringResource(R.string.onboarding_join_the_community_subtitle),
            animationRes = R.raw.android_onboarding_flow_login_signup,
            buttonText = stringResource(R.string.Sign_up_or_log_in),
            secondaryButtonText = stringResource(R.string.Explore_the_app),
        )
    )

    var currentPage by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.borderAccentGreenSubtle)
            .navigationBarsPadding() // fix bottom nav bar overlap
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_squiggle),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Progress bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = dimensions.paddingLarge)
                    .padding(top = dimensions.paddingDoubleLarge, bottom = dimensions.paddingLarge)
            ) {
                LinearProgressIndicator(
                    progress = (currentPage + 1) / pages.size.toFloat(),
                    color = colors.textAccentGreen,
                    backgroundColor = colors.kds_white,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .weight(1f)
                        .height(dimensions.paddingSmall)
                )
                KSIconButton(onClick = {}, imageVector = Icons.Filled.Close)
            }

            // Animated content spans the page
            OnboardingPageAnimation(modifier = Modifier.weight(1.0f), pageData = pages[currentPage])

            // Buttons footer
            Column(
                modifier = Modifier
                    .height(dimensions.footerHeight)
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingSmall)
            ) {
                KSPrimaryBlackButton(
                    text = pages[currentPage].buttonText,
                    onClickAction = {
                        if (currentPage < pages.lastIndex) {
                            currentPage++
                        } else {
                            // Complete onboarding
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isEnabled = true
                )

                pages[currentPage].secondaryButtonText?.let { secondaryText ->
                    KSButton(
                        text = secondaryText,
                        onClickAction = { /* handle skip */ },
                        modifier = Modifier.fillMaxWidth(),
                        type = KSButtonType.BORDERLESS
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageAnimation(modifier: Modifier, pageData: OnboardingPageData) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Vertical transition for text
        AnimatedContent(
            targetState = pageData,
            transitionSpec = {
                (fadeIn() + slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 4 }))
                    .togetherWith(fadeOut(animationSpec = tween(200)))
            },
            label = "TextTransition"
        ) { targetPage ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.paddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = targetPage.title,
                    style = typographyV2.heading2XL,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                Text(
                    text = targetPage.description,
                    style = typographyV2.bodyMD,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    minLines = 4
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        // Horizontal transition for Lottie Animation
        AnimatedContent(
            targetState = pageData,
            transitionSpec = {
                (fadeIn(tween(2000)) + slideInHorizontally(initialOffsetX = { it })).togetherWith(
                    fadeOut(tween(2000)) + slideOutHorizontally(targetOffsetX = { -it })
                )
            },
            label = "ImageTransition"
        ) { targetPage ->

            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(targetPage.animationRes))
            val progress by animateLottieCompositionAsState(
                composition,
                iterations = LottieConstants.IterateForever,
            )
            LottieAnimation(
                modifier = Modifier.fillMaxSize(),
                composition = composition,
                progress = { progress },
            )
        }
    }
}
