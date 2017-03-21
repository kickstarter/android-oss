package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.SwipeRefresher;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.ui.adapters.MessageThreadsAdapter;
import com.kickstarter.viewmodels.MessageThreadsViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;

@RequiresActivityViewModel(MessageThreadsViewModel.ViewModel.class)
public class MessageThreadsActivity extends BaseActivity<MessageThreadsViewModel.ViewModel> {
  private MessageThreadsAdapter adapter;
  private RecyclerViewPaginator recyclerViewPaginator;
  private SwipeRefresher swipeRefresher;

  protected @Bind(R.id.message_threads_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.message_threads_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.message_threads_layout);
    ButterKnife.bind(this);

    this.adapter = new MessageThreadsAdapter(this.viewModel.inputs);
    this.recyclerView.setAdapter(this.adapter);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.recyclerViewPaginator = new RecyclerViewPaginator(this.recyclerView, this.viewModel.inputs::nextPage);
//    this.swipeRefresher = new SwipeRefresher(this, this.swipeRefreshLayout, this.viewModel.inputs::refresh);

    this.viewModel.outputs.messageThreads()
      .compose(bindToLifecycle())
      .compose(Transformers.observeForUI())
      .subscribe(this.adapter::messageThreads);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.recyclerViewPaginator.stop();
    this.recyclerView.setAdapter(null);
  }
}
