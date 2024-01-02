package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.UrlUtils.appendRefTag
import com.kickstarter.libs.utils.UrlUtils.refTag
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.canUpdateFulfillment
import com.kickstarter.libs.utils.extensions.isCheckoutUri
import com.kickstarter.libs.utils.extensions.isEmailDomain
import com.kickstarter.libs.utils.extensions.isKSDomain
import com.kickstarter.libs.utils.extensions.isMainPage
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isProjectCommentUri
import com.kickstarter.libs.utils.extensions.isProjectPreviewUri
import com.kickstarter.libs.utils.extensions.isProjectSaveUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateCommentsUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.libs.utils.extensions.isRewardFulfilledDl
import com.kickstarter.libs.utils.extensions.isSettingsUrl
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.lang.Exception

interface CustomNetworkClient {
    fun obtainUriFromRedirection(uri: Uri): Observable<Response>
}
interface DeepLinkViewModel {
    interface Outputs {
        /** Emits when we should start an external browser because we don't want to deep link.  */
        fun startBrowser(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.DiscoveryActivity].  */
        fun startDiscoveryActivity(): Observable<Unit>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivity(): Observable<Uri>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivityForComment(): Observable<Uri>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivityForUpdate(): Observable<Uri>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivityForCommentToUpdate(): Observable<Uri>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity] with pledge sheet expanded.  */
        fun startProjectActivityForCheckout(): Observable<Uri>

