package com.kickstarter.viewmodels.projectpage

import android.content.Intent
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ProjectPagerTabs
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.htmlparser.VideoViewElement
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.errors
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValues
import com.kickstarter.libs.rx.transformers.Transformers.neverError
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhen
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.rx.transformers.Transformers.values
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.CAMPAIGN
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.ENVIRONMENT
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.FAQS
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.OVERVIEW
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.RISKS
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectViewUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.ProjectMetadata
import com.kickstarter.libs.utils.extensions.backedReward
import com.kickstarter.libs.utils.extensions.isErrored
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.metadataForProject
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.libs.utils.extensions.updateProjectWith
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectPageActivity
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import com.kickstarter.viewmodels.usecases.ShowPledgeFragmentUseCase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

interface ProjectPageViewModel {
    interface Inputs {

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

        /** Call when the play video button is clicked.  */
        fun playVideoButtonClicked()

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

        /** Call when the update pledge option is clicked.  */
        fun updatePledgeClicked()

        /** Call when the updates button is clicked.  */
        fun updatesTextViewClicked()

        /** Call when the view rewards option is clicked.  */
        fun viewRewardsClicked()

        /** Call when some tab on the Tablayout has been pressed, with the position  */
        fun tabSelected(position: Int)

        fun closeFullScreenVideo(seekPosition: Long)
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
        fun goBack(): Observable<Void>

        /** Emits a drawable id that corresponds to whether the project is saved. */
        fun heartDrawableId(): Observable<Int>

        /** Emits a menu for managing your pledge or null if there's no menu. */
        fun managePledgeMenu(): Observable<Int?>

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
        fun revealRewardsFragment(): Observable<Void>

        /** Emits a boolean that determines if the scrim for secondary pledging actions should be visible. */
        fun scrimIsVisible(): Observable<Boolean>

        /** Emits when we should set the Y position of the rewards container. */
        fun setInitialRewardsContainerY(): Observable<Void>

        /** Emits when we should show the [com.kickstarter.ui.fragments.CancelPledgeFragment]. */
        fun showCancelPledgeFragment(): Observable<Project>

        /** Emits when the backing has successfully been canceled. */
        fun showCancelPledgeSuccess(): Observable<Void>

        /** Emits when we should show the not cancelable dialog. */
        fun showPledgeNotCancelableDialog(): Observable<Void>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Void>

        /** Emits when we should show the share sheet with the name of the project and share URL.  */
        fun showShareSheet(): Observable<Pair<String, String>>

        /** Emits when we should show the [com.kickstarter.ui.fragments.PledgeFragment]. */
        fun showUpdatePledge(): Observable<Triple<PledgeData, PledgeReason, Boolean>>

        /** Emits when the backing has successfully been updated. */
        fun showUpdatePledgeSuccess(): Observable<Void>

        /** Emits when we should start [com.kickstarter.ui.activities.RootCommentsActivity]. */
        fun startRootCommentsActivity(): Observable<ProjectData>

        fun startRootCommentsForCommentsThreadActivity(): Observable<Pair<String, ProjectData>>

        /** Emits when we should start [com.kickstarter.ui.activities.LoginToutActivity].  */
        fun startLoginToutActivity(): Observable<Void>

        /** Emits when we should show the [com.kickstarter.ui.activities.MessagesActivity]. */
        fun startMessagesActivity(): Observable<Project>

        /** Emits when we should start [com.kickstarter.ui.activities.UpdateActivity].  */
        fun startProjectUpdateActivity(): Observable< Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>>

        /** Emits when we should start [com.kickstarter.ui.activities.UpdateActivity].  */
        fun startProjectUpdateToRepliesDeepLinkActivity(): Observable< Pair<Pair<String, String>, Pair<Project, ProjectData>>>

        /** Emits when we the pledge was successful and should start the [com.kickstarter.ui.activities.ThanksActivity]. */
        fun startThanksActivity(): Observable<Pair<CheckoutData, PledgeData>>

        /** Emits when we should start the [com.kickstarter.ui.activities.VideoActivity].  */
        fun startVideoActivity(): Observable<Project>

        /** Emits when we should update the [com.kickstarter.ui.fragments.BackingFragment] and [com.kickstarter.ui.fragments.RewardsFragment].  */
        fun updateFragments(): Observable<ProjectData>

        fun projectPhoto(): Observable<VideoViewElement>

        /** Emits when the play button should be gone.  */
        fun playButtonIsVisible(): Observable<Boolean>

        /** Emits when the backing view group should be gone. */
        fun backingViewGroupIsVisible(): Observable<Boolean>

        /** Will emmit the need to show/hide the Campaign Tab and the Environmental Tab. */
        fun updateTabs(): Observable<Boolean>

        fun hideVideoPlayer(): Observable<Boolean>

        fun onOpenVideoInFullScreen(): Observable<kotlin.Pair<String, Long>>

        fun updateVideoCloseSeekPosition(): Observable<Long>
    }

