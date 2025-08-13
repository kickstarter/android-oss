import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kickstarter.R
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ACTIVITY_TRACKING_PROMPT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.ALLOW
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DENY
import com.kickstarter.ui.compose.designsystem.KSButton
import com.kickstarter.ui.compose.designsystem.KSButtonType
import com.kickstarter.ui.compose.designsystem.KSIconButton
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.fragments.ConsentManagementDialogFragment

enum class OnboardingPage(val analyticsSectionName: String) {
    WELCOME("welcome"),
    SAVE_PROJECTS("save_projects"),
    ENABLE_NOTIFICATIONS("enable_notifications"),
    ACTIVITY_TRACKING("activity_tracking"),
    SIGNUP_LOGIN("signup_login"),
}

data class OnboardingPageData(
    val page: OnboardingPage,
    val title: String,
    val description: String,
    val animationRes: Int,
    val buttonText: String,
    val secondaryButtonText: String? = null
)

object OnboardingScreenTestTags {
    const val CLOSE_BUTTON = "onboarding_close_button"
    const val PAGE_TITLE = "onboarding_page_title"
    const val PAGE_DESCRIPTION = "onboarding_page_description"
    const val PAGE_ANIMATION = "onboarding_page_animation" // For the LottieAnimation
    const val PRIMARY_BUTTON = "onboarding_primary_button"
    const val SECONDARY_BUTTON = "onboarding_secondary_button"
    const val PROGRESS_BAR = "onboarding_progress_bar"
}

@Composable
@Preview
fun OnboardingScreenPreview() {
    OnboardingScreen()
}

