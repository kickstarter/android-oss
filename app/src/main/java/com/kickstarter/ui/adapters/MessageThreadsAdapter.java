package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.models.MessageThread;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.List;

public final class MessageThreadsAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate {}

  public MessageThreadsAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public void takeData(final @NonNull List<MessageThread> messageThreads) {

  }

  @Override
  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    return 0;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return null;
  }
}
