package com.kickstarter.viewmodels;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.IntegerUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.MessageThreadsActivity;
import com.kickstarter.ui.data.Mailbox;
import com.kickstarter.ui.intentmappers.ProjectIntentMapper;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.utils.IntegerUtils.intValueOrZero;

public interface MessageThreadsViewModel {

  interface Inputs {
    /** Call when pagination should happen. */
    void nextPage();

    /** Call when onResume of the activity's lifecycle happens. */
    void onResume();

    /** Call when the swipe refresher is invoked. */
    void swipeRefresh();
  }

  interface Outputs {
    /** Emits a boolean to determine if there are no messages. */
    Observable<Boolean> hasNoMessages();

    /** Emits a boolean to determine if there are no unread messages. */
    Observable<Boolean> hasNoUnreadMessages();

    /** Emits a boolean indicating whether message threads are being fetched from the API. */
    Observable<Boolean> isFetchingMessageThreads();

    /** Emits a list of message threads to be displayed. */
    Observable<List<MessageThread>> messageThreadList();

    /** Emits a color integer to set the unread count text view to. */
    Observable<Integer> unreadCountTextViewColorInt();

    /** Emits a typeface integer to set the unread count text view to. */
    Observable<Integer> unreadCountTextViewTypefaceInt();

    /** Emits a boolean to determine if the unread count toolbar text view should be gone. */
    Observable<Boolean> unreadCountToolbarTextViewIsGone();

    /** Emits the unread message count to be displayed. */
    Observable<Integer> unreadMessagesCount();
  }

  final class ViewModel extends ActivityViewModel<MessageThreadsActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      // NB: project from intent can be null.
      final Observable<Project> initialProject = intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT));

      final Observable<Void> refreshUserOrProject = Observable.merge(this.onResume, this.swipeRefresh);

      final Observable<User> freshUser = intent()
        .compose(takeWhen(refreshUserOrProject))
        .switchMap(__ -> this.client.fetchCurrentUser())
        .retry(2)
        .compose(neverError());

      freshUser.subscribe(this.currentUser::refresh);

      final Observable<Project> project = Observable.merge(
        initialProject,
        initialProject
          .compose(takeWhen(refreshUserOrProject))
          .map(Project::param)
          .switchMap(this.client::fetchProject)
          .compose(neverError())
          .share()
      );

      // Use project unread messages count if configured with a project,
      // in the case of a creator viewing their project's messages.
      final Observable<Integer> unreadMessagesCount = Observable.combineLatest(
        project,
        this.currentUser.loggedInUser(),
        Pair::create
      )
        .map(projectAndUser ->
          projectAndUser.first != null
            ? projectAndUser.first.unreadMessagesCount()
            : projectAndUser.second.unreadMessagesCount()
        )
        .distinctUntilChanged();

      // todo: MessageSubject switch will also trigger refresh
      final Observable<Void> refreshMessageThreads = Observable.merge(
        unreadMessagesCount.compose(ignoreValues()),
        this.swipeRefresh
      );

      final Observable<Project> startOverWith = Observable.combineLatest(
        project,
        refreshMessageThreads,
        Pair::create
      )
        .map(PairUtils::first);

      final ApiPaginator<MessageThread, MessageThreadsEnvelope, Project> paginator =
        ApiPaginator.<MessageThread, MessageThreadsEnvelope, Project>builder()
          .nextPage(this.nextPage)
          .startOverWith(startOverWith)
          .envelopeToListOfData(MessageThreadsEnvelope::messageThreads)
          .envelopeToMoreUrl(env -> env.urls().api().moreMessageThreads())
          .loadWithParams(p -> this.client.fetchMessageThreads(p, Mailbox.INBOX))
          .loadWithPaginationPath(this.client::fetchMessageThreadsWithPaginationPath)
          .build();

      paginator.isFetching()
        .compose(bindToLifecycle())
        .subscribe(this.isFetchingMessageThreads);

      paginator.paginatedData()
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.messageThreadList);

      unreadMessagesCount
        .map(ObjectUtils::isNull)
        .subscribe(this.hasNoMessages);

      unreadMessagesCount
        .map(IntegerUtils::isZero)
        .subscribe(this.hasNoUnreadMessages);

      unreadMessagesCount
        .map(count -> intValueOrZero(count) > 0 ? R.color.ksr_text_green_700 : R.color.ksr_dark_grey_400)
        .subscribe(this.unreadCountTextViewColorInt);

      unreadMessagesCount
        .map(count -> intValueOrZero(count) > 0 ? Typeface.BOLD : Typeface.NORMAL)
        .subscribe(this.unreadCountTextViewTypefaceInt);

      this.unreadCountToolbarTextViewIsGone = Observable.zip(
        this.hasNoMessages,
        this.hasNoUnreadMessages,
        Pair::create
      )
        .map(noMessagesAndNoUnread -> noMessagesAndNoUnread.first || noMessagesAndNoUnread.second);

      unreadMessagesCount
        .filter(ObjectUtils::isNotNull)
        .filter(IntegerUtils::isNonZero)
        .subscribe(this.unreadMessagesCount);

      final Observable<RefTag> refTag = intent()
        .flatMap(ProjectIntentMapper::refTag);

      Observable.combineLatest(project, refTag, Pair::create)
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(projectAndRefTag -> this.koala.trackViewedMailbox(Mailbox.INBOX, projectAndRefTag.first, projectAndRefTag.second));
    }

    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Void> onResume = PublishSubject.create();
    private final PublishSubject<Void> swipeRefresh = PublishSubject.create();

    private final BehaviorSubject<Boolean> hasNoMessages = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> hasNoUnreadMessages = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> isFetchingMessageThreads = BehaviorSubject.create();
    private final BehaviorSubject<List<MessageThread>> messageThreadList = BehaviorSubject.create();
    private final BehaviorSubject<Integer> unreadCountTextViewColorInt = BehaviorSubject.create();
    private final BehaviorSubject<Integer> unreadCountTextViewTypefaceInt = BehaviorSubject.create();
    private final Observable<Boolean> unreadCountToolbarTextViewIsGone;
    private final BehaviorSubject<Integer> unreadMessagesCount = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void nextPage() {
      this.nextPage.onNext(null);
    }
    @Override public void onResume() {
      this.onResume.onNext(null);
    }
    @Override public void swipeRefresh() {
      this.swipeRefresh.onNext(null);
    }

    @Override public @NonNull Observable<Boolean> hasNoMessages() {
      return this.hasNoMessages;
    }
    @Override public @NonNull Observable<Boolean> hasNoUnreadMessages() {
      return this.hasNoUnreadMessages;
    }
    @Override public @NonNull Observable<Boolean> isFetchingMessageThreads() {
      return this.isFetchingMessageThreads;
    }
    @Override public @NonNull Observable<List<MessageThread>> messageThreadList() {
      return this.messageThreadList;
    }
    @Override public @NonNull Observable<Integer> unreadCountTextViewColorInt() {
      return this.unreadCountTextViewColorInt;
    }
    @Override public @NonNull Observable<Integer> unreadCountTextViewTypefaceInt() {
      return this.unreadCountTextViewTypefaceInt;
    }
    @Override public @NonNull Observable<Boolean> unreadCountToolbarTextViewIsGone() {
      return this.unreadCountToolbarTextViewIsGone;
    }
    @Override public @NonNull Observable<Integer> unreadMessagesCount() {
      return this.unreadMessagesCount;
    }
  }
}
