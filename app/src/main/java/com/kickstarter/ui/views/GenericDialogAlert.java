package com.kickstarter.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GenericDialogAlert extends Dialog {
  @NonNull @Bind(R.id.title_text_view) TextView titleTextView;
  @NonNull @Bind(R.id.message_text_view) TextView messageTextView;
  @NonNull @Bind(R.id.ok_button) Button okButton;

  public GenericDialogAlert(@NonNull final Context context) {
    super(context);
  }

  /**
   * Note, the "show" method triggers "onCreate".
   * Title and message text should therefore be set AFTER calling show().
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.generic_dialog_alert);
    ButterKnife.bind(this);
  }

  /**
   * Set the title on the TextView with id title_text_view.
   * Note, default visibility is GONE since we may not always want a title.
   */
  public void setTitleText(@NonNull final String title) {
    titleTextView.setText(title);
    titleTextView.setVisibility(TextView.VISIBLE);

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)messageTextView.getLayoutParams();
    params.topMargin = (int) getContext().getResources().getDimension(R.dimen.grid_1);
    messageTextView.setLayoutParams(params);
  }

  /**
   * Set the message on the TextView with id message_text_view.
   */
  public void setMessage(@NonNull final String message) {
    messageTextView.setText(message);
  }

  /**
   * Dismiss the dialog on click ok_button".
   */
  @OnClick(R.id.ok_button)
  protected void okButtonClick() {
    dismiss();
  }
}
