package com.kickstarter.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.*
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.*
import com.kickstarter.libs.utils.EventContextValues.ContextSectionName.OVERVIEW
import com.kickstarter.libs.utils.extensions.backedReward
import com.kickstarter.libs.utils.extensions.isErrored
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ProjectActivity
import com.kickstarter.ui.adapters.ProjectAdapter
import com.kickstarter.ui.data.*
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import com.kickstarter.ui.viewholders.ProjectViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode
import java.net.CookieManager

interface ProjectViewModel {
    interface Inputs {
        /** Call when the blurb view is clicked.  */
        fun blurbTextViewClicked()

        /** Call when the blurb variant view is clicked.  */
        fun blurbVariantClicked()

        /** Call when the cancel pledge option is clicked.  */
        fun cancelPledgeClicked()

        /** Call when the comments text view is clicked.  */
        fun commentsTextViewClicked()

        /** Call when the contact creator option is clicked.  */
        fun contactCreatorClicked()

        /** Call when the creator dashboard button is clicked.  */
        fun creatorDashboardButtonClicked()

        /** Call when the creator info variant is clicked.  */
        fun creatorInfoVariantClicked()

        /** Call when the creator name is clicked.  */
        fun creatorNameTextViewClicked()

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
        fun showUpdatePledge(): Observable<Pair<PledgeData, PledgeReason>>

        /** Emits when the backing has successfully been updated. */
        fun showUpdatePledgeSuccess(): Observable<Void>

        /** Emits when we should start the campaign [com.kickstarter.ui.activities.CampaignDetailsActivity].  */
        fun startCampaignWebViewActivity(): Observable<ProjectData>

        /** Emits when we should start [com.kickstarter.ui.activities.CommentsActivity].  */
        fun startCommentsActivity(): Observable<Pair<Project, ProjectData>>

        /** Emits when we should start the creator bio [com.kickstarter.ui.activities.CreatorBioActivity].  */
        fun startCreatorBioWebViewActivity(): Observable<Project>

        /** Emits when we should start the creator dashboard [com.kickstarter.ui.activities.CreatorDashboardActivity].  */
        fun startCreatorDashboardActivity(): Observable<Project>

        /** Emits when we should start [com.kickstarter.ui.activities.LoginToutActivity].  */
        fun startLoginToutActivity(): Observable<Void>

        /** Emits when we should show the [com.kickstarter.ui.activities.MessagesActivity]. */
        fun startMessagesActivity(): Observable<Project>

        /** Emits when we should start [com.kickstarter.ui.activities.ProjectUpdatesActivity].  */
        fun startProjectUpdatesActivity(): Observable<Pair<Project, ProjectData>>

        /** Emits when we the pledge was successful and should start the [com.kickstarter.ui.activities.ThanksActivity]. */
        fun startThanksActivity(): Observable<Pair<CheckoutData, PledgeData>>

        /** Emits when we should start the [com.kickstarter.ui.activities.VideoActivity].  */
        fun startVideoActivity(): Observable<Project>

