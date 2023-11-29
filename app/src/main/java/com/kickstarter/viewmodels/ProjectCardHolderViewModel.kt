package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.extensions.ProjectMetadata
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.libs.utils.extensions.isCompleted
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.metadataForProject
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.models.extensions.replaceSmallImageWithMediumIfEmpty
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.Editorial
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

interface ProjectCardHolderViewModel {
    interface Inputs {
        /** Call to configure view model with a project and current discovery params.  */
        fun configureWith(projectAndDiscoveryParams: Pair<Project, DiscoveryParams>)

        /** Call when the project card has been clicked.  */
        fun projectCardClicked()

        /** Call when the heart button is clicked.  */
        fun heartButtonClicked()
    }

    interface Outputs {
        /** Emits the project's number of backers.  */
        fun backersCountTextViewText(): Observable<String>

        /** Emits to determine if backing view should be shown.  */
        fun backingViewGroupIsGone(): Observable<Boolean>

        /** Emits the a string representing how much time the project has remaining.  */
        fun deadlineCountdownText(): Observable<String>

        /** Emits to determine if featured view should be shown.  */
        fun featuredViewGroupIsGone(): Observable<Boolean>

        /** Emits list of friends who have also backed this project.  */
        fun friendsForNamepile(): Observable<List<User>>

        /** Emits to determine if second face in facepile should be shown.  */
        fun friendAvatar2IsGone(): Observable<Boolean>

        /** Emits to determine if third face in facepile should be shown.  */
        fun friendAvatar3IsGone(): Observable<Boolean>

        /** Emits URL string of first friend's avatar.  */
        fun friendAvatarUrl1(): Observable<String>

        /** Emits URL string of second friend's avatar.  */
        fun friendAvatarUrl2(): Observable<String>

        /** Emits URL string of third friend's avatar.  */
        fun friendAvatarUrl3(): Observable<String>

        /** Emits to determine if project has a photo to display.  */
        fun imageIsInvisible(): Observable<Boolean>

        /** Emits to determine if friends who have also backed should be shown.  */
        fun friendBackingViewIsHidden(): Observable<Boolean>

        /** Emits to determine if successful funding state should be shown.  */
        fun fundingSuccessfulViewGroupIsGone(): Observable<Boolean>

        /** Emits to determine if unsuccessful funding state should be shown.  */
        fun fundingUnsuccessfulViewGroupIsGone(): Observable<Boolean>

        /** Emits a Boolean determining if the project's location should be shown.  */
        fun locationContainerIsGone(): Observable<Boolean>

        /** Emits the displayable name of the location of the project.  */
        fun locationName(): Observable<String>

        /** Emits to determine if metadata container should be shown.  */
        fun metadataViewGroupIsGone(): Observable<Boolean>

        /** Emits background drawable resource ID of metadata container.  */
        fun metadataViewGroupBackgroundDrawable(): Observable<Int>

        /** Emits project to be used for calculating countdown.  */
        fun projectForDeadlineCountdownDetail(): Observable<Project>

        /** Emits percentage representing project funding.  */
        fun percentageFundedForProgressBar(): Observable<Int>

        /** Emits to determine if funded progress bar should be shown.  */
        fun percentageFundedProgressBarIsGone(): Observable<Boolean>

        /** Emits string representation of project funding percentage.  */
        fun percentageFundedTextViewText(): Observable<String>

        /** Emits URL string of project cover photo.  */
        fun photoUrl(): Observable<String>

        /** Emits project name and blurb.  */
        fun nameAndBlurbText(): Observable<Pair<String, String>>

        /** Emits when project card is clicked.  */
        fun notifyDelegateOfProjectClick(): Observable<Project>

        /** Emits time project was canceled.  */
        fun projectCanceledAt(): Observable<DateTime>

        /** Emits to determine if stats container should be shown.  */
        fun projectCardStatsViewGroupIsGone(): Observable<Boolean>

        /** Emits time project was unsuccessfully funded.  */
        fun projectFailedAt(): Observable<DateTime>

        /** Emits to determine if state container should be shown.  */
        fun projectStateViewGroupIsGone(): Observable<Boolean>

        /** Emits to determine if project (sub)category tag should be shown.  */
        fun projectSubcategoryIsGone(): Observable<Boolean>

        /** Emits project (sub)category.  */
        fun projectSubcategoryName(): Observable<String>

