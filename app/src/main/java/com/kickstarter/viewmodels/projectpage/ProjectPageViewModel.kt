package com.kickstarter.viewmodels.projectpage

import android.content.Intent
import android.net.Uri
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValuesV2
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.AI
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.CAMPAIGN
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ENVIRONMENT
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.FAQS
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.OVERVIEW
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.RISKS
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.ProjectViewUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.ProjectMetadata
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.backedReward
import com.kickstarter.libs.utils.extensions.isErrored
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.isUIEmptyValues
import com.kickstarter.libs.utils.extensions.metadataForProject
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.libs.utils.extensions.showLatePledgeFlow
import com.kickstarter.libs.utils.extensions.updateProjectWith
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.MediaElement
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.data.VideoModelElement
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

data class PagerTabConfig(val tab: ProjectPagerTabs, val isActive: Boolean)

interface ProjectPageViewModel {
    interface Inputs {
        fun configureWith(intent: Intent)

        /** Call when the cancel pledge option is clicked.  */
        fun cancelPledgeClicked()

        /** Call when the comments text view is clicked.  */
        fun commentsTextViewClicked()

        /** Call when the contact creator option is clicked.  */
        fun contactCreatorClicked()

        /** Call when the fix payment method is clicked.  */
        fun fixPaymentMethodButtonClicked()

        /** Call when the count of fragments on the back stack changes.  */
        fun fragmentStackCount(count: Int)

        /** Call when the heart button is clicked.  */
        fun heartButtonClicked()

        /** Call when the native_project_action_button is clicked.  */
        fun nativeProjectActionButtonClicked()

        /** Call when the view has been laid out. */
        fun onGlobalLayout()

        /** Call when the fullscreen video button is clicked.  */
        fun fullScreenVideoButtonClicked(videoInfo: kotlin.Pair<String, Long>)

        /** Call when the pledge's payment method has been successfully updated. */
        fun pledgePaymentSuccessfullyUpdated()

        /** Call when the pledge has been successfully canceled.  */
        fun pledgeSuccessfullyCancelled()

        /** Call when the pledge has been successfully created.  */
        fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>)

        /** Call when the pledge has been successfully updated. */
        fun pledgeSuccessfullyUpdated()

        /** Call when the user clicks the navigation icon of the pledge toolbar. */
        fun pledgeToolbarNavigationClicked()

        /** Call when the user has triggered a manual refresh of the project. */
        fun refreshProject()

        /** Call when the reload container is clicked.  */
        fun reloadProjectContainerClicked()

        /** Call when the share button is clicked.  */
        fun shareButtonClicked()

        /** Call when the update payment option is clicked.  */
        fun updatePaymentClicked()

        /** Call when the updates button is clicked.  */
        fun updatesTextViewClicked()

        /** Call when the view rewards option is clicked.  */
        fun viewRewardsClicked()

        /** Call when some tab on the Tablayout has been pressed, with the position  */
        fun tabSelected(position: Int)

        fun closeFullScreenVideo(seekPosition: Long)

        fun onVideoPlayButtonClicked()

