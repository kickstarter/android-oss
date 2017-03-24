package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Message;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class MessageViewHolder extends KSViewHolder {
  protected @Bind(R.id.message_body_text_view) TextView messageBodyTextView;

  public MessageViewHolder(final @NonNull View view) {
    super(view);

    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Message message = ObjectUtils.requireNonNull((Message) data);

    messageBodyTextView.setText(message.body());
  }
}