        /** Emits time project was successfully funded.  */
        fun projectSuccessfulAt(): Observable<DateTime>

        /** Emits time project was suspended.  */
        fun projectSuspendedAt(): Observable<DateTime>

        /** Emits to determine if project tags container should be shown.  */
        fun projectTagContainerIsGone(): Observable<Boolean>

        /** Emits to determine if project we love tag container should be shown.  */
        fun projectWeLoveIsGone(): Observable<Boolean>

        /** Emits project's root category.  */
        fun rootCategoryNameForFeatured(): Observable<String>

        /** Emits to determine if saved container should shown.  */
        fun savedViewGroupIsGone(): Observable<Boolean>

        fun comingSoonViewGroupIsGone(): Observable<Boolean>

        /** Emits to determine if padding should be added to top of view.  */
        fun setDefaultTopPadding(): Observable<Boolean>

        /** Emits a drawable id that corresponds to whether the project is saved. */
        fun heartDrawableId(): Observable<Int>

        /** Emits the current [Project] to Toggle save  */
        fun notifyDelegateOfHeartButtonClicked(): Observable<Project>
    }

    class ViewModel : Inputs, Outputs {
        private fun shouldShowLocationTag(params: DiscoveryParams): Boolean {
            return params.tagId() != null && params.tagId() == Editorial.LIGHTS_ON.tagId
        }

        private fun areParamsAllOrSameCategoryAsProject(categoryPair: Pair<Category, Category>): Boolean {
            return categoryPair.first.isNotNull() && categoryPair.first?.id() == categoryPair.second.id()
        }

        private val heartButtonClicked = PublishSubject.create<Unit>()
        private val discoveryParams = PublishSubject.create<DiscoveryParams?>()
        private val project = PublishSubject.create<Project>()
        private val projectCardClicked = PublishSubject.create<Unit>()
        private val backersCountTextViewText: Observable<String>
        private val backingViewGroupIsGone: Observable<Boolean>
        private val deadlineCountdownText: Observable<String>
        private val featuredViewGroupIsGone: Observable<Boolean>
        private val friendAvatar2IsGone: Observable<Boolean>
        private val friendAvatar3IsGone: Observable<Boolean>
        private val friendAvatarUrl1: Observable<String>
        private val friendAvatarUrl2: Observable<String>
        private val friendAvatarUrl3: Observable<String>
        private val friendBackingViewIsHidden: Observable<Boolean>
        private val friendsForNamepile: Observable<List<User>>
        private val fundingSuccessfulViewGroupIsGone: Observable<Boolean>
        private val fundingUnsuccessfulViewGroupIsGone: Observable<Boolean>
        private val imageIsInvisible: Observable<Boolean>
        private val locationName = BehaviorSubject.create<String>()
        private val locationContainerIsGone = BehaviorSubject.create<Boolean>()
        private val metadataViewGroupBackground: Observable<Int>
        private val metadataViewGroupIsGone: Observable<Boolean>
        private val nameAndBlurbText: Observable<Pair<String, String>>
        private val notifyDelegateOfProjectClick: Observable<Project>
        private val percentageFundedForProgressBar: Observable<Int>
        private val percentageFundedProgressBarIsGone: Observable<Boolean>
        private val percentageFundedTextViewText: Observable<String>
        private val photoUrl: Observable<String>
        private val projectForDeadlineCountdownDetail: Observable<Project>
        private val projectCardStatsViewGroupIsGone: Observable<Boolean>
        private val projectStateViewGroupIsGone: Observable<Boolean>
        private val projectCanceledAt: Observable<DateTime>
        private val projectFailedAt: Observable<DateTime>
        private val projectSubcategoryName: Observable<String>
        private val projectSubcategoryIsGone: Observable<Boolean>
        private val projectSuccessfulAt: Observable<DateTime>
        private val projectSuspendedAt: Observable<DateTime>
        private val projectTagContainerIsGone: Observable<Boolean>
        private val projectWeLoveIsGone: Observable<Boolean>
        private val rootCategoryNameForFeatured: Observable<String>
        private val savedViewGroupIsGone: Observable<Boolean>
        private val comingSoonViewGroupIsGone: Observable<Boolean>
        private val setDefaultTopPadding: Observable<Boolean>
        private val heartDrawableId = BehaviorSubject.create<Int>()
        private val notifyDelegateOfHeartButtonClicked = BehaviorSubject.create<Project>()

