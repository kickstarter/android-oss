package com.kickstarter.viewmodels

import android.content.Intent
import android.content.SharedPreferences
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.*
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.BackingActivity
import com.kickstarter.ui.activities.ProjectActivity
import com.kickstarter.ui.adapters.ProjectAdapter
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.intentmappers.IntentMapper
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import com.kickstarter.ui.viewholders.ProjectViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode
import java.net.CookieManager

interface ProjectViewModel {
    interface Inputs {
        /** Call when the back project button is clicked.  */
        fun backProjectButtonClicked()

        /** Call when the blurb view is clicked.  */
        fun blurbTextViewClicked()

        /** Call when the cancel pledge option is clicked.  */
        fun cancelPledgeClicked()

        /** Call when horizontal rewards sheet should collapse. */
        fun collapsePledgeSheet()

        /** Call when the comments text view is clicked.  */
        fun commentsTextViewClicked()

        /** Call when the contact creator option is clicked.  */
        fun contactCreatorClicked()

        /** Call when the creator name is clicked.  */
        fun creatorNameTextViewClicked()

        /** Call when the count of fragments on the back stack changes.  */
        fun fragmentStackCount(count: Int)

        /** Call when the heart button is clicked.  */
        fun heartButtonClicked()

        /** Call when the manage pledge button is clicked.  */
        fun managePledgeButtonClicked()

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

        /** Call when the pledge has been successfully updated. */
        fun pledgeSuccessfullyUpdated()

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

        /** Call when the view pledge button is clicked.  */
        fun viewPledgeButtonClicked()

        /** Call when the view rewards option is clicked.  */
        fun viewRewardsClicked()
    }

    interface Outputs {
        /** Emits a string with the backing details to be displayed in the manage pledge view. */
        fun backingDetails(): Observable<String>

        /** Emits a boolean that determines if the backing details should be visible. */
        fun backingDetailsIsVisible(): Observable<Boolean>

        /** Emits when rewards sheet should expand. */
        fun expandPledgeSheet(): Observable<Boolean>

        /** Emits a drawable id that corresponds to whether the project is saved. */
        fun heartDrawableId(): Observable<Int>

        /** Emits a menu for managing your pledge or null if there's no menu. */
        fun managePledgeMenu(): Observable<Int?>

        /** Emits the url of a prelaunch activated project to open in the browser. */
        fun prelaunchUrl(): Observable<String>

        /** Emits a boolean that determines if the progress bar should be visible. */
        fun progressBarIsGone(): Observable<Boolean>

        /** Emits a boolean that determines if the project action button container should be visible. */
        fun projectActionButtonContainerIsGone(): Observable<Boolean>

        /** Emits a project and country when a new value is available. If the view model is created with a full project
         * model, this observable will emit that project immediately, and then again when it has updated from the api.  */
        fun projectAndUserCountry(): Observable<Pair<Project, String>>

        /** Emits a boolean that determines if the reload project container should be visible. */
        fun reloadProjectContainerIsGone(): Observable<Boolean>

        /** Emits when we should reveal the [com.kickstarter.ui.fragments.RewardsFragment] with an animation. */
        fun revealRewardsFragment(): Observable<Void>

        /** Emits the color resource ID for the reward button based on (View, Manage, or Back this project). */
        fun rewardsButtonColor(): Observable<Int>

        /** Emits the proper string resource ID for the reward button. */
        fun rewardsButtonText(): Observable<Int>

        /** Emits the proper string resource ID for the rewards toolbar. */
        fun rewardsToolbarTitle(): Observable<Int>

        /** Emits a boolean that determines if the scrim for secondary pledging actions should be visible. */
        fun scrimIsVisible(): Observable<Boolean>

        /** Emits when we should set the Y position of the rewards container. */
        fun setInitialRewardsContainerY(): Observable<Void>

        /** Emits when we should show the [com.kickstarter.ui.fragments.CancelPledgeFragment]. */
        fun showCancelPledgeFragment(): Observable<Project>

