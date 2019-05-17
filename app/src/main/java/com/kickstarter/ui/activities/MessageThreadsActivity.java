package com.kickstarter.ui.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.SwipeRefresher;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.libs.utils.ToolbarUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.MessageThread;
import com.kickstarter.ui.adapters.MessageThreadsAdapter;
import com.kickstarter.ui.data.Mailbox;
import com.kickstarter.viewmodels.MessageThreadsViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(MessageThreadsViewModel.ViewModel.class)
public class MessageThreadsActivity extends BaseActivity<MessageThreadsViewModel.ViewModel> {
  private MessageThreadsAdapter adapter;
  private KSString ksString;
  private RecyclerViewPaginator recyclerViewPaginator;

  protected @Bind(R.id.mailbox_switch) Button mailboxSwitch;
  protected @Bind(R.id.message_threads_app_bar_layout) AppBarLayout appBarLayout;
  protected @Bind(R.id.message_threads_collapsed_toolbar_title) View collapsedToolbarTitle;
  protected @Bind(R.id.message_threads_collapsed_toolbar_mailbox_title) TextView collapsedToolbarMailboxTitle;
  protected @Bind(R.id.message_threads_collapsing_toolbar_layout) CollapsingToolbarLayout collapsingToolbarLayout;
  protected @Bind(R.id.mailbox_text_view) TextView mailboxTextView;
  protected @Bind(R.id.message_threads_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.message_threads_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
  protected @Bind(R.id.unread_count_text_view) TextView unreadCountTextView;
  protected @Bind(R.id.message_threads_toolbar_unread_count_text_view) TextView unreadCountToolbarTextView;

  protected @BindString(R.string.messages_navigation_inbox) String inboxString;
  protected @BindString(R.string.messages_navigation_sent) String sentString;
  protected @BindString(R.string.No_messages) String noMessagesString;
  protected @BindString(R.string.No_unread_messages) String noUnreadMessagesString;
  protected @BindString(R.string.font_family_sans_serif) String sansSerifString;
  protected @BindString(R.string.unread_count_unread) String unreadCountUnreadString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.message_threads_layout);
    ButterKnife.bind(this);

    setUpAdapter();
    this.ksString = environment().ksString();

    this.recyclerView.setAdapter(this.adapter);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.recyclerViewPaginator = new RecyclerViewPaginator(this.recyclerView, this.viewModel.inputs::nextPage, this.viewModel.outputs.isFetchingMessageThreads());
    new SwipeRefresher(
      this, this.swipeRefreshLayout, this.viewModel.inputs::swipeRefresh, this.viewModel.outputs::isFetchingMessageThreads
    );

    ToolbarUtils.INSTANCE.fadeToolbarTitleOnExpand(this.appBarLayout, this.collapsedToolbarTitle);

    this.viewModel.outputs.mailboxTitle()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setMailboxStrings);

    this.viewModel.outputs.hasNoMessages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.unreadCountTextView.setText(this.noMessagesString));

    this.viewModel.outputs.hasNoUnreadMessages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.unreadCountTextView.setText(this.noUnreadMessagesString));

    this.viewModel.outputs.messageThreadList()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.adapter::messageThreads);

    this.viewModel.outputs.unreadCountTextViewColorInt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(colorInt -> this.unreadCountTextView.setTextColor(ContextCompat.getColor(this, colorInt)));

    this.viewModel.outputs.unreadCountTextViewTypefaceInt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(typeInt -> this.unreadCountTextView.setTypeface(Typeface.create(this.sansSerifString, typeInt)));

    this.viewModel.outputs.unreadCountToolbarTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.unreadCountToolbarTextView));

    this.viewModel.outputs.unreadMessagesCount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setUnreadTextViewText);
  }

  private void setMailboxStrings(final @NonNull Integer stringRes) {
    final String mailbox = getString(stringRes);
    this.mailboxTextView.setText(mailbox);
    this.collapsedToolbarMailboxTitle.setText(mailbox);
    this.mailboxSwitch.setText(mailbox.equals(this.inboxString) ? this.sentString : this.inboxString);
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

  @Override
  protected void onResume() {
    super.onResume();
    this.viewModel.inputs.onResume();
  }

  @OnClick(R.id.mailbox_switch)
  protected void mailboxSwitchClicked() {
    this.viewModel.inputs.mailbox(this.mailboxTextView.getText().equals(this.inboxString) ? Mailbox.SENT : Mailbox.INBOX);
  }

  private void setUnreadTextViewText(final @NonNull Integer unreadCount) {
    final String unreadCountString = NumberUtils.format(unreadCount);
    this.unreadCountTextView.setText(
      this.ksString.format(this.unreadCountUnreadString, "unread_count", unreadCountString)
    );
    this.unreadCountToolbarTextView.setText(StringUtils.wrapInParentheses(unreadCountString));
  }

  private void setUpAdapter() {
    this.adapter = new MessageThreadsAdapter(new DiffUtil.ItemCallback<Object>() {
      @Override
      public boolean areItemsTheSame(final @NonNull Object oldItem, final @NonNull Object newItem) {
        return threadsAreTheSame(oldItem, newItem);
      }
      @Override
      public boolean areContentsTheSame(final @NonNull Object oldItem, final @NonNull Object newItem) {
        return threadsAreTheSame(oldItem, newItem);
      }

      private boolean threadsAreTheSame(final @NonNull Object oldItem, final @NonNull Object newItem) {
        final MessageThread oldThread = (MessageThread) oldItem;
        final MessageThread newThread = (MessageThread) newItem;

        return oldThread.id() == newThread.id();
      }
    });
  }
}