        private val disposables = CompositeDisposable()

        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this
        override fun configureWith(projectAndDiscoveryParams: Pair<Project, DiscoveryParams>) {
            project.onNext(projectAndDiscoveryParams.first)
            discoveryParams.onNext(projectAndDiscoveryParams.second)
        }

        override fun heartButtonClicked() {
            this.heartButtonClicked.onNext(Unit)
        }

        override fun projectCardClicked() {
            projectCardClicked.onNext(Unit)
        }

        override fun heartDrawableId(): Observable<Int> = this.heartDrawableId
        override fun backersCountTextViewText(): Observable<String> = backersCountTextViewText
        override fun backingViewGroupIsGone(): Observable<Boolean> = backingViewGroupIsGone
        override fun deadlineCountdownText(): Observable<String> = deadlineCountdownText
        override fun featuredViewGroupIsGone(): Observable<Boolean> = featuredViewGroupIsGone
        override fun friendAvatar2IsGone(): Observable<Boolean> = friendAvatar2IsGone
        override fun friendAvatar3IsGone(): Observable<Boolean> = friendAvatar3IsGone
        override fun friendAvatarUrl1(): Observable<String> = friendAvatarUrl1
        override fun friendAvatarUrl2(): Observable<String> = friendAvatarUrl2
        override fun friendAvatarUrl3(): Observable<String> = friendAvatarUrl3
        override fun friendBackingViewIsHidden(): Observable<Boolean> = friendBackingViewIsHidden
        override fun friendsForNamepile(): Observable<List<User>> = friendsForNamepile
        override fun fundingSuccessfulViewGroupIsGone(): Observable<Boolean> =
            fundingSuccessfulViewGroupIsGone

        override fun fundingUnsuccessfulViewGroupIsGone(): Observable<Boolean> =
            fundingUnsuccessfulViewGroupIsGone

        override fun imageIsInvisible(): Observable<Boolean> = imageIsInvisible
        override fun locationContainerIsGone(): Observable<Boolean> = locationContainerIsGone
        override fun locationName(): Observable<String> = locationName
        override fun metadataViewGroupBackgroundDrawable(): Observable<Int> =
            metadataViewGroupBackground

        override fun metadataViewGroupIsGone(): Observable<Boolean> = metadataViewGroupIsGone
        override fun nameAndBlurbText(): Observable<Pair<String, String>> = nameAndBlurbText
        override fun notifyDelegateOfProjectClick(): Observable<Project> =
            notifyDelegateOfProjectClick

        override fun percentageFundedForProgressBar(): Observable<Int> =
            percentageFundedForProgressBar

        override fun percentageFundedProgressBarIsGone(): Observable<Boolean> =
            percentageFundedProgressBarIsGone

        override fun percentageFundedTextViewText(): Observable<String> =
            percentageFundedTextViewText

        override fun photoUrl(): Observable<String> = photoUrl
        override fun projectCardStatsViewGroupIsGone(): Observable<Boolean> =
            projectCardStatsViewGroupIsGone

        override fun projectForDeadlineCountdownDetail(): Observable<Project> =
            projectForDeadlineCountdownDetail

        override fun projectStateViewGroupIsGone(): Observable<Boolean> =
            projectStateViewGroupIsGone

        override fun projectSubcategoryIsGone(): Observable<Boolean> = projectSubcategoryIsGone
        override fun projectSubcategoryName(): Observable<String> = projectSubcategoryName
        override fun projectCanceledAt(): Observable<DateTime> = projectCanceledAt
        override fun projectFailedAt(): Observable<DateTime> = projectFailedAt
        override fun projectSuccessfulAt(): Observable<DateTime> = projectSuccessfulAt
        override fun projectSuspendedAt(): Observable<DateTime> = projectSuspendedAt
        override fun projectTagContainerIsGone(): Observable<Boolean> = projectTagContainerIsGone
        override fun projectWeLoveIsGone(): Observable<Boolean> = projectWeLoveIsGone
        override fun rootCategoryNameForFeatured(): Observable<String> = rootCategoryNameForFeatured
        override fun setDefaultTopPadding(): Observable<Boolean> = setDefaultTopPadding
        override fun savedViewGroupIsGone(): Observable<Boolean> = savedViewGroupIsGone
        override fun comingSoonViewGroupIsGone(): Observable<Boolean> = comingSoonViewGroupIsGone
        override fun notifyDelegateOfHeartButtonClicked(): Observable<Project> =
            this.notifyDelegateOfHeartButtonClicked

