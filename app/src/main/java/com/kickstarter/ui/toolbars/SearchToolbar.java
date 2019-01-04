package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.kickstarter.R;
import com.kickstarter.ui.activities.SearchActivity;
import com.kickstarter.ui.views.IconButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public final class SearchToolbar extends KSToolbar {
  public @Bind(R.id.clear_button) IconButton clearButton;
  public @Bind(R.id.search_edit_text) EditText searchEditText;

  public SearchToolbar(final @NonNull Context context) {
    super(context);
  }

  public SearchToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public SearchToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ButterKnife.bind(this);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    final Observable<CharSequence> text = RxTextView.textChanges(this.searchEditText);
    final Observable<Boolean> clearable = text.map(t -> t.length() > 0);

    addSubscription(clearable
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(c -> this.clearButton.setVisibility(c ? View.VISIBLE : View.INVISIBLE)));

    addSubscription(text
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(t -> ((SearchActivity) getContext()).viewModel().inputs.search(t.toString())));
  }

  @OnClick(R.id.clear_button)
  public void clearButtonClick() {
    this.searchEditText.setText(null);
  }
}
