package com.kickstarter.ui.views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KSDialog extends AppCompatDialog {
  @NonNull @Bind(R.id.title_text_view) TextView titleTextView;
  @NonNull @Bind(R.id.message_text_view) TextView messageTextView;
  @NonNull @Bind(R.id.ok_button) Button okButton;

  @Nullable protected final String title;
  @NonNull protected final String message;
  @Nullable protected final String buttonText;

  @BindString(R.string.___Okay) String okayString;

  public KSDialog(@NonNull final Context context, @Nullable final String title, @NonNull final String message) {
    super(context);
    this.title = title;
    this.message = message;
    this.buttonText = null;
  }

  public KSDialog(@NonNull final Context context, @Nullable final String title, @NonNull final String message,
    @NonNull final String buttonText) {
    super(context);
    this.title = title;
    this.message = message;
    this.buttonText = buttonText;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    setContentView(R.layout.generic_dialog_alert);
    ButterKnife.bind(this);

    if (title != null) {
      setTitleText(title);
    } else {
      titleTextView.setVisibility(View.GONE);
    }

    if (buttonText != null) {
      setButtonText(buttonText);
    } else {
      setButtonText(okayString);
    }

    setMessage(message);
  }

  public void setButtonText(@NonNull final String buttonText) {
    okButton.setText(buttonText);
  }

  /**
   * Set the title on the TextView with id title_text_view.
   * Note, default visibility is GONE since we may not always want a title.
   */
  public void setTitleText(@NonNull final String title) {
    titleTextView.setText(title);
    titleTextView.setVisibility(TextView.VISIBLE);

    final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)messageTextView.getLayoutParams();
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
