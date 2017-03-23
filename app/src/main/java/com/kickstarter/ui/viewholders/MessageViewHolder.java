package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Message;

public final class MessageViewHolder extends KSViewHolder {

  public MessageViewHolder(final @NonNull View view) {
    super(view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Message message = ObjectUtils.requireNonNull((Message) data);
  }
}
