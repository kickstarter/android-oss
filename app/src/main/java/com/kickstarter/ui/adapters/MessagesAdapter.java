package com.kickstarter.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.databinding.MessageCenterTimestampLayoutBinding;
import com.kickstarter.databinding.MessageViewBinding;
import com.kickstarter.models.Message;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.MessageCenterTimestampViewHolder;
import com.kickstarter.ui.viewholders.MessageViewHolder;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import rx.Observable;

public final class MessagesAdapter extends KSAdapter {
  public MessagesAdapter() {}

  private int getLayoutId(final @NonNull SectionRow sectionRow) {
    if (objectFromSectionRow(sectionRow) instanceof DateTime) {
      return R.layout.message_center_timestamp_layout;
    } else if (objectFromSectionRow(sectionRow) instanceof Message) {
      return R.layout.message_view;
    }
    return R.layout.empty_view;
  }

  public void messages(final @NonNull List<Message> messages) {
    clearSections();

    // Group messages by start of day.
    Observable.from(messages)
      .groupBy(message -> message.createdAt().withTimeAtStartOfDay())
      .forEach(dateAndGroupedMessages -> {
        addSection(Collections.singletonList(dateAndGroupedMessages.getKey()));
        dateAndGroupedMessages
          .forEach(message -> addSection(Collections.singletonList(message)));
      });

    notifyDataSetChanged();
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    return getLayoutId(sectionRow);
  }

  @Override
  public void onBindViewHolder(final @NonNull KSViewHolder holder, final int position,
    final @NonNull List<Object> payloads) {
    super.onBindViewHolder(holder, position, payloads);

    if (holder instanceof MessageViewHolder) {
      // Let the MessageViewHolder know if it is the last position in the RecyclerView.
      ((MessageViewHolder) holder).isLastPosition(position == getItemCount() - 1);
    }
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull ViewGroup viewGroup) {
    switch (layout) {
      case R.layout.message_center_timestamp_layout:
        return new MessageCenterTimestampViewHolder(MessageCenterTimestampLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
      case R.layout.message_view:
        return new MessageViewHolder(MessageViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));

      default:
        throw new IllegalStateException("Invalid layout.");
    }
  }
}