        /** Emits when we should finish the current activity  */
        fun finishDeeplinkActivity(): Observable<Unit>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectPageActivity].  */
        fun startProjectActivityToSave(): Observable<Uri>

        /** Emits a Project and RefTag pair when we should start the [com.kickstarter.ui.activities.PreLaunchProjectPageActivity].  */
        fun startPreLaunchProjectActivity(): Observable<Project>
    }

    class DeepLinkViewModel(environment: Environment, private val intent : Intent?) :
        ViewModel(), Outputs {

        private val startBrowser = BehaviorSubject.create<String>()
        private val startDiscoveryActivity = BehaviorSubject.create<Unit>()
        private val startProjectActivity = BehaviorSubject.create<Uri>()
        private val startProjectActivityForComment = BehaviorSubject.create<Uri>()
        private val startProjectActivityForUpdate = BehaviorSubject.create<Uri>()
        private val startProjectActivityForCommentToUpdate = BehaviorSubject.create<Uri>()
        private val startProjectActivityWithCheckout = BehaviorSubject.create<Uri>()
        private val startProjectActivityToSave = BehaviorSubject.create<Uri>()
        private val updateUserPreferences = BehaviorSubject.create<Boolean>()
        private val finishDeeplinkActivity = BehaviorSubject.create<Unit>()
        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val apiClientType = requireNotNull(environment.apiClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val webEndpoint = requireNotNull(environment.webEndpoint())
        private val projectObservable: Observable<Project>
        private val startPreLaunchProjectActivity = PublishSubject.create<Project>()

        private val ffClient = requireNotNull(environment.featureFlagClient())

        private val disposables = CompositeDisposable()
        private fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()

        val outputs: Outputs = this

        /**
         * Custom Networking Client as plain as possible. Given an URI, will execute the network call on Schedulers.io pool,
         * and return the response as Observable.
         */
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var externalCall = object : CustomNetworkClient {
            override fun obtainUriFromRedirection(uri: Uri): Observable<Response> {
                val httpClient: OkHttpClient = OkHttpClient.Builder().build()

                return Observable.fromCallable {
                    httpClient.newCall(Request.Builder().url(uri.toString()).build()).execute()
                }
                    .subscribeOn(Schedulers.io())
            }
        }

        init {

            val uriFromIntent = intent()
                .map { obj: Intent -> obj.data }
                .ofType(Uri::class.java)

            // - Takes URI from Marketing email domain, executes network call that and redirection took place
            val uriFromEmailDomain = uriFromIntent
                .filter { it.isEmailDomain() }
                .switchMap {
                    externalCall.obtainUriFromRedirection(it)
                }
                .filter { it.priorResponse?.code == 302 }

            // TODO: on following tickets recognize discovery with category_id links, for now if not project URL, open discovery
            val isKSDomainUriFromEmail = uriFromEmailDomain
                .map { Uri.parse(it.request.url.toString()) }
                .filter { !it.isProjectUri() && it.isKSDomain() }

            // - The redirected URI is a project URI
            val projectFromEmail = uriFromEmailDomain
                .filter {
                    Uri.parse(it.request.url.toString()).isProjectUri()
                }
                .map {
                    Uri.parse(it.request.url.toString())
                }

            // - Take URI from main page Open button with URL - ksr://www.kickstarter.com/?app_banner=1&ref=nav
            val mainPageUri = uriFromIntent
                .filter { it.isMainPage() }

            mainPageUri
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    startDiscoveryActivity.onNext(Unit)
                }.addToDisposable(disposables)

            projectFromEmail
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    startProjectActivity.onNext(it)
                }.addToDisposable(disposables)

            isKSDomainUriFromEmail
                .subscribe {
                    startDiscoveryActivity.onNext(Unit)
                }.addToDisposable(disposables)

            uriFromIntent
                .filter { lastPathSegmentIsProjects(it) }
                .compose(Transformers.ignoreValuesV2())
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    startDiscoveryActivity.onNext(it)
                }.addToDisposable(disposables)

            projectObservable = uriFromIntent
                .filter { ProjectIntentMapper.paramFromUri(it).isNotNull() }
                .map { ProjectIntentMapper.paramFromUri(it) }
                .switchMap {
                    getProject(it)
                        .doOnError {
                            finishDeeplinkActivity.onNext(Unit)
                        }
                }
                .filter { it.value.isNotNull() }
                .map { it.value }

            uriFromIntent
                .filter { it.isNotNull() }
                .filter {
                    !it.isProjectSaveUri(webEndpoint)
                }
                .filter {
                    !it.isCheckoutUri(webEndpoint)
                }
                .filter {
                    !it.isProjectPreviewUri(webEndpoint)
                }
                .filter {
                    !it.isProjectCommentUri(webEndpoint)
                }
                .filter {
                    !it.isProjectUpdateUri(webEndpoint)
                }
                .filter {
                    !it.isProjectUpdateCommentsUri(webEndpoint)
                }
                .filter {
                    !it.isRewardFulfilledDl()
                }
                .filter {
                    it.isProjectUri(webEndpoint)
                }
                .map { appendRefTagIfNone(it) }
                .compose(Transformers.combineLatestPair(projectObservable))
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    onDeepLinkToProjectPage(it, startProjectActivity)
                }.addToDisposable(disposables)

            uriFromIntent
                .filter { it.isNotNull() }
                .filter {
                    it.isProjectSaveUri(webEndpoint)
                }
                .map { appendRefTagIfNone(it) }
                .compose(Transformers.combineLatestPair(projectObservable))
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    onDeepLinkToProjectPage(it, startProjectActivityToSave)
                }.addToDisposable(disposables)

            uriFromIntent
                .filter { it.isNotNull() }
                .filter {
                    it.isProjectCommentUri(webEndpoint)
                }
                .map { appendRefTagIfNone(it) }
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    startProjectActivityForComment.onNext(it)
                }.addToDisposable(disposables)

            uriFromIntent
                .filter { it.isNotNull() }
                .filter {
                    it.isProjectUpdateUri(webEndpoint)
                }
                .filter {
                    !it.isProjectUpdateCommentsUri(webEndpoint)
                }
                .map { appendRefTagIfNone(it) }
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    startProjectActivityForUpdate.onNext(it)
                }.addToDisposable(disposables)

            uriFromIntent
                .filter { it.isNotNull() }
                .filter {
                    it.isProjectUpdateCommentsUri(webEndpoint)
                }
                .map { appendRefTagIfNone(it) }
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    startProjectActivityForCommentToUpdate.onNext(it)
                }.addToDisposable(disposables)

            uriFromIntent
                .filter { it.isNotNull() }
                .filter { it.isSettingsUrl() }
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    updateUserPreferences.onNext(true)
                }.addToDisposable(disposables)

            currentUser.observable()
                .filter { it.isPresent() }
                .map { it.getValue() }
                    .compose<Pair<User, Boolean>>(combineLatestPair(updateUserPreferences))
                .switchMap { it: Pair<User, Boolean> ->
                    updateSettings(it.first, apiClientType)
                }
                .compose(Transformers.valuesV2())
                .distinctUntilChanged()
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    refreshUserAndFinishActivity(it, currentUser)
                }.addToDisposable(disposables)

            projectObservable
                .filter { it.backing() == null || !it.canUpdateFulfillment() }
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    finishDeeplinkActivity.onNext(Unit)
                }.addToDisposable(disposables)

            projectObservable
                .filter { it.canUpdateFulfillment() }
                .switchMap {
                    postBacking(it)
                        .doOnError {
                            finishDeeplinkActivity.onNext(Unit)
                        }
                        .distinctUntilChanged()
                }
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    finishDeeplinkActivity.onNext(Unit)
                }.addToDisposable(disposables)

            uriFromIntent
                .filter { it.isNotNull() }
                .filter { it.isCheckoutUri(webEndpoint) }
                .map { appendRefTagIfNone(it) }
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    startProjectActivityWithCheckout.onNext(it)
                }.addToDisposable(disposables)

            val projectPreview = uriFromIntent
                .filter { it.isNotNull() }
                .filter { it.isProjectPreviewUri(webEndpoint) }

            val unsupportedDeepLink = uriFromIntent
                .filter { !lastPathSegmentIsProjects(it) }
                .filter { !it.isSettingsUrl() }
                .filter { !it.isProjectSaveUri(webEndpoint) }
                .filter { !it.isCheckoutUri(webEndpoint) }
                .filter { !it.isProjectCommentUri(webEndpoint) }
                .filter { !it.isProjectUpdateUri(webEndpoint) }
                .filter { !it.isProjectUpdateCommentsUri(webEndpoint) }
                .filter { !it.isProjectUri(webEndpoint) }
                .filter { !it.isRewardFulfilledDl() }
                .filter { !it.isEmailDomain() }

            Observable.merge(projectPreview, unsupportedDeepLink)
                .map { obj: Uri -> obj.toString() }
                .filter { !TextUtils.isEmpty(it) }
                    .onErrorResumeNext { error: Throwable ->
                        throw CompositeException(error, Exception())
                    }
                .subscribe {
                    startBrowser.onNext(it)
                }.addToDisposable(disposables)
        }

        private fun onDeepLinkToProjectPage(it: Pair<Uri, Project>, startProjectPage: BehaviorSubject<Uri>) {
            if (
                it.second.displayPrelaunch() == true &&
                ffClient.getBoolean(FlagKey.ANDROID_PRE_LAUNCH_SCREEN)
            ) {
                startPreLaunchProjectActivity.onNext(it.second)
            } else {
                startProjectPage.onNext(it.first)
            }
        }

        private fun postBacking(it: Project) =
            apiClientType.postBacking(it, requireNotNull(it.backing()), true)
                .compose(Transformers.neverErrorV2())
                .distinctUntilChanged()

        private fun getProject(it: String) = apolloClient.getProject(it)
            .materialize()
            .share()
            .distinctUntilChanged()

        private fun refreshUserAndFinishActivity(user: User, currentUser: CurrentUserTypeV2) {
            currentUser.refresh(user)
            finishDeeplinkActivity.onNext(Unit)
        }

        private fun appendRefTagIfNone(uri: Uri): Uri {
            val url = uri.toString()
            val ref = refTag(url)
            return if (ref.isNull()) {
                Uri.parse(appendRefTag(url, RefTag.deepLink().tag()))
            } else uri
        }

        private fun lastPathSegmentIsProjects(uri: Uri): Boolean {
            return uri.lastPathSegment == "projects"
        }

        private fun updateSettings(
            user: User,
            apiClientType: ApiClientTypeV2
        ): Observable<Notification<User>> {
            val updatedUser = user.toBuilder().notifyMobileOfMarketingUpdate(true).build()
            return apiClientType.updateUserSettings(updatedUser)
                .materialize()
                .share()
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        override fun startBrowser(): Observable<String> = startBrowser

        override fun startDiscoveryActivity(): Observable<Unit> = startDiscoveryActivity

        override fun startProjectActivityForComment(): Observable<Uri> = startProjectActivityForComment

        override fun startProjectActivityForUpdate(): Observable<Uri> = startProjectActivityForUpdate

        override fun startProjectActivityForCommentToUpdate(): Observable<Uri> = startProjectActivityForCommentToUpdate

        override fun startProjectActivity(): Observable<Uri> = startProjectActivity

        override fun startProjectActivityForCheckout(): Observable<Uri> = startProjectActivityWithCheckout

        override fun finishDeeplinkActivity(): Observable<Unit> = finishDeeplinkActivity

        override fun startProjectActivityToSave(): Observable<Uri> = startProjectActivityToSave

        override fun startPreLaunchProjectActivity(): Observable<Project> = startPreLaunchProjectActivity
    }


    class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeepLinkViewModel(environment, intent) as T
        }
    }
}
