package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.SwipeRefresher;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.ui.adapters.MessageThreadsAdapter;
import com.kickstarter.viewmodels.MessageThreadsViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(MessageThreadsViewModel.ViewModel.class)
public class MessageThreadsActivity extends BaseActivity<MessageThreadsViewModel.ViewModel> {
  private MessageThreadsAdapter adapter;
  private KSString ksString;
  private RecyclerViewPaginator recyclerViewPaginator;
  private SwipeRefresher swipeRefresher;

  protected @Bind(R.id.mailbox_text_view) TextView mailboxTextView;
  protected @Bind(R.id.message_threads_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.message_threads_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
  protected @Bind(R.id.unread_count_text_view) TextView unreadCountTextView;

  protected @BindString(R.string.messages_navigation_inbox) String inboxString;
  protected @BindString(R.string.No_messages) String noMessagesString;
  protected @BindString(R.string.No_unread_messages) String noUnreadMessagesString;
  protected @BindString(R.string.unread_count_unread) String unreadCountUnreadString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.message_threads_layout);
    ButterKnife.bind(this);

    this.adapter = new MessageThreadsAdapter();
    this.ksString = environment().ksString();
    this.recyclerView.setAdapter(this.adapter);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.recyclerViewPaginator = new RecyclerViewPaginator(this.recyclerView, this.viewModel.inputs::nextPage);
    this.swipeRefresher = new SwipeRefresher(
      this, this.swipeRefreshLayout, this.viewModel.inputs::refresh, this.viewModel.outputs::isFetchingMessageThreads
    );

    this.mailboxTextView.setText(this.inboxString);  // todo: Sent mailbox logic

    this.viewModel.outputs.messageThreads()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.adapter::messageThreads);

    this.viewModel.outputs.unreadMessagesCount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setUnreadTextViewText);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.recyclerViewPaginator.stop();
    this.recyclerView.setAdapter(null);
  }

  private void setUnreadTextViewText(final @Nullable Integer unreadCount) {
    if (unreadCount == null) {
      this.unreadCountTextView.setText(this.noMessagesString);
    } else if (unreadCount == 0 ) {
      this.unreadCountTextView.setText(this.noUnreadMessagesString);
    } else {
      this.unreadCountTextView.setText(
        this.ksString.format(this.unreadCountUnreadString, "unread_count", NumberUtils.format(unreadCount))
      );
    }
  }
}
