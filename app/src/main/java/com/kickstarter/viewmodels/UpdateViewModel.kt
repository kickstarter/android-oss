package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.UrlUtils.appendRefTag
import com.kickstarter.libs.utils.UrlUtils.refTag
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isProjectPreviewUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.models.Update
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.intentmappers.ProjectIntentMapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import okhttp3.Request

interface UpdateViewModel {
    interface Inputs {

        /** Call when a project update comments uri request has been made.  */
        fun goToCommentsRequest(request: Request)

        /** Call when a project uri request has been made.  */
        fun goToProjectRequest(request: Request)

        /** Call when a project update uri request has been made.  */
        fun goToUpdateRequest(request: Request)

        /** Call when the share button is clicked.  */
        fun shareIconButtonClicked()

        /** Call when a project update comments deep link*/
        fun goToCommentsActivity()

        /** Call when a project update comments deep link*/
        fun goToCommentsActivityToDeepLinkThreadActivity(commentableID: String)
    }

    interface Outputs {
        /** Emits a project url to open externally.  */
        fun openProjectExternally(): Observable<String>

        /** Emits when we should start the share intent to show the share sheet.  */
        fun startShareIntent(): Observable<Pair<Update, String>>

        /** Emits an update to start the comments activity with.  */
        fun startRootCommentsActivity(): Observable<Update>

        /** Emits an update to start the comments activity with.  */
        fun deepLinkToThreadActivity(): Observable<Pair<String, Boolean>>

        /** Emits a Uri and a ref tag to start the project activity with.  */
        fun startProjectActivity(): Observable<Pair<Uri, RefTag>>

        fun startRootCommentsActivityToDeepLinkThreadActivity(): Observable<Pair<String, Update>>

        /** Emits a string to display in the toolbar title.  */
        fun updateSequence(): Observable<String>

        /** Emits a url to load in the web view.  */
        fun webViewUrl(): Observable<String>

        fun hasCommentsDeepLinks(): Observable<Boolean>
    }

    class UpdateViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {

        private val client = requireNotNull(environment.apiClientV2())
        private val goToCommentsRequest = PublishSubject.create<Request>()
        private val goToProjectRequest = PublishSubject.create<Request>()
        private val goToUpdateRequest = PublishSubject.create<Request>()
        private val shareButtonClicked = PublishSubject.create<Unit>()
        private val goToCommentsActivity = PublishSubject.create<Unit>()
        private val goToCommentsActivityToDeepLinkThreadActivity = PublishSubject.create<String>()

        private val openProjectExternally = PublishSubject.create<String>()
        private val startShareIntent = PublishSubject.create<Pair<Update, String>>()
        private val startRootCommentsActivity = PublishSubject.create<Update>()
        private val startRootCommentsActivityToDeepLinkThreadActivity =
            PublishSubject.create<Pair<String, Update>>()
        private val startProjectActivity = PublishSubject.create<Pair<Uri, RefTag>>()
        private val updateSequence = BehaviorSubject.create<String>()
        private val webViewUrl = BehaviorSubject.create<String>()
        private val deepLinkToRootComment = BehaviorSubject.create<Boolean>()
        private val deepLinkToThreadActivity = BehaviorSubject.create<Pair<String, Boolean>>()

        private val intent = PublishSubject.create<Intent>()

        @JvmField
        val inputs: Inputs = this

        @JvmField
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()

        init {
            val initialUpdate = intent
                .filter {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getParcelableExtra(IntentKey.UPDATE, Update::class.java) != null
                    } else {
                        it.getParcelableExtra(IntentKey.UPDATE) as? Update? != null
                    }
                }
                .map {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getParcelableExtra(IntentKey.UPDATE, Update::class.java)
                    } else {
                        it.getParcelableExtra(IntentKey.UPDATE) as? Update?
                    }
                }

            intent
                .filter { it.hasExtra(IntentKey.IS_UPDATE_COMMENT) && !it.hasExtra(IntentKey.COMMENT) }
                .map {
                    it.getBooleanExtra(IntentKey.IS_UPDATE_COMMENT, false)
                }
                .subscribe { this.deepLinkToRootComment.onNext(it) }
                .addToDisposable(disposables)

            intent
                .filter {
                    it.hasExtra(IntentKey.COMMENT) && !it.getStringExtra(IntentKey.COMMENT)
                        .isNullOrEmpty()
                }
                .map {
                    Pair(
                        requireNotNull(it.getStringExtra(IntentKey.COMMENT)),
                        it.getBooleanExtra(IntentKey.IS_UPDATE_COMMENT, false)
                    )
                }
                .subscribe {
                    this.deepLinkToThreadActivity.onNext(it)
                }
                .addToDisposable(disposables)

            val project = intent
                .filter { it != null }
                .flatMap { intent ->
                    ProjectIntentMapper
                        .project(intent, client)
                        .compose(Transformers.neverErrorV2())
                }
                .share()

            val initialUpdateUrl = initialUpdate
                ?.map {
                    it.urls()?.web()?.update() ?: ""
                }

            val deepLinkUpdate = intent
                .filter {
                    it.hasExtra(IntentKey.UPDATE_POST_ID) && !it.getStringExtra(IntentKey.UPDATE_POST_ID)
                        .isNullOrEmpty()
                }
                .map { requireNotNull(it.getStringExtra(IntentKey.UPDATE_POST_ID)) }
                .compose(Transformers.combineLatestPair(project))
                .map {
                    Pair(requireNotNull(it.second.slug()), requireNotNull(it.first))
                }
                .switchMap {
                    client.fetchUpdate(it.first, it.second).compose(Transformers.neverErrorV2())
                }
                .share()