        /** Emits when the backing has successfully been canceled. */
        fun showCancelPledgeSuccess(): Observable<Void>

        /** Emits when we should reveal the [com.kickstarter.ui.fragments.BackingFragment]. */
        fun showBackingFragment(): Observable<Project>

        /** Emits when we should show the not cancelable dialog. */
        fun showPledgeNotCancelableDialog(): Observable<Void>

        /** Emits when we should reveal the [com.kickstarter.ui.fragments.RewardsFragment]. */
        fun showRewardsFragment(): Observable<Project>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Void>

        /** Emits when we should show the share sheet with the name of the project and share URL.  */
        fun showShareSheet(): Observable<Pair<String, String>>

        /** Emits when we should show the [com.kickstarter.ui.fragments.PledgeFragment]. */
        fun showUpdatePledge(): Observable<Pair<PledgeData, PledgeReason>>

        /** Emits when the backing has successfully been updated. */
        fun showUpdatePledgeSuccess(): Observable<Void>

        /** Emits when we should start the [BackingActivity].  */
        fun startBackingActivity(): Observable<Pair<Project, User>>

        /** Emits when we should start the campaign [com.kickstarter.ui.activities.WebViewActivity].  */
        fun startCampaignWebViewActivity(): Observable<Project>

        /** Emits when we should start the [com.kickstarter.ui.activities.CheckoutActivity].  */
        fun startCheckoutActivity(): Observable<Project>

        /** Emits when we should start [com.kickstarter.ui.activities.CommentsActivity].  */
        fun startCommentsActivity(): Observable<Project>

        /** Emits when we should start the creator bio [com.kickstarter.ui.activities.CreatorBioActivity].  */
        fun startCreatorBioWebViewActivity(): Observable<Project>

        /** Emits when we should start [com.kickstarter.ui.activities.LoginToutActivity].  */
        fun startLoginToutActivity(): Observable<Void>

        /** Emits when we should start the [com.kickstarter.ui.activities.CheckoutActivity] to manage the pledge.  */
        fun startManagePledgeActivity(): Observable<Project>

        /** Emits when we should show the [com.kickstarter.ui.activities.MessagesActivity]. */
        fun startMessagesActivity(): Observable<Project>

        /** Emits when we should start [com.kickstarter.ui.activities.ProjectUpdatesActivity].  */
        fun startProjectUpdatesActivity(): Observable<Project>

