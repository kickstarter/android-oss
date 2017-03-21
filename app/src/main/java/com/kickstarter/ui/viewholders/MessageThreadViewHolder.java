package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.MessageThread;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class MessageThreadViewHolder extends KSViewHolder {
  public @Bind(R.id.participant_name_text_view) TextView participantNameTextView;

  public MessageThreadViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final MessageThread messageThread = requireNonNull((MessageThread) data);

    participantNameTextView.setText(messageThread.participant().name());
  }
}
