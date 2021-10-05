package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Category
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.Location
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.ProjectOverviewFragment
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
    }

    interface Outputs {
        /** Emits the creator's avatar photo url for display.  */
        fun avatarPhotoUrl(): Observable<String>

        /** Emits the backers count string for display.  */
        fun backersCountTextViewText(): Observable<String>

        /** Emits when the backing view group should be gone.  */
        fun backingViewGroupIsGone(): Observable<Boolean>

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

        /** Emits root category to display in the featured metadata.  */
        fun featuredTextViewRootCategory(): Observable<String>

        /** Emits the featured view group should be gone.  */
        fun featuredViewGroupIsGone(): Observable<Boolean>

        /** Emits the goal string for display.  */
        fun goalStringForTextView(): Observable<String>

        /** Emits the location for display.  */
        fun locationTextViewText(): Observable<String>

        /** Emits the percentage funded amount for display in the progress bar.  */
        fun percentageFundedProgress(): Observable<Int>

        /** Emits when the progress bar should be gone.  */
        fun percentageFundedProgressBarIsGone(): Observable<Boolean>

        /** Emits when the play button should be gone.  */
        fun playButtonIsGone(): Observable<Boolean>

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

        /** Emits the background drawable for the metadata view group.  */
        fun projectMetadataViewGroupBackgroundDrawableInt(): Observable<Int>

        /** Emits when the metadata view group should be gone.  */
        fun projectMetadataViewGroupIsGone(): Observable<Boolean>

        /** Emits the project name for display.  */
        fun projectNameTextViewText(): Observable<String>

        /** Emits the project for display.  */
        fun projectOutput(): Observable<Project>

        /** Emits the project photo for display.  */
        fun projectPhoto(): Observable<Photo>

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
    }

    class ViewModel(environment: Environment) :
        FragmentViewModel<ProjectOverviewFragment?>(environment), Inputs, Outputs {
        private val apolloClient: ApolloClientType
        private val currentUser: CurrentUserType
        private val ksCurrency: KSCurrency
        private val optimizely: ExperimentsClientType
        val kSString: KSString
        private val projectData = PublishSubject.create<ProjectData>()
        private val projectSocialViewGroupClicked = PublishSubject.create<Void?>()
        private val avatarPhotoUrl: Observable<String>
        private val backersCountTextViewText: Observable<String>
        private val backingViewGroupIsGone: Observable<Boolean>
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
        private val featuredTextViewRootCategory: Observable<String>
        private val featuredViewGroupIsGone: Observable<Boolean>
        private val goalStringForTextView: Observable<String>
        private val locationTextViewText: Observable<String>
        private val percentageFundedProgress: Observable<Int>
        private val percentageFundedProgressBarIsGone: Observable<Boolean>
        private val playButtonIsGone: Observable<Boolean>
        private val pledgedTextViewText: Observable<String>
        private val projectDashboardButtonText: Observable<Int>
        private val projectDashboardContainerIsGone: Observable<Boolean>
        private val projectDisclaimerGoalReachedDateTime: Observable<DateTime>
        private val projectDisclaimerGoalNotReachedString: Observable<Pair<String, DateTime>>
        private val projectDisclaimerTextViewIsGone: Observable<Boolean>
        private val projectLaunchDate: Observable<String>
        private val projectLaunchDateIsGone: Observable<Boolean>
        private val projectMetadataViewGroupBackgroundDrawableInt: Observable<Int>
        private val projectMetadataViewGroupIsGone: Observable<Boolean>
        private val projectNameTextViewText: Observable<String>
        private val projectOutput: Observable<Project>
        private val projectPhoto: Observable<Photo>
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
        private val updatesContainerIsClickable = BehaviorSubject.create<Boolean>()
        private val updatesCountTextViewText: Observable<String>
        val inputs: Inputs = this
        val outputs: Outputs = this
        override fun configureWith(projectData: ProjectData) {
            this.projectData.onNext(projectData)
        }

        override fun projectSocialViewGroupClicked() {
            projectSocialViewGroupClicked.onNext(null)
        }

        override fun avatarPhotoUrl(): Observable<String> {
            return avatarPhotoUrl
        }

        override fun backingViewGroupIsGone(): Observable<Boolean> {
            return backingViewGroupIsGone
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

        override fun featuredTextViewRootCategory(): Observable<String> {
            return featuredTextViewRootCategory
        }

        override fun featuredViewGroupIsGone(): Observable<Boolean> {
            return featuredViewGroupIsGone
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

        override fun playButtonIsGone(): Observable<Boolean> {
            return playButtonIsGone
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

        override fun projectMetadataViewGroupBackgroundDrawableInt(): Observable<Int> {
            return projectMetadataViewGroupBackgroundDrawableInt
        }

        override fun projectMetadataViewGroupIsGone(): Observable<Boolean> {
            return projectMetadataViewGroupIsGone
        }

        override fun projectNameTextViewText(): Observable<String> {
            return projectNameTextViewText
        }

        override fun projectOutput(): Observable<Project> {
            return projectOutput
        }

        override fun projectPhoto(): Observable<Photo> {
            return projectPhoto
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

        init {
            apolloClient = environment.apolloClient()
            currentUser = environment.currentUser()
            ksCurrency = environment.ksCurrency()
            optimizely = environment.optimizely()
            kSString = environment.ksString()
            val project = projectData
                .map { obj: ProjectData -> obj.project() }
            val projectMetadata = project
                .map { project: Project? ->
                    ProjectUtils.metadataForProject(
                        project!!
                    )
                }
            avatarPhotoUrl = project.map { p: Project -> p.creator().avatar().medium() }
            backersCountTextViewText = project.map { obj: Project -> obj.backersCount() }
                .map { value: Int? ->
                    NumberUtils.format(
                        value!!
                    )
                }
            backingViewGroupIsGone = projectMetadata
                .map { other: ProjectUtils.Metadata? -> ProjectUtils.Metadata.BACKING.equals(other) }
                .map { bool: Boolean? ->
                    BooleanUtils.negate(
                        bool!!
                    )
                }
            blurbTextViewText = project.map { obj: Project -> obj.blurb() }
            val projectDataAndCurrentUser = projectData
                .compose(Transformers.combineLatestPair(currentUser.observable()))
            projectDataAndCurrentUser
                .map { projectDataAndUser: Pair<ProjectData, User> ->
                    ExperimentData(
                        projectDataAndUser.second,
                        projectDataAndUser.first
                            .refTagFromIntent(),
                        projectDataAndUser.first.refTagFromCookie()
                    )
                }
                .map { experimentData: ExperimentData? ->
                    optimizely.variant(
                        OptimizelyExperiment.Key.CAMPAIGN_DETAILS,
                        experimentData!!
                    )
                }
                .map { variant: OptimizelyExperiment.Variant? -> variant !== OptimizelyExperiment.Variant.CONTROL }
                .compose(bindToLifecycle())
                .subscribe { v: Boolean -> blurbVariantIsVisible.onNext(v) }
            categoryTextViewText = project.map { obj: Project -> obj.category() }
                .filter { `object`: Category? -> ObjectUtils.isNotNull(`object`) }
                .map { obj: Category? -> obj!!.name() }
            commentsCountTextViewText = project
                .map { obj: Project -> obj.commentsCount() }
                .filter { `object`: Int? -> ObjectUtils.isNotNull(`object`) }
                .map { value: Int? ->
                    NumberUtils.format(
                        value!!
                    )
                }
            conversionTextViewIsGone = project
                .map { pc: Project -> pc.currency() != pc.currentCurrency() }
                .map { bool: Boolean? ->
                    BooleanUtils.negate(
                        bool!!
                    )
                }
            conversionPledgedAndGoalText = project
                .map { p: Project ->
                    val pledged = ksCurrency.format(p.pledged(), p)
                    val goal = ksCurrency.format(p.goal(), p)
                    Pair.create(pledged, goal)
                }
            creatorNameTextViewText = project.map { p: Project -> p.creator().name() }
            val creatorDetailsNotification = project
                .take(1)
                .distinctUntilChanged()
                .map { obj: Project -> obj.slug() }
                .switchMap { slug: String? ->
                    apolloClient.creatorDetails(
                        slug!!
                    )
                        .doOnSubscribe { creatorDetailsLoadingContainerIsVisible.onNext(true) }
                        .doAfterTerminate { creatorDetailsLoadingContainerIsVisible.onNext(false) }
                        .materialize()
                }
                .share()
            creatorDetailsNotification
                .compose(Transformers.errors())
                .map { _: Throwable? -> false }
                .compose(bindToLifecycle())
                .subscribe { v: Boolean -> creatorDetailsVariantIsVisible.onNext(v) }
            val creatorDetails = creatorDetailsNotification
                .compose(Transformers.values())
            creatorDetails
                .map { details: CreatorDetails ->
                    Pair.create(
                        details.backingsCount(),
                        details.launchedProjectsCount()
                    )
                }
                .compose(bindToLifecycle())
                .subscribe { v: Pair<Int, Int> -> creatorBackedAndLaunchedProjectsCount.onNext(v) }
            creatorDetails
                .compose(Transformers.combineLatestPair(projectDataAndCurrentUser))
                .take(1)
                .map { cosa: Pair<CreatorDetails, Pair<ProjectData, User>> -> cosa.second }
                .map { projectDataAndUser: Pair<ProjectData, User> ->
                    ExperimentData(
                        projectDataAndUser.second,
                        projectDataAndUser.first
                            .refTagFromIntent(),
                        projectDataAndUser.first.refTagFromCookie()
                    )
                }
                .map { experimentData: ExperimentData? ->
                    optimizely.variant(
                        OptimizelyExperiment.Key.CREATOR_DETAILS,
                        experimentData!!
                    )
                }
                .map { variant: OptimizelyExperiment.Variant? -> variant !== OptimizelyExperiment.Variant.CONTROL }
                .compose(bindToLifecycle())
                .subscribe { v: Boolean -> creatorDetailsVariantIsVisible.onNext(v) }
            deadlineCountdownTextViewText = project.map { project: Project? ->
                ProjectUtils.deadlineCountdownValue(
                    project!!
                )
            }.map { value: Int? ->
                NumberUtils.format(
                    value!!
                )
            }
            featuredViewGroupIsGone = projectMetadata
                .map { other: ProjectUtils.Metadata? ->
                    ProjectUtils.Metadata.CATEGORY_FEATURED.equals(
                        other
                    )
                }
                .map { bool: Boolean? ->
                    BooleanUtils.negate(
                        bool!!
                    )
                }
            featuredTextViewRootCategory = featuredViewGroupIsGone
                .filter { bool: Boolean? -> BooleanUtils.isFalse(bool) }
                .compose(Transformers.combineLatestPair(project))
                .map { bp: Pair<Boolean?, Project> -> bp.second.category() }
                .filter { `object`: Category? -> ObjectUtils.isNotNull(`object`) }
                .map { obj: Category? -> obj!!.root() }
                .filter { `object`: Category? -> ObjectUtils.isNotNull(`object`) }
                .map { obj: Category -> obj.name() }
            goalStringForTextView = project
                .map { p: Project -> ksCurrency.formatWithUserPreference(p.goal(), p) }
            locationTextViewText = project
                .map { obj: Project -> obj.location() }
                .filter { `object`: Location? -> ObjectUtils.isNotNull(`object`) }
                .map { obj: Location? -> obj!!.displayableName() }
            percentageFundedProgress = project.map { obj: Project -> obj.percentageFunded() }
                .map { value: Float? -> ProgressBarUtils.progress(value!!) }
            percentageFundedProgressBarIsGone = project
                .map { p: Project -> p.isSuccessful || p.isCanceled || p.isFailed || p.isSuspended }
            playButtonIsGone = project.map { obj: Project -> obj.hasVideo() }
                .map { bool: Boolean? ->
                    BooleanUtils.negate(
                        bool!!
                    )
                }
            pledgedTextViewText = project
                .map { p: Project -> ksCurrency.formatWithUserPreference(p.pledged(), p) }
            val userIsCreatorOfProject = project
                .map { obj: Project -> obj.creator() }
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
                .map { bool: Boolean? ->
                    BooleanUtils.negate(
                        bool!!
                    )
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
                .map { obj: Project -> obj.launchedAt() }
                .filter { `object`: DateTime? -> ObjectUtils.isNotNull(`object`) }
                .map { dateTime: DateTime? ->
                    DateTimeUtils.longDate(
                        dateTime!!
                    )
                }
            projectLaunchDateIsGone = project
                .map { obj: Project -> obj.launchedAt() }
                .compose(Transformers.combineLatestPair(userIsCreatorOfProject))
                .map { launchDateAndIsCreator: Pair<DateTime?, Boolean?> ->
                    ObjectUtils.isNotNull(
                        launchDateAndIsCreator.first
                    ) && BooleanUtils.isTrue(launchDateAndIsCreator.second)
                }
                .map { bool: Boolean? ->
                    BooleanUtils.negate(
                        bool!!
                    )
                }
            projectMetadataViewGroupBackgroundDrawableInt = projectMetadata
                .filter { other: ProjectUtils.Metadata? ->
                    ProjectUtils.Metadata.BACKING.equals(
                        other
                    )
                }
                .map { pm: ProjectUtils.Metadata? -> R.drawable.rect_green_grey_stroke }
            projectMetadataViewGroupIsGone = projectMetadata
                .map { m: ProjectUtils.Metadata? -> m != ProjectUtils.Metadata.CATEGORY_FEATURED && m != ProjectUtils.Metadata.BACKING }
            projectNameTextViewText = project.map { obj: Project -> obj.name() }
            projectOutput = project
            projectPhoto = project.map { obj: Project -> obj.photo() }
            projectSocialImageViewUrl = project
                .filter { obj: Project -> obj.isFriendBacking }
                .map { obj: Project -> obj.friends() }
                .map { xs: List<User> -> ListUtils.first(xs) }
                .map { f: User? -> f!!.avatar().small() }
            projectSocialTextViewFriends = project
                .filter { obj: Project -> obj.isFriendBacking }
                .map { obj: Project -> obj.friends() }
            projectSocialViewGroupIsGone = project.map { obj: Project -> obj.isFriendBacking }
                .map { bool: Boolean? ->
                    BooleanUtils.negate(
                        bool!!
                    )
                }
            projectStateViewGroupBackgroundColorInt = project
                .filter { p: Project -> !p.isLive }
                .map { p: Project -> if (p.state() == Project.STATE_SUCCESSFUL) R.color.green_alpha_50 else R.color.kds_support_300 }
            projectStateViewGroupIsGone = project.map { obj: Project -> obj.isLive }
            projectSocialImageViewIsGone = projectSocialViewGroupIsGone
            shouldSetDefaultStatsMargins = projectSocialViewGroupIsGone
            setCanceledProjectStateView = project.filter { obj: Project -> obj.isCanceled }
                .compose(Transformers.ignoreValues())
            setProjectSocialClickListener = project
                .filter { obj: Project -> obj.isFriendBacking }
                .map { obj: Project -> obj.friends() }
                .filter { fs: List<User>? -> fs!!.size > 2 }
                .compose(Transformers.ignoreValues())
            setSuccessfulProjectStateView = project
                .filter { obj: Project -> obj.isSuccessful }
                .map { p: Project -> ObjectUtils.coalesce(p.stateChangedAt(), DateTime()) }
            setSuspendedProjectStateView = project.filter { obj: Project -> obj.isSuspended }
                .compose(Transformers.ignoreValues())
            setUnsuccessfulProjectStateView = project
                .filter { obj: Project -> obj.isFailed }
                .map { p: Project -> ObjectUtils.coalesce(p.stateChangedAt(), DateTime()) }
            startProjectSocialActivity = project.compose(
                Transformers.takeWhen(
                    projectSocialViewGroupClicked
                )
            )
            updatesCountTextViewText = project
                .map { obj: Project -> obj.updatesCount() }
                .filter { `object`: Int? -> ObjectUtils.isNotNull(`object`) }
                .map { value: Int? ->
                    NumberUtils.format(
                        value!!
                    )
                }
        }
    }
}
