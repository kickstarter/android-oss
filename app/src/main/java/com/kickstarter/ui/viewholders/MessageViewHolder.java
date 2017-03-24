package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Message;
import com.kickstarter.viewmodels.MessageHolderViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

public final class MessageViewHolder extends KSViewHolder {
  private final MessageHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.message_body_text_view) TextView messageBodyTextView;

  public MessageViewHolder(final @NonNull View view) {
    super(view);

    this.viewModel = new MessageHolderViewModel.ViewModel(environment());

    ButterKnife.bind(this, view);

    this.viewModel.outputs.messageBodyTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.messageBodyTextView::setText);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Message message = ObjectUtils.requireNonNull((Message) data);
    this.viewModel.inputs.configureWith(message);
  }
}