        /** Emits when we should update the [com.kickstarter.ui.fragments.BackingFragment] and [com.kickstarter.ui.fragments.RewardsFragment].  */
        fun updateFragments(): Observable<ProjectData>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ProjectActivity>(environment), ProjectAdapter.Delegate, Inputs, Outputs {
        private val client: ApiClientType = environment.apiClient()
        private val cookieManager: CookieManager = environment.cookieManager()
        private val currentUser: CurrentUserType = environment.currentUser()
        private val ksCurrency: KSCurrency = environment.ksCurrency()
        private val optimizely: ExperimentsClientType = environment.optimizely()
        private val sharedPreferences: SharedPreferences = environment.sharedPreferences()
        private val apolloClient = environment.apolloClient()

        private val blurbTextViewClicked = PublishSubject.create<Void>()
        private val blurbVariantClicked = PublishSubject.create<Void>()
        private val cancelPledgeClicked = PublishSubject.create<Void>()
        private val commentsTextViewClicked = PublishSubject.create<Void>()
        private val contactCreatorClicked = PublishSubject.create<Void>()
        private val creatorDashboardButtonClicked = PublishSubject.create<Void>()
        private val creatorInfoVariantClicked = PublishSubject.create<Void>()
        private val creatorNameTextViewClicked = PublishSubject.create<Void>()
        private val fixPaymentMethodButtonClicked = PublishSubject.create<Void>()
        private val fragmentStackCount = PublishSubject.create<Int>()
        private val heartButtonClicked = PublishSubject.create<Void>()
        private val nativeProjectActionButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val playVideoButtonClicked = PublishSubject.create<Void>()
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
        private val showUpdatePledge = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showUpdatePledgeSuccess = PublishSubject.create<Void>()
        private val startCampaignWebViewActivity = PublishSubject.create<ProjectData>()
        private val startCommentsActivity = PublishSubject.create<Pair<Project, ProjectData>>()
        private val startCreatorBioWebViewActivity = PublishSubject.create<Project>()
        private val startCreatorDashboardActivity = PublishSubject.create<Project>()
        private val startLoginToutActivity = PublishSubject.create<Void>()
        private val startMessagesActivity = PublishSubject.create<Project>()
        private val startProjectUpdatesActivity = PublishSubject.create<Pair<Project, ProjectData>>()
        private val startThanksActivity = PublishSubject.create<Pair<CheckoutData, PledgeData>>()
        private val startVideoActivity = PublishSubject.create<Project>()
        private val updateFragments = BehaviorSubject.create<ProjectData>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val progressBarIsGone = PublishSubject.create<Boolean>()

            val mappedProjectNotification = Observable.merge(intent(), intent()
                    .compose(takeWhen<Intent, Void>(this.reloadProjectContainerClicked)))
                    .flatMap {
                        ProjectIntentMapper.project(it, this.client)
                                .doOnSubscribe {
                                    progressBarIsGone.onNext(false)
                                }
                                .doAfterTerminate {
                                    progressBarIsGone.onNext(true)
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
                    .filter { BooleanUtils.isTrue(it.prelaunchActivated()) }
                    .map { it.webProjectUrl() }
                    .compose(bindToLifecycle())
                    .subscribe(this.prelaunchUrl)

            val initialProject = mappedProjectValues
                    .filter { BooleanUtils.isFalse(it.prelaunchActivated()) }

            // An observable of the ref tag stored in the cookie for the project. Can emit `null`.
            val cookieRefTag = initialProject
                    .take(1)
                    .map { p -> RefTagUtils.storedCookieRefTagForProject(p, this.cookieManager, this.sharedPreferences) }

            val refTag = intent()
                    .flatMap { ProjectIntentMapper.refTag(it) }

            val pushNotificationEnvelope = intent()
                    .flatMap { ProjectIntentMapper.pushNotificationEnvelope(it) }

            val loggedInUserOnHeartClick = this.currentUser.observable()
                    .compose<User>(takeWhen(this.heartButtonClicked))
                    .filter { u -> u != null }

            val loggedOutUserOnHeartClick = this.currentUser.observable()
                    .compose<User>(takeWhen(this.heartButtonClicked))
                    .filter { u -> u == null }

            val projectOnUserChangeSave = initialProject
                    .compose(takeWhen<Project, User>(loggedInUserOnHeartClick))
                    .switchMap { this.toggleProjectSave(it) }
                    .share()

            val refreshProjectEvent = Observable.merge(this.pledgeSuccessfullyCancelled,
                    this.pledgeSuccessfullyCreated.compose(ignoreValues()),
                    this.pledgeSuccessfullyUpdated,
                    this.pledgePaymentSuccessfullyUpdated,
                    this.refreshProject)

            val refreshedProjectNotification = initialProject
                    .compose(takeWhen<Project, Void>(refreshProjectEvent))
                    .switchMap {
                        it.slug()?.let { slug ->
                            this.client.fetchProject(slug)
                                    .doOnSubscribe {
                                        progressBarIsGone.onNext(false)
                                    }
                                    .doAfterTerminate {
                                        progressBarIsGone.onNext(true)
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
                    .switchMap { this.saveProject(it) }
                    .share()

            val currentProject = Observable.merge(
                    initialProject,
                    refreshedProjectNotification.compose(values()),
                    projectOnUserChangeSave,
                    savedProjectOnLoginSuccess
            )

            val projectSavedStatus = projectOnUserChangeSave.mergeWith(savedProjectOnLoginSuccess)

            projectSavedStatus
                    .compose(bindToLifecycle())
                    .subscribe{ this.lake.trackWatchProjectCTA(it) }

            projectSavedStatus
                    .filter { p -> p.isStarred && p.isLive && !p.isApproachingDeadline }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showSavedPrompt)

            val currentProjectData = Observable.combineLatest<RefTag, RefTag, Project, ProjectData>(refTag, cookieRefTag, currentProject)
            { refTagFromIntent, refTagFromCookie, project -> projectData(refTagFromIntent, refTagFromCookie, project) }

            currentProjectData
                    .compose(bindToLifecycle())
                    .subscribe(this.projectData)

            currentProject
                    .compose<Project>(takeWhen(this.shareButtonClicked))
                    .map { Pair(it.name(), UrlUtils.appendRefTag(it.webProjectUrl(), RefTag.projectShare().tag())) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showShareSheet)

            val blurbClicked = Observable.merge(this.blurbTextViewClicked, this.blurbVariantClicked)

            currentProjectData
                    .compose<ProjectData>(takeWhen(blurbClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startCampaignWebViewActivity)

            val creatorInfoClicked = Observable.merge(this.creatorNameTextViewClicked, this.creatorInfoVariantClicked)

            currentProject
                    .compose<Project>(takeWhen(creatorInfoClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startCreatorBioWebViewActivity)

            currentProject
                    .compose<Project>(takeWhen(this.commentsTextViewClicked))
                    .compose<Pair<Project, ProjectData>>(combineLatestPair(projectData))
                    .compose(bindToLifecycle())
                    .subscribe(this.startCommentsActivity)

            currentProject
                    .compose<Project>(takeWhen(this.creatorDashboardButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startCreatorDashboardActivity)

            currentProject
                    .compose<Project>(takeWhen(this.updatesTextViewClicked))
                    .compose<Pair<Project, ProjectData>>(combineLatestPair(projectData))
                    .compose(bindToLifecycle())
                    .subscribe(this.startProjectUpdatesActivity)

            currentProject
                    .compose<Project>(takeWhen(this.playVideoButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startVideoActivity)

            this.onGlobalLayout
                    .compose(bindToLifecycle())
                    .subscribe(this.setInitialRewardPosition)

            this.nativeProjectActionButtonClicked
                    .map { Pair(true, true) }
                    .compose(bindToLifecycle())
                    .subscribe(this.expandPledgeSheet)

            val fragmentStackCount = this.fragmentStackCount.startWith(0)

            fragmentStackCount
                    .compose<Int>(takeWhen(this.pledgeToolbarNavigationClicked))
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
                    .filter { BooleanUtils.isFalse(it.second) }
                    .map { it.first }

            val rewardsLoaded = projectHasRewardsAndSheetCollapsed
                    .filter { BooleanUtils.isTrue(it) }
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
                    .map { BooleanUtils.negate(it.first && it.second) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeActionButtonContainerIsGone)

            val projectData = Observable.combineLatest<RefTag, RefTag, Project, ProjectData>(refTag, cookieRefTag, currentProject)
            { refTagFromIntent, refTagFromCookie, project -> projectData(refTagFromIntent, refTagFromCookie, project) }

            projectData
                    .filter { it.project().hasRewards() && !it.project().isBacking }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.updateFragments)

            currentProject
                    .compose<Pair<Project, Int>>(combineLatestPair(fragmentStackCount))
                    .map { managePledgeMenu(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.managePledgeMenu)

            val backedProject = currentProject
                    .filter { it.isBacking }

            val backing = backedProject
                    .switchMap {
                        this.apolloClient.getProjectBacking(it.slug()?: "")
                                .doOnSubscribe {
                                    progressBarIsGone.onNext(false)
                                }
                                .doAfterTerminate {
                                    progressBarIsGone.onNext(true)
                                }
                                .materialize()
                    }
                    .compose(neverError())
                    .compose(values())
                    .filter { ObjectUtils.isNotNull(it) }
                    .share()

            // - Update fragments with the backing data
            projectData
                    .filter { it.project().hasRewards() }
                    .compose<Pair<ProjectData, Backing>>(combineLatestPair(backing))
                    .map {
                        val updatedProject = if (it.first.project().isBacking)
                            it.first.project().toBuilder().backing(it.second).build()
                        else it.first.project()

                        projectData(it.first.refTagFromIntent(), it.first.refTagFromCookie(), updatedProject)
                    }
                    .compose(bindToLifecycle())
                    .subscribe(this.updateFragments)

            backedProject
                    .compose<Project>(takeWhen(this.cancelPledgeClicked))
                    .filter { BooleanUtils.isTrue(it.backing()?.cancelable()?: false) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showCancelPledgeFragment)

            backedProject
                    .compose<Project>(takeWhen(this.cancelPledgeClicked))
                    .filter { BooleanUtils.isFalse(it.backing()?.cancelable()?: true) }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeNotCancelableDialog)

            currentProject
                    .compose<Project>(takeWhen(this.contactCreatorClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startMessagesActivity)

            val projectDataAndBackedReward = projectData
                    .compose<Pair<ProjectData, Backing>>(combineLatestPair(backing))
                    .map {
                        pD -> pD.first.project().backing()?.backedReward(pD.first.project())?.let {
                            Pair(pD.first.toBuilder().backing(pD.second).build(), it)
                        }
                    }

            projectDataAndBackedReward
                    .compose(takeWhen<Pair<ProjectData, Reward>, Void>(this.fixPaymentMethodButtonClicked))
                    .map { Pair(pledgeData(it.second, it.first, PledgeFlowContext.FIX_ERRORED_PLEDGE), PledgeReason.FIX_PLEDGE) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledge)

            projectDataAndBackedReward
                    .compose(takeWhen<Pair<ProjectData, Reward>, Void>(this.updatePaymentClicked))
                    .map { Pair(pledgeData(it.second, it.first, PledgeFlowContext.MANAGE_REWARD), PledgeReason.UPDATE_PAYMENT) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledge)

            projectDataAndBackedReward
                    .compose(takeWhen<Pair<ProjectData, Reward>, Void>(this.updatePledgeClicked))
                    .map { Pair(pledgeData(it.second, it.first, PledgeFlowContext.MANAGE_REWARD), PledgeReason.UPDATE_PLEDGE) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledge)

            this.viewRewardsClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.revealRewardsFragment)

            currentProject
                    .map { it.isBacking && it.isLive || it.backing()?.isErrored() == true }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backingDetailsIsVisible)

            currentProject
                    .filter { it.isBacking }
                    .map { if (it.backing()?.isErrored() == true) R.string.Payment_failure else R.string.Youre_a_backer }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backingDetailsTitle)

            currentProject
                    .filter { it.isBacking }
                    .map { backingDetailsSubtitle(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backingDetailsSubtitle)

            val currentProjectAndUser = currentProject
                    .compose<Pair<Project, User>>(combineLatestPair(this.currentUser.observable()))

            Observable.combineLatest(currentProjectData, this.currentUser.observable())
            { data, user ->
                    val experimentData = ExperimentData(user, data.refTagFromIntent(), data.refTagFromCookie())
                    ProjectViewUtils.pledgeActionButtonText(
                            data.project(),
                            user,
                            this.optimizely.variant(OptimizelyExperiment.Key.PLEDGE_CTA_COPY, experimentData))
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
                    .map { if (it.second.isBacking) it.first > 4 else it.first > 3 }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.scrimIsVisible)

            currentProject
                    .map { p -> if (p.isStarred) R.drawable.icon__heart else R.drawable.icon__heart_outline }
                    .subscribe(this.heartDrawableId)

            //Tracking
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

                        this.lake.trackProjectPageViewed(dataWithStoredCookieRefTag, pledgeFlowContext)
                        this.lake.trackProjectScreenViewed(dataWithStoredCookieRefTag, OVERVIEW.contextName)
                    }

            fullProjectDataAndCurrentUser
                    .map { Pair(ExperimentData(it.second, it.first.refTagFromIntent(), it.first.refTagFromCookie()), it.first.project()) }
                    .compose(bindToLifecycle())
                    .subscribe { this.optimizely.track(PROJECT_PAGE_VIEWED, it.first) }

            fullProjectDataAndPledgeFlowContext
                    .compose<Pair<ProjectData, PledgeFlowContext?>>(takeWhen(this.nativeProjectActionButtonClicked))
                    .filter { it.first.project().isLive && !it.first.project().isBacking }
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.lake.trackProjectPagePledgeButtonClicked(storeCurrentCookieRefTag(it.first), it.second)
                        this.lake.trackPledgeInitiateCTA(it.first)
                    }

            fullProjectDataAndPledgeFlowContext
                    .map { it.first }
                    .compose<ProjectData>(takeWhen(blurbClicked))
                    .filter { it.project().isLive && !it.project().isBacking }
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.lake.trackCampaignDetailsCTAClicked(it)
                        this.lake.trackCampaignDetailsButtonClicked(it)
                    }

            fullProjectDataAndCurrentUser
                    .map { Pair(ExperimentData(it.second, it.first.refTagFromIntent(), it.first.refTagFromCookie()), it.first.project()) }
                    .compose<Pair<ExperimentData, Project>>(takeWhen(blurbClicked))
                    .filter { it.second.isLive && !it.second.isBacking }
                    .subscribe { this.optimizely.track(CAMPAIGN_DETAILS_BUTTON_CLICKED, it.first) }

            val shouldTrackCTAClickedEvent = this.pledgeActionButtonText
                    .map { isPledgeCTA(it) }
                    .compose<Boolean>(takeWhen(this.nativeProjectActionButtonClicked))

            fullProjectDataAndCurrentUser
                    .map { ExperimentData(it.second, it.first.refTagFromIntent(), it.first.refTagFromCookie()) }
                    .compose<Pair<ExperimentData, Boolean>>(combineLatestPair(shouldTrackCTAClickedEvent))
                    .filter { it.second }
                    .compose(bindToLifecycle())
                    .subscribe { this.optimizely.track(PROJECT_PAGE_PLEDGE_BUTTON_CLICKED, it.first) }

            projectDataAndBackedReward
                    .compose(takeWhen<Pair<ProjectData, Reward>, Void>(this.nativeProjectActionButtonClicked))
                    .filter { it.first.project().isLive && it.first.project().isBacking }
                    .map { Pair(pledgeData(it.second, it.first, PledgeFlowContext.MANAGE_REWARD), PledgeReason.UPDATE_PLEDGE) }
                    .compose(bindToLifecycle())
                    .subscribe{
                        this.lake.trackManagePledgePageViewed(it.first)
                    }

            fullProjectDataAndCurrentUser
                    .map { it.first }
                    .compose<ProjectData>(takeWhen(creatorInfoClicked))
                    .filter { it.project().isLive && !it.project().isBacking }
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.lake.trackCreatorDetailsCTA(it)
                        this.lake.trackCreatorDetailsClicked(it) }

            fullProjectDataAndCurrentUser
                    .map { Pair(ExperimentData(it.second, it.first.refTagFromIntent(), it.first.refTagFromCookie()), it.first.project()) }
                    .compose<Pair<ExperimentData, Project>>(takeWhen(creatorInfoClicked))
                    .filter { it.second.isLive && !it.second.isBacking }
                    .compose(bindToLifecycle())
                    .subscribe { this.optimizely.track(CREATOR_DETAILS_CLICKED, it.first) }

            fullProjectDataAndPledgeFlowContext
                    .compose<Pair<ProjectData, PledgeFlowContext?>>(takeWhen(this.nativeProjectActionButtonClicked))
                    .filter { it.second == PledgeFlowContext.FIX_ERRORED_PLEDGE }
                    .compose(bindToLifecycle())
                    .subscribe{ this.lake.trackManagePledgeButtonClicked(it.first, it.second) }

            projectData
                    .compose<ProjectData>(takeWhen(this.fixPaymentMethodButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe{ this.lake.trackFixPledgeButtonClicked(it) }
        }

        private fun eventName(projectActionButtonStringRes: Int) : String {
            return when (projectActionButtonStringRes) {
                R.string.Back_this_project -> KoalaEvent.BACK_THIS_PROJECT_BUTTON_CLICKED
                R.string.View_the_rewards -> KoalaEvent.BACK_THIS_PROJECT_BUTTON_CLICKED
                R.string.See_the_rewards -> KoalaEvent.BACK_THIS_PROJECT_BUTTON_CLICKED
                R.string.Manage -> KoalaEvent.MANAGE_PLEDGE_BUTTON_CLICKED
                R.string.View_your_pledge -> KoalaEvent.VIEW_YOUR_PLEDGE_BUTTON_CLICKED
                else -> KoalaEvent.VIEW_REWARDS_BUTTON_CLICKED
            }
        }

        private fun isPledgeCTA(projectActionButtonStringRes: Int) : Boolean {
            return when (projectActionButtonStringRes) {
                R.string.Back_this_project -> true
                R.string.View_the_rewards -> true
                R.string.See_the_rewards -> true
                R.string.Manage -> false
                R.string.View_your_pledge -> false
                else -> false
            }
        }

        private fun managePledgeMenu(projectAndFragmentStackCount: Pair<Project, Int>): Int? {
            val project = projectAndFragmentStackCount.first
            val count = projectAndFragmentStackCount.second
            return when {
                !project.isBacking || IntegerUtils.isNonZero(count) -> null
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
                ProjectUtils.userIsCreator(project, currentUser) -> null
                project.isLive && !project.isBacking -> PledgeFlowContext.NEW_PLEDGE
                project.isLive && project.isBacking -> PledgeFlowContext.MANAGE_REWARD
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

        override fun blurbTextViewClicked() {
            this.blurbTextViewClicked.onNext(null)
        }

        override fun blurbVariantClicked() {
            this.blurbVariantClicked.onNext(null)
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

        override fun creatorDashboardButtonClicked() {
            this.creatorDashboardButtonClicked.onNext(null)
        }

        override fun creatorInfoVariantClicked() {
            this.creatorInfoVariantClicked.onNext(null)
        }

        override fun creatorNameTextViewClicked() {
            this.creatorNameTextViewClicked.onNext(null)
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

        override fun projectViewHolderBlurbClicked(viewHolder: ProjectViewHolder) {
            this.blurbTextViewClicked()
        }

        override fun projectViewHolderBlurbVariantClicked(viewHolder: ProjectViewHolder) {
            this.blurbVariantClicked()
        }

        override fun projectViewHolderCommentsClicked(viewHolder: ProjectViewHolder) {
            this.commentsTextViewClicked()
        }

        override fun projectViewHolderCreatorClicked(viewHolder: ProjectViewHolder) {
            this.creatorNameTextViewClicked()
        }

        override fun projectViewHolderCreatorInfoVariantClicked(viewHolder: ProjectViewHolder) {
            this.creatorInfoVariantClicked()
        }

        override fun projectViewHolderDashboardClicked(viewHolder: ProjectViewHolder) {
            this.creatorDashboardButtonClicked()
        }

        override fun projectViewHolderVideoStarted(viewHolder: ProjectViewHolder) {
            this.playVideoButtonClicked()
        }

        override fun projectViewHolderUpdatesClicked(viewHolder: ProjectViewHolder) {
            this.updatesTextViewClicked()
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
        override fun showUpdatePledge(): Observable<Pair<PledgeData, PledgeReason>> = this.showUpdatePledge

        @NonNull
        override fun showUpdatePledgeSuccess(): Observable<Void> = this.showUpdatePledgeSuccess

        @NonNull
        override fun startCampaignWebViewActivity(): Observable<ProjectData> = this.startCampaignWebViewActivity

        @NonNull
        override fun startCommentsActivity(): Observable<Pair<Project, ProjectData>> = this.startCommentsActivity

        @NonNull
        override fun startCreatorBioWebViewActivity(): Observable<Project> = this.startCreatorBioWebViewActivity

        @NonNull
        override fun startCreatorDashboardActivity(): Observable<Project> = this.startCreatorDashboardActivity

        @NonNull
        override fun startLoginToutActivity(): Observable<Void> = this.startLoginToutActivity

        @NonNull
        override fun startMessagesActivity(): Observable<Project> = this.startMessagesActivity

        @NonNull
        override fun startThanksActivity(): Observable<Pair<CheckoutData, PledgeData>> = this.startThanksActivity

        @NonNull
        override fun startProjectUpdatesActivity(): Observable<Pair<Project, ProjectData>> = this.startProjectUpdatesActivity

        @NonNull
        override fun startVideoActivity(): Observable<Project> = this.startVideoActivity

        @NonNull
        override fun updateFragments(): Observable<ProjectData> = this.updateFragments

        private fun backingDetailsSubtitle(project: Project): Either<String, Int>? {
            return project.backing()?.let { backing ->
                return if(backing.status() == Backing.STATUS_ERRORED) {
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
            return this.client.saveProject(project)
                    .compose(neverError())
        }

        private fun toggleProjectSave(project: Project): Observable<Project> {
            return this.client.toggleProjectSave(project)
                    .compose(neverError())
        }
    }
}