        /** Emits when we should start the [com.kickstarter.ui.activities.VideoActivity].  */
        fun startVideoActivity(): Observable<Project>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ProjectActivity>(environment), ProjectAdapter.Delegate, Inputs, Outputs {
        private val client: ApiClientType = environment.apiClient()
        private val currentUser: CurrentUserType = environment.currentUser()
        private val cookieManager: CookieManager = environment.cookieManager()
        private val currentConfig: CurrentConfigType = environment.currentConfig()
        private val ksCurrency: KSCurrency = environment.ksCurrency()
        private val nativeCheckoutPreference: BooleanPreferenceType = environment.nativeCheckoutPreference()
        private val sharedPreferences: SharedPreferences = environment.sharedPreferences()

        private val backProjectButtonClicked = PublishSubject.create<Void>()
        private val blurbTextViewClicked = PublishSubject.create<Void>()
        private val cancelPledgeClicked = PublishSubject.create<Void>()
        private val collapsePledgeSheet = PublishSubject.create<Void>()
        private val commentsTextViewClicked = PublishSubject.create<Void>()
        private val contactCreatorClicked = PublishSubject.create<Void>()
        private val creatorNameTextViewClicked = PublishSubject.create<Void>()
        private val fragmentStackCount = PublishSubject.create<Int>()
        private val heartButtonClicked = PublishSubject.create<Void>()
        private val managePledgeButtonClicked = PublishSubject.create<Void>()
        private val nativeProjectActionButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val playVideoButtonClicked = PublishSubject.create<Void>()
        private val pledgePaymentSuccessfullyUpdated = PublishSubject.create<Void>()
        private val pledgeSuccessfullyCancelled = PublishSubject.create<Void>()
        private val pledgeSuccessfullyUpdated = PublishSubject.create<Void>()
        private val reloadProjectContainerClicked = PublishSubject.create<Void>()
        private val shareButtonClicked = PublishSubject.create<Void>()
        private val updatePaymentClicked = PublishSubject.create<Void>()
        private val updatePledgeClicked = PublishSubject.create<Void>()
        private val updatesTextViewClicked = PublishSubject.create<Void>()
        private val viewRewardsClicked = PublishSubject.create<Void>()
        private val viewPledgeButtonClicked = PublishSubject.create<Void>()

        private val backingDetails = BehaviorSubject.create<String>()
        private val backingDetailsIsVisible = BehaviorSubject.create<Boolean>()
        private val expandPledgeSheet = BehaviorSubject.create<Boolean>()
        private val heartDrawableId = BehaviorSubject.create<Int>()
        private val managePledgeMenu = BehaviorSubject.create<Int?>()
        private val prelaunchUrl = PublishSubject.create<String>()
        private val projectActionButtonContainerIsGone = BehaviorSubject.create<Boolean>()
        private val progressBarIsGone = BehaviorSubject.create<Boolean>()
        private val projectAndUserCountry = BehaviorSubject.create<Pair<Project, String>>()
        private val reloadProjectContainerIsGone = BehaviorSubject.create<Boolean>()
        private val revealRewardsFragment = PublishSubject.create<Void>()
        private val rewardsButtonColor = BehaviorSubject.create<Int>()
        private val rewardsButtonText = BehaviorSubject.create<Int>()
        private val rewardsToolbarTitle = BehaviorSubject.create<Int>()
        private val scrimIsVisible = BehaviorSubject.create<Boolean>()
        private val setInitialRewardPosition = BehaviorSubject.create<Void>()
        private val showBackingFragment = BehaviorSubject.create<Project>()
        private val showCancelPledgeFragment = PublishSubject.create<Project>()
        private val showCancelPledgeSuccess = PublishSubject.create<Void>()
        private val showPledgeNotCancelableDialog = PublishSubject.create<Void>()
        private val showRewardsFragment = BehaviorSubject.create<Project>()
        private val showShareSheet = PublishSubject.create<Pair<String, String>>()
        private val showSavedPrompt = PublishSubject.create<Void>()
        private val showUpdatePledge = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val showUpdatePledgeSuccess = PublishSubject.create<Void>()
        private val startCampaignWebViewActivity = PublishSubject.create<Project>()
        private val startCheckoutActivity = PublishSubject.create<Project>()
        private val startCommentsActivity = PublishSubject.create<Project>()
        private val startCreatorBioWebViewActivity = PublishSubject.create<Project>()
        private val startLoginToutActivity = PublishSubject.create<Void>()
        private val startManagePledgeActivity = PublishSubject.create<Project>()
        private val startMessagesActivity = PublishSubject.create<Project>()
        private val startProjectUpdatesActivity = PublishSubject.create<Project>()
        private val startVideoActivity = PublishSubject.create<Project>()
        private val startBackingActivity = PublishSubject.create<Pair<Project, User>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val mappedProjectNotification = Observable.merge(intent(), intent()
                    .compose(takeWhen<Intent, Void>(this.reloadProjectContainerClicked)))
                    .flatMap {
                        ProjectIntentMapper.project(it, this.client)
                                .doOnSubscribe {
                                    this.progressBarIsGone.onNext(false)
                                }
                                .doAfterTerminate {
                                    this.progressBarIsGone.onNext(true)
                                }
                                .materialize()
                    }
                    .share()

            val mappedProjectValues = mappedProjectNotification
                    .compose(values())

            val mappedProjectErrors = mappedProjectNotification
                    .compose(errors())
                    .compose<Pair<Throwable, Boolean>>(combineLatestPair(Observable.just(this.nativeCheckoutPreference.get())))
                    .filter { BooleanUtils.isTrue(it.second) }

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
                    this.pledgeSuccessfullyUpdated,
                    this.pledgePaymentSuccessfullyUpdated)

