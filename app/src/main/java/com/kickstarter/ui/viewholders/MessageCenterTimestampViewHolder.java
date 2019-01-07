package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ObjectUtils;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;

public final class MessageCenterTimestampViewHolder extends KSViewHolder {
  protected @Bind(R.id.message_center_timestamp_text_view) TextView centerTimestampTextView;

  public MessageCenterTimestampViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final DateTime dateTime = ObjectUtils.requireNonNull((DateTime) data);
    this.centerTimestampTextView.setText(DateTimeUtils.longDate(dateTime));
  }
}