            val anotherUpdateUrl = goToUpdateRequest
                .map { it.url.toString() }

            val deepLinkUrl = deepLinkUpdate
                .map { it.urls()?.web()?.update() ?: "" }

            Observable.merge(initialUpdateUrl, anotherUpdateUrl, deepLinkUrl)
                .distinctUntilChanged()
                .filter { it.isNotEmpty() }
                .subscribe {
                    it?.let { url ->
                        webViewUrl.onNext(url)
                    }
                }
                .addToDisposable(disposables)

            val anotherUpdate = goToUpdateRequest
                .map { projectUpdateParams(it) }
                .switchMap {
                    client.fetchUpdate(it.first, it.second).compose(Transformers.neverErrorV2())
                }
                .share()

            val currentUpdate = Observable.merge(initialUpdate, anotherUpdate, deepLinkUpdate)
                .filter {
                    it.isNotNull()
                }

            currentUpdate
                .compose(Transformers.takeWhenV2(shareButtonClicked))
                .map {
                    it.let { update ->
                        Pair.create(
                            update,
                            update.urls()?.web()?.let { web ->
                                appendRefTag(web.update(), RefTag.updateShare().tag())
                            }
                        )
                    }
                }
                .subscribe {
                    it?.let { pair ->
                        startShareIntent.onNext(pair)
                    }
                }
                .addToDisposable(disposables)

            goToCommentsActivity
                .withLatestFrom(currentUpdate) { _, update -> update }
                .distinctUntilChanged()
                .subscribe {
                    startRootCommentsActivity.onNext(it)
                    deepLinkToRootComment.onNext(false)
                }
                .addToDisposable(disposables)

            goToCommentsActivityToDeepLinkThreadActivity
                .withLatestFrom(currentUpdate) { commentableId, update ->
                    Pair(commentableId, requireNotNull(update))
                }
                .distinctUntilChanged()
                .subscribe {
                    startRootCommentsActivityToDeepLinkThreadActivity.onNext(it)
                    deepLinkToThreadActivity.onNext(Pair(it.first, false))
                }
                .addToDisposable(disposables)

            currentUpdate
                .compose(Transformers.takeWhenV2(goToCommentsRequest))
                .subscribe {
                    it?.let { update ->
                        startRootCommentsActivity.onNext(update)
                    }
                }
                .addToDisposable(disposables)

            currentUpdate
                .map { NumberUtils.format(it.sequence()) }
                .subscribe { updateSequence.onNext(it) }
                .addToDisposable(disposables)

            goToProjectRequest
                .map { request -> Uri.parse(request.url.toUri().toString()) }
                .filter { it.isProjectUri(Secrets.WebEndpoint.PRODUCTION) }
                .filter { !it.isProjectPreviewUri(Secrets.WebEndpoint.PRODUCTION) }
                .subscribe {
                    startProjectActivity.onNext(
                        Pair(
                            it,
                            RefTag.update(),
                        )
                    )
                }
                .addToDisposable(disposables)

            goToProjectRequest
                .map { Uri.parse(it.url.toUri().toString()) }
                .filter { it.isProjectUri(Secrets.WebEndpoint.PRODUCTION) }
                .filter { it.isProjectPreviewUri(Secrets.WebEndpoint.PRODUCTION) }
                .map { it.toString() }
                .map {
                    if (refTag(it) == null)
                        appendRefTag(it, RefTag.update().tag())
                    else
                        it
                }
                .subscribe { openProjectExternally.onNext(it) }
                .addToDisposable(disposables)
        }

        fun provideIntent(intent: Intent?) {
            intent?.let {
                this.intent.onNext(it)
            }
        }

        /**
         * Parses a request for project and update params.
         *
         * @param request   Comments or update request.
         * @return Pair of project param string and update param string.
         */
        private fun projectUpdateParams(request: Request): Pair<String, String> {
            // todo: build a Navigation helper for better param extraction
            val projectParam = request.url.encodedPathSegments[2]
            val updateParam = request.url.encodedPathSegments[4]
            return Pair.create(projectParam, updateParam)
        }

        override fun goToCommentsRequest(request: Request) = goToCommentsRequest.onNext(request)

        override fun goToProjectRequest(request: Request) = goToProjectRequest.onNext(request)

        override fun goToUpdateRequest(request: Request) = goToUpdateRequest.onNext(request)

        override fun shareIconButtonClicked() = shareButtonClicked.onNext(Unit)

        override fun goToCommentsActivity() = goToCommentsActivity.onNext(Unit)

        override fun goToCommentsActivityToDeepLinkThreadActivity(commentableID: String) =
            goToCommentsActivityToDeepLinkThreadActivity.onNext(commentableID)

        override fun openProjectExternally(): Observable<String> = openProjectExternally

        override fun startShareIntent(): Observable<Pair<Update, String>> = startShareIntent

        override fun startRootCommentsActivity(): Observable<Update> = startRootCommentsActivity

        override fun deepLinkToThreadActivity(): Observable<Pair<String, Boolean>> =
            deepLinkToThreadActivity

        override fun startProjectActivity(): Observable<Pair<Uri, RefTag>> = startProjectActivity

        override fun startRootCommentsActivityToDeepLinkThreadActivity(): Observable<Pair<String, Update>> =
            startRootCommentsActivityToDeepLinkThreadActivity

        override fun updateSequence(): Observable<String> = updateSequence

        override fun webViewUrl(): Observable<String> = webViewUrl

        override fun hasCommentsDeepLinks(): Observable<Boolean> = deepLinkToRootComment

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpdateViewModel(environment) as T
        }
    }
}
