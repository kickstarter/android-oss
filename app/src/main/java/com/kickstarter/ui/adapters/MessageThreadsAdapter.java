package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.MessageThread;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.MessageThreadViewHolder;

import java.util.List;

import static java.util.Collections.emptyList;

public final class MessageThreadsAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends MessageThreadViewHolder.Delegate {}

  public MessageThreadsAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
    addSection(emptyList());
  }

  public void messageThreads(final @NonNull List<MessageThread> messageThreads) {
    setSection(0, messageThreads);
    notifyDataSetChanged();
  }

  @Override
  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.message_thread_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new MessageThreadViewHolder(view, this.delegate);
  }
}
