package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Message;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.MessageViewHolder;

import java.util.List;

import static java.util.Collections.emptyList;

public final class MessagesAdapter extends KSAdapter {

  public MessagesAdapter() {
    addSection(emptyList());
  }

  public void messages(final @NonNull List<Message> messages) {
    setSection(0, messages);
    notifyDataSetChanged();
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.message_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new MessageViewHolder(view);
  }
}