            val refreshedProject = initialProject
                    .compose(takeWhen<Project, Void>(refreshProjectEvent))
                    .switchMap {
                        this.client.fetchProject(it)
                                .compose(neverError())
                    }

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
                    refreshedProject,
                    projectOnUserChangeSave,
                    savedProjectOnLoginSuccess
            )

            projectOnUserChangeSave.mergeWith(savedProjectOnLoginSuccess)
                    .filter { p -> p.isStarred && p.isLive && !p.isApproachingDeadline }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showSavedPrompt)

            currentProject
                    .compose<Pair<Project, String>>(combineLatestPair(this.currentConfig.observable().map { it.countryCode() }))
                    .compose(bindToLifecycle())
                    .subscribe(this.projectAndUserCountry)

            currentProject
                    .compose<Project>(takeWhen(this.shareButtonClicked))
                    .map { Pair(it.name(), UrlUtils.appendRefTag(it.webProjectUrl(), RefTag.projectShare().tag())) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showShareSheet)

            currentProject
                    .compose<Project>(takeWhen(this.blurbTextViewClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startCampaignWebViewActivity)

            currentProject
                    .compose<Project>(takeWhen(this.backProjectButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startCheckoutActivity)

            currentProject
                    .compose<Project>(takeWhen(this.creatorNameTextViewClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startCreatorBioWebViewActivity)

            currentProject
                    .compose<Project>(takeWhen(this.commentsTextViewClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startCommentsActivity)

            currentProject
                    .compose<Project>(takeWhen(this.managePledgeButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startManagePledgeActivity)

            currentProject
                    .compose<Project>(takeWhen(this.updatesTextViewClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startProjectUpdatesActivity)

            currentProject
                    .compose<Project>(takeWhen(this.playVideoButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startVideoActivity)

            Observable.combineLatest<Project, User, Pair<Project, User>>(currentProject, this.currentUser.observable())
            { project, user -> Pair.create(project, user) }
                    .compose<Pair<Project, User>>(takeWhen(this.viewPledgeButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startBackingActivity)

            this.onGlobalLayout
                    .compose(bindToLifecycle())
                    .subscribe(this.setInitialRewardPosition)

            this.nativeProjectActionButtonClicked
                    .map { true }
                    .compose(bindToLifecycle())
                    .subscribe(this.expandPledgeSheet)

            Observable.merge(this.collapsePledgeSheet, this.pledgeSuccessfullyCancelled)
                    .map { false }
                    .compose(bindToLifecycle())
                    .subscribe(this.expandPledgeSheet)

            val nativeCheckoutProject = currentProject
                    .compose<Pair<Project, Boolean>>(combineLatestPair(Observable.just(this.nativeCheckoutPreference.get())))
                    .filter { BooleanUtils.isTrue(it.second) }
                    .map<Project> { it.first }

            val projectHasRewards = nativeCheckoutProject
                    .map { it.hasRewards() }
                    .distinctUntilChanged()
                    .takeUntil(this.expandPledgeSheet)

            val rewardsLoaded = projectHasRewards
                    .filter { BooleanUtils.isTrue(it) }
                    .map { true }

            Observable.merge(rewardsLoaded, this.reloadProjectContainerClicked.map { true })
                    .compose(bindToLifecycle())
                    .subscribe(this.reloadProjectContainerIsGone)

            mappedProjectErrors
                    .map { false }
                    .compose(bindToLifecycle())
                    .subscribe(this.reloadProjectContainerIsGone)

            projectHasRewards
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.projectActionButtonContainerIsGone)

            nativeCheckoutProject
                    .filter { it.isBacking && it.hasRewards() }
                    .distinctUntilChanged { old, new -> old.backing() == new.backing() }
                    .compose(bindToLifecycle())
                    .subscribe(this.showBackingFragment)

            nativeCheckoutProject
                    .filter { !it.isBacking && it.hasRewards() }
                    .distinctUntilChanged { old, new -> old.backing() == new.backing() }
                    .compose(bindToLifecycle())
                    .subscribe(this.showRewardsFragment)

            nativeCheckoutProject
                    .compose<Pair<Project, Int>>(combineLatestPair(this.fragmentStackCount.startWith(0)))
                    .map { managePledgeMenu(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.managePledgeMenu)

            val backedProject = nativeCheckoutProject
                    .filter { it.isBacking }

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

            nativeCheckoutProject
                    .compose<Project>(takeWhen(this.contactCreatorClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.startMessagesActivity)

            val projectAndBackedReward = nativeCheckoutProject
                    .map { project -> Pair(project, project.rewards()?.firstOrNull { BackingUtils.isBacked(project, it) }) }
                    .map { projectAndReward -> projectAndReward.second?.let { Pair(projectAndReward.first, it) } }

            projectAndBackedReward
                    .compose(takeWhen<Pair<Project, Reward>, Void>(this.updatePaymentClicked))
                    .map { Pair(PledgeData(reward = it.second, project = it.first), PledgeReason.UPDATE_PAYMENT) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledge)

            projectAndBackedReward
                    .compose(takeWhen<Pair<Project, Reward>, Void>(this.updatePledgeClicked))
                    .map { Pair(PledgeData(reward = it.second, project = it.first), PledgeReason.UPDATE_PLEDGE) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledge)

            this.viewRewardsClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.revealRewardsFragment)

            nativeCheckoutProject
                    .map { it.isBacking && it.isLive }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backingDetailsIsVisible)

            nativeCheckoutProject
                    .filter { it.isBacking && it.isLive }
                    .map { backingDetails(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backingDetails)

            nativeCheckoutProject
                    .map { ProjectViewUtils.rewardsButtonText(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe { this.rewardsButtonText.onNext(it) }

            nativeCheckoutProject
                    .map { ProjectViewUtils.rewardsToolbarTitle(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardsToolbarTitle)

            nativeCheckoutProject
                    .map { ProjectViewUtils.rewardsButtonColor(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardsButtonColor)

            this.pledgePaymentSuccessfullyUpdated
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledgeSuccess)

            this.pledgeSuccessfullyCancelled
                    .compose(bindToLifecycle())
                    .subscribe(this.showCancelPledgeSuccess)

            this.pledgeSuccessfullyUpdated
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledgeSuccess)

            this.fragmentStackCount
                    .compose<Pair<Int, Project>>(combineLatestPair(nativeCheckoutProject))
                    .map { if (it.second.isBacking) it.first > 2 else it.first > 1}
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.scrimIsVisible)

            currentProject
                    .compose<Project>(takeWhen(this.showShareSheet))
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackShowProjectShareSheet(it) }

            this.startVideoActivity
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackVideoStart(it) }

            currentProject
                    .map { p -> if (p.isStarred) R.drawable.icon__heart else R.drawable.icon__heart_outline }
                    .subscribe(this.heartDrawableId)

            projectOnUserChangeSave
                    .mergeWith(savedProjectOnLoginSuccess)
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackProjectStar(it) }

            Observable.combineLatest<RefTag, RefTag, Project, RefTagsAndProject>(refTag, cookieRefTag, currentProject)
            { refTagFromIntent, refTagFromCookie, project -> RefTagsAndProject(refTagFromIntent, refTagFromCookie, project) }
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe { data ->
                        // If a cookie hasn't been set for this ref+project then do so.
                        if (data.refTagFromCookie == null && data.refTagFromIntent != null) {
                            RefTagUtils.storeCookie(data.refTagFromIntent, data.project, this.cookieManager, this.sharedPreferences)
                        }

                        this.koala.trackProjectShow(
                                data.project,
                                data.refTagFromIntent,
                                RefTagUtils.storedCookieRefTagForProject(data.project, this.cookieManager, this.sharedPreferences)
                        )
                    }

            pushNotificationEnvelope
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackPushNotification(it) }

            intent()
                    .filter { IntentMapper.appBannerIsSet(it) }
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackOpenedAppBanner() }

        }

        private fun managePledgeMenu(projectAndFragmentStackCount: Pair<Project, Int>): Int? {
            val project = projectAndFragmentStackCount.first
            val count = projectAndFragmentStackCount.second
            return when {
                !project.isBacking || IntegerUtils.isNonZero(count) -> null
                project.isLive -> R.menu.manage_pledge_live
                else -> R.menu.manage_pledge_ended
            }
        }

        /**
         * A light-weight value to hold two ref tags and a project. Two ref tags are stored: one comes from parceled
         * data in the activity and the other comes from the ref stored in a cookie associated to the project.
         */
        private inner class RefTagsAndProject internal constructor(val refTagFromIntent: RefTag?, val refTagFromCookie: RefTag?,
                                                                   val project: Project)

        override fun backProjectButtonClicked() {
            this.backProjectButtonClicked.onNext(null)
        }

        override fun blurbTextViewClicked() {
            this.blurbTextViewClicked.onNext(null)
        }

        override fun cancelPledgeClicked() {
            this.cancelPledgeClicked.onNext(null)
        }

        override fun collapsePledgeSheet() {
            this.collapsePledgeSheet.onNext(null)
        }

        override fun commentsTextViewClicked() {
            this.commentsTextViewClicked.onNext(null)
        }

        override fun contactCreatorClicked() {
            this.contactCreatorClicked.onNext(null)
        }

        override fun creatorNameTextViewClicked() {
            this.creatorNameTextViewClicked.onNext(null)
        }

        override fun fragmentStackCount(count: Int) {
            this.fragmentStackCount.onNext(count)
        }

        override fun heartButtonClicked() {
            this.heartButtonClicked.onNext(null)
        }

        override fun managePledgeButtonClicked() {
            this.managePledgeButtonClicked.onNext(null)
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

        override fun pledgeSuccessfullyUpdated() {
            this.pledgeSuccessfullyUpdated.onNext(null)
        }

        override fun projectViewHolderBackProjectClicked(viewHolder: ProjectViewHolder) {
            if (this.nativeCheckoutPreference.get()) {
                this.nativeProjectActionButtonClicked()
            } else {
                this.backProjectButtonClicked()
            }
        }

        override fun projectViewHolderBlurbClicked(viewHolder: ProjectViewHolder) {
            this.blurbTextViewClicked()
        }

        override fun projectViewHolderCommentsClicked(viewHolder: ProjectViewHolder) {
            this.commentsTextViewClicked()
        }

        override fun projectViewHolderCreatorClicked(viewHolder: ProjectViewHolder) {
            this.creatorNameTextViewClicked()
        }

        override fun projectViewHolderManagePledgeClicked(viewHolder: ProjectViewHolder) {
            if (this.nativeCheckoutPreference.get()) {
                this.nativeProjectActionButtonClicked()
            } else {
                this.managePledgeButtonClicked()
            }
        }

        override fun projectViewHolderVideoStarted(viewHolder: ProjectViewHolder) {
            this.playVideoButtonClicked()
        }

        override fun projectViewHolderViewPledgeClicked(viewHolder: ProjectViewHolder) {
            if (this.nativeCheckoutPreference.get()) {
                this.nativeProjectActionButtonClicked()
            } else {
                this.viewPledgeButtonClicked()
            }
        }

        override fun projectViewHolderUpdatesClicked(viewHolder: ProjectViewHolder) {
            this.updatesTextViewClicked()
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

        override fun viewPledgeButtonClicked() {
            this.viewPledgeButtonClicked.onNext(null)
        }

        override fun viewRewardsClicked() {
            this.viewRewardsClicked.onNext(null)
        }

        @NonNull
        override fun backingDetails(): Observable<String> = this.backingDetails

        @NonNull
        override fun backingDetailsIsVisible(): Observable<Boolean> = this.backingDetailsIsVisible

        @NonNull
        override fun expandPledgeSheet(): Observable<Boolean> = this.expandPledgeSheet

        @NonNull
        override fun heartDrawableId(): Observable<Int> = this.heartDrawableId

        @NonNull
        override fun managePledgeMenu(): Observable<Int?> = this.managePledgeMenu

        @NonNull
        override fun prelaunchUrl(): Observable<String> = this.prelaunchUrl

        @NonNull
        override fun progressBarIsGone(): Observable<Boolean> = this.progressBarIsGone

        @NonNull
        override fun projectActionButtonContainerIsGone(): Observable<Boolean> = this.projectActionButtonContainerIsGone

        @NonNull
        override fun projectAndUserCountry(): Observable<Pair<Project, String>> = this.projectAndUserCountry

        @NonNull
        override fun reloadProjectContainerIsGone(): Observable<Boolean> = this.reloadProjectContainerIsGone

        @NonNull
        override fun revealRewardsFragment(): Observable<Void> = this.revealRewardsFragment

        @NonNull
        override fun rewardsButtonColor(): Observable<Int> = this.rewardsButtonColor

        @NonNull
        override fun rewardsButtonText(): Observable<Int> = this.rewardsButtonText

        @NonNull
        override fun rewardsToolbarTitle(): Observable<Int> = this.rewardsToolbarTitle

        @NonNull
        override fun scrimIsVisible(): Observable<Boolean> = this.scrimIsVisible

        @NonNull
        override fun setInitialRewardsContainerY(): Observable<Void> = this.setInitialRewardPosition

        @NonNull
        override fun showBackingFragment(): Observable<Project> = this.showBackingFragment

        @NonNull
        override fun showCancelPledgeFragment(): Observable<Project> = this.showCancelPledgeFragment

        @NonNull
        override fun showCancelPledgeSuccess(): Observable<Void> = this.showCancelPledgeSuccess

        @NonNull
        override fun showPledgeNotCancelableDialog(): Observable<Void> = this.showPledgeNotCancelableDialog

        @NonNull
        override fun showRewardsFragment(): Observable<Project> = this.showRewardsFragment

        @NonNull
        override fun showSavedPrompt(): Observable<Void> = this.showSavedPrompt

        @NonNull
        override fun showShareSheet(): Observable<Pair<String, String>> = this.showShareSheet

        @NonNull
        override fun showUpdatePledge(): Observable<Pair<PledgeData, PledgeReason>> = this.showUpdatePledge

        @NonNull
        override fun showUpdatePledgeSuccess(): Observable<Void> = this.showUpdatePledgeSuccess

        @NonNull
        override fun startBackingActivity(): Observable<Pair<Project, User>> = this.startBackingActivity

        @NonNull
        override fun startCampaignWebViewActivity(): Observable<Project> = this.startCampaignWebViewActivity

        @NonNull
        override fun startCheckoutActivity(): Observable<Project> = this.startCheckoutActivity

        @NonNull
        override fun startCommentsActivity(): Observable<Project> = this.startCommentsActivity

        @NonNull
        override fun startCreatorBioWebViewActivity(): Observable<Project> = this.startCreatorBioWebViewActivity

        @NonNull
        override fun startLoginToutActivity(): Observable<Void> = this.startLoginToutActivity

        @NonNull
        override fun startManagePledgeActivity(): Observable<Project> = this.startManagePledgeActivity

        @NonNull
        override fun startMessagesActivity(): Observable<Project> = this.startMessagesActivity

        @NonNull
        override fun startProjectUpdatesActivity(): Observable<Project> = this.startProjectUpdatesActivity

        @NonNull
        override fun startVideoActivity(): Observable<Project> = this.startVideoActivity

        private fun backingDetails(project: Project): String {
            return project.backing()?.let { backing ->
                val reward = project.rewards()?.firstOrNull { it.id() == backing.rewardId() }
                val title = reward?.let { "• ${it.title()}" } ?: ""

                val backingAmount = backing.amount()

                val formattedAmount = this.ksCurrency.format(backingAmount, project, RoundingMode.HALF_UP)

                return "$formattedAmount $title".trim()
            } ?: ""
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
