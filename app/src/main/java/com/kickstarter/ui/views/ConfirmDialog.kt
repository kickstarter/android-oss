package com.kickstarter.ui.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfirmDialog extends AppCompatDialog {
  private final @Nullable String title;
  private final @NonNull String message;
  private final @Nullable String buttonText;

  protected @Bind(R.id.title_text_view) TextView titleTextView;
  protected @Bind(R.id.message_text_view) TextView messageTextView;
  protected @Bind(R.id.ok_button) Button okButton;

  protected @BindString(R.string.general_alert_buttons_ok) String okString;

  public ConfirmDialog(final @NonNull Context context, final @Nullable String title, final @NonNull String message) {
    super(context);
    this.title = title;
    this.message = message;
    this.buttonText = null;
  }

  public ConfirmDialog(final @NonNull Context context, final @Nullable String title, final @NonNull String message,
    final @Nullable String buttonText) {
    super(context);
    this.title = title;
    this.message = message;
    this.buttonText = buttonText;
  }

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    setContentView(R.layout.generic_dialog_alert);
    ButterKnife.bind(this);

    if (this.title != null) {
      setTitleText(this.title);
    } else {
      this.titleTextView.setVisibility(View.GONE);
    }

    if (this.buttonText != null) {
      setButtonText(this.buttonText);
    } else {
      setButtonText(this.okString);
    }

    setMessage(this.message);
  }

  public void setButtonText(final @NonNull String buttonText) {
    this.okButton.setText(buttonText);
  }

  /**
   * Set the title on the TextView with id title_text_view.
   * Note, default visibility is GONE since we may not always want a title.
   */
  public void setTitleText(final @NonNull String title) {
    this.titleTextView.setText(title);
    this.titleTextView.setVisibility(TextView.VISIBLE);

    final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.messageTextView.getLayoutParams();
    params.topMargin = (int) getContext().getResources().getDimension(R.dimen.grid_1);
    this.messageTextView.setLayoutParams(params);
  }

  /**
   * Set the message on the TextView with id message_text_view.
   */
  public void setMessage(final @NonNull String message) {
    this.messageTextView.setText(message);
  }

  /**
   * Dismiss the dialog on click ok_button".
   */
  @OnClick(R.id.ok_button)
  protected void okButtonClick() {
    dismiss();
  }
}
