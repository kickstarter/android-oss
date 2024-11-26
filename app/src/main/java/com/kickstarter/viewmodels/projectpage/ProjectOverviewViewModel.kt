package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES_TAG
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.OUR_RULES
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.OUR_RULES_TAG
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

interface ProjectOverviewViewModel {
    interface Inputs {
        /** Call to configure view holder with ProjectData.  */
        fun configureWith(projectData: ProjectData)

        /** Call when the project social view group is clicked.  */
        fun projectSocialViewGroupClicked()

        /** Call when the creator button is clicked  */
        fun creatorInfoButtonClicked()

        /** Call when the campaign clicked  */
        fun campaignButtonClicked()

        /** Call when the comments clicked  */
        fun commentsButtonClicked()

        /** Call when the updates clicked  */
        fun updatesButtonClicked()

        /** Called when the report project button  */
        fun reportProjectButtonClicked()

        /** Called when the report project flow is completed with flaggingKind  */
        fun refreshFlaggedState(flaggingKind: String)

        /** Called when the user press some of the links on the flagged project text  */
        fun linkClicked(urlTag: String)
    }

    interface Outputs {
        /** Emits the creator's avatar photo url for display.  */
        fun avatarPhotoUrl(): Observable<String>

        /** Emits the backers count string for display.  */
        fun backersCountTextViewText(): Observable<String>

        /** Emits the project blurb for display.  */
        fun blurbTextViewText(): Observable<String>

        /** Emits the project category for display.  */
        fun categoryTextViewText(): Observable<String>

        /** Emits the comments count for display.  */
        fun commentsCountTextViewText(): Observable<String>

        /** Emits the usd conversion text for display.  */
        fun conversionPledgedAndGoalText(): Observable<Pair<String, String>>

        /** Emits when the usd conversion view should be gone.  */
        fun conversionTextViewIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the creator details loading container should be visible.  */
        fun creatorDetailsLoadingContainerIsVisible(): Observable<Boolean>

        /** Emits a boolean determining if the creator details should be visible.  */
        fun creatorDetailsIsGone(): Observable<Boolean>

        /** Emits the project creator's name for display.  */
        fun creatorNameTextViewText(): Observable<String>

        /** Emits the deadline countdown text for display.  */
        fun deadlineCountdownTextViewText(): Observable<String>

        /** Emits the goal string for display.  */
        fun goalStringForTextView(): Observable<String>

        /** Emits the location for display.  */
        fun locationTextViewText(): Observable<String>

        /** Emits the percentage funded amount for display in the progress bar.  */
        fun percentageFundedProgress(): Observable<Int>

        /** Emits when the progress bar should be gone.  */
        fun percentageFundedProgressBarIsGone(): Observable<Boolean>

        /** Emits the pledged amount for display.  */
        fun pledgedTextViewText(): Observable<String>

        /** Emits the date time to be displayed in the disclaimer.  */
        fun projectDisclaimerGoalReachedDateTime(): Observable<DateTime>

        /** Emits a string and date time for an unsuccessful project disclaimer.  */
        fun projectDisclaimerGoalNotReachedString(): Observable<Pair<String, DateTime>>

        /** Emits when the disclaimer view should be gone.  */
        fun projectDisclaimerTextViewIsGone(): Observable<Boolean>

        /** Emits the localized date time to be displayed in the launch date text view.  */
        fun projectLaunchDate(): Observable<String>

        /** Emits when the launch date view should be gone.  */
        fun projectLaunchDateIsGone(): Observable<Boolean>

        /** Emits the project name for display.  */
        fun projectNameTextViewText(): Observable<String>

        /** Emits the project for display.  */
        fun projectOutput(): Observable<Project>

        /** Emits when the social image view should be gone.  */
        fun projectSocialImageViewIsGone(): Observable<Boolean>

        /** Emits the social image view url for display.  */
        fun projectSocialImageViewUrl(): Observable<String>

        /** Emits the list of friends to display display in the facepile. */
        fun projectSocialTextViewFriends(): Observable<List<User>>

        /** Emits when the social view group should be gone.  */
        fun projectSocialViewGroupIsGone(): Observable<Boolean>

