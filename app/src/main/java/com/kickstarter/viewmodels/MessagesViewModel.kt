package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Either
import com.kickstarter.libs.Either.Left
import com.kickstarter.libs.Either.Right
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isPresent
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Message
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.MessageSubject
import com.kickstarter.ui.data.MessagesData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface MessagesViewModel {
    interface Inputs {
        /** Call with the app bar's vertical offset value.  */
        fun appBarOffset(verticalOffset: Int)

        /** Call with the app bar's total scroll range.  */
        fun appBarTotalScrollRange(totalScrollRange: Int)

        /** Call when the message edit text changes.  */
        fun messageEditTextChanged(messageBody: String)

        /** Call when the message edit text is in focus.  */
        fun messageEditTextIsFocused(isFocused: Boolean)

        /** Call when the send message button has been clicked.  */
        fun sendMessageButtonClicked()

        /** Call when the view pledge button is clicked.  */
        fun viewPledgeButtonClicked()

        /** Call when the project container view is clicked.  */
        fun projectContainerViewClicked()
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

        /** Emits a boolean to determine if the loading indicator should be gone.  */
        fun loadingIndicatorViewIsGone(): Observable<Boolean>

        /** Emits a string to display as the message edit text hint.  */
        fun messageEditTextHint(): Observable<String>

        /** Emits when the edit text should request focus.  */
        fun messageEditTextShouldRequestFocus(): Observable<Unit>

        /** Emits a list of messages to be displayed.  */
        fun messageList(): Observable<List<Message>>

        /** Emits the project name to be displayed.  */
        fun projectNameTextViewText(): Observable<String>

        /** Emits the project name to be displayed in the toolbar.  */
        fun projectNameToolbarTextViewText(): Observable<String>

        /** Emits the bottom padding for the recycler view.  */
        fun recyclerViewDefaultBottomPadding(): Observable<Unit>

        /** Emits the initial bottom padding for the recycler view to account for the app bar scroll range.  */
        fun recyclerViewInitialBottomPadding(): Observable<Int>

        /** Emits when the RecyclerView should be scrolled to the bottom.  */
        fun scrollRecyclerViewToBottom(): Observable<Unit>

        /** Emits a boolean that determines if the Send button should be enabled.  */
        fun sendMessageButtonIsEnabled(): Observable<Boolean>

        /** Emits a string to set the message edit text to.  */
        fun setMessageEditText(): Observable<String>

        /** Emits a string to display in the message error toast.  */
        fun showMessageErrorToast(): Observable<String>

        /** Emits when we should start the [BackingActivity].  */
        fun startBackingActivity(): Observable<BackingWrapper>

        /** Emits when we should start the [ProjectPageActivity].  */
        fun startProjectPageActivity(): Observable<Project>

        /** Emits when the thread has been marked as read.  */
        fun successfullyMarkedAsRead(): Observable<Unit>

        /** Emits a boolean to determine when the toolbar should be expanded.  */
        fun toolbarIsExpanded(): Observable<Boolean>

        /** Emits a boolean that determines if the View pledge button should be gone.  */
        fun viewPledgeButtonIsGone(): Observable<Boolean>
    }

    class MessagesViewModel(environment: Environment, private val intent: Intent? = null) :
        ViewModel(),
        Inputs,
        Outputs {
        private val client: ApiClientTypeV2
        private val currentUser: CurrentUserTypeV2
        private val disposables = CompositeDisposable()

        private fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()
        private fun projectAndBacker(envelopeAndData: Pair<MessageThreadEnvelope, MessagesData>): Pair<Project, User> {
            val project = envelopeAndData.second.project
            val backer =
                if (project.isBacking()) envelopeAndData.second.currentUser else envelopeAndData.first.messageThread()
                    ?.participant()
            return Pair.create(project, backer)
        }

        private val appBarOffset = PublishSubject.create<Int>()
        private val appBarTotalScrollRange = PublishSubject.create<Int>()
        private val messageEditTextChanged = PublishSubject.create<String>()
        private val messageEditTextIsFocused = PublishSubject.create<Boolean>()
        private val sendMessageButtonClicked = PublishSubject.create<Unit>()
        private val projectContainerViewClicked = PublishSubject.create<Unit>()
        private val viewPledgeButtonClicked = PublishSubject.create<Unit>()
        private val backButtonIsGone: Observable<Boolean>
        private val backingAndProject = BehaviorSubject.create<Pair<Backing, Project>>()
        private val backingInfoViewIsGone = BehaviorSubject.create<Boolean>()
        private val closeButtonIsGone: Observable<Boolean>
        private val creatorNameTextViewText = BehaviorSubject.create<String>()
        private val loadingIndicatorViewIsGone: Observable<Boolean>
        private val messageEditTextHint = BehaviorSubject.create<String>()
        private val messageEditTextShouldRequestFocus = BehaviorSubject.create<Unit>()
        private val messageList = BehaviorSubject.create<List<Message>>()
        private val projectNameTextViewText = BehaviorSubject.create<String>()
        private val projectNameToolbarTextViewText: Observable<String>
        private val recyclerViewDefaultBottomPadding: Observable<Unit>
        private val recyclerViewInitialBottomPadding: Observable<Int>
        private val scrollRecyclerViewToBottom: Observable<Unit>
        private val showMessageErrorToast = PublishSubject.create<String>()
        private val sendMessageButtonIsEnabled: Observable<Boolean>
        private val setMessageEditText: Observable<String>
        private val startBackingActivity = PublishSubject.create<BackingWrapper>()
        private val startProjectPageActivity = PublishSubject.create<Project>()
        private val successfullyMarkedAsRead = BehaviorSubject.create<Unit>()
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

        override fun messageEditTextChanged(messageBody: String) {
            messageEditTextChanged.onNext(messageBody)
        }

        override fun messageEditTextIsFocused(isFocused: Boolean) {
            messageEditTextIsFocused.onNext(isFocused)
        }

        override fun sendMessageButtonClicked() {
            sendMessageButtonClicked.onNext(Unit)
        }

        override fun viewPledgeButtonClicked() {
            viewPledgeButtonClicked.onNext(Unit)
        }

        override fun projectContainerViewClicked() {
            projectContainerViewClicked.onNext(Unit)
        }

        override fun backButtonIsGone(): Observable<Boolean> = backButtonIsGone
        override fun backingAndProject(): Observable<Pair<Backing, Project>> = backingAndProject
        override fun backingInfoViewIsGone(): Observable<Boolean> = backingInfoViewIsGone
        override fun closeButtonIsGone(): Observable<Boolean> = closeButtonIsGone
        override fun loadingIndicatorViewIsGone(): Observable<Boolean> = loadingIndicatorViewIsGone
        override fun messageEditTextHint(): Observable<String> = messageEditTextHint
        override fun messageEditTextShouldRequestFocus(): Observable<Unit> = messageEditTextShouldRequestFocus
        override fun messageList(): Observable<List<Message>> = messageList
        override fun creatorNameTextViewText(): Observable<String> = creatorNameTextViewText
        override fun projectNameTextViewText(): Observable<String> = projectNameTextViewText
        override fun projectNameToolbarTextViewText(): Observable<String> = projectNameToolbarTextViewText
        override fun recyclerViewDefaultBottomPadding(): Observable<Unit> = recyclerViewDefaultBottomPadding
        override fun recyclerViewInitialBottomPadding(): Observable<Int> = recyclerViewInitialBottomPadding
        override fun scrollRecyclerViewToBottom(): Observable<Unit> = scrollRecyclerViewToBottom
        override fun showMessageErrorToast(): Observable<String> = showMessageErrorToast
        override fun sendMessageButtonIsEnabled(): Observable<Boolean> = sendMessageButtonIsEnabled
        override fun setMessageEditText(): Observable<String> = setMessageEditText
        override fun startBackingActivity(): Observable<BackingWrapper> = startBackingActivity
        override fun startProjectPageActivity(): Observable<Project> = startProjectPageActivity
        override fun successfullyMarkedAsRead(): Observable<Unit> = successfullyMarkedAsRead
        override fun toolbarIsExpanded(): Observable<Boolean> = toolbarIsExpanded
        override fun viewPledgeButtonIsGone(): Observable<Boolean> = viewPledgeButtonIsGone

        companion object {
            private fun backingAndProjectFromData(
                data: MessagesData,
                client: ApiClientTypeV2
            ): Observable<Pair<Backing?, Project>> {
                val backingAndProjectObs = data.backingOrThread.either(
                    ifLeft = {
                        return@either Observable.just(Pair.create<Backing, Project>(it, data.project))
                    },
                    ifRight = {
                        val backingNotification =
                            if (data.project.isBacking()) client.fetchProjectBacking(
                                data.project,
                                data.currentUser
                            ).materialize().share() else client.fetchProjectBacking(
                                data.project,
                                data.participant
                            ).materialize().share()

                        return@either backingNotification
                            .filter { data.project.isNotNull() }
                            .map {
                                Pair.create(it.value, data.project)
                            }
                            .take(1)
                    }
                )

                return backingAndProjectObs
            }
        }

        init {
            client = requireNotNull(environment.apiClientV2())
            currentUser = requireNotNull(environment.currentUserV2())

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
                .filter { it.isNotNull() && it.right().isNotNull() }
                .map { requireNotNull(it.right()) }
                .filter { it.second.isNotNull() }
                .map { requireNotNull(it.second) }

            val configThread = configData
                .filter { it.isNotNull() && it.left().isNotNull() }
                .map { requireNotNull(it.left()) }

            val backingOrThread: Observable<Either<Backing, MessageThread>> = Observable.merge<Either<Backing, MessageThread>?>(
                configBacking.filter { it.isNotNull() }.map { Left(it) },
                configThread.filter { it.isNotNull() }.map { Right(it) }
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
                    it.either(
                        ifLeft = { backing ->
                            client.fetchMessagesForBacking(backing)
                        },
                        ifRight = { messageThread ->
                            client.fetchMessagesForThread(messageThread)
                        }
                    )
                        .doOnSubscribe { messagesAreLoading.onNext(true) }
                        .doAfterTerminate { messagesAreLoading.onNext(false) }
                        .compose(Transformers.neverErrorV2())
                        .share()
                }

            loadingIndicatorViewIsGone = messagesAreLoading
                .map { it.negate() }
                .distinctUntilChanged()

            val thread = initialMessageThreadEnvelope
                .map { it.messageThread() ?: MessageThread.builder().build() }

            // If view model was not initialized with a MessageThread, participant is
            // the project creator.
            val participant =
                Observable.combineLatest(
                    thread,
                    project
                ) { a: MessageThread, b: Project -> Pair.create(a, b) }
                    .map {
                        if (it.first != null && it.first.participant() != null)
                            it.first?.participant()
                        else
                            it.second?.creator()
                    }
                    .filter { it.isNotNull() }
                    .map { requireNotNull(it) }
                    .take(1)

            participant
                .map { it.name() }
                .subscribe { messageEditTextHint.onNext(it) }
                .addToDisposable(disposables)

            val messagesData: Observable<MessagesData> = Observable.combineLatest(
                backingOrThread,
                project,
                participant,
                currentUser.observable()
            ) { backOrThread, proj, part, currentUserOptional ->

                return@combineLatest MessagesData(
                    backOrThread,
                    proj,
                    part,
                    requireNotNull(currentUserOptional.getValue())
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
                    Transformers.takeWhenV2(
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

            val messageSent = messageNotification.compose(Transformers.valuesV2()).ofType(
                Message::class.java
            )

            val sentMessageThreadEnvelope = backingOrThread
                .compose(Transformers.takeWhenV2(messageSent))
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
                .compose(Transformers.neverErrorV2())
                .share()

            val messageThreadEnvelope = Observable.merge(
                initialMessageThreadEnvelope,
                sentMessageThreadEnvelope
            )
                .distinctUntilChanged()

            val messageHasBody = messageEditTextChanged
                .map { it.isNotNull() && it.isPresent() }

            messageThreadEnvelope
                .filter { it.isNotNull() && it.messageThread().isNotNull() }
                .map { requireNotNull(it.messageThread()) }
                .switchMap {
                    client.markAsRead(
                        it
                    )
                }
                .materialize()
                .compose(Transformers.ignoreValuesV2())
                .subscribe { successfullyMarkedAsRead.onNext(it) }
                .addToDisposable(disposables)

            val initialMessages = initialMessageThreadEnvelope
                .filter { it.isNotNull() && it.messages().isNotNull() }
                .map { it.messages() }

            val newMessages = sentMessageThreadEnvelope
                .filter { it.isNotNull() && it.messages().isNotNull() }
                .map { requireNotNull(it.messages()) }

            // Concat distinct messages to initial message list. Return just the new messages if
            // initial list is null, i.e. a new message thread.
            val updatedMessages = initialMessages
                .compose(
                    Transformers.takePairWhenV2(
                        newMessages
                    )
                )
                .map {
                    val messagesList = it.first
                    val message = it.second?.toList()

                    messagesList?.let { initialMessages ->
                        message?.let { m ->
                            ListUtils.concatDistinct(
                                initialMessages, m
                            )
                        }
                    } ?: it.second
                }

            // Load the initial messages once, subsequently load newer messages if any.
            initialMessages
                .filter { it.isNotNull() }
                .take(1)
                .subscribe { messageList.onNext(it) }
                .addToDisposable(disposables)

            updatedMessages
                .subscribe { v: List<Message> -> messageList.onNext(v) }
                .addToDisposable(disposables)

            project
                .filter { it.creator().name().isNotNull() }
                .map { it.creator().name() }
                .subscribe { v: String -> creatorNameTextViewText.onNext(v) }
                .addToDisposable(disposables)

            initialMessageThreadEnvelope
                .filter { it.messages().isNotNull() }
                .map { it.messages() }
                .filter { it.isEmpty() }
                .distinctUntilChanged()
                .subscribe { messageEditTextShouldRequestFocus.onNext(Unit) }
                .addToDisposable(disposables)

            messagesData
                .switchMap {
                    backingAndProjectFromData(it, client)
                }
                .filter { it.isNotNull() }
                .subscribe { backingAndProject ->
                    if (backingAndProject.first.isNotNull() && backingAndProject.second.isNotNull()) {
                        this.backingAndProject.onNext(Pair<Backing, Project>(backingAndProject.first, backingAndProject.second))
                        backingInfoViewIsGone.onNext(false)
                    }

                    if (backingAndProject.first == null) {
                        backingInfoViewIsGone.onNext(true)
                    }
                }
                .addToDisposable(disposables)

            messageAccountTypeObservable
                .map { c: MessagePreviousScreenType -> c == MessagePreviousScreenType.BACKER_MODAL }
                .subscribe { v: Boolean -> viewPledgeButtonIsGone.onNext(v) }
                .addToDisposable(disposables)

            backButtonIsGone = viewPledgeButtonIsGone.map { it.negate() }

            closeButtonIsGone = backButtonIsGone.map { it.negate() }

            projectNameToolbarTextViewText = projectNameTextViewText

            scrollRecyclerViewToBottom = updatedMessages.compose(Transformers.ignoreValuesV2())

            sendMessageButtonIsEnabled = Observable.merge(
                messageHasBody, messageIsSending.map { it.negate() }
            )

            setMessageEditText = messageSent.map { "" }

            toolbarIsExpanded = messageList
                .map {
                    it
                }
                .compose(
                    Transformers.takePairWhenV2(
                        messageEditTextIsFocused
                    )
                )
                .map { it.second }
                .map { it.negate() }

            messageNotification
                .compose(Transformers.errorsV2())
                .map { ErrorEnvelope.fromThrowable(it) }
                .map { it?.errorMessage() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .subscribe { showMessageErrorToast.onNext(it) }
                .addToDisposable(disposables)

            project
                .map { it.name() }
                .subscribe { projectNameTextViewText.onNext(it) }
                .addToDisposable(disposables)

            project
                .compose(Transformers.takeWhenV2(projectContainerViewClicked))
                .subscribe { startProjectPageActivity.onNext(it) }
                .addToDisposable(disposables)

            messageThreadEnvelope
                .compose(Transformers.combineLatestPair(messagesData))
                .compose(Transformers.takeWhenV2(viewPledgeButtonClicked))
                .map {
                    projectAndBacker(
                        it
                    )
                }
                .compose(
                    Transformers.combineLatestPair(
                        this.backingAndProject.filter { it.isNotNull() }
                            .map { requireNotNull(it) }
                    )
                )
                .filter { it.second.first.isNotNull() }
                .map {
                    BackingWrapper(
                        requireNotNull(it.second.first), it.first.second, it.first.first
                    )
                }
                .subscribe { v: BackingWrapper -> startBackingActivity.onNext(v) }
                .addToDisposable(disposables)

            // Set only the initial padding once to counteract the appbar offset.
            recyclerViewInitialBottomPadding = appBarTotalScrollRange.take(1)

            // Take only the first instance in which the offset changes.
            recyclerViewDefaultBottomPadding = appBarOffset
                .filter { it.isNonZero() }
                .compose(Transformers.ignoreValuesV2())
                .take(1)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MessagesViewModel(environment, intent) as T
        }
    }
}
