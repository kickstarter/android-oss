package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.ProjectOverviewFragment
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES_TAG
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.OUR_RULES
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.OUR_RULES_TAG
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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

        /** Call when the creator dashboard is clicked  */
        fun creatorDashboardClicked()

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

        /** Emits a boolean determining if the variant blurb should be visible.  */
        fun blurbVariantIsVisible(): Observable<Boolean>

        /** Emits the project category for display.  */
        fun categoryTextViewText(): Observable<String>

        /** Emits the comments count for display.  */
        fun commentsCountTextViewText(): Observable<String>

        /** Emits the usd conversion text for display.  */
        fun conversionPledgedAndGoalText(): Observable<Pair<String, String>>

        /** Emits when the usd conversion view should be gone.  */
        fun conversionTextViewIsGone(): Observable<Boolean>

        /** Emits the count of the project creator's backed and launched projects.  */
        fun creatorBackedAndLaunchedProjectsCount(): Observable<Pair<Int, Int>>

        /** Emits a boolean determining if the creator details loading container should be visible.  */
        fun creatorDetailsLoadingContainerIsVisible(): Observable<Boolean>

        /** Emits a boolean determining if the variant creator details should be visible.  */
        fun creatorDetailsVariantIsVisible(): Observable<Boolean>

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

        /** Emits the string resource ID of the project dashboard button.  */
        fun projectDashboardButtonText(): Observable<Int>

        /** Emits a boolean determining if the project dashboard container should be visible.  */
        fun projectDashboardContainerIsGone(): Observable<Boolean>

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
        fun setCanceledProjectStateView(): Observable<Void>

        /** Emits when we should set an on click listener to the social view.  */
        fun setProjectSocialClickListener(): Observable<Void>

        /** Emits when we should set the successful state view.  */
        fun setSuccessfulProjectStateView(): Observable<DateTime>

        /** Emits when we should set the suspended state view.  */
        fun setSuspendedProjectStateView(): Observable<Void>

        /** Emits when we should set the unsuccessful state view.  */
        fun setUnsuccessfulProjectStateView(): Observable<DateTime>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectSocialActivity].  */
        fun startProjectSocialActivity(): Observable<Project>

        /** Emits the updates count for display.  */
        fun updatesCountTextViewText(): Observable<String>

        fun startCreatorView(): Observable<ProjectData>
        fun startCommentsView(): Observable<ProjectData>
        fun startUpdatesView(): Observable<ProjectData>
        fun startCampaignView(): Observable<ProjectData>
        fun startCreatorDashboardView(): Observable<ProjectData>
        fun startReportProjectView(): Observable<ProjectData>
        fun startLoginView(): Observable<Void>
        fun shouldShowReportProject(): Observable<Boolean>
        fun shouldShowProjectFlagged(): Observable<Boolean>
        fun openExternallyWithUrl(): Observable<String>
    }

    class ViewModel(environment: Environment) : FragmentViewModel<ProjectOverviewFragment?>(environment), Inputs, Outputs {

        private val apolloClient = requireNotNull(environment.apolloClient())
        private val currentUser = requireNotNull(environment.currentUser())
        private val ksCurrency = requireNotNull(environment.ksCurrency())
        private val optimizely = environment.optimizely()
        val kSString = requireNotNull(environment.ksString())

        // Inputs
        private val projectData = PublishSubject.create<ProjectData>()
        private val projectSocialViewGroupClicked = PublishSubject.create<Void>()
        private val creatorInfoClicked = PublishSubject.create<Void>()
        private val campaignClicked = PublishSubject.create<Void>()
        private val commentsClicked = PublishSubject.create<Void>()
        private val updatesClicked = PublishSubject.create<Void>()
        private val creatorDashboardClicked = PublishSubject.create<Void>()
        private val reportProjectButtonClicked = PublishSubject.create<Void>()
        private val refreshFlagged = PublishSubject.create<String>()
        private val linkTagClicked = PublishSubject.create<String>()

        // Outputs
        private val avatarPhotoUrl: Observable<String>
        private val backersCountTextViewText: Observable<String>
        private val blurbTextViewText: Observable<String>
        private val blurbVariantIsVisible = BehaviorSubject.create<Boolean>()
        private val categoryTextViewText: Observable<String>
        private val commentsCountTextViewText: Observable<String>
        private val conversionPledgedAndGoalText: Observable<Pair<String, String>>
        private val conversionTextViewIsGone: Observable<Boolean>
        private val creatorBackedAndLaunchedProjectsCount = BehaviorSubject.create<Pair<Int, Int>>()
        private val creatorDetailsLoadingContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val creatorDetailsVariantIsVisible = BehaviorSubject.create<Boolean>()
        private val creatorNameTextViewText: Observable<String>
        private val deadlineCountdownTextViewText: Observable<String>
        private val goalStringForTextView: Observable<String>
        private val locationTextViewText: Observable<String>
        private val percentageFundedProgress: Observable<Int>
        private val percentageFundedProgressBarIsGone: Observable<Boolean>
        private val pledgedTextViewText: Observable<String>
        private val projectDashboardButtonText: Observable<Int>
        private val projectDashboardContainerIsGone: Observable<Boolean>
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
        private val setCanceledProjectStateView: Observable<Void>
        private val setProjectSocialClickListener: Observable<Void>
        private val setSuccessfulProjectStateView: Observable<DateTime>
        private val setSuspendedProjectStateView: Observable<Void>
        private val setUnsuccessfulProjectStateView: Observable<DateTime>
        private val startProjectSocialActivity: Observable<Project>
        private val shouldSetDefaultStatsMargins: Observable<Boolean>
        private val updatesCountTextViewText: Observable<String>
        private val startCreatorView: Observable<ProjectData>
        private val startCommentsView: Observable<ProjectData>
        private val startUpdatesView: Observable<ProjectData>
        private val startCampaignView: Observable<ProjectData>
        private val creatorDashBoardView: Observable<ProjectData>
        private val startReportProjectView: Observable<ProjectData>
        private val startLogin = PublishSubject.create<Void>()
        private val shouldShowReportProject: Observable<Boolean>
        private val shouldShowProjectFlagged: Observable<Boolean>
        private val openExternally = PublishSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        // - Inputs
        override fun configureWith(projectData: ProjectData) = this.projectData.onNext(projectData)

        override fun projectSocialViewGroupClicked() = projectSocialViewGroupClicked.onNext(null)

        override fun creatorInfoButtonClicked() = this.creatorInfoClicked.onNext(null)

        override fun campaignButtonClicked() = this.campaignClicked.onNext(null)

        override fun commentsButtonClicked() = this.commentsClicked.onNext(null)

        override fun updatesButtonClicked() = this.updatesClicked.onNext(null)

        override fun creatorDashboardClicked() = this.creatorDashboardClicked.onNext(null)

        override fun reportProjectButtonClicked() = this.reportProjectButtonClicked.onNext(null)

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

        override fun blurbVariantIsVisible(): Observable<Boolean> {
            return blurbVariantIsVisible
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

        override fun creatorBackedAndLaunchedProjectsCount(): Observable<Pair<Int, Int>> {
            return creatorBackedAndLaunchedProjectsCount
        }

        override fun creatorDetailsLoadingContainerIsVisible(): Observable<Boolean> {
            return creatorDetailsLoadingContainerIsVisible
        }

        override fun creatorDetailsVariantIsVisible(): Observable<Boolean> {
            return creatorDetailsVariantIsVisible
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

        override fun projectDashboardButtonText(): Observable<Int> {
            return projectDashboardButtonText
        }

        override fun projectDashboardContainerIsGone(): Observable<Boolean> {
            return projectDashboardContainerIsGone
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

        override fun setCanceledProjectStateView(): Observable<Void> {
            return setCanceledProjectStateView
        }

        override fun setProjectSocialClickListener(): Observable<Void> {
            return setProjectSocialClickListener
        }

        override fun setSuccessfulProjectStateView(): Observable<DateTime> {
            return setSuccessfulProjectStateView
        }

        override fun setSuspendedProjectStateView(): Observable<Void> {
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

        override fun startCampaignView(): Observable<ProjectData> {
            return this.startCampaignView
        }

        override fun startUpdatesView(): Observable<ProjectData> {
            return this.startUpdatesView
        }

        override fun startCreatorDashboardView(): Observable<ProjectData> {
            return this.creatorDashBoardView
        }

        override fun startReportProjectView(): Observable<ProjectData> {
            return this.startReportProjectView
        }

        override fun startLoginView(): Observable<Void> {
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
                .map { it.project() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            avatarPhotoUrl = project
                .map { it.creator().avatar().medium() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            backersCountTextViewText = project
                .map { NumberUtils.format(it.backersCount()) }

            blurbTextViewText = project
                .map { it.blurb() }

            val projectDataAndCurrentUser = projectData
                .compose(Transformers.combineLatestPair(currentUser.observable()))

            projectDataAndCurrentUser
                .map { projectDataAndUser ->
                    ExperimentData(
                        projectDataAndUser.second,
                        projectDataAndUser.first.refTagFromIntent(),
                        projectDataAndUser.first.refTagFromCookie()
                    )
                }
                .map { experimentData ->
                    optimizely?.variant(
                        OptimizelyExperiment.Key.CAMPAIGN_DETAILS,
                        experimentData
                    )
                }
                .map { variant -> variant !== OptimizelyExperiment.Variant.CONTROL }
                .compose(bindToLifecycle())
                .subscribe { blurbVariantIsVisible.onNext(it) }

            categoryTextViewText = project
                .map { it.category() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { it?.name() ?: "" }

            commentsCountTextViewText = project
                .map { it.commentsCount() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
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
                .compose(Transformers.errors())
                .map { _: Throwable? -> false }
                .compose(bindToLifecycle())
                .subscribe { creatorDetailsVariantIsVisible.onNext(it) }

            val creatorDetails = creatorDetailsNotification
                .compose(Transformers.values())

            creatorDetails
                .map { details ->
                    Pair.create(
                        details.backingsCount(),
                        details.launchedProjectsCount()
                    )
                }
                .compose(bindToLifecycle())
                .subscribe { creatorBackedAndLaunchedProjectsCount.onNext(it) }

            creatorDetails
                .compose(Transformers.combineLatestPair(projectDataAndCurrentUser))
                .take(1)
                .map { it.second }
                .map { projectDataAndUser: Pair<ProjectData, User> ->
                    ExperimentData(
                        projectDataAndUser.second,
                        projectDataAndUser.first
                            .refTagFromIntent(),
                        projectDataAndUser.first.refTagFromCookie()
                    )
                }
                .map { experimentData: ExperimentData? ->
                    optimizely?.variant(
                        OptimizelyExperiment.Key.CREATOR_DETAILS,
                        experimentData!!
                    )
                }
                .map { variant -> variant !== OptimizelyExperiment.Variant.CONTROL }
                .compose(bindToLifecycle())
                .subscribe { creatorDetailsVariantIsVisible.onNext(it) }

            deadlineCountdownTextViewText = project
                .map { proj -> proj.deadlineCountdownValue() }
                .map { NumberUtils.format(it) }

            goalStringForTextView = project
                .map { p: Project -> ksCurrency.formatWithUserPreference(p.goal(), p) }

            locationTextViewText = project
                .map { it.location() }
                .filter { ObjectUtils.isNotNull(it) }
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
                .map { creatorAndCurrentUser: Pair<User, User> ->
                    ObjectUtils.isNotNull(
                        creatorAndCurrentUser.second
                    ) && creatorAndCurrentUser.first.id() == creatorAndCurrentUser.second.id()
                }

            projectDashboardButtonText = project
                .map { obj: Project -> obj.isLive }
                .map { live: Boolean -> if (live) R.string.View_progress else R.string.View_dashboard }
                .compose(Transformers.combineLatestPair(userIsCreatorOfProject))
                .filter { buttonTextAndIsCreator: Pair<Int, Boolean?> -> buttonTextAndIsCreator.second }
                .map { buttonTextAndIsCreator: Pair<Int, Boolean?> -> buttonTextAndIsCreator.first }

            projectDashboardContainerIsGone = userIsCreatorOfProject
                .map { it.negate() }

            projectDisclaimerGoalReachedDateTime = project
                .filter { obj: Project -> obj.isFunded }
                .map { obj: Project -> obj.deadline() }
                .compose(bindToLifecycle())

            projectDisclaimerGoalNotReachedString = project
                .filter { p: Project -> p.deadline() != null && p.isLive && !p.isFunded }
                .map { p: Project -> Pair.create(ksCurrency.format(p.goal(), p), p.deadline()) }

            projectDisclaimerTextViewIsGone =
                project.map { p: Project -> p.deadline() == null || !p.isLive }

            projectLaunchDate = project
                .map { it.launchedAt() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .map { DateTimeUtils.longDate(it) }

            projectLaunchDateIsGone = project
                .map { it.launchedAt() }
                .compose(Transformers.combineLatestPair(userIsCreatorOfProject))
                .map { launchDateAndIsCreator: Pair<DateTime?, Boolean?> ->
                    ObjectUtils.isNotNull(
                        launchDateAndIsCreator.first
                    ) && launchDateAndIsCreator.second.isTrue()
                }
                .map { it.negate() }

            projectNameTextViewText = project
                .map { it.name() }

            projectOutput = project

            projectSocialTextViewFriends = project
                .filter { it.isFriendBacking }
                .map { it.friends() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            projectSocialImageViewUrl = projectSocialTextViewFriends
                .map { it.first() }
                .map { requireNotNull(it) }
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
                .compose(Transformers.ignoreValues())

            setProjectSocialClickListener = project
                .filter { it.isFriendBacking }
                .map { it.friends() }
                .filter { it.size > 2 }
                .compose(Transformers.ignoreValues())

            setSuccessfulProjectStateView = project
                .filter { it.isSuccessful }
                .map { ObjectUtils.coalesce(it.stateChangedAt(), DateTime()) }

            setSuspendedProjectStateView = project
                .filter { it.isSuspended }
                .compose(Transformers.ignoreValues())

            setUnsuccessfulProjectStateView = project
                .filter { it.isFailed }
                .map { ObjectUtils.coalesce(it.stateChangedAt(), DateTime()) }

            startProjectSocialActivity = project.compose(
                Transformers.takeWhen(
                    projectSocialViewGroupClicked
                )
            )

            updatesCountTextViewText = project
                .map { it.updatesCount() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .map { NumberUtils.format(it) }

            startCreatorView = projectData
                .compose(Transformers.takePairWhen(creatorInfoClicked))
                .map { it.first }

            startCommentsView = projectData
                .compose(Transformers.takePairWhen(commentsClicked))
                .map { it.first }

            startUpdatesView = projectData
                .compose(Transformers.takePairWhen(updatesClicked))
                .map { it.first }

            startCampaignView = projectData
                .compose(Transformers.takePairWhen(campaignClicked))
                .map { it.first }

            creatorDashBoardView = projectData
                .compose(Transformers.takePairWhen(creatorDashboardClicked))
                .map { it.first }

            startReportProjectView = projectData
                .compose(Transformers.takePairWhen(reportProjectButtonClicked))
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
                .compose(bindToLifecycle())
                .subscribe {
                    this.startLogin.onNext(null)
                }

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
                .compose(bindToLifecycle())
                .subscribe {
                    openExternally.onNext(it)
                }

            projectData
                .compose(Transformers.takePairWhen(campaignClicked))
                .map { it.first }
                .filter { it.project().isLive && !it.project().isBacking() }
                .compose(bindToLifecycle())
                .subscribe {
                    this.analyticEvents.trackCampaignDetailsCTAClicked(it)
                }

            projectData
                .compose(Transformers.takePairWhen(creatorDashboardClicked))
                .map { it.first }
                .compose(bindToLifecycle())
                .subscribe {
                    this.analyticEvents.trackCreatorDetailsCTA(it)
                }
        }
    }
}