        init {
            projectForDeadlineCountdownDetail = project
            backersCountTextViewText = project
                .map { it.backersCount() }
                .map { value ->
                    NumberUtils.format(
                        value,
                    )
                }

            backingViewGroupIsGone = project
                .map { it.metadataForProject() !== ProjectMetadata.BACKING }

            comingSoonViewGroupIsGone = project
                .map { it.metadataForProject() !== ProjectMetadata.COMING_SOON }

            deadlineCountdownText = project
                .map { it.deadlineCountdownValue() }
                .map {
                    NumberUtils.format(it)
                }

            project
                .map { p -> if (p.isStarred()) R.drawable.icon__heart else R.drawable.icon__heart_outline }
                .subscribe { this.heartDrawableId.onNext(it) }
                .addToDisposable(disposables)

            project
                .compose(Transformers.takeWhenV2(heartButtonClicked))
                .filter { it.isNotNull() }
                .subscribe { notifyDelegateOfHeartButtonClicked.onNext(it) }
                .addToDisposable(disposables)

            featuredViewGroupIsGone = project
                .map { it.metadataForProject() !== ProjectMetadata.CATEGORY_FEATURED }

            friendAvatarUrl1 = project
                .filter(Project::isFriendBacking)
                .filter { it.isNotNull() }
                .map { it.friends() }
                .filter { it.isNotEmpty() }
                .map { it[0].avatar().replaceSmallImageWithMediumIfEmpty() }
                .filter { it.isNotEmpty() }

            friendAvatarUrl2 = project
                .filter(Project::isFriendBacking)
                .filter { it.isNotNull() }
                .map { it.friends() }
                .filter { it.size > 1 }
                .map { it[1].avatar().replaceSmallImageWithMediumIfEmpty() }
                .filter { it.isNotEmpty() }

            friendAvatarUrl3 = project
                .filter(Project::isFriendBacking)
                .filter { it.isNotNull() }
                .map { it.friends() }
                .filter { it.size > 2 }
                .map { it[2].avatar().replaceSmallImageWithMediumIfEmpty() }
                .filter { it.isNotEmpty() }

            friendAvatar2IsGone = project
                .filter { it.isNotNull() }
                .map { it.friends() }
                .map { it.size > 1 }
                .map { it.negate() }

            friendAvatar3IsGone = project
                .filter { it.isNotNull() }
                .map { it.friends() }
                .map { it.size > 2 }
                .map { it.negate() }

            friendBackingViewIsHidden = project
                .map(Project::isFriendBacking)
                .map { it.negate() }

            friendsForNamepile = project
                .filter(Project::isFriendBacking)
                .filter { it.isNotNull() }
                .filter { it.friends().isNotNull() }
                .map { it.friends() }

            fundingUnsuccessfulViewGroupIsGone = project
                .filter { it.isNotNull() }
                .map {
                    it.state() != Project.STATE_CANCELED &&
                        it.state() != Project.STATE_FAILED &&
                        it.state() != Project.STATE_SUSPENDED
                }

            fundingSuccessfulViewGroupIsGone = project
                .filter { it.isNotNull() }
                .map { it.state() != Project.STATE_SUCCESSFUL }

            imageIsInvisible = project
                .filter { it.isNotNull() }
                .map { it.photo().isNull() }

            project
                .filter { it.isNotNull() && it.location().isNotNull() }
                .map { requireNotNull(it.location()) }
                .map { it.displayableName() }
                .distinctUntilChanged()
                .subscribe { locationName.onNext(it) }
                .addToDisposable(disposables)

            discoveryParams
                .map { shouldShowLocationTag(it) }
                .compose(Transformers.combineLatestPair(project))
                .map { distanceSortAndProject: Pair<Boolean, Project> ->
                    distanceSortAndProject.first == true && distanceSortAndProject.second.location()
                        .isNotNull()
                }
                .map { it.negate() }
                .distinctUntilChanged()
                .subscribe { locationContainerIsGone.onNext(it) }
                .addToDisposable(disposables)

            metadataViewGroupIsGone = project
                .map { it.metadataForProject() == ProjectMetadata.NONE }

            metadataViewGroupBackground = backingViewGroupIsGone
                .compose(Transformers.combineLatestPair(comingSoonViewGroupIsGone))
                .map {
                    val backingViewGroupIsGone = it.first
                    val comingSoonViewGroupIsGone = it.second
                    if (backingViewGroupIsGone && comingSoonViewGroupIsGone) {
                        R.drawable.rect_white_grey_stroke
                    } else {
                        R.drawable.rect_green_grey_stroke
                    }
                }

            nameAndBlurbText = project
                .filter { it.isNotNull() }
                .map {
                    Pair.create(
                        it.name(),
                        it.blurb(),
                    )
                }

            notifyDelegateOfProjectClick = project
                .compose(Transformers.takeWhenV2(projectCardClicked))

            percentageFundedForProgressBar = project
                .filter { it.isNotNull() }
                .map {
                    if (it.state() == Project.STATE_LIVE || it.state() == Project.STATE_SUCCESSFUL) {
                        it.percentageFunded()
                    } else {
                        0.0f
                    }
                }.map {
                    ProgressBarUtils.progress(it)
                }

            percentageFundedProgressBarIsGone = project
                .filter { it.isNotNull() }
                .map {
                    it.state() == Project.STATE_CANCELED
                }
            percentageFundedTextViewText = project
                .map { it.percentageFunded() }
                .map {
                    NumberUtils.flooredPercentage(it)
                }

            photoUrl = project
                .filter { it.isNotNull() }
                .map {
                    if (it.photo() == null) {
                        ""
                    } else {
                        it.photo()?.full()
                    }
                }

            projectCanceledAt = project
                .filter { it.isNotNull() }
                .filter { it.state() == Project.STATE_CANCELED }
                .map { it.stateChangedAt() ?: DateTime() }

            projectCardStatsViewGroupIsGone = project
                .filter { it.isNotNull() }
                .map { it.state() != Project.STATE_LIVE }

            projectFailedAt = project
                .filter { it.isNotNull() }
                .filter { it.state() == Project.STATE_FAILED }
                .map { it.stateChangedAt() ?: DateTime() }

            projectStateViewGroupIsGone = project
                .filter { it.isNotNull() }
                .map { it.isCompleted() }
                .map { it.negate() }

            val projectCategory = project
                .filter { it.isNotNull() && it.category() != null }
                .map { requireNotNull(it.category()) }

            projectSubcategoryIsGone = discoveryParams
                .filter { it.category().isNotNull() }
                .map { requireNotNull(it.category()) }
                .compose(Transformers.combineLatestPair(projectCategory))
                .map {
                    areParamsAllOrSameCategoryAsProject(it)
                }.distinctUntilChanged()

            projectSubcategoryName = projectCategory
                .map { it.name() }

            projectSuccessfulAt = project
                .filter { it.isNotNull() }
                .filter { it.state() == Project.STATE_SUCCESSFUL }
                .map { it.stateChangedAt() ?: DateTime() }

            projectSuspendedAt = project
                .filter { it.isNotNull() }
                .filter { it.state() == Project.STATE_SUSPENDED }
                .map { it.stateChangedAt() ?: DateTime() }

            projectWeLoveIsGone = project
                .filter { it.isNotNull() }
                .map { it.staffPick() ?: false }
                .compose(
                    Transformers.combineLatestPair(
                        discoveryParams.map { it.staffPicks() ?: false },
                    ),
                )
                .map { it.first == true && it.second == false }
                .map { it.negate() }
                .distinctUntilChanged()

            projectTagContainerIsGone =
                Observable.combineLatest<Boolean, Boolean, Pair<Boolean, Boolean>>(
                    projectSubcategoryIsGone,
                    projectWeLoveIsGone,
                ) { a: Boolean?, b: Boolean? -> Pair.create(a, b) }
                    .map { it.first && it.second }
                    .distinctUntilChanged()

            rootCategoryNameForFeatured = projectCategory
                .filter { it.isNotNull() && it.root().isNotNull() }
                .map { requireNotNull(it.root()) }
                .map { it.name() }

            savedViewGroupIsGone = project
                .filter { it.isNotNull() }
                .map { it.metadataForProject() !== ProjectMetadata.SAVING }

            setDefaultTopPadding = metadataViewGroupIsGone
        }

        fun onCleared() {
            disposables.clear()
        }
    }
}
