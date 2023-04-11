package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.VisibleForTesting
import com.facebook.appevents.cloudbridge.ConversionsAPIEventName
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.THANKS
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.extensions.combineProjectsAndParams
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.updateStartedProjectAndDiscoveryParamsList
import com.kickstarter.models.Category
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isLocationGermany
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ThanksActivity
import com.kickstarter.ui.adapters.ThanksAdapter
import com.kickstarter.ui.adapters.data.ThanksData
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ProjectData.Companion.builder
import com.kickstarter.ui.viewholders.ProjectCardViewHolder
import com.kickstarter.ui.viewholders.ThanksCategoryViewHolder
import com.kickstarter.viewmodels.usecases.SendCAPIEventUseCase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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
        fun finish(): Observable<Void>

        /** Show a dialog confirming the user will be signed up to the games newsletter. Required for German users.  */
        fun showConfirmGamesNewsletterDialog(): Observable<Void?>

        /** Show a dialog prompting the user to sign-up to the games newsletter.  */
        fun showGamesNewsletterDialog(): Observable<Void?>

        /** Show a dialog prompting the user to rate the app.  */
        fun showRatingDialog(): Observable<Void?>

        /** Emits when we should start the [com.kickstarter.ui.activities.DiscoveryActivity].  */
        fun startDiscoveryActivity(): Observable<DiscoveryParams>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivity(): Observable<Pair<Project, RefTag>>

        /** Emits when the success prompt for saving should be displayed.  */
        fun showSavedPrompt(): Observable<Void>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<ThanksActivity>(environment),
        Inputs,
        Outputs {
        private val apiClient = requireNotNull(environment.apiClient())
        private val apolloClient = requireNotNull(environment.apolloClient())
        private val hasSeenAppRatingPreference = environment.hasSeenAppRatingPreference()
        private val hasSeenGamesNewsletterPreference = environment.hasSeenGamesNewsletterPreference()
        private val currentUser = requireNotNull(environment.currentUser())
        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val cookieManager = requireNotNull(environment.cookieManager())
        private val optimizely = requireNotNull(environment.optimizely())
        private val ffClient = requireNotNull(environment.featureFlagClient())

        private val categoryCardViewHolderClicked = PublishSubject.create<Category>()
        private val closeButtonClicked = PublishSubject.create<Void?>()
        private val projectCardViewHolderClicked = PublishSubject.create<Project>()
        private val signupToGamesNewsletterClick = PublishSubject.create<Void?>()
        private val adapterData = BehaviorSubject.create<ThanksData>()
        private val finish = PublishSubject.create<Void>()
        private val showConfirmGamesNewsletterDialog = PublishSubject.create<Void?>()
        private val showGamesNewsletterDialog = PublishSubject.create<Void?>()
        private val showRatingDialog = PublishSubject.create<Void?>()
        private val signedUpToGamesNewsletter = PublishSubject.create<User>()
        private val startDiscoveryActivity = PublishSubject.create<DiscoveryParams>()
        private val startProjectActivity = PublishSubject.create<Pair<Project, RefTag>>()
        private val onHeartButtonClicked = PublishSubject.create<Project>()
        private val showSavedPrompt = PublishSubject.create<Void>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val onCAPIEventSent = BehaviorSubject.create<Boolean?>()
        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this

        init {
            val project = intent()
                .map<Project?> { it.getParcelableExtra(IntentKey.PROJECT) }
                .ofType(Project::class.java)
                .take(1)
                .compose(bindToLifecycle())

            val rootCategory = project
                .switchMap {
                    rootCategory(it, apolloClient)
                }
                .compose(Transformers.neverError())
                .filter {
                    ObjectUtils.isNotNull(it)
                }.map { requireNotNull(it) }

            val isGamesCategory = rootCategory
                .map { "games" == it?.slug() }

            val hasSeenGamesNewsletterDialog = Observable.just(
                hasSeenGamesNewsletterPreference?.get()
            ).filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            val isSignedUpToGamesNewsletter = currentUser.observable()
                .map { it != null && it.gamesNewsletter().isTrue() }

            val showGamesNewsletter = Observable.combineLatest(
                isGamesCategory,
                hasSeenGamesNewsletterDialog,
                isSignedUpToGamesNewsletter
            ) { isGames, hasSeen, isSignedUp ->
                isGames && !hasSeen && !isSignedUp
            }.take(1)

            categoryCardViewHolderClicked
                .map { DiscoveryParams.builder().category(it).build() }
                .compose(bindToLifecycle())
                .subscribe { startDiscoveryActivity.onNext(it) }

            closeButtonClicked
                .compose(bindToLifecycle())
                .subscribe(finish)

            projectCardViewHolderClicked
                .compose(bindToLifecycle())
                .subscribe {
                    startProjectActivity.onNext(
                        Pair(
                            it,
                            RefTag.thanks()
                        )
                    )
                }

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
                .map { requireNotNull(it) }
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
            }.compose(bindToLifecycle())
                .subscribe { adapterData.onNext(it) }

            adapterData
                .compose(Transformers.takePairWhen(projectOnUserChangeSave))
                .map {
                    Pair(it.first, it.second.updateStartedProjectAndDiscoveryParamsList(it.first.recommendedProjects))
                }
                .map {
                    ThanksData(it.first.backedProject, it.first.category, it.second)
                }.distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    adapterData.onNext(it)
                }

            projectOnUserChangeSave
                .compose(bindToLifecycle())
                .subscribe { this.analyticEvents.trackWatchProjectCTA(it, THANKS) }

            projectOnUserChangeSave
                .filter { p -> p.isStarred() && p.isLive && !p.isApproachingDeadline }
                .compose(Transformers.ignoreValues())
                .compose(bindToLifecycle())
                .subscribe(this.showSavedPrompt)

            Observable.just(hasSeenAppRatingPreference?.get())
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .take(1)
                .compose(Transformers.combineLatestPair(showGamesNewsletter))
                .filter { !it.first && !it.second }
                .filter { environment.featureFlagClient()?.getBoolean(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG) == false }
                .compose(Transformers.ignoreValues())
                .compose(bindToLifecycle())
                .subscribe { showRatingDialog.onNext(null) }

            showGamesNewsletter
                .filter { it }
                .compose(bindToLifecycle())
                .subscribe { showGamesNewsletterDialog.onNext(null) }

            showGamesNewsletterDialog
                .compose(bindToLifecycle())
                .subscribe { hasSeenGamesNewsletterPreference?.set(true) }

            currentUser.observable()
                .filter { ObjectUtils.isNotNull(it) }
                .compose(Transformers.takeWhen(signupToGamesNewsletterClick))
                .flatMap { signupToGamesNewsletter(it, apiClient) }
                .compose(bindToLifecycle())
                .subscribe { signedUpToGamesNewsletter.onNext(it) }

            currentUser.observable()
                .filter { ObjectUtils.isNotNull(it) }
                .compose(Transformers.takeWhen(signedUpToGamesNewsletter))
                .filter {
                    it.isLocationGermany()
                }.compose(bindToLifecycle())
                .subscribe { showConfirmGamesNewsletterDialog.onNext(null) }

            val checkoutData = intent()
                .map<CheckoutData?> { it.getParcelableExtra(IntentKey.CHECKOUT_DATA) }
                .ofType(CheckoutData::class.java)
                .take(1)

            val pledgeData = intent()
                .map<PledgeData?> { it.getParcelableExtra(IntentKey.PLEDGE_DATA) }
                .ofType(PledgeData::class.java)
                .take(1)

            val checkoutAndPledgeData =
                Observable.combineLatest<CheckoutData, PledgeData, Pair<CheckoutData, PledgeData>>(
                    checkoutData,
                    pledgeData
                ) { a, b -> Pair.create(a, b) }

            checkoutAndPledgeData
                .compose(bindToLifecycle())
                .subscribe { checkoutDataPledgeData: Pair<CheckoutData, PledgeData> ->
                    analyticEvents.trackThanksScreenViewed(
                        checkoutDataPledgeData.first,
                        checkoutDataPledgeData.second
                    )
                }

            val cAPIPurchaseValueAndCurrency = checkoutAndPledgeData.map {
                Pair(
                    it.first.amount().toString(),
                    it.second.projectData().project().currency()
                )
            }

            SendCAPIEventUseCase(optimizely, sharedPreferences, ffClient)
                .sendCAPIEvent(
                    project,
                    currentUser,
                    apolloClient,
                    ConversionsAPIEventName.PURCHASED,
                    cAPIPurchaseValueAndCurrency
                ).compose(Transformers.neverError())
                .compose(bindToLifecycle())
                .subscribe {
                    onCAPIEventSent.onNext(it.first.triggerCAPIEvent()?.success() ?: false)
                }

            checkoutAndPledgeData
                .compose(Transformers.takePairWhen(projectCardViewHolderClicked))
                .compose(bindToLifecycle())
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

                    analyticEvents.trackThanksActivityProjectCardClicked(
                        projectData,
                        dataCheckoutProjectPair.first.first,
                        dataCheckoutProjectPair.first.second
                    )
                }
        }

        /**
         * Returns a shuffled list of 3 recommended projects, with fallbacks to similar and staff picked projects
         * for users with fewer than 3 recommendations.
         */
        private fun relatedProjects(
            project: Project,
            client: ApiClientType
        ): Observable<List<Project>?> {
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
                .flatMap { Observable.from(it) }
                .take(3)
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            val similarToProjects = client.fetchProjects(similarToParams)
                .retry(2)
                .map { it.projects() }
                .flatMap { Observable.from(it) }

            val staffPickProjects = client.fetchProjects(staffPickParams)
                .retry(2)
                .map { it.projects() }
                .flatMap { Observable.from(it) }

            return Observable.concat(recommendedProjects, similarToProjects, staffPickProjects)
                .compose(Transformers.neverError())
                .distinct()
                .take(3)
                .toList()
        }

        private fun signupToGamesNewsletter(user: User, client: ApiClientType): Observable<User> {
            return client
                .updateUserSettings(user.toBuilder().gamesNewsletter(true).build())
                .compose(Transformers.neverError())
        }
        override fun categoryViewHolderClicked(category: Category?) = categoryCardViewHolderClicked.onNext(category)
        override fun closeButtonClicked() = closeButtonClicked.onNext(null)
        override fun signupToGamesNewsletterClick() = signupToGamesNewsletterClick.onNext(null)
        override fun onHeartButtonClicked(project: Project) = onHeartButtonClicked.onNext(project)
        override fun projectCardViewHolderClicked(project: Project?) = projectCardViewHolderClicked.onNext(project)

        override fun adapterData(): Observable<ThanksData> = this.adapterData
        override fun finish(): Observable<Void> = this.finish
        override fun showConfirmGamesNewsletterDialog(): Observable<Void?> = this.showConfirmGamesNewsletterDialog
        override fun showGamesNewsletterDialog(): Observable<Void?> = this.showGamesNewsletterDialog
        override fun showRatingDialog(): Observable<Void?> = this.showRatingDialog
        override fun startDiscoveryActivity(): Observable<DiscoveryParams> = this.startDiscoveryActivity
        override fun startProjectActivity(): Observable<Pair<Project, RefTag>> = this.startProjectActivity
        override fun showSavedPrompt(): Observable<Void> = this.showSavedPrompt

        private fun saveProject(project: Project): Observable<Project> {
            return this.apolloClient.watchProject(project)
                .compose(Transformers.neverError())
        }

        private fun unSaveProject(project: Project): Observable<Project> {
            return this.apolloClient.unWatchProject(project).compose(Transformers.neverError())
        }

        private fun toggleProjectSave(project: Project): Observable<Project> {
            return if (project.isStarred()) {
                unSaveProject(project)
            } else {
                saveProject(project)
            }
        }

        companion object {
            /**
             * Given a project, returns an observable that emits the project's root category.
             */
            private fun rootCategory(
                project: Project,
                client: ApolloClientType
            ): Observable<Category?> {
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
}