        /** Emits the state background color int for display.  */
        fun projectStateViewGroupBackgroundColorInt(): Observable<Int>

        /** Emits when the project state view group should be gone.  */
        fun projectStateViewGroupIsGone(): Observable<Boolean>

        /** Emits when we should set default stats margins.  */
        fun shouldSetDefaultStatsMargins(): Observable<Boolean>

        /** Emits when we should set the canceled state view.  */
        fun setCanceledProjectStateView(): Observable<Unit>

        /** Emits when we should set an on click listener to the social view.  */
        fun setProjectSocialClickListener(): Observable<Unit>

        /** Emits when we should set the successful state view.  */
        fun setSuccessfulProjectStateView(): Observable<DateTime>

        /** Emits when we should set the suspended state view.  */
        fun setSuspendedProjectStateView(): Observable<Unit>

        /** Emits when we should set the unsuccessful state view.  */
        fun setUnsuccessfulProjectStateView(): Observable<DateTime>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectSocialActivity].  */
        fun startProjectSocialActivity(): Observable<Project>

        /** Emits the updates count for display.  */
        fun updatesCountTextViewText(): Observable<String>

        fun startCreatorView(): Observable<ProjectData>
        fun startCommentsView(): Observable<ProjectData>
        fun startUpdatesView(): Observable<ProjectData>
        fun startReportProjectView(): Observable<ProjectData>
        fun startLoginView(): Observable<Unit>
        fun shouldShowReportProject(): Observable<Boolean>
        fun shouldShowProjectFlagged(): Observable<Boolean>
        fun openExternallyWithUrl(): Observable<String>
    }

    class ProjectOverviewViewModel(
        private val environment: Environment
    ) : ViewModel(), Inputs, Outputs {

        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val ksCurrency = requireNotNull(environment.ksCurrency())
        private val analyticEvents = requireNotNull(environment.analytics())
        val ksString = requireNotNull(environment.ksString())

        // Inputs
        private val projectData = PublishSubject.create<ProjectData>()
        private val projectSocialViewGroupClicked = PublishSubject.create<Unit>()
        private val creatorInfoClicked = PublishSubject.create<Unit>()
        private val campaignClicked = PublishSubject.create<Unit>()
        private val commentsClicked = PublishSubject.create<Unit>()
        private val updatesClicked = PublishSubject.create<Unit>()
        private val reportProjectButtonClicked = PublishSubject.create<Unit>()
        private val refreshFlagged = PublishSubject.create<String>()
        private val linkTagClicked = PublishSubject.create<String>()

        // Outputs
        private val avatarPhotoUrl: Observable<String>
        private val backersCountTextViewText: Observable<String>
        private val blurbTextViewText: Observable<String>
        private val categoryTextViewText: Observable<String>
        private val commentsCountTextViewText: Observable<String>
        private val conversionPledgedAndGoalText: Observable<Pair<String, String>>
        private val conversionTextViewIsGone: Observable<Boolean>
        private val creatorDetailsLoadingContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val creatorDetailsIsGone = BehaviorSubject.create<Boolean>()
        private val creatorNameTextViewText: Observable<String>
        private val deadlineCountdownTextViewText: Observable<String>
        private val goalStringForTextView: Observable<String>
        private val locationTextViewText: Observable<String>
        private val percentageFundedProgress: Observable<Int>
        private val percentageFundedProgressBarIsGone: Observable<Boolean>
        private val pledgedTextViewText: Observable<String>
        private val projectDisclaimerGoalReachedDateTime: Observable<DateTime>
        private val projectDisclaimerGoalNotReachedString: Observable<Pair<String, DateTime>>
        private val projectDisclaimerTextViewIsGone: Observable<Boolean>
        private val projectLaunchDate: Observable<String>
        private val projectLaunchDateIsGone: Observable<Boolean>
        private val projectNameTextViewText: Observable<String>
        private val projectOutput: Observable<Project>
        private val projectSocialImageViewIsGone: Observable<Boolean>
        private val projectSocialImageViewUrl: Observable<String>
        private val projectSocialTextViewFriends: Observable<List<User>>
        private val projectSocialViewGroupIsGone: Observable<Boolean>
        private val projectStateViewGroupBackgroundColorInt: Observable<Int>
        private val projectStateViewGroupIsGone: Observable<Boolean>
        private val setCanceledProjectStateView: Observable<Unit>
        private val setProjectSocialClickListener: Observable<Unit>
        private val setSuccessfulProjectStateView: Observable<DateTime>
        private val setSuccessfulProjectStillCollectingView: Observable<DateTime>
        private val setSuspendedProjectStateView: Observable<Unit>
        private val setUnsuccessfulProjectStateView: Observable<DateTime>
        private val startProjectSocialActivity: Observable<Project>
        private val shouldSetDefaultStatsMargins: Observable<Boolean>
        private val updatesCountTextViewText: Observable<String>
        private val startCreatorView: Observable<ProjectData>
        private val startCommentsView: Observable<ProjectData>
        private val startUpdatesView: Observable<ProjectData>
        private val startReportProjectView: Observable<ProjectData>
        private val startLogin = PublishSubject.create<Unit>()
        private val shouldShowReportProject: Observable<Boolean>
        private val shouldShowProjectFlagged: Observable<Boolean>
        private val openExternally = PublishSubject.create<String>()

        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        // - Inputs
        override fun configureWith(projectData: ProjectData) = this.projectData.onNext(projectData)

        override fun projectSocialViewGroupClicked() = projectSocialViewGroupClicked.onNext(Unit)

        override fun creatorInfoButtonClicked() = this.creatorInfoClicked.onNext(Unit)

        override fun campaignButtonClicked() = this.campaignClicked.onNext(Unit)

        override fun commentsButtonClicked() = this.commentsClicked.onNext(Unit)

        override fun updatesButtonClicked() = this.updatesClicked.onNext(Unit)

        override fun reportProjectButtonClicked() = this.reportProjectButtonClicked.onNext(Unit)

        override fun refreshFlaggedState(flaggingKind: String) = this.refreshFlagged.onNext(flaggingKind)

        override fun linkClicked(urlTag: String) = this.linkTagClicked.onNext(urlTag)

        // - Outputs
        override fun avatarPhotoUrl(): Observable<String> {
            return avatarPhotoUrl
        }

        override fun backersCountTextViewText(): Observable<String> {
            return backersCountTextViewText
        }

        override fun blurbTextViewText(): Observable<String> {
            return blurbTextViewText
        }

        override fun categoryTextViewText(): Observable<String> {
            return categoryTextViewText
        }

        override fun commentsCountTextViewText(): Observable<String> {
            return commentsCountTextViewText
        }

        override fun conversionTextViewIsGone(): Observable<Boolean> {
            return conversionTextViewIsGone
        }

        override fun conversionPledgedAndGoalText(): Observable<Pair<String, String>> {
            return conversionPledgedAndGoalText
        }

        override fun creatorDetailsLoadingContainerIsVisible(): Observable<Boolean> {
            return creatorDetailsLoadingContainerIsVisible
        }

        override fun creatorDetailsIsGone(): Observable<Boolean> {
            return creatorDetailsIsGone
        }

        override fun creatorNameTextViewText(): Observable<String> {
            return creatorNameTextViewText
        }

        override fun deadlineCountdownTextViewText(): Observable<String> {
            return deadlineCountdownTextViewText
        }

        override fun goalStringForTextView(): Observable<String> {
            return goalStringForTextView
        }

        override fun locationTextViewText(): Observable<String> {
            return locationTextViewText
        }

        override fun percentageFundedProgress(): Observable<Int> {
            return percentageFundedProgress
        }

        override fun percentageFundedProgressBarIsGone(): Observable<Boolean> {
            return percentageFundedProgressBarIsGone
        }

        override fun pledgedTextViewText(): Observable<String> {
            return pledgedTextViewText
        }

        override fun projectDisclaimerGoalReachedDateTime(): Observable<DateTime> {
            return projectDisclaimerGoalReachedDateTime
        }

        override fun projectDisclaimerGoalNotReachedString(): Observable<Pair<String, DateTime>> {
            return projectDisclaimerGoalNotReachedString
        }

        override fun projectDisclaimerTextViewIsGone(): Observable<Boolean> {
            return projectDisclaimerTextViewIsGone
        }

        override fun projectLaunchDate(): Observable<String> {
            return projectLaunchDate
        }

        override fun projectLaunchDateIsGone(): Observable<Boolean> {
            return projectLaunchDateIsGone
        }

        override fun projectNameTextViewText(): Observable<String> {
            return projectNameTextViewText
        }

        override fun projectOutput(): Observable<Project> {
            return projectOutput
        }

        override fun projectSocialImageViewIsGone(): Observable<Boolean> {
            return projectSocialImageViewIsGone
        }

        override fun projectSocialImageViewUrl(): Observable<String> {
            return projectSocialImageViewUrl
        }

        override fun projectSocialTextViewFriends(): Observable<List<User>> {
            return projectSocialTextViewFriends
        }

        override fun projectSocialViewGroupIsGone(): Observable<Boolean> {
            return projectSocialViewGroupIsGone
        }

        override fun projectStateViewGroupBackgroundColorInt(): Observable<Int> {
            return projectStateViewGroupBackgroundColorInt
        }

        override fun projectStateViewGroupIsGone(): Observable<Boolean> {
            return projectStateViewGroupIsGone
        }

        override fun startProjectSocialActivity(): Observable<Project> {
            return startProjectSocialActivity
        }

        override fun setCanceledProjectStateView(): Observable<Unit> {
            return setCanceledProjectStateView
        }

        override fun setProjectSocialClickListener(): Observable<Unit> {
            return setProjectSocialClickListener
        }

        override fun setSuccessfulProjectStateView(): Observable<DateTime> {
            return setSuccessfulProjectStateView
        }

        fun setSuccessfulProjectStillCollectingView(): Observable<DateTime> {
            return setSuccessfulProjectStillCollectingView
        }

        override fun setSuspendedProjectStateView(): Observable<Unit> {
            return setSuspendedProjectStateView
        }

        override fun setUnsuccessfulProjectStateView(): Observable<DateTime> {
            return setUnsuccessfulProjectStateView
        }

        override fun shouldSetDefaultStatsMargins(): Observable<Boolean> {
            return shouldSetDefaultStatsMargins
        }

        override fun updatesCountTextViewText(): Observable<String> {
            return updatesCountTextViewText
        }

        override fun startCreatorView(): Observable<ProjectData> {
            return this.startCreatorView
        }

        override fun startCommentsView(): Observable<ProjectData> {
            return this.startCommentsView
        }

        override fun startUpdatesView(): Observable<ProjectData> {
            return this.startUpdatesView
        }

        override fun startReportProjectView(): Observable<ProjectData> {
            return this.startReportProjectView
        }

        override fun startLoginView(): Observable<Unit> {
            return this.startLogin
        }

        override fun shouldShowReportProject(): Observable<Boolean> {
            return this.shouldShowReportProject
        }

        override fun shouldShowProjectFlagged(): Observable<Boolean> {
            return this.shouldShowProjectFlagged
        }

        override fun openExternallyWithUrl(): Observable<String> {
            return this.openExternally
        }

        init {
            val project = projectData
                .distinctUntilChanged()
                .filter { it.project().isNotNull() }
                .map { requireNotNull(it.project()) }

            avatarPhotoUrl = project
                .filter { it.creator().avatar().medium().isNotNull() }
                .map { requireNotNull(it.creator().avatar().medium()) }

            backersCountTextViewText = project
                .map { NumberUtils.format(it.backersCount()) }

            blurbTextViewText = project
                .map { it.blurb() }

            categoryTextViewText = project
                .filter { it.category().isNotNull() }
                .map { it.category() }
                .map { it?.name() ?: "" }

            commentsCountTextViewText = project
                .filter() { it.commentsCount().isNotNull() }
                .map { requireNotNull(it.commentsCount()) }
                .map { NumberUtils.format(it) }

            conversionTextViewIsGone = project
                .map { it.currency() != it.currentCurrency() }
                .map { it.negate() }

            conversionPledgedAndGoalText = project
                .map { proj ->
                    val pledged = ksCurrency.format(proj.pledged(), proj)
                    val goal = ksCurrency.format(proj.goal(), proj)
                    Pair.create(pledged, goal)
                }

            creatorNameTextViewText = project
                .map { it.creator().name() }

            val creatorDetailsNotification = project
                .take(1)
                .distinctUntilChanged()
                .map { it.slug() }
                .switchMap { slug ->
                    apolloClient.creatorDetails(slug ?: "")
                        .doOnSubscribe { creatorDetailsLoadingContainerIsVisible.onNext(true) }
                        .doAfterTerminate { creatorDetailsLoadingContainerIsVisible.onNext(false) }
                        .materialize()
                }
                .share()

            creatorDetailsNotification
                .compose(Transformers.errorsV2())
                .map { _: Throwable? -> true }
                .subscribe { creatorDetailsIsGone.onNext(it) }
                .addToDisposable(disposables)

            deadlineCountdownTextViewText = project
                .map { proj -> proj.deadlineCountdownValue() }
                .map { NumberUtils.format(it) }

            goalStringForTextView = project
                .map { p: Project -> ksCurrency.formatWithUserPreference(p.goal(), p) }

            locationTextViewText = project
                .filter { it.location().isNotNull() }
                .map { it.location() }
                .map { it?.displayableName() ?: "" }

            percentageFundedProgress = project
                .map { it.percentageFunded() }
                .map { ProgressBarUtils.progress(it) }

            percentageFundedProgressBarIsGone = project
                .map { p: Project -> p.isSuccessful || p.isCanceled || p.isFailed || p.isSuspended }

            pledgedTextViewText = project
                .map { p: Project -> ksCurrency.formatWithUserPreference(p.pledged(), p) }

            val userIsCreatorOfProject = project
                .map { it.creator() }
                .compose(Transformers.combineLatestPair(currentUser.observable()))
                .map { creatorAndCurrentUser: Pair<User, KsOptional<User>> ->
                    creatorAndCurrentUser.second.isNotNull() &&
                        creatorAndCurrentUser.first.id() == creatorAndCurrentUser.second?.getValue()?.id()
                }

            projectDisclaimerGoalReachedDateTime = project
                .filter { obj: Project -> obj.isFunded }
                .map { obj: Project -> obj.deadline() }

            projectDisclaimerGoalNotReachedString = project
                .filter { p: Project -> p.deadline() != null && p.isLive && !p.isFunded }
                .map { p: Project -> Pair.create(ksCurrency.format(p.goal(), p), p.deadline()) }

            projectDisclaimerTextViewIsGone =
                project.map { p: Project -> p.deadline() == null || !p.isLive }

            projectLaunchDate = project
                .filter { it.launchedAt().isNotNull() }
                .map { requireNotNull(it.launchedAt()) }
                .map { DateTimeUtils.longDate(it) }

            projectLaunchDateIsGone = project
                .compose(Transformers.combineLatestPair(userIsCreatorOfProject))
                .map { launchDateAndIsCreator: Pair<Project, Boolean> ->
                    launchDateAndIsCreator.first.launchedAt().isNotNull() && launchDateAndIsCreator.second.isTrue()
                }
                .map { it.negate() }

            projectNameTextViewText = project
                .map { it.name() }

            projectOutput = project

            projectSocialTextViewFriends = project
                .filter { it.isFriendBacking }
                .filter { it.friends().isNotNull() }
                .map { requireNotNull(it.friends()) }

            projectSocialImageViewUrl = projectSocialTextViewFriends
                .filter { it.first().isNotNull() }
                .map { requireNotNull(it.first()) }
                .map { it.avatar().medium() }

            projectSocialViewGroupIsGone = project
                .map { it.isFriendBacking }
                .map { it.negate() }

            projectStateViewGroupBackgroundColorInt = project
                .filter { p: Project -> !p.isLive }
                .map { p: Project ->
                    if (p.state() == Project.STATE_SUCCESSFUL) R.color.green_alpha_50
                    else R.color.kds_support_300
                }

            projectStateViewGroupIsGone = project
                .map { it.isLive }

            projectSocialImageViewIsGone = projectSocialViewGroupIsGone
            shouldSetDefaultStatsMargins = projectSocialViewGroupIsGone

            setCanceledProjectStateView = project
                .filter { it.isCanceled }
                .compose(Transformers.ignoreValuesV2())

            setProjectSocialClickListener = project
                .filter { it.isFriendBacking }
                .map { it.friends() }
                .filter { it.size >= 2 }
                .compose(Transformers.ignoreValuesV2())

            setSuccessfulProjectStateView = project
                .filter { it.isSuccessful }
                .filter { !(it.postCampaignPledgingEnabled() ?: false) || !(it.isInPostCampaignPledgingPhase() ?: false) }
                .map { it.stateChangedAt() ?: DateTime() }

            setSuccessfulProjectStillCollectingView = project
                .filter { it.isSuccessful }
                .filter { it.postCampaignPledgingEnabled() ?: false && it.isInPostCampaignPledgingPhase() ?: false }
                .map { it.stateChangedAt() ?: DateTime() }

            setSuspendedProjectStateView = project
                .filter { it.isSuspended }
                .compose(Transformers.ignoreValuesV2())

            setUnsuccessfulProjectStateView = project
                .filter { it.isFailed }
                .map { it.stateChangedAt() ?: DateTime() }

            startProjectSocialActivity = project.compose(
                Transformers.takeWhenV2(
                    projectSocialViewGroupClicked
                )
            )

            updatesCountTextViewText = project
                .filter { it.updatesCount().isNotNull() }
                .map { requireNotNull(it.updatesCount()) }
                .map { NumberUtils.format(it) }

            startCreatorView = projectData
                .compose(Transformers.takePairWhenV2(creatorInfoClicked))
                .map { it.first }

            startCommentsView = projectData
                .compose(Transformers.takePairWhenV2(commentsClicked))
                .map { it.first }

            startUpdatesView = projectData
                .compose(Transformers.takePairWhenV2(updatesClicked))
                .map { it.first }

            startReportProjectView = projectData
                .compose(Transformers.takePairWhenV2(reportProjectButtonClicked))
                .map { it.first }
                .withLatestFrom(this.currentUser.isLoggedIn) { pData, isLoggedIn ->
                    return@withLatestFrom Pair(pData, isLoggedIn)
                }
                .filter { it.second }
                .map { it.first }

            reportProjectButtonClicked
                .withLatestFrom(this.currentUser.isLoggedIn) { _, isUser ->
                    return@withLatestFrom isUser
                }
                .filter { !it }
                .subscribe {
                    this.startLogin.onNext(Unit)
                }
                .addToDisposable(disposables)

            shouldShowProjectFlagged = project
                .map { it.isFlagged() ?: false }
                .compose(Transformers.combineLatestPair(refreshFlagged.startWith("")))
                .map { pair ->
                    val isFlagged = pair.first
                    val shouldRefresh = pair.second

                    if (shouldRefresh.isNotEmpty()) {
                        true
                    } else
                        isFlagged
                }

            shouldShowReportProject = shouldShowProjectFlagged
                .map { !it }

            linkTagClicked
                .map {
                    if (it.contains(OUR_RULES_TAG)) "${environment.webEndpoint()}$OUR_RULES"
                    else if (it.contains(COMMUNITY_GUIDELINES_TAG)) "${environment.webEndpoint()}$COMMUNITY_GUIDELINES"
                    else ""
                }
                .filter { it.isNotEmpty() }
                .subscribe {
                    openExternally.onNext(it)
                }
                .addToDisposable(disposables)

            projectData
                .compose(Transformers.takePairWhenV2(campaignClicked))
                .map { it.first }
                .filter { it.project().isLive && !it.project().isBacking() }
                .subscribe {
                    this.analyticEvents.trackCampaignDetailsCTAClicked(it)
                }
                .addToDisposable(disposables)
        }
        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        @Suppress("UNCHECKED_CAST")
        class Factory(private val environment: Environment) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProjectOverviewViewModel(
                    environment
                ) as T
            }
        }
    }
}
