package com.kickstarter.viewmodels

import android.content.Intent
import android.graphics.Typeface
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.R
import com.kickstarter.libs.ApiPaginatorV2
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isZero
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.Mailbox
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface MessageThreadsViewModel {
    interface Inputs {
        fun mailbox(mailbox: Mailbox)

        /** Call when pagination should happen.  */
        fun nextPage()

        /** Call when onResume of the activity's lifecycle happens.  */
        fun onResume()

        /** Call when the swipe refresher is invoked.  */
        fun swipeRefresh()
    }

    interface Outputs {
        /** Emits a boolean to determine if there are no messages.  */
        fun hasNoMessages(): Observable<Boolean>

        /** Emits a boolean to determine if there are no unread messages.  */
        fun hasNoUnreadMessages(): Observable<Boolean>

        /** Emits a boolean indicating whether message threads are being fetched from the API.  */
        fun isFetchingMessageThreads(): Observable<Boolean>

        /** Emits a string resource integer to set the mailbox title text view to.  */
        fun mailboxTitle(): Observable<Int>

        /** Emits a list of message threads to be displayed.  */
        fun messageThreadList(): Observable<List<MessageThread>>

        /** Emits a color integer to set the unread count text view to.  */
        fun unreadCountTextViewColorInt(): Observable<Int>

        /** Emits a typeface integer to set the unread count text view to.  */
        fun unreadCountTextViewTypefaceInt(): Observable<Int>

        /** Emits a boolean to determine if the unread count toolbar text view should be gone.  */
        fun unreadCountToolbarTextViewIsGone(): Observable<Boolean>

        /** Emits the unread message count to be displayed.  */
        fun unreadMessagesCount(): Observable<Int>

        /** Emits a boolean determining the unread messages count visibility.  */
        fun unreadMessagesCountIsGone(): Observable<Boolean>
    }

    class MessageThreadsViewModel(environment: Environment, private val intent: Intent? = null) : ViewModel(), Inputs, Outputs {
        private val client: ApiClientTypeV2
        private val currentUser: CurrentUserTypeV2

        private val disposables = CompositeDisposable()
        private fun getStringResForMailbox(mailbox: Mailbox): Int {
            return if (mailbox === Mailbox.INBOX) {
                R.string.messages_navigation_inbox
            } else {
                R.string.messages_navigation_sent
            }
        }

        private val mailbox = PublishSubject.create<Mailbox>()
        private val nextPage = PublishSubject.create<Unit>()
        private val onResume = PublishSubject.create<Unit>()
        private val swipeRefresh = PublishSubject.create<Unit>()
        private val hasNoMessages = BehaviorSubject.create<Boolean>()
        private val hasNoUnreadMessages = BehaviorSubject.create<Boolean>()
        private val isFetchingMessageThreads = BehaviorSubject.create<Boolean>()
        private val mailboxTitle = BehaviorSubject.create<Int>()
        private val messageThreadList = BehaviorSubject.create<List<MessageThread>>()
        private val unreadCountTextViewColorInt = BehaviorSubject.create<Int>()
        private val unreadCountTextViewTypefaceInt = BehaviorSubject.create<Int>()
        private val unreadCountToolbarTextViewIsGone: Observable<Boolean>
        private val unreadMessagesCount = BehaviorSubject.create<Int>()
        private val unreadMessagesCountIsGone: Observable<Boolean>

        val inputs: Inputs = this
        val outputs: Outputs = this

        private fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()
        init {
            client = requireNotNull(environment.apiClientV2())
            currentUser = requireNotNull(environment.currentUserV2())

            // NB: project from intent can be null.
            val initialProject = intent()
                .distinctUntilChanged()
                .filter { it.getParcelableExtra<Project>(IntentKey.PROJECT) != null }
                .map { i: Intent -> i.getParcelableExtra<Project>(IntentKey.PROJECT) }

            val refreshUserOrProject = Observable.merge(onResume, swipeRefresh)

            intent()
                .filter { it.isNotNull() }
                .compose(Transformers.takeWhenV2(refreshUserOrProject))
                .switchMap {
                    client.fetchCurrentUser()
                }
                .compose(Transformers.neverErrorV2())
                .distinctUntilChanged()
                .subscribe {
                    currentUser.refresh(it)
                }
                .addToDisposable(disposables)

            val refreshedProject = initialProject.compose(Transformers.takeWhenV2(refreshUserOrProject))
                .filter { it.param().isNotNull() }
                .map { it.param() }
                .switchMap { param ->
                    client.fetchProject(param)
                }
                .distinctUntilChanged()
                .compose(Transformers.neverErrorV2())
                .share()

            val project = Observable.merge(
                initialProject,
                refreshedProject
            )

            // vm configured with creator
            val unreadFromUser = currentUser.loggedInUser()
                .map { it.unreadMessagesCount() }

            // Use project unread messages count if configured with a project,
            // in the case of a creator viewing their project's messages
            val unreadFromProject = project
                .map { it.unreadMessagesCount() }

            val unreadMessagesCount =
                Observable.merge(unreadFromUser, unreadFromProject)
                    .distinctUntilChanged()

            // todo: MessageSubject switch will also trigger refresh
            val refreshMessageThreads = Observable.merge(
                unreadMessagesCount.compose(Transformers.ignoreValuesV2()),
                swipeRefresh
            )

            val mailbox = mailbox
                .startWith(Mailbox.INBOX)
                .distinctUntilChanged()

            mailbox
                .map { getStringResForMailbox(it) }
                .subscribe { mailboxTitle.onNext(it) }
                .addToDisposable(disposables)

            val projectAndMailbox = mailbox
                // start wil empty project instead of null
                .withLatestFrom(project.startWith(Project.Builder().build())) { box, proj ->
                    Pair(box, proj)
                }

            val startOverWith =
                Observable.combineLatest(
                    projectAndMailbox,
                    refreshMessageThreads
                ) { a, b: Unit -> a }
                    .distinctUntilChanged()

            val paginator =
                ApiPaginatorV2.builder<MessageThread, MessageThreadsEnvelope, Pair<Mailbox, Project>>()
                    .nextPage(nextPage)
                    .startOverWith(startOverWith)
                    .envelopeToListOfData { it.messageThreads() }
                    .envelopeToMoreUrl {
                        it.urls().api().moreMessageThreads()
                    }
                    .loadWithParams {
                        // - if empty project send null to the API
                        val proj = if (it.second.name().isNotEmpty()) it.second else null
                        client.fetchMessageThreads(
                            proj,
                            it.first
                        )
                    }
                    .loadWithPaginationPath {
                        client.fetchMessageThreadsWithPaginationPath(
                            it
                        )
                    }
                    .clearWhenStartingOver(true)
                    .build()

            paginator.isFetching
                .subscribe { isFetchingMessageThreads.onNext(it) }
                .addToDisposable(disposables)

            paginator.paginatedData()
                .distinctUntilChanged()
                .subscribe {
                    messageThreadList.onNext(it)
                }
                .addToDisposable(disposables)

            unreadMessagesCount
                .map { it.isZero() }
                .subscribe { hasNoMessages.onNext(it) }
                .addToDisposable(disposables)

            unreadMessagesCount
                .map { it.isZero() }
                .subscribe { hasNoUnreadMessages.onNext(it) }
                .addToDisposable(disposables)

            unreadMessagesCount
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .map { if (it.intValueOrZero() > 0) R.color.accent else R.color.kds_support_400 }
                .subscribe { unreadCountTextViewColorInt.onNext(it) }
                .addToDisposable(disposables)

            unreadMessagesCount
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .map { if (it.intValueOrZero() > 0) Typeface.BOLD else Typeface.NORMAL }
                .subscribe { unreadCountTextViewTypefaceInt.onNext(it) }
                .addToDisposable(disposables)

            unreadCountToolbarTextViewIsGone =
                Observable.zip<Boolean, Boolean, Pair<Boolean, Boolean>>(
                    hasNoMessages,
                    hasNoUnreadMessages
                ) { a: Boolean?, b: Boolean? -> Pair.create(a, b) }
                    .map { noMessagesAndNoUnread: Pair<Boolean, Boolean> -> noMessagesAndNoUnread.first || noMessagesAndNoUnread.second }
                    .compose(Transformers.combineLatestPair(mailbox))
                    .map { noMessagesAndMailbox: Pair<Boolean, Mailbox> -> noMessagesAndMailbox.first || noMessagesAndMailbox.second == Mailbox.SENT }

            unreadMessagesCount
                .filter { it.isNotNull() }
                .filter { it.isNonZero() }
                .subscribe(this.unreadMessagesCount)
            unreadMessagesCountIsGone = mailbox
                .map { m: Mailbox -> m == Mailbox.SENT }
        }
        override fun mailbox(mailbox: Mailbox) {
            this.mailbox.onNext(mailbox)
        }

        override fun nextPage() {
            nextPage.onNext(Unit)
        }

        override fun onResume() {
            onResume.onNext(Unit)
        }

        override fun swipeRefresh() {
            swipeRefresh.onNext(Unit)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        override fun hasNoMessages(): Observable<Boolean> = hasNoMessages
        override fun hasNoUnreadMessages(): Observable<Boolean> = hasNoUnreadMessages
        override fun isFetchingMessageThreads(): Observable<Boolean> = isFetchingMessageThreads
        override fun mailboxTitle(): Observable<Int> = mailboxTitle
        override fun messageThreadList(): Observable<List<MessageThread>> = messageThreadList
        override fun unreadCountTextViewColorInt(): Observable<Int> = unreadCountTextViewColorInt
        override fun unreadCountTextViewTypefaceInt(): Observable<Int> = unreadCountTextViewTypefaceInt
        override fun unreadCountToolbarTextViewIsGone(): Observable<Boolean> = unreadCountToolbarTextViewIsGone
        override fun unreadMessagesCount(): Observable<Int> = unreadMessagesCount
        override fun unreadMessagesCountIsGone(): Observable<Boolean> = unreadMessagesCountIsGone
    }

    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MessageThreadsViewModel(environment, intent) as T
        }
    }
}