        fun activityResult(result: ActivityResult)
    }

    interface Outputs {
        /** Emits a boolean that determines if the backing details should be visible. */
        fun backingDetailsIsVisible(): Observable<Boolean>

        /** Emits a string or string resource ID of the backing details subtitle. */
        fun backingDetailsSubtitle(): Observable<Either<String, Int>?>

        /** Emits the string resource ID of the backing details title. */
        fun backingDetailsTitle(): Observable<Int>

        /** Emits when rewards sheet should expand and if it should animate. */
        fun expandPledgeSheet(): Observable<Pair<Boolean, Boolean>>

        /** Emits when we should go back in the navigation hierarchy. */
        fun goBack(): Observable<Unit>

        /** Emits a drawable id that corresponds to whether the project is saved. */
        fun heartDrawableId(): Observable<Int>

        /** Emits a menu for managing your pledge or null if there's no menu. */
        fun managePledgeMenu(): Observable<Int>

        /** Emits the color resource ID for the pledge action button. */
        fun pledgeActionButtonColor(): Observable<Int>

        /** Emits a boolean that determines if the pledge action button container should be visible. */
        fun pledgeActionButtonContainerIsGone(): Observable<Boolean>

        /** Emits the string resource ID for the pledge action button. */
        fun pledgeActionButtonText(): Observable<Int>

        /** Emits the proper string resource ID for the pledge toolbar navigation icon. */
        fun pledgeToolbarNavigationIcon(): Observable<Int>

        /** Emits the proper string resource ID for the pledge toolbar title. */
        fun pledgeToolbarTitle(): Observable<Int>

        /** Emits the url of a prelaunch activated project to open in the browser. */
        fun prelaunchUrl(): Observable<String>

        /** Emits [ProjectData]. If the view model is created with a full project
         * model, this observable will emit that project immediately, and then again when it has updated from the api. */
        fun projectData(): Observable<ProjectData>

        /** Emits a boolean that determines if the reload project container should be visible. */
        fun reloadProjectContainerIsGone(): Observable<Boolean>

        /** Emits a boolean that determines if the progress bar in the retry container should be visible. */
        fun reloadProgressBarIsGone(): Observable<Boolean>

        /** Emits when we should reveal the [com.kickstarter.ui.fragments.RewardsFragment] with an animation. */
        fun revealRewardsFragment(): Observable<Unit>

        /** Emits a boolean that determines if the scrim for secondary pledging actions should be visible. */
        fun scrimIsVisible(): Observable<Boolean>

        /** Emits when we should set the Y position of the rewards container. */
        fun setInitialRewardsContainerY(): Observable<Unit>

        /** Emits when we should show the [com.kickstarter.ui.fragments.CancelPledgeFragment]. */
        fun showCancelPledgeFragment(): Observable<Project>

        /** Emits when the backing has successfully been canceled. */
        fun showCancelPledgeSuccess(): Observable<Unit>

        /** Emits when we should show the not cancelable dialog. */
        fun showPledgeNotCancelableDialog(): Observable<Unit>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Unit>

        /** Emits when we should show the share sheet with the name of the project and share URL.  */
        fun showShareSheet(): Observable<Pair<String, String>>

        /** Emits when we should show the [com.kickstarter.ui.fragments.PledgeFragment]. */
        fun showUpdatePledge(): Observable<Pair<PledgeData, PledgeReason>>

        /** Emits when the backing has successfully been updated. */
        fun showUpdatePledgeSuccess(): Observable<Unit>

        /** Emits when we should start [com.kickstarter.ui.activities.RootCommentsActivity]. */
        fun startRootCommentsActivity(): Observable<ProjectData>

        fun startRootCommentsForCommentsThreadActivity(): Observable<Pair<String, ProjectData>>

        /** Emits when we should start [com.kickstarter.ui.activities.LoginToutActivity].  */
        fun startLoginToutActivity(): Observable<Unit>

        /** Emits when we should show the [com.kickstarter.ui.activities.MessagesActivity]. */
        fun startMessagesActivity(): Observable<Project>

        /** Emits when we should start [com.kickstarter.ui.activities.UpdateActivity].  */
        fun startProjectUpdateActivity(): Observable<Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>>

        /** Emits when we should start [com.kickstarter.ui.activities.UpdateActivity].  */
        fun startProjectUpdateToRepliesDeepLinkActivity(): Observable<Pair<Pair<String, String>, Pair<Project, ProjectData>>>

        /** Emits when we the pledge was successful and should start the [com.kickstarter.ui.activities.ThanksActivity]. */
        fun startThanksActivity(): Observable<Pair<CheckoutData, PledgeData>>

        /** Emits when we should update the [com.kickstarter.ui.fragments.BackingFragment] and [com.kickstarter.ui.fragments.RewardsFragment].  */
        fun updateFragments(): Observable<ProjectData>

        fun projectMedia(): Observable<MediaElement>

        /** Emits when the play button should be gone.  */
        fun playButtonIsVisible(): Observable<Boolean>

        /** Emits when the backing view group should be gone. */
        fun backingViewGroupIsVisible(): Observable<Boolean>

        /** Will emmit the need to show/hide the Campaign Tab and the Environmental Tab. */
        fun updateTabs(): Observable<List<PagerTabConfig>>

        fun hideVideoPlayer(): Observable<Boolean>

        fun onOpenVideoInFullScreen(): Observable<kotlin.Pair<String, Long>>

        fun updateVideoCloseSeekPosition(): Observable<Long>

        fun showLatePledgeFlow(): Observable<Boolean>

        fun showPledgeRedemptionScreen(): Observable<Pair<Project, User>>
    }

    class ProjectPageViewModel(val environment: Environment) :
        ViewModel(),
        Inputs,
        Outputs {

        private val cookieManager = requireNotNull(environment.cookieManager())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val ksCurrency = requireNotNull(environment.ksCurrency())
        private val ffClient = requireNotNull(environment.featureFlagClient())
        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val currentConfig = requireNotNull(environment.currentConfigV2())
        private val featureFlagClient = requireNotNull(environment.featureFlagClient())
        private val analyticEvents = requireNotNull(environment.analytics())
        private val attributionEvents = requireNotNull(environment.attributionEvents())

        private val intent = PublishSubject.create<Intent>()
        private val activityResult = BehaviorSubject.create<ActivityResult>()
        private val closeFullScreenVideo = BehaviorSubject.create<Long>()
        private val cancelPledgeClicked = PublishSubject.create<Unit>()
        private val commentsTextViewClicked = PublishSubject.create<Unit>()
        private val contactCreatorClicked = PublishSubject.create<Unit>()
        private val fixPaymentMethodButtonClicked = PublishSubject.create<Unit>()
        private val fragmentStackCount = PublishSubject.create<Int>()
        private val heartButtonClicked = PublishSubject.create<Unit>()
        private val nativeProjectActionButtonClicked = PublishSubject.create<Unit>()
        private val onGlobalLayout = PublishSubject.create<Unit>()
        private val fullScreenVideoButtonClicked =
            PublishSubject.create<kotlin.Pair<String, Long>>()
        private val pledgePaymentSuccessfullyUpdated = PublishSubject.create<Unit>()
        private val pledgeSuccessfullyCancelled = PublishSubject.create<Unit>()
        private val pledgeSuccessfullyCreated =
            PublishSubject.create<Pair<CheckoutData, PledgeData>>()
        private val pledgeSuccessfullyUpdated = PublishSubject.create<Unit>()
        private val pledgeToolbarNavigationClicked = PublishSubject.create<Unit>()
        private val refreshProject = PublishSubject.create<Unit>()
        private val reloadProjectContainerClicked = PublishSubject.create<Unit>()
        private val shareButtonClicked = PublishSubject.create<Unit>()
        private val updatePaymentClicked = PublishSubject.create<Unit>()
        private val updatesTextViewClicked = PublishSubject.create<Unit>()
        private val viewRewardsClicked = PublishSubject.create<Unit>()
        private val onVideoPlayButtonClicked = PublishSubject.create<Unit>()

        private val backingDetailsIsVisible = BehaviorSubject.create<Boolean>()
        private val backingDetailsSubtitle = BehaviorSubject.create<Either<String, Int>?>()
        private val backingDetailsTitle = BehaviorSubject.create<Int>()
        private val expandPledgeSheet = BehaviorSubject.create<Pair<Boolean, Boolean>>()
        private val goBack = PublishSubject.create<Unit>()
        private val heartDrawableId = BehaviorSubject.create<Int>()
        private val managePledgeMenu = BehaviorSubject.create<Int>()
        private val pledgeActionButtonColor = BehaviorSubject.create<Int>()
        private val pledgeActionButtonContainerIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeActionButtonText = BehaviorSubject.create<Int>()
        private val pledgeToolbarNavigationIcon = BehaviorSubject.create<Int>()
        private val pledgeToolbarTitle = BehaviorSubject.create<Int>()
        private val prelaunchUrl = BehaviorSubject.create<String>()
        private val projectData = BehaviorSubject.create<ProjectData>()
        private val retryProgressBarIsGone = BehaviorSubject.create<Boolean>()
        private val reloadProjectContainerIsGone = BehaviorSubject.create<Boolean>()
        private val revealRewardsFragment = PublishSubject.create<Unit>()
        private val scrimIsVisible = BehaviorSubject.create<Boolean>()
        private val setInitialRewardPosition = BehaviorSubject.create<Unit>()
        private val showCancelPledgeFragment = PublishSubject.create<Project>()
        private val showCancelPledgeSuccess = PublishSubject.create<Unit>()
        private val showPledgeNotCancelableDialog = PublishSubject.create<Unit>()
        private val showShareSheet = PublishSubject.create<Pair<String, String>>()
        private val showSavedPrompt = PublishSubject.create<Unit>()
        private val updatePledgeData = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showUpdatePledge = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showUpdatePledgeSuccess = PublishSubject.create<Unit>()
        private val startRootCommentsActivity = PublishSubject.create<ProjectData>()
        private val startRootCommentsForCommentsThreadActivity =
            PublishSubject.create<Pair<String, ProjectData>>()
        private val startLoginToutActivity = PublishSubject.create<Unit>()
        private val startMessagesActivity = PublishSubject.create<Project>()
        private val startProjectUpdateActivity =
            PublishSubject.create<Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>>()
        private val startProjectUpdateToRepliesDeepLinkActivity =
            PublishSubject.create<Pair<Pair<String, String>, Pair<Project, ProjectData>>>()
        private val startThanksActivity = PublishSubject.create<Pair<CheckoutData, PledgeData>>()
        private val updateFragments = BehaviorSubject.create<ProjectData>()
        private val hideVideoPlayer = BehaviorSubject.create<Boolean>()
        private val tabSelected = PublishSubject.create<Int>()
        private val projectMedia = PublishSubject.create<MediaElement>()
        private val playButtonIsVisible = PublishSubject.create<Boolean>()
        private val backingViewGroupIsVisible = PublishSubject.create<Boolean>()
        private val updateTabs = PublishSubject.create<List<PagerTabConfig>>()
        private val onOpenVideoInFullScreen = PublishSubject.create<kotlin.Pair<String, Long>>()
        private val updateVideoCloseSeekPosition = BehaviorSubject.create<Long>()
        private val showLatePledgeFlow = BehaviorSubject.create<Boolean>()
        private val showPledgeRedemptionScreen = BehaviorSubject.create<Pair<Project, User>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val onThirdPartyEventSent = BehaviorSubject.create<Boolean?>()

        val disposables = CompositeDisposable()

        init {

            val progressBarIsGone = PublishSubject.create<Boolean>()

            val mappedProjectNotification = Observable.merge(
                intent,
                intent
                    .compose(
                        takeWhenV2(
                            this.reloadProjectContainerClicked
                        )
                    )
            ).switchMap {
                ProjectIntentMapper.project(it, this.apolloClient)
                    .doOnSubscribe {
                        progressBarIsGone.onNext(false)
                    }
                    .doAfterTerminate {
                        progressBarIsGone.onNext(true)
                    }
                    .withLatestFrom(
                        currentConfig.observable(),
                        currentUser.observable()
                    ) { project, config, user ->
                        return@withLatestFrom project.updateProjectWith(config, user.getValue())
                    }
                    .materialize()
            }
                .share()

            activityResult
                .filter { it.isOk }
                .filter { it.isRequestCode(ActivityRequestCodes.SHOW_REWARDS) }
                .subscribe { this.expandPledgeSheet.onNext(Pair(true, true)) }
                .addToDisposable(disposables)

            intent
                .take(1)
                .filter { it.getBooleanExtra(IntentKey.EXPAND_PLEDGE_SHEET, false) }
                .subscribe { this.expandPledgeSheet.onNext(Pair(true, true)) }
                .addToDisposable(disposables)

            val pledgeSheetExpanded = this.expandPledgeSheet
                .map { it.first }
                .startWith(false)

            progressBarIsGone
                .subscribe { this.retryProgressBarIsGone.onNext(it) }
                .addToDisposable(disposables)

            val mappedProjectValues = mappedProjectNotification
                .compose(valuesV2())

            val mappedProjectErrors = mappedProjectNotification
                .compose(errorsV2())

            mappedProjectValues
                .subscribe {
                    if (it.showLatePledgeFlow()) {
                        val isFFEnabled =
                            featureFlagClient.getBoolean(FlagKey.ANDROID_POST_CAMPAIGN_PLEDGES)
                        this.showLatePledgeFlow.onNext(it.showLatePledgeFlow() && isFFEnabled)
                    }

                    if (it.displayPrelaunch().isTrue()) {
                        this.prelaunchUrl.onNext(it.webProjectUrl())
                    }
                }
                .addToDisposable(disposables)

            val initialProject = mappedProjectValues
                .filter {
                    it.displayPrelaunch().isFalse()
                }

            // An observable of the ref tag stored in the cookie for the project. Emits an optional since this value can be null.
            val cookieRefTag = initialProject
                .take(1)
                .map { p ->
                    KsOptional.of(
                        RefTagUtils.storedCookieRefTagForProject(
                            p,
                            this.cookieManager,
                            this.sharedPreferences
                        )
                    )
                }

            val refTag = intent
                .flatMap { ProjectIntentMapper.refTag(it) }

            val fullDeeplink = intent
                .flatMap { Observable.just(KsOptional.of(it.data)) }

            val saveProjectFromDeepLinkActivity = intent
                .take(1)
                .delay(
                    3,
                    TimeUnit.SECONDS,
                    environment.schedulerV2()
                ) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_SAVE, false)
                }
                .flatMap { ProjectIntentMapper.deepLinkSaveFlag(it) }

            val saveProjectFromDeepUrl = intent
                .take(1)
                .delay(
                    3,
                    TimeUnit.SECONDS,
                    environment.schedulerV2()
                ) // add delay to wait until activity subscribed to viewmodel
                .filter { it.data.isNotNull() }
                .map { requireNotNull(it.data) }
                .filter {
                    ProjectIntentMapper.hasSaveQueryFromUri(it)
                }
                .map { UrlUtils.saveFlag(it.toString()) }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }

            val loggedInUserOnHeartClick = this.currentUser.observable()
                .filter { it.isPresent() }
                .map { it.getValue() }
                .compose<User>(takeWhenV2(this.heartButtonClicked))

            val loggedOutUserOnHeartClick = this.currentUser.observable()
                .compose(takeWhenV2(this.heartButtonClicked))
                .filter { !it.isPresent() }

            val projectOnUserChangeSave = initialProject
                .compose(takeWhenV2<Project, User>(loggedInUserOnHeartClick))
                .withLatestFrom(projectData) { initProject, latestProjectData ->
                    if (latestProjectData.project().isStarred() != initProject.isStarred()) {
                        latestProjectData.project()
                    } else {
                        initProject
                    }
                }
                .switchMap {
                    this.toggleProjectSave(it)
                }
                .share()

            val refreshProjectEvent = Observable.mergeArray(
                this.pledgeSuccessfullyCancelled,
                this.pledgeSuccessfullyCreated.compose(ignoreValuesV2()),
                this.pledgeSuccessfullyUpdated,
                this.pledgePaymentSuccessfullyUpdated,
                this.refreshProject
            )

            val refreshedProjectNotification = initialProject
                .compose(takeWhenV2(refreshProjectEvent))
                .switchMap {
                    it.slug()?.let { slug ->
                        this.apolloClient.getProject(slug)
                            .doOnSubscribe {
                                progressBarIsGone.onNext(false)
                            }
                            .doAfterTerminate {
                                progressBarIsGone.onNext(true)
                            }
                            .withLatestFrom(
                                currentConfig.observable(),
                                currentUser.observable()
                            ) { project, config, user ->
                                return@withLatestFrom project.updateProjectWith(
                                    config,
                                    user.getValue()
                                )
                            }
                            .materialize()
                    }
                }
                .share()

            loggedOutUserOnHeartClick
                .compose(ignoreValuesV2())
                .subscribe { this.startLoginToutActivity.onNext(it) }
                .addToDisposable(disposables)

            val savedProjectOnLoginSuccess = this.startLoginToutActivity
                .compose<Pair<Unit, KsOptional<User>>>(combineLatestPair(this.currentUser.observable()))
                .filter { su -> su.second.isPresent() }
                .withLatestFrom<Project, Project>(initialProject) { _, p -> p }
                .take(1)
                .switchMap {
                    this.saveProject(it)
                }
                .share()

            val projectOnDeepLinkChangeSave =
                Observable.merge(saveProjectFromDeepLinkActivity, saveProjectFromDeepUrl)
                    .compose(combineLatestPair(this.currentUser.observable()))
                    .filter { it.second.isPresent() }
                    .withLatestFrom(initialProject) { userAndFlag, p ->
                        Pair(userAndFlag, p)
                    }
                    .take(1)
                    .filter {
                        it.second.isStarred() != it.first.first
                    }.switchMap {
                        if (it.first.first) {
                            this.saveProject(it.second)
                        } else {
                            this.unSaveProject(it.second)
                        }
                    }.share()

            val currentProject = Observable.mergeArray(
                initialProject,
                refreshedProjectNotification.compose(valuesV2()),
                projectOnUserChangeSave,
                projectOnDeepLinkChangeSave,
                savedProjectOnLoginSuccess
            )

            var previousScreen = ""
            intent
                .subscribe { previousScreen = it.getStringExtra(IntentKey.PREVIOUS_SCREEN) ?: "" }
                .addToDisposable(disposables)

            SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
                .sendThirdPartyEvent(
                    project = currentProject,
                    apolloClient = apolloClient,
                    currentUser = currentUser,
                    eventName = ThirdPartyEventValues.EventName.SCREEN_VIEW,
                    firebaseScreen = ThirdPartyEventValues.ScreenName.PROJECT.value,
                    firebasePreviousScreen = previousScreen
                )
                .compose(neverErrorV2())
                .subscribe {
                    onThirdPartyEventSent.onNext(it.first)
                }
                .addToDisposable(disposables)

            val projectSavedStatus = Observable.merge(
                projectOnUserChangeSave,
                savedProjectOnLoginSuccess,
                projectOnDeepLinkChangeSave
            )

            projectSavedStatus
                .subscribe { this.analyticEvents.trackWatchProjectCTA(it, PROJECT) }
                .addToDisposable(disposables)

            projectSavedStatus
                .filter { p -> p.isStarred() && p.isLive && !p.isApproachingDeadline }
                .compose(ignoreValuesV2())
                .subscribe { this.showSavedPrompt.onNext(it) }
                .addToDisposable(disposables)

            val currentProjectData =
                Observable.combineLatest<KsOptional<RefTag?>, KsOptional<RefTag?>, KsOptional<Uri?>, Project, ProjectData>(
                    refTag,
                    cookieRefTag,
                    fullDeeplink,
                    currentProject
                ) { refTagFromIntent, refTagFromCookie, fullDeeplink, project ->
                    projectData(refTagFromIntent, refTagFromCookie, fullDeeplink, project)
                }

            currentProjectData
                .distinctUntilChanged()
                .subscribe {
                    this.projectData.onNext(it)
                    val showEnvironmentalTab = it.project().envCommitments()?.isNotEmpty() ?: false
                    val tabConfigEnv = PagerTabConfig(
                        ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT,
                        showEnvironmentalTab
                    )

                    val showAiTab = it.project().aiDisclosure()
                        ?.let { disclosure -> !disclosure.isUIEmptyValues() } ?: false
                    val tabConfigAi = PagerTabConfig(ProjectPagerTabs.USE_OF_AI, showAiTab)

                    this.updateTabs.onNext(listOf(tabConfigAi, tabConfigEnv))
                }
                .addToDisposable(disposables)

            currentProject
                .compose(takeWhenV2(this.shareButtonClicked))
                .map {
                    Pair(
                        it.name(),
                        UrlUtils.appendRefTag(it.webProjectUrl(), RefTag.projectShare().tag())
                    )
                }
                .subscribe { this.showShareSheet.onNext(it) }
                .addToDisposable(disposables)

            val latestProjectAndProjectData =
                currentProject.compose<Pair<Project, ProjectData>>(combineLatestPair(projectData))

            intent
                .take(1)
                .delay(
                    3,
                    TimeUnit.SECONDS,
                    environment.schedulerV2()
                ) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_COMMENT, false) && it.getStringExtra(IntentKey.COMMENT)?.isEmpty() ?: true
                }
                .withLatestFrom(latestProjectAndProjectData) { _, project ->
                    project
                }
                .map { it.second }
                .subscribe {
                    this.startRootCommentsActivity.onNext(it)
                }.addToDisposable(disposables)

            intent
                .take(1)
                .delay(
                    3,
                    TimeUnit.SECONDS,
                    environment.schedulerV2()
                ) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_COMMENT, false) &&
                        it.getStringExtra(IntentKey.COMMENT)?.isNotEmpty() ?: false
                }
                .withLatestFrom(latestProjectAndProjectData) { intent, project ->
                    Pair(intent.getStringExtra(IntentKey.COMMENT) ?: "", project)
                }
                .map { Pair(it.first, it.second.second) }
                .subscribe {
                    this.startRootCommentsForCommentsThreadActivity.onNext(it)
                }
                .addToDisposable(disposables)

            intent
                .take(1)
                .delay(
                    3,
                    TimeUnit.SECONDS,
                    environment.schedulerV2()
                ) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE)
                        ?.isNotEmpty() ?: false &&
                        it.getStringExtra(IntentKey.COMMENT)?.isEmpty() ?: true
                }.map {
                    Pair(
                        it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE) ?: "",
                        it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE_COMMENT, false)
                    )
                }
                .withLatestFrom(latestProjectAndProjectData) { updateId, project ->
                    Pair(updateId, project)
                }.subscribe {
                    this.startProjectUpdateActivity.onNext(it)
                }
                .addToDisposable(disposables)

            intent
                .take(1)
                .delay(
                    3,
                    TimeUnit.SECONDS,
                    environment.schedulerV2()
                ) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE)
                        ?.isNotEmpty() ?: false &&
                        it.getStringExtra(IntentKey.COMMENT)?.isNotEmpty() ?: false
                }.map {
                    Pair(
                        it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE) ?: "",
                        it.getStringExtra(IntentKey.COMMENT) ?: ""
                    )
                }
                .withLatestFrom(latestProjectAndProjectData) { updateId, project ->
                    Pair(updateId, project)
                }
                .subscribe { this.startProjectUpdateToRepliesDeepLinkActivity.onNext(it) }
                .addToDisposable(disposables)

            fullScreenVideoButtonClicked
                .subscribe { this.onOpenVideoInFullScreen.onNext(it) }
                .addToDisposable(disposables)

            closeFullScreenVideo
                .subscribe {
                    updateVideoCloseSeekPosition.onNext(it)
                }
                .addToDisposable(disposables)

            this.onGlobalLayout
                .subscribe { this.setInitialRewardPosition.onNext(it) }
                .addToDisposable(disposables)

            this.nativeProjectActionButtonClicked
                .map { Pair(true, true) }
                .subscribe { this.expandPledgeSheet.onNext(it) }
                .addToDisposable(disposables)

            val fragmentStackCount = this.fragmentStackCount.startWith(0)

            fragmentStackCount
                .compose(takeWhenV2(this.pledgeToolbarNavigationClicked))
                .filter { it <= 0 }
                .map { Pair(false, true) }
                .subscribe { this.expandPledgeSheet.onNext(it) }
                .addToDisposable(disposables)

            fragmentStackCount
                .compose(takeWhenV2(this.pledgeToolbarNavigationClicked))
                .filter { it > 0 }
                .compose(ignoreValuesV2())
                .subscribe { this.goBack.onNext(it) }
                .addToDisposable(disposables)

            Observable.merge(this.pledgeSuccessfullyCancelled, this.pledgeSuccessfullyCreated)
                .map { Pair(false, false) }
                .subscribe { this.expandPledgeSheet.onNext(it) }
                .addToDisposable(disposables)

            val projectHasRewardsAndSheetCollapsed = currentProject
                .map { it.hasRewards() }
                .distinctUntilChanged()
                .compose<Pair<Boolean, Boolean>>(combineLatestPair(pledgeSheetExpanded))
                .filter { it.second.isFalse() }
                .map { it.first }

            val rewardsLoaded = projectHasRewardsAndSheetCollapsed
                .filter { it.isTrue() }
                .map { true }

            Observable.merge(rewardsLoaded, this.reloadProjectContainerClicked.map { true })
                .subscribe { this.reloadProjectContainerIsGone.onNext(it) }
                .addToDisposable(disposables)

            mappedProjectErrors
                .map { false }
                .subscribe { this.reloadProjectContainerIsGone.onNext(it) }
                .addToDisposable(disposables)

            projectHasRewardsAndSheetCollapsed
                .compose<Pair<Boolean, Boolean>>(combineLatestPair(this.retryProgressBarIsGone))
                .map { (it.first && it.second).negate() }
                .distinctUntilChanged()
                .subscribe { this.pledgeActionButtonContainerIsGone.onNext(it) }
                .addToDisposable(disposables)

            currentProjectData
                .filter { it.project().hasRewards() && !it.project().isBacking() }
                .distinctUntilChanged()
                .subscribe { this.updateFragments.onNext(it) }
                .addToDisposable(disposables)

            currentProject
                .compose<Pair<Project, Int>>(combineLatestPair(fragmentStackCount))
                .map { managePledgeMenu(it) }
                .distinctUntilChanged()
                .subscribe { this.managePledgeMenu.onNext(it) }
                .addToDisposable(disposables)

            currentProjectData
                .compose(takePairWhenV2(this.tabSelected))
                .distinctUntilChanged()
                .delay(150, TimeUnit.MILLISECONDS, environment.schedulerV2()) // add delay to wait
                // until fragment subscribed to viewmodel
                .subscribe {
                    this.projectData.onNext(it.first)
                }
                .addToDisposable(disposables)

            tabSelected
                .map { it != 0 }
                .subscribe { this.hideVideoPlayer.onNext(it) }
                .addToDisposable(disposables)

            val backedProject = currentProject
                .filter { it.isBacking() }

            val projectBacking = backedProject
                .filter { it.backing().isNotNull() }
                .map { requireNotNull(it) }

            val backing = projectBacking.map { requireNotNull(it.backing()) }

            val isAdmin = this.currentUser.observable()
                .filter { it.isPresent() }
                .map { requireNotNull(it.getValue()) }
                .filter { it.isAdmin() && ffClient.getBoolean(FlagKey.ANDROID_PLEDGE_REDEMPTION) }
                .map { it }

            Observable.combineLatest(projectBacking, isAdmin) { pBacking, adminUser ->
                Pair(pBacking, adminUser)
            }
                .subscribe {
                    // remove userId tracking when removing feature flag or giving access to all users
                    this.environment.firebaseAnalyticsClient()?.sendUserId(it.second)
                    this.showPledgeRedemptionScreen.onNext(it)
                }
                .addToDisposable(disposables)

            // - Update fragments with the backing data
            currentProjectData
                .filter { it.project().hasRewards() }
                .compose<Pair<ProjectData, Backing>>(combineLatestPair(backing))
                .map {
                    val updatedProject = if (it.first.project().isBacking()) {
                        it.first.project().toBuilder().backing(it.second).build()
                    } else it.first.project()

                    projectData(
                        KsOptional.of(it.first.refTagFromIntent()),
                        KsOptional.of(it.first.refTagFromCookie()),
                        KsOptional.of(it.first.fullDeeplink()),
                        updatedProject
                    )
                }
                .subscribe { this.updateFragments.onNext(it) }
                .addToDisposable(disposables)

            backedProject
                .compose<Project>(takeWhenV2(this.cancelPledgeClicked))
                .filter { (it.backing()?.cancelable() ?: false).isTrue() }
                .subscribe { this.showCancelPledgeFragment.onNext(it) }
                .addToDisposable(disposables)

            backedProject
                .compose(takeWhenV2(this.cancelPledgeClicked))
                .filter { it.backing()?.cancelable().isFalse() }
                .compose(ignoreValuesV2())
                .subscribe { this.showPledgeNotCancelableDialog.onNext(it) }
                .addToDisposable(disposables)

            currentProject
                .compose(takeWhenV2(this.contactCreatorClicked))
                .subscribe { this.startMessagesActivity.onNext(it) }
                .addToDisposable(disposables)

            val projectDataAndBackedReward = currentProjectData
                .compose<Pair<ProjectData, Backing>>(combineLatestPair(backing))
                .filter { it.first.project().backing().isNotNull() }
                .filter {
                    it.first.project().backing()?.backedReward(it.first.project()).isNotNull()
                }
                .map { pD ->
                    pD.first.project().backing()?.backedReward(pD.first.project())?.let {
                        Pair(pD.first.toBuilder().backing(pD.second).build(), it)
                    }
                }

            projectDataAndBackedReward
                .compose(takeWhenV2(this.fixPaymentMethodButtonClicked))
                .map {
                    Pair(
                        pledgeData(it.second, it.first, PledgeFlowContext.FIX_ERRORED_PLEDGE),
                        PledgeReason.FIX_PLEDGE
                    )
                }
                .subscribe { this.updatePledgeData.onNext(it) }
                .addToDisposable(disposables)

            projectDataAndBackedReward
                .compose(takeWhenV2<Pair<ProjectData, Reward>, Unit>(this.updatePaymentClicked))
                .map {
                    Pair(
                        pledgeData(it.second, it.first, PledgeFlowContext.MANAGE_REWARD),
                        PledgeReason.UPDATE_PAYMENT
                    )
                }
                .subscribe {
                    this.updatePledgeData.onNext(it)
                    this.analyticEvents.trackChangePaymentMethod(it.first)
                }.addToDisposable(disposables)

            this.viewRewardsClicked
                .subscribe { this.revealRewardsFragment.onNext(it) }
                .addToDisposable(disposables)

            currentProject
                .map { it.isBacking() && it.isLive || it.backing()?.isErrored() == true }
                .distinctUntilChanged()
                .subscribe { this.backingDetailsIsVisible.onNext(it) }
                .addToDisposable(disposables)

            currentProject
                .filter { it.isBacking() }
                .map {
                    if (it.backing()
                        ?.isErrored() == true
                    ) R.string.Payment_failure else R.string.Youre_a_backer
                }
                .distinctUntilChanged()
                .subscribe { this.backingDetailsTitle.onNext(it) }
                .addToDisposable(disposables)

            currentProject
                .filter { it.isBacking() }
                .filter {
                    backingDetailsSubtitle(it).isNotNull()
                }
                .map { requireNotNull(backingDetailsSubtitle(it)) }
                .distinctUntilChanged()
                .subscribe { this.backingDetailsSubtitle.onNext(it) }
                .addToDisposable(disposables)

            val currentProjectAndUser = currentProject
                .compose<Pair<Project, KsOptional<User>>>(combineLatestPair(this.currentUser.observable()))

            Observable.combineLatest(
                currentProjectData,
                this.currentUser.observable()
            ) { data, user ->
                ProjectViewUtils.pledgeActionButtonText(
                    data.project(),
                    user.getValue(),
                )
            }
                .distinctUntilChanged()
                .subscribe { this.pledgeActionButtonText.onNext(it) }
                .addToDisposable(disposables)

            currentProject
                .compose<Pair<Project, Int>>(combineLatestPair(fragmentStackCount))
                .map { if (it.second <= 0) R.drawable.ic_arrow_down else R.drawable.ic_arrow_back }
                .distinctUntilChanged()
                .subscribe { this.pledgeToolbarNavigationIcon.onNext(it) }
                .addToDisposable(disposables)

            currentProjectAndUser
                .map { ProjectViewUtils.pledgeToolbarTitle(it.first, it.second.getValue()) }
                .distinctUntilChanged()
                .subscribe { this.pledgeToolbarTitle.onNext(it) }
                .addToDisposable(disposables)

            currentProjectAndUser
                .map { ProjectViewUtils.pledgeActionButtonColor(it.first, it.second.getValue()) }
                .distinctUntilChanged()
                .subscribe { this.pledgeActionButtonColor.onNext(it) }
                .addToDisposable(disposables)

            this.pledgePaymentSuccessfullyUpdated
                .subscribe { this.showUpdatePledgeSuccess.onNext(it) }
                .addToDisposable(disposables)

            this.pledgeSuccessfullyCancelled
                .subscribe { this.showCancelPledgeSuccess.onNext(it) }
                .addToDisposable(disposables)

            this.pledgeSuccessfullyCreated
                .subscribe { this.startThanksActivity.onNext(it) }
                .addToDisposable(disposables)

            this.pledgeSuccessfullyUpdated
                .subscribe { this.showUpdatePledgeSuccess.onNext(it) }
                .addToDisposable(disposables)

            this.fragmentStackCount
                .compose<Pair<Int, Project>>(combineLatestPair(currentProject))
                .map { if (it.second.isBacking()) it.first > 4 else it.first > 3 }
                .distinctUntilChanged()
                .subscribe { this.scrimIsVisible.onNext(it) }
                .addToDisposable(disposables)

            currentProject
                .map { p -> if (p.isStarred()) R.drawable.icon__heart else R.drawable.icon__heart_outline }
                .distinctUntilChanged()
                .subscribe { this.heartDrawableId.onNext(it) }
                .addToDisposable(disposables)

            val projectPhoto = currentProject
                .filter { it.photo()?.full().isNotNull() }
                .map { requireNotNull(it.photo()?.full()) }

            val projectVideo = currentProject
                .map { it.video()?.hls() ?: it.video()?.high() ?: "" }
                .distinctUntilChanged()
                .take(1)

            projectPhoto
                .compose(combineLatestPair(projectVideo))
                .subscribe {
                    this.projectMedia.onNext(MediaElement(VideoModelElement(it.second), it.first))
                }
                .addToDisposable(disposables)

            currentProject
                .map {
                    it.hasVideo()
                }
                .subscribe { this.playButtonIsVisible.onNext(it) }
                .addToDisposable(disposables)

            // Tracking
            val currentFullProjectData = currentProjectData
                .filter { it.project().hasRewards() }

            val fullProjectDataAndCurrentUser = currentFullProjectData
                .compose<Pair<ProjectData, KsOptional<User>>>(combineLatestPair(this.currentUser.observable()))

            val fullProjectDataAndPledgeFlowContext = fullProjectDataAndCurrentUser
                .map { Pair(it.first, pledgeFlowContext(it.first.project(), it.second.getValue())) }

            fullProjectDataAndPledgeFlowContext
                .take(1)
                .subscribe { projectDataAndPledgeFlowContext ->
                    val data = projectDataAndPledgeFlowContext.first
                    val pledgeFlowContext = projectDataAndPledgeFlowContext.second
                    // If a cookie hasn't been set for this ref+project then do so.
                    if (data.refTagFromCookie() == null) {
                        data.refTagFromIntent()?.let {
                            RefTagUtils.storeCookie(
                                it,
                                data.project(),
                                this.cookieManager,
                                this.sharedPreferences
                            )
                        }
                    }

                    val dataWithStoredCookieRefTag = storeCurrentCookieRefTag(data)
                    // Send event to segment
                    this.analyticEvents.trackProjectScreenViewed(
                        dataWithStoredCookieRefTag,
                        OVERVIEW.contextName
                    )
                    // Send event to backend event attribution
                    this.attributionEvents.trackProjectPageViewed(dataWithStoredCookieRefTag)
                }.addToDisposable(disposables)

            fullProjectDataAndPledgeFlowContext
                .map { it.first }
                .take(1)
                .compose(takePairWhenV2(this.tabSelected))
                .distinctUntilChanged()
                .subscribe {
                    this.analyticEvents.trackProjectPageTabChanged(
                        it.first,
                        getSelectedTabContextName(it.second)
                    )
                }.addToDisposable(disposables)

            fullProjectDataAndPledgeFlowContext
                .compose<Pair<ProjectData, PledgeFlowContext?>>(takeWhenV2(this.nativeProjectActionButtonClicked))
                .filter { it.first.project().isLive && !it.first.project().isBacking() }
                .subscribe {
                    this.analyticEvents.trackPledgeInitiateCTA(it.first)
                }.addToDisposable(disposables)

            currentProject
                .map { it.metadataForProject() }
                .map { ProjectMetadata.BACKING == it }
                .distinctUntilChanged()
                .subscribe { backingViewGroupIsVisible.onNext(it) }
                .addToDisposable(disposables)

            this.updatePledgeData
                .subscribe {
                    this.showUpdatePledge.onNext(it)
                }.addToDisposable(disposables)

            onVideoPlayButtonClicked
                .distinctUntilChanged()
                .subscribe {
                    backingViewGroupIsVisible.onNext(false)
                }.addToDisposable(disposables)
        }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        private fun getSelectedTabContextName(selectedTabIndex: Int): String =
            when (selectedTabIndex) {
                ProjectPagerTabs.OVERVIEW.ordinal -> OVERVIEW.contextName
                ProjectPagerTabs.CAMPAIGN.ordinal -> CAMPAIGN.contextName
                ProjectPagerTabs.FAQS.ordinal -> FAQS.contextName
                ProjectPagerTabs.RISKS.ordinal -> RISKS.contextName
                ProjectPagerTabs.USE_OF_AI.ordinal -> AI.contextName
                ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT.ordinal -> ENVIRONMENT.contextName
                else -> OVERVIEW.contextName
            }

        private fun managePledgeMenu(projectAndFragmentStackCount: Pair<Project, Int>): Int {
            val project = projectAndFragmentStackCount.first
            val count = projectAndFragmentStackCount.second
            val isPledgeOverTimeEnabled =
                featureFlagClient.getBoolean(FlagKey.ANDROID_PLEDGE_OVER_TIME) && project.isPledgeOverTimeAllowed() == true && project.backing()?.incremental() == true
            return when {
                !project.isBacking() || count.isNonZero() -> 0
                project.isLive -> when {
                    isPledgeOverTimeEnabled -> R.menu.manage_pledge_plot_selected
                    project.backing()
                        ?.status() == Backing.STATUS_PREAUTH -> R.menu.manage_pledge_preauth

                    else -> R.menu.manage_pledge_live
                }

                else -> R.menu.manage_pledge_ended
            }
        }

        private fun pledgeData(
            reward: Reward,
            projectData: ProjectData,
            pledgeFlowContext: PledgeFlowContext
        ): PledgeData {
            return PledgeData.with(pledgeFlowContext, projectData, reward)
        }

        private fun pledgeFlowContext(project: Project, currentUser: User?): PledgeFlowContext? {
            return when {
                project.userIsCreator(currentUser) -> null
                project.isLive && !project.isBacking() -> PledgeFlowContext.NEW_PLEDGE
                !project.isLive && project.backing()
                    ?.isErrored() ?: false -> PledgeFlowContext.FIX_ERRORED_PLEDGE

                else -> null
            }
        }

        private fun projectData(
            refTagFromIntent: KsOptional<RefTag?>,
            refTagFromCookie: KsOptional<RefTag?>,
            fullDeeplink: KsOptional<Uri?>,
            project: Project
        ): ProjectData {
            return ProjectData
                .builder()
                .refTagFromIntent(refTagFromIntent.getValue())
                .refTagFromCookie(refTagFromCookie.getValue())
                .fullDeeplink(fullDeeplink.getValue())
                .project(project)
                .build()
        }

        private fun storeCurrentCookieRefTag(data: ProjectData): ProjectData {
            return data
                .toBuilder()
                .refTagFromCookie(
                    RefTagUtils.storedCookieRefTagForProject(
                        data.project(),
                        cookieManager,
                        sharedPreferences
                    )
                )
                .build()
        }

        override fun tabSelected(position: Int) {
            this.tabSelected.onNext(position)
        }

        override fun configureWith(intent: Intent) {
            this.intent.onNext(intent)
        }

        override fun cancelPledgeClicked() {
            this.cancelPledgeClicked.onNext(Unit)
        }

        override fun commentsTextViewClicked() {
            this.commentsTextViewClicked.onNext(Unit)
        }

        override fun contactCreatorClicked() {
            this.contactCreatorClicked.onNext(Unit)
        }

        override fun fixPaymentMethodButtonClicked() {
            this.fixPaymentMethodButtonClicked.onNext(Unit)
        }

        override fun fragmentStackCount(count: Int) {
            this.fragmentStackCount.onNext(count)
        }

        override fun heartButtonClicked() {
            this.heartButtonClicked.onNext(Unit)
        }

        override fun nativeProjectActionButtonClicked() {
            this.nativeProjectActionButtonClicked.onNext(Unit)
        }

        override fun onGlobalLayout() {
            this.onGlobalLayout.onNext(Unit)
        }

        override fun pledgePaymentSuccessfullyUpdated() {
            this.pledgePaymentSuccessfullyUpdated.onNext(Unit)
        }

        override fun pledgeSuccessfullyCancelled() {
            this.pledgeSuccessfullyCancelled.onNext(Unit)
        }

        override fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>) {
            this.pledgeSuccessfullyCreated.onNext(checkoutDataAndPledgeData)
        }

        override fun pledgeSuccessfullyUpdated() {
            this.pledgeSuccessfullyUpdated.onNext(Unit)
        }

        override fun pledgeToolbarNavigationClicked() {
            this.pledgeToolbarNavigationClicked.onNext(Unit)
        }

        override fun refreshProject() {
            this.refreshProject.onNext(Unit)
        }

        override fun reloadProjectContainerClicked() {
            this.reloadProjectContainerClicked.onNext(Unit)
        }

        override fun shareButtonClicked() {
            this.shareButtonClicked.onNext(Unit)
        }

        override fun updatePaymentClicked() {
            this.updatePaymentClicked.onNext(Unit)
        }

        override fun updatesTextViewClicked() {
            this.updatesTextViewClicked.onNext(Unit)
        }

        override fun viewRewardsClicked() {
            this.viewRewardsClicked.onNext(Unit)
        }

        override fun fullScreenVideoButtonClicked(videoInfo: kotlin.Pair<String, Long>) {
            fullScreenVideoButtonClicked.onNext(videoInfo)
        }

        override fun closeFullScreenVideo(position: Long) = closeFullScreenVideo.onNext(position)

        override fun onVideoPlayButtonClicked() = onVideoPlayButtonClicked.onNext(Unit)

        override fun updateVideoCloseSeekPosition(): Observable<Long> =
            updateVideoCloseSeekPosition

        override fun activityResult(result: ActivityResult) = this.activityResult.onNext(result)

        override fun backingDetailsSubtitle(): Observable<Either<String, Int>?> =
            this.backingDetailsSubtitle

        override fun backingDetailsTitle(): Observable<Int> = this.backingDetailsTitle

        override fun backingDetailsIsVisible(): Observable<Boolean> = this.backingDetailsIsVisible

        override fun expandPledgeSheet(): Observable<Pair<Boolean, Boolean>> =
            this.expandPledgeSheet

        override fun goBack(): Observable<Unit> = this.goBack

        override fun heartDrawableId(): Observable<Int> = this.heartDrawableId

        override fun managePledgeMenu(): Observable<Int> = this.managePledgeMenu

        override fun pledgeActionButtonColor(): Observable<Int> = this.pledgeActionButtonColor

        override fun pledgeActionButtonContainerIsGone(): Observable<Boolean> =
            this.pledgeActionButtonContainerIsGone

        override fun pledgeActionButtonText(): Observable<Int> = this.pledgeActionButtonText

        override fun pledgeToolbarNavigationIcon(): Observable<Int> =
            this.pledgeToolbarNavigationIcon

        override fun pledgeToolbarTitle(): Observable<Int> = this.pledgeToolbarTitle

        override fun prelaunchUrl(): Observable<String> = this.prelaunchUrl

        override fun projectData(): Observable<ProjectData> = this.projectData

        override fun reloadProjectContainerIsGone(): Observable<Boolean> =
            this.reloadProjectContainerIsGone

        override fun reloadProgressBarIsGone(): Observable<Boolean> = this.retryProgressBarIsGone

        override fun revealRewardsFragment(): Observable<Unit> = this.revealRewardsFragment

        override fun scrimIsVisible(): Observable<Boolean> = this.scrimIsVisible

        override fun setInitialRewardsContainerY(): Observable<Unit> = this.setInitialRewardPosition

        override fun showCancelPledgeFragment(): Observable<Project> = this.showCancelPledgeFragment

        override fun showCancelPledgeSuccess(): Observable<Unit> = this.showCancelPledgeSuccess

        override fun showPledgeNotCancelableDialog(): Observable<Unit> =
            this.showPledgeNotCancelableDialog

        override fun showSavedPrompt(): Observable<Unit> = this.showSavedPrompt

        override fun showShareSheet(): Observable<Pair<String, String>> = this.showShareSheet

        override fun showUpdatePledge(): Observable<Pair<PledgeData, PledgeReason>> =
            this.showUpdatePledge

        override fun showUpdatePledgeSuccess(): Observable<Unit> = this.showUpdatePledgeSuccess

        override fun startRootCommentsActivity(): Observable<ProjectData> =
            this.startRootCommentsActivity

        override fun startRootCommentsForCommentsThreadActivity(): Observable<Pair<String, ProjectData>> =
            this.startRootCommentsForCommentsThreadActivity

        override fun startLoginToutActivity(): Observable<Unit> = this.startLoginToutActivity

        override fun startMessagesActivity(): Observable<Project> = this.startMessagesActivity

        override fun startThanksActivity(): Observable<Pair<CheckoutData, PledgeData>> =
            this.startThanksActivity

        override fun startProjectUpdateActivity(): Observable<Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>> =
            this.startProjectUpdateActivity

        override fun startProjectUpdateToRepliesDeepLinkActivity(): Observable<Pair<Pair<String, String>, Pair<Project, ProjectData>>> =
            this.startProjectUpdateToRepliesDeepLinkActivity

        override fun onOpenVideoInFullScreen(): Observable<kotlin.Pair<String, Long>> =
            this.onOpenVideoInFullScreen

        override fun updateTabs(): Observable<List<PagerTabConfig>> = this.updateTabs

        override fun hideVideoPlayer(): Observable<Boolean> = this.hideVideoPlayer

        override fun updateFragments(): Observable<ProjectData> = this.updateFragments

        override fun projectMedia(): Observable<MediaElement> = this.projectMedia

        override fun playButtonIsVisible(): Observable<Boolean> = this.playButtonIsVisible

        override fun backingViewGroupIsVisible(): Observable<Boolean> =
            this.backingViewGroupIsVisible

        override fun showLatePledgeFlow(): Observable<Boolean> = this.showLatePledgeFlow

        override fun showPledgeRedemptionScreen(): Observable<Pair<Project, User>> =
            this.showPledgeRedemptionScreen

        private fun backingDetailsSubtitle(project: Project): Either<String, Int>? {
            return project.backing()?.let { backing ->
                return if (backing.status() == Backing.STATUS_ERRORED) {
                    Either.Right(R.string.We_cant_process_your_pledge)
                } else {
                    val reward = project.rewards()?.firstOrNull { it.id() == backing.rewardId() }
                    val title = reward?.let { "• ${it.title()}" } ?: ""

                    val backingAmount = backing.amount()

                    val formattedAmount =
                        this.ksCurrency.format(backingAmount, project, RoundingMode.HALF_UP)

                    Either.Left("$formattedAmount $title".trim())
                }
            }
        }

        private fun saveProject(project: Project): Observable<Project> {
            return this.apolloClient.watchProject(project)
                .compose(neverErrorV2())
        }

        private fun unSaveProject(project: Project): Observable<Project> {
            return this.apolloClient.unWatchProject(project).compose(neverErrorV2())
        }

        private fun toggleProjectSave(project: Project): Observable<Project> {
            return if (project.isStarred()) {
                unSaveProject(project)
            } else {
                saveProject(project)
            }
        }
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectPageViewModel(environment) as T
        }
    }
}
