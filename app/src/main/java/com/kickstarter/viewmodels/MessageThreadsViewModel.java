package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.MessageThread;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;
import com.kickstarter.ui.activities.MessageThreadsActivity;
import com.kickstarter.ui.adapters.MessageThreadsAdapter;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface MessageThreadsViewModel {

  interface Inputs extends MessageThreadsAdapter.Delegate {
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
  }

  final class ViewModel extends ActivityViewModel<MessageThreadsActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();

      final ApiPaginator<MessageThread, MessageThreadsEnvelope, Void> paginator =
        ApiPaginator.<MessageThread, MessageThreadsEnvelope, Void>builder()
          .nextPage(this.nextPage)
          .envelopeToListOfData(MessageThreadsEnvelope::messageThreads)
          .envelopeToMoreUrl(env -> env.urls().api().moreMessageThreads())
          .loadWithParams(__ -> this.client.fetchMessageThreads())
          .loadWithPaginationPath(this.client::fetchMessageThreadsWithPaginationPath)
          .startOverWith(this.refresh)  // todo: fix initial load
          .build();

      this.isFetchingMessageThreads = paginator.isFetching();
      this.messageThreads = paginator.paginatedData();
    }

    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Void> refresh = PublishSubject.create();

    private final Observable<Boolean> isFetchingMessageThreads;
    private final Observable<List<MessageThread>> messageThreads;

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
  }
}
