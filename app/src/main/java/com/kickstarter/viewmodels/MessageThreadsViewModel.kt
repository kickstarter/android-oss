package com.kickstarter.viewmodels

import android.content.Intent
import android.graphics.Typeface
import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.ApiPaginator
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.libs.utils.extensions.intValueOrZero
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isZero
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.MessageThreadsActivity
import com.kickstarter.ui.data.Mailbox
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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

    class ViewModel(environment: Environment) :
        ActivityViewModel<MessageThreadsActivity?>(environment), Inputs, Outputs {
        private val client: ApiClientType?
        private val currentUser: CurrentUserType?
        private fun getStringResForMailbox(mailbox: Mailbox): Int {
            return if (mailbox === Mailbox.INBOX) {
                R.string.messages_navigation_inbox
            } else {
                R.string.messages_navigation_sent
            }
        }

        private val mailbox = PublishSubject.create<Mailbox>()
        private val nextPage = PublishSubject.create<Void?>()
        private val onResume = PublishSubject.create<Void?>()
        private val swipeRefresh = PublishSubject.create<Void?>()
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

        override fun mailbox(mailbox: Mailbox) {
            this.mailbox.onNext(mailbox)
        }

        override fun nextPage() {
            nextPage.onNext(null)
        }

        override fun onResume() {
            onResume.onNext(null)
        }

        override fun swipeRefresh() {
            swipeRefresh.onNext(null)
        }

        override fun hasNoMessages(): Observable<Boolean> {
            return hasNoMessages
        }

        override fun hasNoUnreadMessages(): Observable<Boolean> {
            return hasNoUnreadMessages
        }

        override fun isFetchingMessageThreads(): Observable<Boolean> {
            return isFetchingMessageThreads
        }

        override fun mailboxTitle(): Observable<Int> {
            return mailboxTitle
        }

        override fun messageThreadList(): Observable<List<MessageThread>> {
            return messageThreadList
        }

        override fun unreadCountTextViewColorInt(): Observable<Int> {
            return unreadCountTextViewColorInt
        }

        override fun unreadCountTextViewTypefaceInt(): Observable<Int> {
            return unreadCountTextViewTypefaceInt
        }

        override fun unreadCountToolbarTextViewIsGone(): Observable<Boolean> {
            return unreadCountToolbarTextViewIsGone
        }

        override fun unreadMessagesCount(): Observable<Int> {
            return unreadMessagesCount
        }

        override fun unreadMessagesCountIsGone(): Observable<Boolean> {
            return unreadMessagesCountIsGone
        }

        init {
            client = requireNotNull(environment.apiClient())
            currentUser = requireNotNull(environment.currentUser())

            // NB: project from intent can be null.
            val initialProject = intent()
                .map { i: Intent -> i.getParcelableExtra<Project>(IntentKey.PROJECT) }

            val refreshUserOrProject = Observable.merge(onResume, swipeRefresh)
            val freshUser = intent()
                .compose(Transformers.takeWhen(refreshUserOrProject))
                .switchMap { client.fetchCurrentUser() }
                .retry(2)
                .compose(Transformers.neverError())

            freshUser.subscribe {
                currentUser.refresh(
                    it
                )
            }
            val project = Observable.merge(
                initialProject,
                initialProject
                    .compose(Transformers.takeWhen(refreshUserOrProject))
                    .map { it?.param() }
                    .switchMap {
                        it?.let {
                            client.fetchProject(
                                it
                            )
                        }
                    }
                    .compose(Transformers.neverError())
                    .share()
            )

            // Use project unread messages count if configured with a project,
            // in the case of a creator viewing their project's messages.
            val unreadMessagesCount =
                Observable.combineLatest<Project?, User, Pair<Project?, User>>(
                    project,
                    currentUser.loggedInUser()
                ) { a: Project?, b: User? -> Pair.create(a, b) }
                    .map {
                        if (it.first != null)
                            it.first?.unreadMessagesCount()
                        else
                            it.second.unreadMessagesCount()
                    }
                    .distinctUntilChanged()

            // todo: MessageSubject switch will also trigger refresh
            val refreshMessageThreads = Observable.merge(
                unreadMessagesCount.compose(Transformers.ignoreValues()),
                swipeRefresh
            )
            val mailbox = mailbox
                .startWith(Mailbox.INBOX)
                .distinctUntilChanged()
            mailbox
                .map { mailbox: Mailbox -> getStringResForMailbox(mailbox) }
                .compose(bindToLifecycle())
                .subscribe(mailboxTitle)

            val projectAndMailbox =
                Observable.combineLatest<Project?, Mailbox, Pair<Project, Mailbox>>(
                    project.distinctUntilChanged(), mailbox.distinctUntilChanged()
                ) { a: Project?, b: Mailbox? -> Pair.create(a, b) }

            val startOverWith =
                Observable.combineLatest(
                    projectAndMailbox,
                    refreshMessageThreads
                ) { a: Pair<Project, Mailbox>?, b: Void? -> Pair.create(a, b) }
                    .map { PairUtils.first(it) }

            val paginator =
                ApiPaginator.builder<MessageThread, MessageThreadsEnvelope, Pair<Project, Mailbox>>()
                    .nextPage(nextPage)
                    .startOverWith(startOverWith)
                    .envelopeToListOfData { obj: MessageThreadsEnvelope -> obj.messageThreads() }
                    .envelopeToMoreUrl { env: MessageThreadsEnvelope ->
                        env.urls().api().moreMessageThreads()
                    }
                    .loadWithParams { pm: Pair<Project, Mailbox> ->
                        client.fetchMessageThreads(
                            pm.first,
                            pm.second
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
                .compose(bindToLifecycle())
                .subscribe(isFetchingMessageThreads)
            paginator.paginatedData()
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(messageThreadList)
            unreadMessagesCount
                .map { it.isZero() }
                .subscribe(hasNoMessages)
            unreadMessagesCount
                .map { it.isZero() }
                .subscribe(hasNoUnreadMessages)
            unreadMessagesCount
                .map { if (it?.intValueOrZero()!! > 0) R.color.accent else R.color.kds_support_400 }
                .subscribe(unreadCountTextViewColorInt)
            unreadMessagesCount
                .map { if (it?.intValueOrZero()!! > 0) Typeface.BOLD else Typeface.NORMAL }
                .subscribe(unreadCountTextViewTypefaceInt)
            unreadCountToolbarTextViewIsGone =
                Observable.zip<Boolean, Boolean, Pair<Boolean, Boolean>>(
                    hasNoMessages,
                    hasNoUnreadMessages
                ) { a: Boolean?, b: Boolean? -> Pair.create(a, b) }
                    .map { noMessagesAndNoUnread: Pair<Boolean, Boolean> -> noMessagesAndNoUnread.first || noMessagesAndNoUnread.second }
                    .compose(Transformers.combineLatestPair(mailbox))
                    .map { noMessagesAndMailbox: Pair<Boolean, Mailbox> -> noMessagesAndMailbox.first || noMessagesAndMailbox.second == Mailbox.SENT }
            unreadMessagesCount
                .filter { `object`: Int? -> ObjectUtils.isNotNull(`object`) }
                .filter { it.isNonZero() }
                .subscribe(this.unreadMessagesCount)
            unreadMessagesCountIsGone = mailbox
                .map { m: Mailbox -> m == Mailbox.SENT }
        }
    }
}
