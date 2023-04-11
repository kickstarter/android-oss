package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils.appendRefTag
import com.kickstarter.libs.utils.UrlUtils.refTag
import com.kickstarter.libs.utils.extensions.*
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.DeepLinkActivity
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import rx.Notification
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface DeepLinkViewModel {
    interface Outputs {
        /** Emits when we should start an external browser because we don't want to deep link.  */
        fun startBrowser(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.DiscoveryActivity].  */
        fun startDiscoveryActivity(): Observable<Void>

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
        fun finishDeeplinkActivity(): Observable<Void>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectPageActivity].  */
        fun startProjectActivityToSave(): Observable<Uri>

        /** Emits a Project and RefTag pair when we should start the [com.kickstarter.ui.activities.PreLaunchProjectPageActivity].  */
        fun startPreLaunchProjectActivity(): Observable<Project>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<DeepLinkActivity?>(environment), Outputs {

        private val startBrowser = BehaviorSubject.create<String>()
        private val startDiscoveryActivity = BehaviorSubject.create<Void>()
        private val startProjectActivity = BehaviorSubject.create<Uri>()
        private val startProjectActivityForComment = BehaviorSubject.create<Uri>()
        private val startProjectActivityForUpdate = BehaviorSubject.create<Uri>()
        private val startProjectActivityForCommentToUpdate = BehaviorSubject.create<Uri>()
        private val startProjectActivityWithCheckout = BehaviorSubject.create<Uri>()
        private val startProjectActivityToSave = BehaviorSubject.create<Uri>()
        private val updateUserPreferences = BehaviorSubject.create<Boolean>()
        private val finishDeeplinkActivity = BehaviorSubject.create<Void?>()
        private val apolloClient = requireNotNull(environment.apolloClient())
        private val apiClientType = requireNotNull(environment.apiClient())
        private val currentUser = requireNotNull(environment.currentUser())
        private val webEndpoint = requireNotNull(environment.webEndpoint())
        private val projectObservable: Observable<Project>
        private val startPreLaunchProjectActivity = PublishSubject.create<Project>()

        private val ffClient = requireNotNull(environment.featureFlagClient())

        val outputs: Outputs = this

        init {

            val uriFromIntent = intent()
                .map { obj: Intent -> obj.data }
                .ofType(Uri::class.java)

            uriFromIntent
                .filter { lastPathSegmentIsProjects(it) }
                .compose(Transformers.ignoreValues())
                .compose(bindToLifecycle())
                .subscribe {
                    startDiscoveryActivity.onNext(it)
                }

            projectObservable = uriFromIntent
                .map { ProjectIntentMapper.paramFromUri(it) }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .switchMap {
                    getProject(it)
                        .doOnError {
                            finishDeeplinkActivity.onNext(null)
                        }
                }
                .filter { ObjectUtils.isNotNull(it.value) }
                .map { it.value }

            uriFromIntent
                .filter { ObjectUtils.isNotNull(it) }
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
                .compose(bindToLifecycle())
                .subscribe {

                    onDeepLinkToProjectPage(it, startProjectActivity)
                }

            uriFromIntent
                .filter { ObjectUtils.isNotNull(it) }
                .filter {
                    it.isProjectSaveUri(webEndpoint)
                }
                .map { appendRefTagIfNone(it) }
                .compose(Transformers.combineLatestPair(projectObservable))
                .compose(bindToLifecycle())
                .subscribe {
                    onDeepLinkToProjectPage(it, startProjectActivityToSave)
                }

            uriFromIntent
                .filter { ObjectUtils.isNotNull(it) }
                .filter {
                    it.isProjectCommentUri(webEndpoint)
                }
                .map { appendRefTagIfNone(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    startProjectActivityForComment.onNext(it)
                }

            uriFromIntent
                .filter { ObjectUtils.isNotNull(it) }
                .filter {
                    it.isProjectUpdateUri(webEndpoint)
                }
                .filter {
                    !it.isProjectUpdateCommentsUri(webEndpoint)
                }
                .map { appendRefTagIfNone(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    startProjectActivityForUpdate.onNext(it)
                }

            uriFromIntent
                .filter { ObjectUtils.isNotNull(it) }
                .filter {
                    it.isProjectUpdateCommentsUri(webEndpoint)
                }
                .map { appendRefTagIfNone(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    startProjectActivityForCommentToUpdate.onNext(it)
                }

            uriFromIntent
                .filter { ObjectUtils.isNotNull(it) }
                .filter { it.isSettingsUrl() }
                .compose(bindToLifecycle())
                .subscribe {
                    updateUserPreferences.onNext(true)
                }

            currentUser.observable()
                .filter { ObjectUtils.isNotNull(it) }
                .compose(Transformers.combineLatestPair(updateUserPreferences))
                .switchMap { it: Pair<User, Boolean?> ->
                    updateSettings(it.first, apiClientType)
                }
                .compose(Transformers.values())
                .distinctUntilChanged()
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    refreshUserAndFinishActivity(it, currentUser)
                }

            projectObservable
                .filter { it.backing() == null || !it.canUpdateFulfillment() }
                .subscribe {
                    finishDeeplinkActivity.onNext(null)
                }

            projectObservable
                .filter { it.canUpdateFulfillment() }
                .switchMap {
                    postBacking(it)
                        .doOnError {
                            finishDeeplinkActivity.onNext(null)
                        }
                        .distinctUntilChanged()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    finishDeeplinkActivity.onNext(null)
                }

            uriFromIntent
                .filter { ObjectUtils.isNotNull(it) }
                .filter { it.isCheckoutUri(webEndpoint) }
                .map { appendRefTagIfNone(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    startProjectActivityWithCheckout.onNext(it)
                }

            val projectPreview = uriFromIntent
                .filter { ObjectUtils.isNotNull(it) }
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

            Observable.merge(projectPreview, unsupportedDeepLink)
                .map { obj: Uri -> obj.toString() }
                .filter { !TextUtils.isEmpty(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    startBrowser.onNext(it)
                }
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
                .compose(Transformers.neverError())
                .distinctUntilChanged()

        private fun getProject(it: String) = apolloClient.getProject(it)
            .materialize()
            .share()
            .distinctUntilChanged()

        private fun refreshUserAndFinishActivity(user: User, currentUser: CurrentUserType) {
            currentUser.refresh(user)
            finishDeeplinkActivity.onNext(null)
        }

        private fun appendRefTagIfNone(uri: Uri): Uri {
            val url = uri.toString()
            val ref = refTag(url)
            return if (ObjectUtils.isNull(ref)) {
                Uri.parse(appendRefTag(url, RefTag.deepLink().tag()))
            } else uri
        }

        private fun lastPathSegmentIsProjects(uri: Uri): Boolean {
            return uri.lastPathSegment == "projects"
        }

        private fun updateSettings(
            user: User,
            apiClientType: ApiClientType
        ): Observable<Notification<User?>?> {
            val updatedUser = user.toBuilder().notifyMobileOfMarketingUpdate(true).build()
            return apiClientType.updateUserSettings(updatedUser)
                .materialize()
                .share()
        }

        override fun startBrowser(): Observable<String> = startBrowser

        override fun startDiscoveryActivity(): Observable<Void> = startDiscoveryActivity

        override fun startProjectActivityForComment(): Observable<Uri> = startProjectActivityForComment

        override fun startProjectActivityForUpdate(): Observable<Uri> = startProjectActivityForUpdate

        override fun startProjectActivityForCommentToUpdate(): Observable<Uri> = startProjectActivityForCommentToUpdate

        override fun startProjectActivity(): Observable<Uri> = startProjectActivity

        override fun startProjectActivityForCheckout(): Observable<Uri> = startProjectActivityWithCheckout

        override fun finishDeeplinkActivity(): Observable<Void> = finishDeeplinkActivity

        override fun startProjectActivityToSave(): Observable<Uri> = startProjectActivityToSave

        override fun startPreLaunchProjectActivity(): Observable<Project> = startPreLaunchProjectActivity
    }
}