    class ViewModel(@NonNull val environment: Environment) :
        ActivityViewModel<ProjectPageActivity>(environment),
        Inputs,
        Outputs {

        private val cookieManager = requireNotNull(environment.cookieManager())
        private val currentUser = requireNotNull(environment.currentUser())
        private val ksCurrency = requireNotNull(environment.ksCurrency())
        private val optimizely = requireNotNull(environment.optimizely())
        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val apolloClient = requireNotNull(environment.apolloClient())
        private val currentConfig = requireNotNull(environment.currentConfig())
        private val closeFullScreenVideo = BehaviorSubject.create<Long>()

        private val cancelPledgeClicked = PublishSubject.create<Void>()
        private val commentsTextViewClicked = PublishSubject.create<Void>()
        private val contactCreatorClicked = PublishSubject.create<Void>()
        private val fixPaymentMethodButtonClicked = PublishSubject.create<Void>()
        private val fragmentStackCount = PublishSubject.create<Int>()
        private val heartButtonClicked = PublishSubject.create<Void>()
        private val nativeProjectActionButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val playVideoButtonClicked = PublishSubject.create<Void>()
        private val fullScreenVideoButtonClicked = PublishSubject.create<kotlin.Pair<String, Long>>()
        private val pledgePaymentSuccessfullyUpdated = PublishSubject.create<Void>()
        private val pledgeSuccessfullyCancelled = PublishSubject.create<Void>()
        private val pledgeSuccessfullyCreated = PublishSubject.create<Pair<CheckoutData, PledgeData>>()
        private val pledgeSuccessfullyUpdated = PublishSubject.create<Void>()
        private val pledgeToolbarNavigationClicked = PublishSubject.create<Void>()
        private val refreshProject = PublishSubject.create<Void>()
        private val reloadProjectContainerClicked = PublishSubject.create<Void>()
        private val shareButtonClicked = PublishSubject.create<Void>()
        private val updatePaymentClicked = PublishSubject.create<Void>()
        private val updatePledgeClicked = PublishSubject.create<Void>()
        private val updatesTextViewClicked = PublishSubject.create<Void>()
        private val viewRewardsClicked = PublishSubject.create<Void>()

        private val backingDetailsIsVisible = BehaviorSubject.create<Boolean>()
        private val backingDetailsSubtitle = BehaviorSubject.create<Either<String, Int>?>()
        private val backingDetailsTitle = BehaviorSubject.create<Int>()
        private val expandPledgeSheet = BehaviorSubject.create<Pair<Boolean, Boolean>>()
        private val goBack = PublishSubject.create<Void>()
        private val heartDrawableId = BehaviorSubject.create<Int>()
        private val managePledgeMenu = BehaviorSubject.create<Int?>()
        private val pledgeActionButtonColor = BehaviorSubject.create<Int>()
        private val pledgeActionButtonContainerIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeActionButtonText = BehaviorSubject.create<Int>()
        private val pledgeToolbarNavigationIcon = BehaviorSubject.create<Int>()
        private val pledgeToolbarTitle = BehaviorSubject.create<Int>()
        private val prelaunchUrl = BehaviorSubject.create<String>()
        private val projectData = BehaviorSubject.create<ProjectData>()
        private val retryProgressBarIsGone = BehaviorSubject.create<Boolean>()
        private val reloadProjectContainerIsGone = BehaviorSubject.create<Boolean>()
        private val revealRewardsFragment = PublishSubject.create<Void>()
        private val scrimIsVisible = BehaviorSubject.create<Boolean>()
        private val setInitialRewardPosition = BehaviorSubject.create<Void>()
        private val showCancelPledgeFragment = PublishSubject.create<Project>()
        private val showCancelPledgeSuccess = PublishSubject.create<Void>()
        private val showPledgeNotCancelableDialog = PublishSubject.create<Void>()
        private val showShareSheet = PublishSubject.create<Pair<String, String>>()
        private val showSavedPrompt = PublishSubject.create<Void>()
        private val updatePledgeData = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showUpdatePledge = PublishSubject.create<Triple<PledgeData, PledgeReason, Boolean>>()
        private val showUpdatePledgeSuccess = PublishSubject.create<Void>()
        private val startRootCommentsActivity = PublishSubject.create<ProjectData>()
        private val startRootCommentsForCommentsThreadActivity = PublishSubject.create<Pair<String, ProjectData>>()
        private val startLoginToutActivity = PublishSubject.create<Void>()
        private val startMessagesActivity = PublishSubject.create<Project>()
        private val startProjectUpdateActivity = PublishSubject.create< Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>>()
        private val startProjectUpdateToRepliesDeepLinkActivity = PublishSubject.create< Pair<Pair<String, String>, Pair<Project, ProjectData>>>()
        private val startThanksActivity = PublishSubject.create<Pair<CheckoutData, PledgeData>>()
        private val startVideoActivity = PublishSubject.create<Project>()
        private val updateFragments = BehaviorSubject.create<ProjectData>()
        private val hideVideoPlayer = BehaviorSubject.create<Boolean>()
        private val tabSelected = PublishSubject.create<Int>()
        private val projectPhoto = PublishSubject.create<VideoViewElement>()
        private val playButtonIsVisible = PublishSubject.create<Boolean>()
        private val backingViewGroupIsVisible = PublishSubject.create<Boolean>()
        private val updateTabs = PublishSubject.create< Boolean>()
        private val onOpenVideoInFullScreen = PublishSubject.create<kotlin.Pair<String, Long>>()
        private val updateVideoCloseSeekPosition = BehaviorSubject.create< Long>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val progressBarIsGone = PublishSubject.create<Boolean>()

            val mappedProjectNotification = Observable.merge(
                intent(),
                intent()
                    .compose(takeWhen<Intent, Void>(this.reloadProjectContainerClicked))
            )
                .switchMap {
                    ProjectIntentMapper.project(it, this.apolloClient)
                        .doOnSubscribe {
                            progressBarIsGone.onNext(false)
                        }
                        .doAfterTerminate {
                            progressBarIsGone.onNext(true)
                        }
                        .withLatestFrom(currentConfig.observable(), currentUser.observable()) { project, config, user ->
                            return@withLatestFrom project.updateProjectWith(config, user)
                        }
                        .materialize()
                }
                .share()

            activityResult()
                .filter { it.isOk }
                .filter { it.isRequestCode(ActivityRequestCodes.SHOW_REWARDS) }
                .compose(bindToLifecycle())
                .subscribe { this.expandPledgeSheet.onNext(Pair(true, true)) }

            intent()
                .take(1)
                .filter { it.getBooleanExtra(IntentKey.EXPAND_PLEDGE_SHEET, false) }
                .compose(bindToLifecycle())
                .subscribe { this.expandPledgeSheet.onNext(Pair(true, true)) }

            val pledgeSheetExpanded = this.expandPledgeSheet
                .map { it.first }
                .startWith(false)

            progressBarIsGone
                .compose(bindToLifecycle())
                .subscribe(this.retryProgressBarIsGone)

            val mappedProjectValues = mappedProjectNotification
                .compose(values())

            val mappedProjectErrors = mappedProjectNotification
                .compose(errors())

            mappedProjectValues
                .filter { it.displayPrelaunch().isTrue() }
                .map { it.webProjectUrl() }
                .compose(bindToLifecycle())
                .subscribe(this.prelaunchUrl)

            val initialProject = mappedProjectValues
                .filter {
                    it.displayPrelaunch().isFalse()
                }

            // An observable of the ref tag stored in the cookie for the project. Can emit `null`.
            val cookieRefTag = initialProject
                .take(1)
                .map { p -> RefTagUtils.storedCookieRefTagForProject(p, this.cookieManager, this.sharedPreferences) }

            val refTag = intent()
                .flatMap { ProjectIntentMapper.refTag(it) }

            val saveProjectFromDeepLinkActivity = intent()
                .take(1)
                .delay(3, TimeUnit.SECONDS, environment.scheduler()) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_SAVE, false)
                }
                .flatMap { ProjectIntentMapper.deepLinkSaveFlag(it) }

            val saveProjectFromDeepUrl = intent()
                .take(1)
                .delay(3, TimeUnit.SECONDS, environment.scheduler()) // add delay to wait until activity subscribed to viewmodel
                .filter { ObjectUtils.isNotNull(it.data) }
                .map { requireNotNull(it.data) }
                .filter {
                    ProjectIntentMapper.hasSaveQueryFromUri(it)
                }
                .map { UrlUtils.saveFlag(it.toString()) }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            val loggedInUserOnHeartClick = this.currentUser.observable()
                .compose<User>(takeWhen(this.heartButtonClicked))
                .filter { u -> u != null }

            val loggedOutUserOnHeartClick = this.currentUser.observable()
                .compose<User>(takeWhen(this.heartButtonClicked))
                .filter { u -> u == null }

            val projectOnUserChangeSave = initialProject
                .compose(takeWhen<Project, User>(loggedInUserOnHeartClick))
                .withLatestFrom(projectData) { initProject, latestProjectData ->
                    if (latestProjectData.project().isStarred() != initProject.isStarred())
                        latestProjectData.project()
                    else
                        initProject
                }
                .switchMap {
                    this.toggleProjectSave(it)
                }
                .share()

            val refreshProjectEvent = Observable.merge(
                this.pledgeSuccessfullyCancelled,
                this.pledgeSuccessfullyCreated.compose(ignoreValues()),
                this.pledgeSuccessfullyUpdated,
                this.pledgePaymentSuccessfullyUpdated,
                this.refreshProject
            )

            val refreshedProjectNotification = initialProject
                .compose(takeWhen<Project, Void>(refreshProjectEvent))
                .switchMap {
                    it.slug()?.let { slug ->
                        this.apolloClient.getProject(slug)
                            .doOnSubscribe {
                                progressBarIsGone.onNext(false)
                            }
                            .doAfterTerminate {
                                progressBarIsGone.onNext(true)
                            }
                            .withLatestFrom(currentConfig.observable(), currentUser.observable()) { project, config, user ->
                                return@withLatestFrom project.updateProjectWith(config, user)
                            }
                            .materialize()
                    }
                }
                .share()

            loggedOutUserOnHeartClick
                .compose(ignoreValues())
                .subscribe(this.startLoginToutActivity)

            val savedProjectOnLoginSuccess = this.startLoginToutActivity
                .compose<Pair<Void, User>>(combineLatestPair(this.currentUser.observable()))
                .filter { su -> su.second != null }
                .withLatestFrom<Project, Project>(initialProject) { _, p -> p }
                .take(1)
                .switchMap {
                    this.saveProject(it)
                }
                .share()

            val projectOnDeepLinkChangeSave = Observable.merge(saveProjectFromDeepLinkActivity, saveProjectFromDeepUrl)
                .compose(combineLatestPair(this.currentUser.observable()))
                .filter { it.second != null }
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

            val currentProject = Observable.merge(
                initialProject,
                refreshedProjectNotification.compose(values()),
                projectOnUserChangeSave,
                savedProjectOnLoginSuccess,
                projectOnDeepLinkChangeSave
            )

            val projectSavedStatus = Observable.merge(projectOnUserChangeSave, savedProjectOnLoginSuccess, projectOnDeepLinkChangeSave)

            projectSavedStatus
                .compose(bindToLifecycle())
                .subscribe { this.analyticEvents.trackWatchProjectCTA(it, PROJECT) }

            projectSavedStatus
                .filter { p -> p.isStarred() && p.isLive && !p.isApproachingDeadline }
                .compose(ignoreValues())
                .compose(bindToLifecycle())
                .subscribe(this.showSavedPrompt)

            val currentProjectData = Observable.combineLatest<RefTag, RefTag, Project, ProjectData>(refTag, cookieRefTag, currentProject) { refTagFromIntent, refTagFromCookie, project ->
                projectData(refTagFromIntent, refTagFromCookie, project)
            }

            currentProjectData
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.projectData.onNext(it)
                    val showEnvironmentalTab = it.project().envCommitments()?.isNotEmpty() ?: false
                    this.updateTabs.onNext(showEnvironmentalTab)
                }

            currentProject
                .compose<Project>(takeWhen(this.shareButtonClicked))
                .map { Pair(it.name(), UrlUtils.appendRefTag(it.webProjectUrl(), RefTag.projectShare().tag())) }
                .compose(bindToLifecycle())
                .subscribe(this.showShareSheet)

            val latestProjectAndProjectData = currentProject.compose<Pair<Project, ProjectData>>(combineLatestPair(projectData))

            intent()
                .take(1)
                .delay(3, TimeUnit.SECONDS, environment.scheduler()) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_COMMENT, false) &&
                        it.getStringExtra(IntentKey.COMMENT)?.isEmpty() ?: true
                }
                .withLatestFrom(latestProjectAndProjectData) { _, project ->
                    project
                }
                .map { it.second }
                .compose(bindToLifecycle())
                .subscribe {
                    this.startRootCommentsActivity.onNext(it)
                }

            intent()
                .take(1)
                .delay(3, TimeUnit.SECONDS, environment.scheduler()) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_COMMENT, false) &&
                        it.getStringExtra(IntentKey.COMMENT)?.isNotEmpty() ?: false
                }
                .withLatestFrom(latestProjectAndProjectData) { intent, project ->
                    Pair(intent.getStringExtra(IntentKey.COMMENT) ?: "", project)
                }
                .map { Pair(it.first, it.second.second) }
                .compose(bindToLifecycle())
                .subscribe {
                    this.startRootCommentsForCommentsThreadActivity.onNext(it)
                }

            intent()
                .take(1)
                .delay(3, TimeUnit.SECONDS, environment.scheduler()) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE)?.isNotEmpty() ?: false &&
                        it.getStringExtra(IntentKey.COMMENT)?.isEmpty() ?: true
                }.map {
                    Pair(
                        requireNotNull(it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE)),
                        it.getBooleanExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE_COMMENT, false)
                    )
                }
                .withLatestFrom(latestProjectAndProjectData) { updateId, project ->
                    Pair(updateId, project)
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.startProjectUpdateActivity.onNext(it)
                }

            intent()
                .take(1)
                .delay(3, TimeUnit.SECONDS, environment.scheduler()) // add delay to wait until activity subscribed to viewmodel
                .filter {
                    it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE)?.isNotEmpty() ?: false &&
                        it.getStringExtra(IntentKey.COMMENT)?.isNotEmpty() ?: false
                }.map {
                    Pair(
                        requireNotNull(it.getStringExtra(IntentKey.DEEP_LINK_SCREEN_PROJECT_UPDATE)),
                        it.getStringExtra(IntentKey.COMMENT) ?: ""
                    )
                }
                .withLatestFrom(latestProjectAndProjectData) { updateId, project ->
                    Pair(updateId, project)
                }
                .compose(bindToLifecycle())
                .subscribe { this.startProjectUpdateToRepliesDeepLinkActivity.onNext(it) }

            currentProject
                .compose(takeWhen(this.playVideoButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.startVideoActivity)

            fullScreenVideoButtonClicked
                .compose(bindToLifecycle())
                .subscribe(this.onOpenVideoInFullScreen)

            closeFullScreenVideo
                .compose(bindToLifecycle())
                .subscribe {
                    updateVideoCloseSeekPosition.onNext(it)
                }

            this.onGlobalLayout
                .compose(bindToLifecycle())
                .subscribe(this.setInitialRewardPosition)

            this.nativeProjectActionButtonClicked
                .map { Pair(true, true) }
                .compose(bindToLifecycle())
                .subscribe(this.expandPledgeSheet)

            val fragmentStackCount = this.fragmentStackCount.startWith(0)

            fragmentStackCount
                .compose(takeWhen(this.pledgeToolbarNavigationClicked))
                .filter { it <= 0 }
                .map { Pair(false, true) }
                .compose(bindToLifecycle())
                .subscribe(this.expandPledgeSheet)

            fragmentStackCount
                .compose<Int>(takeWhen(this.pledgeToolbarNavigationClicked))
                .filter { it > 0 }
                .compose(ignoreValues())
                .compose(bindToLifecycle())
                .subscribe(this.goBack)

            Observable.merge(this.pledgeSuccessfullyCancelled, this.pledgeSuccessfullyCreated)
                .map { Pair(false, false) }
                .compose(bindToLifecycle())
                .subscribe(this.expandPledgeSheet)

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
                .compose(bindToLifecycle())
                .subscribe(this.reloadProjectContainerIsGone)

            mappedProjectErrors
                .map { false }
                .compose(bindToLifecycle())
                .subscribe(this.reloadProjectContainerIsGone)

            projectHasRewardsAndSheetCollapsed
                .compose<Pair<Boolean, Boolean>>(combineLatestPair(this.retryProgressBarIsGone))
                .map { (it.first && it.second).negate() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.pledgeActionButtonContainerIsGone)

            val projectData = Observable.combineLatest<RefTag, RefTag, Project, ProjectData>(refTag, cookieRefTag, currentProject) { refTagFromIntent, refTagFromCookie, project -> projectData(refTagFromIntent, refTagFromCookie, project) }

            projectData
                .filter { it.project().hasRewards() && !it.project().isBacking() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.updateFragments)

            currentProject
                .compose<Pair<Project, Int>>(combineLatestPair(fragmentStackCount))
                .map { managePledgeMenu(it) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.managePledgeMenu)

            projectData
                .compose(takePairWhen(this.tabSelected))
                .distinctUntilChanged()
                .delay(150, TimeUnit.MILLISECONDS, environment.scheduler()) // add delay to wait
                // until fragment subscribed to viewmodel
                .subscribe {
                    this.projectData.onNext(it.first)
                }

            tabSelected
                .map { it != 0 }
                .compose(bindToLifecycle())
                .subscribe { this.hideVideoPlayer.onNext(it) }

            val backedProject = currentProject
                .filter { it.isBacking() }

            val backing = backedProject
                .map { it.backing() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            // - Update fragments with the backing data
            projectData
                .filter { it.project().hasRewards() }
                .compose<Pair<ProjectData, Backing>>(combineLatestPair(backing))
                .map {
                    val updatedProject = if (it.first.project().isBacking())
                        it.first.project().toBuilder().backing(it.second).build()
                    else it.first.project()

                    projectData(it.first.refTagFromIntent(), it.first.refTagFromCookie(), updatedProject)
                }
                .compose(bindToLifecycle())
                .subscribe(this.updateFragments)

            backedProject
                .compose<Project>(takeWhen(this.cancelPledgeClicked))
                .filter { (it.backing()?.cancelable() ?: false).isTrue() }
                .compose(bindToLifecycle())
                .subscribe(this.showCancelPledgeFragment)

            backedProject
                .compose<Project>(takeWhen(this.cancelPledgeClicked))
                .filter { it.backing()?.cancelable().isFalse() }
                .compose(ignoreValues())
                .compose(bindToLifecycle())
                .subscribe(this.showPledgeNotCancelableDialog)

            currentProject
                .compose<Project>(takeWhen(this.contactCreatorClicked))
                .compose(bindToLifecycle())
                .subscribe(this.startMessagesActivity)

            val projectDataAndBackedReward = projectData
                .compose<Pair<ProjectData, Backing>>(combineLatestPair(backing))
                .map { pD ->
                    pD.first.project().backing()?.backedReward(pD.first.project())?.let {
                        Pair(pD.first.toBuilder().backing(pD.second).build(), it)
                    }
                }

            projectDataAndBackedReward
                .compose(takeWhen<Pair<ProjectData, Reward>, Void>(this.fixPaymentMethodButtonClicked))
                .map { Pair(pledgeData(it.second, it.first, PledgeFlowContext.FIX_ERRORED_PLEDGE), PledgeReason.FIX_PLEDGE) }
                .compose(bindToLifecycle())
                .subscribe(this.updatePledgeData)

            projectDataAndBackedReward
                .compose(takeWhen<Pair<ProjectData, Reward>, Void>(this.updatePaymentClicked))
                .map { Pair(pledgeData(it.second, it.first, PledgeFlowContext.MANAGE_REWARD), PledgeReason.UPDATE_PAYMENT) }
                .compose(bindToLifecycle())
                .subscribe {
                    this.updatePledgeData.onNext(it)
                    this.analyticEvents.trackChangePaymentMethod(it.first)
                }

            projectDataAndBackedReward
                .compose(takeWhen<Pair<ProjectData, Reward>, Void>(this.updatePledgeClicked))
                .map { Pair(pledgeData(it.second, it.first, PledgeFlowContext.MANAGE_REWARD), PledgeReason.UPDATE_PLEDGE) }
                .compose(bindToLifecycle())
                .subscribe(this.updatePledgeData)

            this.viewRewardsClicked
                .compose(bindToLifecycle())
                .subscribe(this.revealRewardsFragment)

            currentProject
                .map { it.isBacking() && it.isLive || it.backing()?.isErrored() == true }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.backingDetailsIsVisible)

            currentProject
                .filter { it.isBacking() }
                .map { if (it.backing()?.isErrored() == true) R.string.Payment_failure else R.string.Youre_a_backer }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.backingDetailsTitle)

            currentProject
                .filter { it.isBacking() }
                .map { backingDetailsSubtitle(it) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.backingDetailsSubtitle)

            val currentProjectAndUser = currentProject
                .compose<Pair<Project, User>>(combineLatestPair(this.currentUser.observable()))

            Observable.combineLatest(currentProjectData, this.currentUser.observable()) { data, user ->
                val experimentData = ExperimentData(user, data.refTagFromIntent(), data.refTagFromCookie())
                ProjectViewUtils.pledgeActionButtonText(
                    data.project(),
                    user,
                    this.optimizely?.variant(OptimizelyExperiment.Key.PLEDGE_CTA_COPY, experimentData)
                )
            }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.pledgeActionButtonText)

            currentProject
                .compose<Pair<Project, Int>>(combineLatestPair(fragmentStackCount))
                .map { if (it.second <= 0) R.drawable.ic_arrow_down else R.drawable.ic_arrow_back }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.pledgeToolbarNavigationIcon)

            currentProjectAndUser
                .map { ProjectViewUtils.pledgeToolbarTitle(it.first, it.second) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.pledgeToolbarTitle)

            currentProjectAndUser
                .map { ProjectViewUtils.pledgeActionButtonColor(it.first, it.second) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.pledgeActionButtonColor)

            this.pledgePaymentSuccessfullyUpdated
                .compose(bindToLifecycle())
                .subscribe(this.showUpdatePledgeSuccess)

            this.pledgeSuccessfullyCancelled
                .compose(bindToLifecycle())
                .subscribe(this.showCancelPledgeSuccess)

            this.pledgeSuccessfullyCreated
                .compose(bindToLifecycle())
                .subscribe(this.startThanksActivity)

            this.pledgeSuccessfullyUpdated
                .compose(bindToLifecycle())
                .subscribe(this.showUpdatePledgeSuccess)

            this.fragmentStackCount
                .compose<Pair<Int, Project>>(combineLatestPair(currentProject))
                .map { if (it.second.isBacking()) it.first > 4 else it.first > 3 }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.scrimIsVisible)

            currentProject
                .map { p -> if (p.isStarred()) R.drawable.icon__heart else R.drawable.icon__heart_outline }
                .subscribe(this.heartDrawableId)

            val projectPhoto = currentProject
                .map { it.photo()?.full() }
                .filter { ObjectUtils.isNotNull(it) }

            val projectVideo = currentProject.map { it.video() }
                .map { it?.hls() ?: it?.high() }
                .distinctUntilChanged()
                .take(1)
            //  .compose(bindToLifecycle())
            // .subscribe { preparePlayerWithUrl.onNext(it) }
            projectPhoto
                .compose(combineLatestPair(projectVideo))
                // .compose(combineLatestPair(projectVidoe)
                .compose(bindToLifecycle())
                .subscribe {
                    this.projectPhoto.onNext(VideoViewElement(it.second ?: "", it.first, 0))
                }

            currentProject
                .map { it.hasVideo() }
                .subscribe(this.playButtonIsVisible)

            // Tracking
            val currentFullProjectData = currentProjectData
                .filter { it.project().hasRewards() }

            val fullProjectDataAndCurrentUser = currentFullProjectData
                .compose<Pair<ProjectData, User?>>(combineLatestPair(this.currentUser.observable()))

            val fullProjectDataAndPledgeFlowContext = fullProjectDataAndCurrentUser
                .map { Pair(it.first, pledgeFlowContext(it.first.project(), it.second)) }

            fullProjectDataAndPledgeFlowContext
                .take(1)
                .compose(bindToLifecycle())
                .subscribe { projectDataAndPledgeFlowContext ->
                    val data = projectDataAndPledgeFlowContext.first
                    val pledgeFlowContext = projectDataAndPledgeFlowContext.second
                    // If a cookie hasn't been set for this ref+project then do so.
                    if (data.refTagFromCookie() == null) {
                        data.refTagFromIntent()?.let { RefTagUtils.storeCookie(it, data.project(), this.cookieManager, this.sharedPreferences) }
                    }

                    val dataWithStoredCookieRefTag = storeCurrentCookieRefTag(data)
                    this.analyticEvents.trackProjectScreenViewed(dataWithStoredCookieRefTag, OVERVIEW.contextName)
                }

            fullProjectDataAndPledgeFlowContext
                .map { it.first }
                .take(1)
                .compose(takePairWhen(this.tabSelected))
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.analyticEvents.trackProjectPageTabChanged(it.first, getSelectedTabContextName(it.second))
                }

            fullProjectDataAndPledgeFlowContext
                .compose<Pair<ProjectData, PledgeFlowContext?>>(takeWhen(this.nativeProjectActionButtonClicked))
                .filter { it.first.project().isLive && !it.first.project().isBacking() }
                .compose(bindToLifecycle())
                .subscribe {
                    this.analyticEvents.trackPledgeInitiateCTA(it.first)
                }

            currentProject
                .map { it.metadataForProject() }
                .map { ProjectMetadata.BACKING == it }
                .compose(bindToLifecycle())
                .subscribe(backingViewGroupIsVisible)

            ShowPledgeFragmentUseCase(this.updatePledgeData)
                .data(currentUser.observable(), this.optimizely)
                .compose(bindToLifecycle())
                .subscribe {
                    this.showUpdatePledge.onNext(it)
                }
        }

        private fun getSelectedTabContextName(selectedTabIndex: Int): String = when (selectedTabIndex) {
            ProjectPagerTabs.OVERVIEW.ordinal -> OVERVIEW.contextName
            ProjectPagerTabs.CAMPAIGN.ordinal -> CAMPAIGN.contextName
            ProjectPagerTabs.FAQS.ordinal -> FAQS.contextName
            ProjectPagerTabs.RISKS.ordinal -> RISKS.contextName
            ProjectPagerTabs.ENVIRONMENTAL_COMMITMENT.ordinal -> ENVIRONMENT.contextName
            else -> OVERVIEW.contextName
        }

        private fun managePledgeMenu(projectAndFragmentStackCount: Pair<Project, Int>): Int? {
            val project = projectAndFragmentStackCount.first
            val count = projectAndFragmentStackCount.second
            return when {
                !project.isBacking() || count.isNonZero() -> null
                project.isLive -> when {
                    project.backing()?.status() == Backing.STATUS_PREAUTH -> R.menu.manage_pledge_preauth
                    else -> R.menu.manage_pledge_live
                }
                else -> R.menu.manage_pledge_ended
            }
        }

        private fun pledgeData(reward: Reward, projectData: ProjectData, pledgeFlowContext: PledgeFlowContext): PledgeData {
            return PledgeData.with(pledgeFlowContext, projectData, reward)
        }

        private fun pledgeFlowContext(project: Project, currentUser: User?): PledgeFlowContext? {
            return when {
                project.userIsCreator(currentUser) -> null
                project.isLive && !project.isBacking() -> PledgeFlowContext.NEW_PLEDGE
                !project.isLive && project.backing()?.isErrored() ?: false -> PledgeFlowContext.FIX_ERRORED_PLEDGE
                else -> null
            }
        }

        private fun projectData(refTagFromIntent: RefTag?, refTagFromCookie: RefTag?, project: Project): ProjectData {
            return ProjectData
                .builder()
                .refTagFromIntent(refTagFromIntent)
                .refTagFromCookie(refTagFromCookie)
                .project(project)
                .build()
        }

        private fun storeCurrentCookieRefTag(data: ProjectData): ProjectData {
            return data
                .toBuilder()
                .refTagFromCookie(RefTagUtils.storedCookieRefTagForProject(data.project(), cookieManager, sharedPreferences))
                .build()
        }

        override fun tabSelected(position: Int) {
            this.tabSelected.onNext(position)
        }

        override fun cancelPledgeClicked() {
            this.cancelPledgeClicked.onNext(null)
        }

        override fun commentsTextViewClicked() {
            this.commentsTextViewClicked.onNext(null)
        }

        override fun contactCreatorClicked() {
            this.contactCreatorClicked.onNext(null)
        }

        override fun fixPaymentMethodButtonClicked() {
            this.fixPaymentMethodButtonClicked.onNext(null)
        }

        override fun fragmentStackCount(count: Int) {
            this.fragmentStackCount.onNext(count)
        }

        override fun heartButtonClicked() {
            this.heartButtonClicked.onNext(null)
        }

        override fun nativeProjectActionButtonClicked() {
            this.nativeProjectActionButtonClicked.onNext(null)
        }

        override fun onGlobalLayout() {
            this.onGlobalLayout.onNext(null)
        }

        override fun playVideoButtonClicked() {
            this.playVideoButtonClicked.onNext(null)
        }

        override fun pledgePaymentSuccessfullyUpdated() {
            this.pledgePaymentSuccessfullyUpdated.onNext(null)
        }

        override fun pledgeSuccessfullyCancelled() {
            this.pledgeSuccessfullyCancelled.onNext(null)
        }

        override fun pledgeSuccessfullyCreated(checkoutDataAndPledgeData: Pair<CheckoutData, PledgeData>) {
            this.pledgeSuccessfullyCreated.onNext(checkoutDataAndPledgeData)
        }

        override fun pledgeSuccessfullyUpdated() {
            this.pledgeSuccessfullyUpdated.onNext(null)
        }

        override fun pledgeToolbarNavigationClicked() {
            this.pledgeToolbarNavigationClicked.onNext(null)
        }

        override fun refreshProject() {
            this.refreshProject.onNext(null)
        }

        override fun reloadProjectContainerClicked() {
            this.reloadProjectContainerClicked.onNext(null)
        }

        override fun shareButtonClicked() {
            this.shareButtonClicked.onNext(null)
        }

        override fun updatePaymentClicked() {
            this.updatePaymentClicked.onNext(null)
        }

        override fun updatePledgeClicked() {
            this.updatePledgeClicked.onNext(null)
        }

        override fun updatesTextViewClicked() {
            this.updatesTextViewClicked.onNext(null)
        }

        override fun viewRewardsClicked() {
            this.viewRewardsClicked.onNext(null)
        }

        override fun fullScreenVideoButtonClicked(videoInfo: kotlin.Pair<String, Long>) {
            fullScreenVideoButtonClicked.onNext(videoInfo)
        }

        override fun closeFullScreenVideo(position: Long) = closeFullScreenVideo.onNext(position)

        override fun updateVideoCloseSeekPosition(): Observable< Long> =
            updateVideoCloseSeekPosition

        @NonNull
        override fun backingDetailsSubtitle(): Observable<Either<String, Int>?> = this.backingDetailsSubtitle

        @NonNull
        override fun backingDetailsTitle(): Observable<Int> = this.backingDetailsTitle

        @NonNull
        override fun backingDetailsIsVisible(): Observable<Boolean> = this.backingDetailsIsVisible

        @NonNull
        override fun expandPledgeSheet(): Observable<Pair<Boolean, Boolean>> = this.expandPledgeSheet

        @NonNull
        override fun goBack(): Observable<Void> = this.goBack

        @NonNull
        override fun heartDrawableId(): Observable<Int> = this.heartDrawableId

        @NonNull
        override fun managePledgeMenu(): Observable<Int?> = this.managePledgeMenu

        @NonNull
        override fun pledgeActionButtonColor(): Observable<Int> = this.pledgeActionButtonColor

        @NonNull
        override fun pledgeActionButtonContainerIsGone(): Observable<Boolean> = this.pledgeActionButtonContainerIsGone

        @NonNull
        override fun pledgeActionButtonText(): Observable<Int> = this.pledgeActionButtonText

        @NonNull
        override fun pledgeToolbarNavigationIcon(): Observable<Int> = this.pledgeToolbarNavigationIcon

        @NonNull
        override fun pledgeToolbarTitle(): Observable<Int> = this.pledgeToolbarTitle

        @NonNull
        override fun prelaunchUrl(): Observable<String> = this.prelaunchUrl

        @NonNull
        override fun projectData(): Observable<ProjectData> = this.projectData

        @NonNull
        override fun reloadProjectContainerIsGone(): Observable<Boolean> = this.reloadProjectContainerIsGone

        @NonNull
        override fun reloadProgressBarIsGone(): Observable<Boolean> = this.retryProgressBarIsGone

        @NonNull
        override fun revealRewardsFragment(): Observable<Void> = this.revealRewardsFragment

        @NonNull
        override fun scrimIsVisible(): Observable<Boolean> = this.scrimIsVisible

        @NonNull
        override fun setInitialRewardsContainerY(): Observable<Void> = this.setInitialRewardPosition

        @NonNull
        override fun showCancelPledgeFragment(): Observable<Project> = this.showCancelPledgeFragment

        @NonNull
        override fun showCancelPledgeSuccess(): Observable<Void> = this.showCancelPledgeSuccess

        @NonNull
        override fun showPledgeNotCancelableDialog(): Observable<Void> = this.showPledgeNotCancelableDialog

        @NonNull
        override fun showSavedPrompt(): Observable<Void> = this.showSavedPrompt

        @NonNull
        override fun showShareSheet(): Observable<Pair<String, String>> = this.showShareSheet

        @NonNull
        override fun showUpdatePledge(): Observable<Triple<PledgeData, PledgeReason, Boolean>> = this.showUpdatePledge

        @NonNull
        override fun showUpdatePledgeSuccess(): Observable<Void> = this.showUpdatePledgeSuccess

        @NonNull
        override fun startRootCommentsActivity(): Observable<ProjectData> = this.startRootCommentsActivity

        @NonNull
        override fun startRootCommentsForCommentsThreadActivity(): Observable<Pair<String, ProjectData>> =
            this.startRootCommentsForCommentsThreadActivity

        @NonNull
        override fun startLoginToutActivity(): Observable<Void> = this.startLoginToutActivity

        @NonNull
        override fun startMessagesActivity(): Observable<Project> = this.startMessagesActivity

        @NonNull
        override fun startThanksActivity(): Observable<Pair<CheckoutData, PledgeData>> = this.startThanksActivity

        @NonNull
        override fun startProjectUpdateActivity(): Observable<Pair<Pair<String, Boolean>, Pair<Project, ProjectData>>> = this.startProjectUpdateActivity

        @NonNull
        override fun startProjectUpdateToRepliesDeepLinkActivity(): Observable<Pair<Pair<String, String>, Pair<Project, ProjectData>>> =
            this.startProjectUpdateToRepliesDeepLinkActivity

        @NonNull
        override fun onOpenVideoInFullScreen(): Observable<kotlin.Pair<String, Long>> = this.onOpenVideoInFullScreen

        @NonNull
        override fun startVideoActivity(): Observable<Project> = this.startVideoActivity

        @NonNull
        override fun updateTabs(): Observable<Boolean> = this.updateTabs

        @NonNull
        override fun hideVideoPlayer(): Observable<Boolean> = this.hideVideoPlayer

        @NonNull
        override fun updateFragments(): Observable<ProjectData> = this.updateFragments

        @NonNull
        override fun projectPhoto(): Observable<VideoViewElement> = this.projectPhoto

        @NonNull
        override fun playButtonIsVisible(): Observable<Boolean> = this.playButtonIsVisible

        @NonNull
        override fun backingViewGroupIsVisible(): Observable<Boolean> = this.backingViewGroupIsVisible

        private fun backingDetailsSubtitle(project: Project): Either<String, Int>? {
            return project.backing()?.let { backing ->
                return if (backing.status() == Backing.STATUS_ERRORED) {
                    Either.Right(R.string.We_cant_process_your_pledge)
                } else {
                    val reward = project.rewards()?.firstOrNull { it.id() == backing.rewardId() }
                    val title = reward?.let { "• ${it.title()}" } ?: ""

                    val backingAmount = backing.amount()

                    val formattedAmount = this.ksCurrency.format(backingAmount, project, RoundingMode.HALF_UP)

                    Either.Left("$formattedAmount $title".trim())
                }
            }
        }

        private fun saveProject(project: Project): Observable<Project> {
            return this.apolloClient.watchProject(project)
                .compose(neverError())
        }

        private fun unSaveProject(project: Project): Observable<Project> {
            return this.apolloClient.unWatchProject(project).compose(neverError())
        }

        private fun toggleProjectSave(project: Project): Observable<Project> {
            return if (project.isStarred())
                unSaveProject(project)
            else
                saveProject(project)
        }
    }
}
