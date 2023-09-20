package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Either
import com.kickstarter.libs.Either.Left
import com.kickstarter.libs.Either.Right
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.PairUtils
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isPresent
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Message
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.data.MessageSubject
import com.kickstarter.ui.data.MessagesData
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface MessagesViewModel {
    interface Inputs {
        /** Call with the app bar's vertical offset value.  */
        fun appBarOffset(verticalOffset: Int)

        /** Call with the app bar's total scroll range.  */
        fun appBarTotalScrollRange(totalScrollRange: Int)

        /** Call when the back or close button has been clicked.  */
        fun backOrCloseButtonClicked()

        /** Call when the message edit text changes.  */
        fun messageEditTextChanged(messageBody: String)

        /** Call when the message edit text is in focus.  */
        fun messageEditTextIsFocused(isFocused: Boolean)

        /** Call when the send message button has been clicked.  */
        fun sendMessageButtonClicked()

        /** Call when the view pledge button is clicked.  */
        fun viewPledgeButtonClicked()
    }

    interface Outputs {
        /** Emits a boolean that determines if the back button should be gone.  */
        fun backButtonIsGone(): Observable<Boolean>

        /** Emits the backing and project to populate the backing info header.  */
        fun backingAndProject(): Observable<Pair<Backing, Project>>

        /** Emits a boolean that determines if the backing info view should be gone.  */
        fun backingInfoViewIsGone(): Observable<Boolean>

        /** Emits a boolean that determines if the close button should be gone.  */
        fun closeButtonIsGone(): Observable<Boolean>

        /** Emits the creator name to be displayed.  */
        fun creatorNameTextViewText(): Observable<String>

        /** Emits when we should navigate back.  */
        fun goBack(): Observable<Void>

        /** Emits a boolean to determine if the loading indicator should be gone.  */
        fun loadingIndicatorViewIsGone(): Observable<Boolean>

        /** Emits a string to display as the message edit text hint.  */
        fun messageEditTextHint(): Observable<String>

        /** Emits when the edit text should request focus.  */
        fun messageEditTextShouldRequestFocus(): Observable<Void>

        /** Emits a list of messages to be displayed.  */
        fun messageList(): Observable<List<Message>?>

        /** Emits the project name to be displayed.  */
        fun projectNameTextViewText(): Observable<String>

        /** Emits the project name to be displayed in the toolbar.  */
        fun projectNameToolbarTextViewText(): Observable<String>

        /** Emits the bottom padding for the recycler view.  */
        fun recyclerViewDefaultBottomPadding(): Observable<Void>

        /** Emits the initial bottom padding for the recycler view to account for the app bar scroll range.  */
        fun recyclerViewInitialBottomPadding(): Observable<Int>

        /** Emits when the RecyclerView should be scrolled to the bottom.  */
        fun scrollRecyclerViewToBottom(): Observable<Void>

        /** Emits a boolean that determines if the Send button should be enabled.  */
        fun sendMessageButtonIsEnabled(): Observable<Boolean>

        /** Emits a string to set the message edit text to.  */
        fun setMessageEditText(): Observable<String>

        /** Emits a string to display in the message error toast.  */
        fun showMessageErrorToast(): Observable<String>

        /** Emits when we should start the [BackingActivity].  */
        fun startBackingActivity(): Observable<BackingWrapper>

        /** Emits when the thread has been marked as read.  */
        fun successfullyMarkedAsRead(): Observable<Void>

        /** Emits a boolean to determine when the toolbar should be expanded.  */
        fun toolbarIsExpanded(): Observable<Boolean>

        /** Emits a boolean that determines if the View pledge button should be gone.  */
        fun viewPledgeButtonIsGone(): Observable<Boolean>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<MessagesActivity?>(environment),
        Inputs,
        Outputs {
        private val client: ApiClientType
        private val currentUser: CurrentUserType

        private fun projectAndBacker(envelopeAndData: Pair<MessageThreadEnvelope, MessagesData>): Pair<Project, User> {
            val project = envelopeAndData.second.project
            val backer =
                if (project.isBacking()) envelopeAndData.second.currentUser else envelopeAndData.first.messageThread()
                    ?.participant()
            return Pair.create(project, backer)
        }

        private val appBarOffset = PublishSubject.create<Int>()
        private val appBarTotalScrollRange = PublishSubject.create<Int>()
        private val backOrCloseButtonClicked = PublishSubject.create<Void>()
        private val messageEditTextChanged = PublishSubject.create<String>()
        private val messageEditTextIsFocused = PublishSubject.create<Boolean?>()
        private val sendMessageButtonClicked = PublishSubject.create<Void>()
        private val viewPledgeButtonClicked = PublishSubject.create<Void>()
        private val backButtonIsGone: Observable<Boolean>
        private val backingAndProject = BehaviorSubject.create<Pair<Backing, Project>?>()
        private val backingInfoViewIsGone = BehaviorSubject.create<Boolean>()
        private val closeButtonIsGone: Observable<Boolean>
        private val creatorNameTextViewText = BehaviorSubject.create<String>()
        private val goBack: Observable<Void>
        private val loadingIndicatorViewIsGone: Observable<Boolean>
        private val messageEditTextHint = BehaviorSubject.create<String>()
        private val messageEditTextShouldRequestFocus = PublishSubject.create<Void>()
        private val messageList = BehaviorSubject.create<List<Message>?>()
        private val projectNameTextViewText = BehaviorSubject.create<String>()
        private val projectNameToolbarTextViewText: Observable<String>
        private val recyclerViewDefaultBottomPadding: Observable<Void>
        private val recyclerViewInitialBottomPadding: Observable<Int>
        private val scrollRecyclerViewToBottom: Observable<Void>
        private val showMessageErrorToast = PublishSubject.create<String>()
        private val sendMessageButtonIsEnabled: Observable<Boolean>
        private val setMessageEditText: Observable<String>
        private val startBackingActivity = PublishSubject.create<BackingWrapper>()
        private val successfullyMarkedAsRead = BehaviorSubject.create<Void>()
        private val toolbarIsExpanded: Observable<Boolean>
        private val viewPledgeButtonIsGone = BehaviorSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        override fun appBarOffset(verticalOffset: Int) {
            appBarOffset.onNext(verticalOffset)
        }

        override fun appBarTotalScrollRange(totalScrollRange: Int) {
            appBarTotalScrollRange.onNext(totalScrollRange)
        }

        override fun backOrCloseButtonClicked() {
            backOrCloseButtonClicked.onNext(null)
        }

        override fun messageEditTextChanged(messageBody: String) {
            messageEditTextChanged.onNext(messageBody)
        }

        override fun messageEditTextIsFocused(isFocused: Boolean) {
            messageEditTextIsFocused.onNext(isFocused)
        }

        override fun sendMessageButtonClicked() {
            sendMessageButtonClicked.onNext(null)
        }

        override fun viewPledgeButtonClicked() {
            viewPledgeButtonClicked.onNext(null)
        }

        override fun backButtonIsGone(): Observable<Boolean> = backButtonIsGone
        override fun backingAndProject(): Observable<Pair<Backing, Project>> = backingAndProject
        override fun backingInfoViewIsGone(): Observable<Boolean> = backingInfoViewIsGone
        override fun closeButtonIsGone(): Observable<Boolean> = closeButtonIsGone
        override fun goBack(): Observable<Void> = goBack
        override fun loadingIndicatorViewIsGone(): Observable<Boolean> = loadingIndicatorViewIsGone
        override fun messageEditTextHint(): Observable<String> = messageEditTextHint
        override fun messageEditTextShouldRequestFocus(): Observable<Void> = messageEditTextShouldRequestFocus
        override fun messageList(): Observable<List<Message>?> = messageList
        override fun creatorNameTextViewText(): Observable<String> = creatorNameTextViewText
        override fun projectNameTextViewText(): Observable<String> = projectNameTextViewText
        override fun projectNameToolbarTextViewText(): Observable<String> = projectNameToolbarTextViewText
        override fun recyclerViewDefaultBottomPadding(): Observable<Void> = recyclerViewDefaultBottomPadding
        override fun recyclerViewInitialBottomPadding(): Observable<Int> = recyclerViewInitialBottomPadding
        override fun scrollRecyclerViewToBottom(): Observable<Void> = scrollRecyclerViewToBottom
        override fun showMessageErrorToast(): Observable<String> = showMessageErrorToast
        override fun sendMessageButtonIsEnabled(): Observable<Boolean> = sendMessageButtonIsEnabled
        override fun setMessageEditText(): Observable<String> = setMessageEditText
        override fun startBackingActivity(): Observable<BackingWrapper> = startBackingActivity
        override fun successfullyMarkedAsRead(): Observable<Void> = successfullyMarkedAsRead
        override fun toolbarIsExpanded(): Observable<Boolean> = toolbarIsExpanded
        override fun viewPledgeButtonIsGone(): Observable<Boolean> = viewPledgeButtonIsGone

        companion object {
            private fun backingAndProjectFromData(
                data: MessagesData,
                client: ApiClientType
            ): Observable<Pair<Backing, Project>?>? {
                return data.backingOrThread.either(
                    { Observable.just(Pair.create(it, data.project)) }
                ) {
                    val backingNotification =
                        if (data.project.isBacking()) client.fetchProjectBacking(
                            data.project,
                            data.currentUser
                        ).materialize().share() else client.fetchProjectBacking(
                            data.project,
                            data.participant
                        ).materialize().share()

                    Observable.merge(
                        backingNotification.compose(Transformers.errors())
                            .map { null },
                        backingNotification.compose(Transformers.values())
                            .map { Pair.create(it, data.project) }
                    )
                        .take(1)
                }
            }
        }

        init {
            client = requireNotNull(environment.apiClient())
            currentUser = requireNotNull(environment.currentUser())

            val configData = intent()
                .map { i: Intent ->
                    val messageThread =
                        i.getParcelableExtra<MessageThread>(IntentKey.MESSAGE_THREAD)
                    messageThread?.let { Left(it) }
                        ?: Right<MessageThread, Pair<Project?, Backing?>>(
                            Pair.create(
                                i.getParcelableExtra(IntentKey.PROJECT),
                                i.getParcelableExtra(IntentKey.BACKING)
                            )
                        )
                }

            val messageAccountTypeObservable = intent()
                .map { it.getSerializableExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT) }
                .ofType(MessagePreviousScreenType::class.java)

            val configBacking = configData
                .map { it.right() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .map { PairUtils.second(it) }
                .map { requireNotNull(it) }

            val configThread = configData
                .map { it.left() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }

            val backingOrThread: Observable<Either<Backing, MessageThread>> = Observable.merge(
                configBacking.map { Left(it) },
                configThread.map { Right(it) }
            )

            val messageIsSending = PublishSubject.create<Boolean>()
            val messagesAreLoading = PublishSubject.create<Boolean>()

            val project = configData
                .map { data: Either<MessageThread, Pair<Project?, Backing?>> ->
                    data.either({ obj: MessageThread -> obj.project() }) {
                            projectAndBacking: Pair<Project?, Backing?> ->
                        projectAndBacking.first
                    }
                }.filter { it.isNotNull() }
                .map { requireNotNull(it) }

            val initialMessageThreadEnvelope = backingOrThread
                .switchMap {
                    val response = it.either({ backing ->
                        client.fetchMessagesForBacking(
                            backing
                        )
                    }) { messageThread ->
                        client.fetchMessagesForThread(
                            messageThread
                        )
                    }
                    response
                        .doOnSubscribe { messagesAreLoading.onNext(true) }
                        .doAfterTerminate { messagesAreLoading.onNext(false) }
                        .compose(Transformers.neverError())
                        .share()
                }

            loadingIndicatorViewIsGone = messagesAreLoading
                .map { it.negate() }
                .distinctUntilChanged()

            // If view model was not initialized with a MessageThread, participant is
            // the project creator.
            val participant =
                Observable.combineLatest(
                    initialMessageThreadEnvelope.map { it.messageThread() },
                    project
                ) { a: MessageThread?, b: Project? -> Pair.create(a, b) }
                    .map {
                        if (it.first != null)
                            it?.first?.participant()
                        else
                            it.second?.creator()
                    }.filter { it.isNotNull() }
                    .map { requireNotNull(it) }
                    .take(1)

            participant
                .map { it.name() }
                .compose(bindToLifecycle())
                .subscribe(messageEditTextHint)

            val messagesData = Observable.combineLatest(
                backingOrThread,
                project,
                participant,
                currentUser.observable()
            ) { backingOrThread: Either<Backing, MessageThread>, project: Project, participant: User, currentUser: User ->
                MessagesData(
                    backingOrThread,
                    project,
                    participant,
                    currentUser
                )
            }

            val messageSubject = messagesData
                .map { (backingOrThread1, project1, _, currentUser1): MessagesData ->
                    backingOrThread1.either( // Message subject is the project if the current user is the backer,
                        // otherwise the current user is the creator and will send a message to the backing.
                        { backing: Backing ->
                            if (backing.backerId() == currentUser1.id()) MessageSubject.Project(
                                project1
                            ) else MessageSubject.Backing(backing)
                        }) { messageThread: MessageThread ->
                        MessageSubject.MessageThread(
                            messageThread
                        )
                    }
                }

            val messageNotification = messageSubject
                .compose(Transformers.combineLatestPair(messageEditTextChanged))
                .compose(
                    Transformers.takeWhen(
                        sendMessageButtonClicked
                    )
                )
                .switchMap {
                    client.sendMessage(
                        it.first, it.second
                    )
                        .doOnSubscribe { messageIsSending.onNext(true) }
                }
                .materialize()
                .share()

            val messageSent = messageNotification.compose(Transformers.values()).ofType(
                Message::class.java
            )

            val sentMessageThreadEnvelope = backingOrThread
                .compose(Transformers.takeWhen(messageSent))
                .switchMap {
                    it.either({ backing: Backing ->
                        client.fetchMessagesForBacking(
                            backing
                        )
                    }) { messageThread: MessageThread ->
                        client.fetchMessagesForThread(
                            messageThread
                        )
                    }
                }
                .compose(Transformers.neverError())
                .share()

            val messageThreadEnvelope = Observable.merge(
                initialMessageThreadEnvelope,
                sentMessageThreadEnvelope
            )
                .distinctUntilChanged()

            val messageHasBody = messageEditTextChanged
                .map { it.isNotNull() && it.isPresent() }

            messageThreadEnvelope
                .map { it.messageThread() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .switchMap {
                    client.markAsRead(
                        it
                    )
                }
                .materialize()
                .compose(Transformers.ignoreValues())
                .compose(bindToLifecycle())
                .subscribe { successfullyMarkedAsRead.onNext(it) }

            val initialMessages = initialMessageThreadEnvelope
                .map { it.messages() }

            val newMessages = sentMessageThreadEnvelope
                .map { it.messages() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }

            // Concat distinct messages to initial message list. Return just the new messages if
            // initial list is null, i.e. a new message thread.
            val updatedMessages = initialMessages
                .compose(
                    Transformers.takePairWhen(
                        newMessages
                    )
                )
                .map {
                    val messagesList = it?.first
                    val message = it?.second?.toList()

                    messagesList?.let { initialMessages ->
                        message?.let {
                            ListUtils.concatDistinct(
                                initialMessages, it
                            )
                        }
                    } ?: it.second
                }

            // Load the initial messages once, subsequently load newer messages if any.
            initialMessages
                .filter { it.isNotNull() }
                .take(1)
                .compose(bindToLifecycle())
                .subscribe { v: List<Message>? -> messageList.onNext(v) }

            updatedMessages
                .compose(bindToLifecycle())
                .subscribe { v: List<Message>? -> messageList.onNext(v) }

            project
                .map { it.creator().name() }
                .compose(bindToLifecycle())
                .subscribe { v: String -> creatorNameTextViewText.onNext(v) }

            initialMessageThreadEnvelope
                .map { it.messages() }
                .filter { it.isNull() }
                .take(1)
                .compose(Transformers.ignoreValues())
                .compose(bindToLifecycle())
                .subscribe { messageEditTextShouldRequestFocus.onNext(it) }

            val backingAndProject = messagesData
                .switchMap { backingAndProjectFromData(it, client) }

            backingAndProject
                .filter { it.isNotNull() }
                .compose(bindToLifecycle())
                .subscribe { this.backingAndProject.onNext(it) }

            backingAndProject
                .map { it.isNull() }
                .compose(bindToLifecycle())
                .subscribe { backingInfoViewIsGone.onNext(it) }

            messageAccountTypeObservable
                .map { c: MessagePreviousScreenType -> c == MessagePreviousScreenType.BACKER_MODAL }
                .compose(bindToLifecycle())
                .subscribe { v: Boolean -> viewPledgeButtonIsGone.onNext(v) }

            backButtonIsGone = viewPledgeButtonIsGone.map { it.negate() }

            closeButtonIsGone = backButtonIsGone.map { it.negate() }

            goBack = backOrCloseButtonClicked

            projectNameToolbarTextViewText = projectNameTextViewText

            scrollRecyclerViewToBottom = updatedMessages.compose(Transformers.ignoreValues())

            sendMessageButtonIsEnabled = Observable.merge(
                messageHasBody, messageIsSending.map { it.negate() }
            )

            setMessageEditText = messageSent.map { "" }

            toolbarIsExpanded = messageList
                .compose(
                    Transformers.takePairWhen(
                        messageEditTextIsFocused
                    )
                )
                .map { PairUtils.second(it) }
                .map { it.negate() }

            messageNotification
                .compose(Transformers.errors())
                .map { ErrorEnvelope.fromThrowable(it) }
                .map { it?.errorMessage() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe { showMessageErrorToast.onNext(it) }

            project
                .map { it.name() }
                .compose(bindToLifecycle())
                .subscribe { projectNameTextViewText.onNext(it) }

            messageThreadEnvelope
                .compose(Transformers.combineLatestPair(messagesData))
                .compose(Transformers.takeWhen(viewPledgeButtonClicked))
                .map {
                    projectAndBacker(
                        it
                    )
                }
                .compose(
                    Transformers.combineLatestPair(
                        backingAndProject.filter { it.isNotNull() }
                            .map { requireNotNull(it) }
                    )
                )
                .map {
                    BackingWrapper(
                        it.second.first, it.first.second, it.first.first
                    )
                }
                .compose(bindToLifecycle())
                .subscribe { v: BackingWrapper -> startBackingActivity.onNext(v) }

            // Set only the initial padding once to counteract the appbar offset.
            recyclerViewInitialBottomPadding = appBarTotalScrollRange.take(1)

            // Take only the first instance in which the offset changes.
            recyclerViewDefaultBottomPadding = appBarOffset
                .filter { it.isNonZero() }
                .compose(Transformers.ignoreValues())
                .take(1)
        }
    }
}
