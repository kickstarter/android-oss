package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.IntegerUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;
import com.kickstarter.ui.activities.MessageThreadsActivity;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.neverError;

public interface MessageThreadsViewModel {

  interface Inputs {
    /** Invoke when pagination should happen. */
    void nextPage();

    /** Call when the swipe refresher is invoked. */
    void refresh();
  }

  interface Outputs {
    /** Emits a boolean indicating whether message threads are being fetched from the API. */
    Observable<Boolean> isFetchingMessageThreads();

    /** Emits a list of message threads to be displayed. */
    Observable<List<MessageThread>> messageThreads();

    /** Emits a boolean to determine if the unread count text view should be hidden. */
    Observable<Boolean> unreadCountTextViewHidden();

    /** Emits the unread message count to be displayed. */
    Observable<String> unreadCountTextViewText();
  }

  final class ViewModel extends ActivityViewModel<MessageThreadsActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<User> freshUser = this.client.fetchCurrentUser()
        .retry(2)
        .compose(neverError());
      freshUser.subscribe(this.currentUser::refresh);

      final ApiPaginator<MessageThread, MessageThreadsEnvelope, Void> paginator =
        ApiPaginator.<MessageThread, MessageThreadsEnvelope, Void>builder()
          .nextPage(this.nextPage)
          .envelopeToListOfData(MessageThreadsEnvelope::messageThreads)
          .envelopeToMoreUrl(env -> env.urls().api().moreMessageThreads())
          .loadWithParams(__ -> this.client.fetchMessageThreads())
          .loadWithPaginationPath(this.client::fetchMessageThreadsWithPaginationPath)
          .build();

      this.isFetchingMessageThreads = paginator.isFetching();
      this.messageThreads = paginator.paginatedData();

      this.unreadCountTextViewText = this.currentUser.loggedInUser()
        .map(User::unreadMessagesCount)
        .filter(count -> ObjectUtils.isNotNull(count) && IntegerUtils.isNonZero(count))
        .map(NumberUtils::format);

      this.unreadCountTextViewHidden = this.currentUser.loggedInUser()
        .map(user -> {
          final Integer count = user.unreadMessagesCount();
          return ObjectUtils.isNull(count) || IntegerUtils.isZero(count);
        });
    }

    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Void> refresh = PublishSubject.create();

    private final Observable<Boolean> isFetchingMessageThreads;
    private final Observable<List<MessageThread>> messageThreads;
    private final Observable<Boolean> unreadCountTextViewHidden;
    private final Observable<String> unreadCountTextViewText;

    public final MessageThreadsViewModel.Inputs inputs = this;
    public final MessageThreadsViewModel.Outputs outputs = this;

    @Override public void nextPage() {
      this.nextPage.onNext(null);
    }
    @Override public void refresh() {
      this.refresh.onNext(null);
    }

    @Override public @NonNull Observable<Boolean> isFetchingMessageThreads() {
      return this.isFetchingMessageThreads;
    }
    @Override public @NonNull Observable<List<MessageThread>> messageThreads() {
      return this.messageThreads;
    }
    @Override public @NonNull Observable<Boolean> unreadCountTextViewHidden() {
      return this.unreadCountTextViewHidden;
    }
    @Override public @NonNull Observable<String> unreadCountTextViewText() {
      return this.unreadCountTextViewText;
    }
  }
}
