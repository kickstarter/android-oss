package com.kickstarter.viewmodels

import android.content.Intent
import android.os.Build
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.THANKS
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.ThirdPartyEventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.combineProjectsAndParams
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.updateStartedProjectAndDiscoveryParamsList
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isLocationGermany
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ThanksAdapter
import com.kickstarter.ui.adapters.data.ThanksData
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ProjectData.Companion.builder
import com.kickstarter.ui.viewholders.ProjectCardViewHolder
import com.kickstarter.ui.viewholders.ThanksCategoryViewHolder
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ThanksViewModel {
    interface Inputs :
        ProjectCardViewHolder.Delegate,
        ThanksCategoryViewHolder.Delegate,
        ThanksAdapter.Delegate {
        /** Call when the user clicks the close button.  */
        fun closeButtonClicked()

        /** Call when the user accepts the prompt to signup to the Games newsletter.  */
        fun signupToGamesNewsletterClick()
    }

    interface Outputs {
        /** Emits the data to configure the adapter with.  */
        fun adapterData(): Observable<ThanksData>

        /** Emits when we should finish the [com.kickstarter.ui.activities.ThanksActivity].  */
        fun finish(): Observable<Unit>

        /** Show a dialog confirming the user will be signed up to the games newsletter. Required for German users.  */
        fun showConfirmGamesNewsletterDialog(): Observable<Unit>

        /** Show a dialog prompting the user to sign-up to the games newsletter.  */
        fun showGamesNewsletterDialog(): Observable<Unit>

        /** Show a dialog prompting the user to rate the app.  */
        fun showRatingDialog(): Observable<Unit>

        /** Emits when we should start the [com.kickstarter.ui.activities.DiscoveryActivity].  */
        fun startDiscoveryActivity(): Observable<DiscoveryParams>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivity(): Observable<Pair<Project, RefTag>>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Unit>
    }

    class ThanksViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {
        private val apiClient = requireNotNull(environment.apiClientV2())
        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val hasSeenAppRatingPreference = requireNotNull(environment.hasSeenAppRatingPreference())
        private val hasSeenGamesNewsletterPreference = requireNotNull(environment.hasSeenGamesNewsletterPreference())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val cookieManager = requireNotNull(environment.cookieManager())
        private val ffClient = requireNotNull(environment.featureFlagClient())

        private val categoryCardViewHolderClicked = PublishSubject.create<Category>()
        private val closeButtonClicked = PublishSubject.create<Unit>()
        private val projectCardViewHolderClicked = PublishSubject.create<Project>()
        private val signupToGamesNewsletterClick = PublishSubject.create<Unit>()
        private val adapterData = BehaviorSubject.create<ThanksData>()
        private val finish = PublishSubject.create<Unit>()
        private val showConfirmGamesNewsletterDialog = PublishSubject.create<Unit>()
        private val showGamesNewsletterDialog = PublishSubject.create<Unit>()
        private val showRatingDialog = PublishSubject.create<Unit>()
        private val signedUpToGamesNewsletter = PublishSubject.create<User>()
        private val startDiscoveryActivity = PublishSubject.create<DiscoveryParams>()
        private val startProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
        private val onHeartButtonClicked = PublishSubject.create<Project>()
        private val showSavedPrompt = PublishSubject.create<Unit>()

        private val intentObservable = PublishSubject.create<Intent>()
        private val disposables = CompositeDisposable()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val onThirdPartyEventSent = BehaviorSubject.create<Boolean?>()
        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this

        init {
            val project = intentObservable
                .filter {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getParcelableExtra(IntentKey.PROJECT, Project::class.java) != null
                    } else {
                        it.getParcelableExtra(IntentKey.PROJECT) as? Project? != null
                    }
                }
                .map {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getParcelableExtra(IntentKey.PROJECT, Project::class.java)
                    } else {
                        it.getParcelableExtra(IntentKey.PROJECT) as? Project?
                    }
                }
                .ofType(Project::class.java)
                .take(1)

            val rootCategory = project
                .switchMap {
                    rootCategory(it, apolloClient)
                }
                .compose(Transformers.neverErrorV2())
                .filter {
                    ObjectUtils.isNotNull(it)
                }

            val isGamesCategory = rootCategory
                .map { "games" == it.slug() }

            val hasSeenGamesNewsletterDialog = Observable.just(
                hasSeenGamesNewsletterPreference.get()
            ).filter { ObjectUtils.isNotNull(it) }

            val isSignedUpToGamesNewsletter = currentUser.observable()
                .map { it.getValue() != null && it.getValue()?.gamesNewsletter().isTrue() }

            val showGamesNewsletter = Observable.combineLatest(
                isGamesCategory,
                hasSeenGamesNewsletterDialog,
                isSignedUpToGamesNewsletter
            ) { isGames, hasSeen, isSignedUp ->
                isGames && !hasSeen && !isSignedUp
            }.take(1)

            categoryCardViewHolderClicked
                .map { DiscoveryParams.builder().category(it).build() }
                .subscribe { startDiscoveryActivity.onNext(it) }
                .addToDisposable(disposables)

            closeButtonClicked
                .subscribe(finish)

            projectCardViewHolderClicked
                .subscribe {
                    startProjectActivity.onNext(
                        Pair(
                            it,
                            RefTag.thanks()
                        )
                    )
                }
                .addToDisposable(disposables)

            val projectOnUserChangeSave = this.onHeartButtonClicked
                .switchMap {
                    this.toggleProjectSave(it)
                }
                .share()

            val recommendedProjects = project.switchMap {
                relatedProjects(
                    it,
                    apiClient
                )
            }.filter { ObjectUtils.isNotNull(it) }
                .map {
                    combineProjectsAndParams(
                        it,
                        DiscoveryParams.builder().build()
                    )
                }

            Observable.combineLatest(
                project,
                rootCategory,
                recommendedProjects
            ) { backedProject, category, projects ->
                ThanksData(backedProject, category, projects)
            }
                .subscribe { adapterData.onNext(it) }
                .addToDisposable(disposables)

            adapterData
                .compose(Transformers.takePairWhenV2(projectOnUserChangeSave))
                .map {
                    Pair(it.first, it.second.updateStartedProjectAndDiscoveryParamsList(it.first.recommendedProjects))
                }
                .map {
                    ThanksData(it.first.backedProject, it.first.category, it.second)
                }.distinctUntilChanged()
                .subscribe {
                    adapterData.onNext(it)
                }
                .addToDisposable(disposables)

            projectOnUserChangeSave
                .subscribe { environment.analytics()?.trackWatchProjectCTA(it, THANKS) }
                .addToDisposable(disposables)

            projectOnUserChangeSave
                .filter { p -> p.isStarred() && p.isLive && !p.isApproachingDeadline }
                .compose(Transformers.ignoreValuesV2())
                .subscribe(this.showSavedPrompt)

                Observable.just(hasSeenAppRatingPreference.get())
                    .take(1)
                    .compose(Transformers.combineLatestPair(showGamesNewsletter))
                    .filter { !it.first && !it.second }
                    .filter { environment.featureFlagClient()?.getBoolean(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG) == false }
                    .compose(Transformers.ignoreValuesV2())
                    .subscribe { showRatingDialog.onNext(Unit) }
                    .addToDisposable(disposables)

            showGamesNewsletter
                .filter { it }
                .subscribe { showGamesNewsletterDialog.onNext(Unit) }
                .addToDisposable(disposables)

            showGamesNewsletterDialog
                .subscribe { hasSeenGamesNewsletterPreference.set(true) }
                .addToDisposable(disposables)

            currentUser.observable()
                .filter { ObjectUtils.isNotNull(it) && ObjectUtils.isNotNull(it.getValue()) }
                .compose(Transformers.takeWhenV2(signupToGamesNewsletterClick))
                .flatMap {
                    it.getValue()?.let { user ->
                        signupToGamesNewsletter(user, apiClient)
                    }
                }
                .subscribe { signedUpToGamesNewsletter.onNext(it) }
                .addToDisposable(disposables)

            currentUser.observable()
                .filter { ObjectUtils.isNotNull(it) }
                .compose(Transformers.takeWhenV2(signedUpToGamesNewsletter))
                .filter {
                    it.getValue()?.isLocationGermany().isTrue()
                }
                .subscribe { showConfirmGamesNewsletterDialog.onNext(Unit) }
                .addToDisposable(disposables)

            val checkoutData = intentObservable
                .map<CheckoutData?> { it.getParcelableExtra(IntentKey.CHECKOUT_DATA) }
                .ofType(CheckoutData::class.java)
                .take(1)

            val pledgeData = intentObservable
                .map<PledgeData?> { it.getParcelableExtra(IntentKey.PLEDGE_DATA) }
                .ofType(PledgeData::class.java)
                .take(1)

            val checkoutAndPledgeData =
                Observable.combineLatest<CheckoutData, PledgeData, Pair<CheckoutData, PledgeData>>(
                    checkoutData,
                    pledgeData
                ) { a, b -> Pair.create(a, b) }

            checkoutAndPledgeData
                .subscribe { checkoutDataPledgeData: Pair<CheckoutData, PledgeData> ->
                    environment.analytics()?.trackThanksScreenViewed(
                        checkoutDataPledgeData.first,
                        checkoutDataPledgeData.second
                    )
                }
                .addToDisposable(disposables)

            SendThirdPartyEventUseCase(sharedPreferences, ffClient)
                .sendThirdPartyEvent(
                    project,
                    apolloClient,
                    checkoutAndPledgeData,
                    currentUser,
                    ThirdPartyEventName.PURCHASE,
                )
                .compose(Transformers.neverErrorV2())
                .subscribe {
                    onThirdPartyEventSent.onNext(it.first.triggerThirdPartyEvent()?.success() ?: false)
                }
                .addToDisposable(disposables)

            checkoutAndPledgeData
                .compose(Transformers.takePairWhenV2(projectCardViewHolderClicked))
                .subscribe { dataCheckoutProjectPair: Pair<Pair<CheckoutData, PledgeData>, Project> ->

                    val cookieRefTag = RefTagUtils.storedCookieRefTagForProject(
                        dataCheckoutProjectPair.second,
                        cookieManager,
                        sharedPreferences
                    )

                    val projectData = builder()
                        .refTagFromIntent(RefTag.thanks())
                        .refTagFromCookie(cookieRefTag)
                        .project(dataCheckoutProjectPair.second)
                        .build()

                    environment.analytics()?.trackThanksActivityProjectCardClicked(
                        projectData,
                        dataCheckoutProjectPair.first.first,
                        dataCheckoutProjectPair.first.second
                    )
                }
                .addToDisposable(disposables)
        }

        /**
         * Returns a shuffled list of 3 recommended projects, with fallbacks to similar and staff picked projects
         * for users with fewer than 3 recommendations.
         */
        private fun relatedProjects(
            project: Project,
            client: ApiClientTypeV2
        ): Observable<List<Project>> {
            val recommendedParams = DiscoveryParams.builder()
                .backed(-1)
                .recommended(true)
                .perPage(6)
                .build()

            val similarToParams = DiscoveryParams.builder()
                .backed(-1)
                .similarTo(project)
                .perPage(3)
                .build()

            val category = project.category()

            val staffPickParams = DiscoveryParams.builder()
                .category(category?.root())
                .backed(-1)
                .staffPicks(true)
                .perPage(3)
                .build()

            val recommendedProjects = client.fetchProjects(recommendedParams)
                .retry(2)
                .map { it.projects() }
                .map { ListUtils.shuffle(it) }
                .flatMap { Observable.fromIterable(it) }
                .take(3)
                .filter { ObjectUtils.isNotNull(it) }

            val similarToProjects = client.fetchProjects(similarToParams)
                .retry(2)
                .map { it.projects() }
                .flatMap { Observable.fromIterable(it) }

            val staffPickProjects = client.fetchProjects(staffPickParams)
                .retry(2)
                .map { it.projects() }
                .flatMap { Observable.fromIterable(it) }

            return Observable.concat(recommendedProjects, similarToProjects, staffPickProjects)
                .compose(Transformers.neverErrorV2())
                .distinct()
                .take(3)
                .toList()
                .toObservable()
        }

        private fun signupToGamesNewsletter(user: User, client: ApiClientTypeV2): Observable<User> {
            return client
                .updateUserSettings(user.toBuilder().gamesNewsletter(true).build())
                .compose(Transformers.neverErrorV2())
        }
        override fun categoryViewHolderClicked(category: Category?) {
            category?.let {
                categoryCardViewHolderClicked.onNext(it)
            }
        }
        override fun closeButtonClicked() = closeButtonClicked.onNext(Unit)
        override fun signupToGamesNewsletterClick() = signupToGamesNewsletterClick.onNext(Unit)
        override fun onHeartButtonClicked(project: Project) = onHeartButtonClicked.onNext(project)
        override fun projectCardViewHolderClicked(project: Project?) {
            project?.let {
                projectCardViewHolderClicked.onNext(it)
            }
        }

        override fun adapterData(): Observable<ThanksData> = this.adapterData
        override fun finish(): Observable<Unit> = this.finish
        override fun showConfirmGamesNewsletterDialog(): Observable<Unit> = this.showConfirmGamesNewsletterDialog
        override fun showGamesNewsletterDialog(): Observable<Unit> = this.showGamesNewsletterDialog
        override fun showRatingDialog(): Observable<Unit> = this.showRatingDialog
        override fun startDiscoveryActivity(): Observable<DiscoveryParams> = this.startDiscoveryActivity
        override fun startProjectActivity(): Observable<Pair<Project, RefTag>> = this.startProjectActivity
        override fun showSavedPrompt(): Observable<Unit> = this.showSavedPrompt

        private fun saveProject(project: Project): Observable<Project> {
            return this.apolloClient.watchProject(project)
                .compose(Transformers.neverErrorV2())
        }

        private fun unSaveProject(project: Project): Observable<Project> {
            return this.apolloClient.unWatchProject(project).compose(Transformers.neverErrorV2())
        }

        private fun toggleProjectSave(project: Project): Observable<Project> {
            return if (project.isStarred()) {
                unSaveProject(project)
            } else {
                saveProject(project)
            }
        }

        fun provideIntent(intent: Intent?) {
            intent?.let {
                this.intentObservable.onNext(it)
            }
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        companion object {
            /**
             * Given a project, returns an observable that emits the project's root category.
             */
            private fun rootCategory(
                project: Project,
                client: ApolloClientTypeV2
            ): Observable<Category> {
                val category = project.category() ?: return Observable.empty()

                return when {
                    category.parent() != null -> {
                        Observable.just(category.parent())
                    }
                    category.isRoot -> {
                        Observable.just(category)
                    }
                    else -> {
                        client.fetchCategory(category.rootId().toString())
                    }
                }
            }
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThanksViewModel(environment) as T
        }
    }
}
