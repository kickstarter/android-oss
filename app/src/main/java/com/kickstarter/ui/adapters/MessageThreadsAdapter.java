package com.kickstarter.ui.adapters;

import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.MessageThread;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.MessageThreadViewHolder;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import static java.util.Collections.emptyList;

public final class MessageThreadsAdapter extends KSAdapter {
  public MessageThreadsAdapter() {
    addSection(emptyList());
  }

  public void messageThreads(final @NonNull List<MessageThread> messageThreads) {
    clearSections();
    addSection(messageThreads);
    notifyDataSetChanged();
  }

  @Override
  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.message_thread_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new MessageThreadViewHolder(view);
  }
}