@Composable
fun OnboardingScreen(
    isUserLoggedIn: Boolean = false,
    deviceNeedsNotificationPermissions: Boolean = false,
    onboardingCompleted: () -> Unit = {},
    onboardingCancelled: (onboardingPage: OnboardingPage) -> Unit = {},
    turnOnNotifications: (permissionLauncher: ActivityResultLauncher<String>) -> Unit = {},
    allowTracking: (fragmentManager: FragmentManager?) -> Unit = {},
    signupOrLogin: () -> Unit = {},
    analyticEvents: AnalyticEvents? = null,
) {
    val initialPages: List<OnboardingPageData> = listOf(
        OnboardingPageData(
            page = OnboardingPage.WELCOME,
            title = stringResource(R.string.onboarding_welcome_to_kickstarter_title),
            description = stringResource(R.string.onboarding_welcome_to_kickstarter_subtitle),
            animationRes = R.raw.android_onboarding_flow_welcome,
            buttonText = stringResource(R.string.project_checkout_navigation_next)
        ),
        OnboardingPageData(
            page = OnboardingPage.SAVE_PROJECTS,
            title = stringResource(R.string.onboarding_save_projects_for_later_title),
            description = stringResource(R.string.onboarding_save_projects_for_later_subtitle),
            animationRes = R.raw.android_onboarding_flow_save_projects,
            buttonText = stringResource(R.string.project_checkout_navigation_next)
        ),
        OnboardingPageData(
            page = OnboardingPage.ENABLE_NOTIFICATIONS,
            title = stringResource(R.string.onboarding_stay_in_the_know_title),
            description = stringResource(R.string.onboarding_stay_in_the_know_subtitle),
            animationRes = R.raw.android_onboarding_flow_enable_notifications,
            buttonText = stringResource(R.string.Get_notified),
            secondaryButtonText = stringResource(R.string.Not_right_now),
        ),
        OnboardingPageData(
            page = OnboardingPage.ACTIVITY_TRACKING,
            title = stringResource(R.string.onboarding_personalize_your_experience_title),
            description = stringResource(R.string.onboarding_personalize_your_experience_subtitle),
            animationRes = R.raw.android_onboarding_flow_activity_tracking,
            buttonText = stringResource(R.string.Use_personalization),
            secondaryButtonText = stringResource(R.string.Not_right_now),
        ),
        OnboardingPageData(
            page = OnboardingPage.SIGNUP_LOGIN,
            title = stringResource(R.string.onboarding_join_the_community_title),
            description = stringResource(R.string.onboarding_join_the_community_subtitle),
            animationRes = R.raw.android_onboarding_flow_login_signup,
            buttonText = stringResource(R.string.Sign_up_or_log_in),
            secondaryButtonText = stringResource(R.string.Explore_the_app),
        )
    )
    // If user is logged in, skip the last page prompting login/signup
    // If user does not need notification permissions, skip the notifications page
    val filteredPages: List<OnboardingPageData> = initialPages.filterNot { pageData ->
        (isUserLoggedIn && pageData.page == OnboardingPage.SIGNUP_LOGIN) ||
            (!deviceNeedsNotificationPermissions && pageData.page == OnboardingPage.ENABLE_NOTIFICATIONS)
    }

    var currentPage by remember { mutableStateOf(0) }
    LaunchedEffect(currentPage) {
        // Launch Page Viewed analytics any time the currentPage changes
        analyticEvents?.trackOnboardingPageViewed(filteredPages[currentPage].page.analyticsSectionName)
    }

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Launcher for notification permission request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            analyticEvents?.trackOnboardingAllowDenyPromptCTAClicked(ACTIVITY_TRACKING_PROMPT.contextName, ALLOW.contextName)
            currentPage++
        } else {
            analyticEvents?.trackOnboardingAllowDenyPromptCTAClicked(ACTIVITY_TRACKING_PROMPT.contextName, DENY.contextName)
            currentPage++
        }
    }

    // Fragment Manager for consent management dialog
    val fragmentManager = remember(context) {
        (context as? FragmentActivity)?.supportFragmentManager
    }
    fragmentManager?.setFragmentResultListener(ConsentManagementDialogFragment.TAG, (context as FragmentActivity)) {
            _, result ->
        val value = result.getString("result") ?: ""
        analyticEvents?.trackOnboardingAllowDenyPromptCTAClicked(ACTIVITY_TRACKING_PROMPT.contextName, value)

        if (isUserLoggedIn) { // Skip signup/login page
            onboardingCompleted()
        } else {
            currentPage++
        }
    }

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
            val animatedProgress by animateFloatAsState(
                targetValue = (currentPage + 1) / filteredPages.size.toFloat(),
                animationSpec = tween(durationMillis = 1000)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = dimensions.paddingLarge)
                    .padding(top = dimensions.paddingDoubleLarge, bottom = dimensions.paddingLarge)
            ) {
                LinearProgressIndicator(
                    progress = animatedProgress,
                    color = colors.textAccentGreen,
                    backgroundColor = colors.kds_white,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .weight(1f)
                        .height(dimensions.paddingSmall)
                        .testTag(OnboardingScreenTestTags.PROGRESS_BAR)
                )
                KSIconButton(
                    modifier = Modifier
                        .testTag(OnboardingScreenTestTags.CLOSE_BUTTON),
                    onClick = { onboardingCancelled(filteredPages[currentPage].page) },
                    contentDescription = stringResource(R.string.Close),
                    imageVector = Icons.Filled.Close
                )
            }

            // Animated content spans the page vertically
            OnboardingPageAnimation(modifier = Modifier.weight(1.0f), pageData = filteredPages[currentPage])

            // Buttons footer
            Column(
                modifier = Modifier
                    .height(dimensions.footerHeight)
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensions.paddingLarge,
                        vertical = dimensions.paddingSmall
                    )
            ) {
                KSPrimaryBlackButton(
                    text = filteredPages[currentPage].buttonText,
                    onClickAction = {
                        when (filteredPages[currentPage].page) {
                            OnboardingPage.WELCOME -> {
                                analyticEvents?.trackOnboardingNextCTAClicked(filteredPages[currentPage].page.analyticsSectionName)
                                currentPage++
                            }
                            OnboardingPage.SAVE_PROJECTS -> {
                                analyticEvents?.trackOnboardingNextCTAClicked(filteredPages[currentPage].page.analyticsSectionName)
                                currentPage++
                            }
                            OnboardingPage.ENABLE_NOTIFICATIONS -> {
                                activity?.let { turnOnNotifications(permissionLauncher) }
                            }

                            OnboardingPage.ACTIVITY_TRACKING -> {
                                activity?.let { allowTracking(fragmentManager) }
                            }

                            OnboardingPage.SIGNUP_LOGIN -> {
                                signupOrLogin()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(OnboardingScreenTestTags.PRIMARY_BUTTON),
                    isEnabled = true
                )

                filteredPages[currentPage].secondaryButtonText?.let { secondaryText ->
                    KSButton(
                        text = secondaryText,
                        onClickAction = {
                            analyticEvents?.trackOnboardingNextCTAClicked(filteredPages[currentPage].page.analyticsSectionName)
                            if (currentPage < filteredPages.lastIndex) { // More pages remaining
                                if (filteredPages[currentPage].page == OnboardingPage.ACTIVITY_TRACKING && isUserLoggedIn) {
                                    // Skip signup/login page
                                    onboardingCompleted()
                                }
                                currentPage++
                            } else {
                                onboardingCompleted()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(OnboardingScreenTestTags.SECONDARY_BUTTON),
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .testTag(OnboardingScreenTestTags.PAGE_TITLE)
                )

                Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                Text(
                    text = targetPage.description,
                    style = typographyV2.bodyLG,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    minLines = 4,
                    modifier = Modifier
                        .testTag(OnboardingScreenTestTags.PAGE_DESCRIPTION)
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
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(OnboardingScreenTestTags.PAGE_ANIMATION),
                composition = composition,
                progress = { progress },
            )
        }
    }
}
