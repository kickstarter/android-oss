package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

public class KSToolbar extends Toolbar {
  @Nullable @Bind(R.id.title_text_view) TextView titleTextView;
  private final List<Subscription> subscriptions = new ArrayList<>();

  public KSToolbar(final @NonNull Context context) {
    super(context);
  }

  public KSToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public KSToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Nullable @OnClick(R.id.back_button)
  protected void backButtonClick() {
    ((BaseActivity) getContext()).back();
  }

  /**
   * If the toolbar has a textview with id title_text_view, set its title.
   */
  public void setTitle(final @NonNull String title) {
    if (titleTextView != null) {
      titleTextView.setText(title);
    }
  }

  @CallSuper
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @CallSuper
  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @CallSuper
  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    for (final Subscription subscription : subscriptions) {
      subscription.unsubscribe();
    }
  }

  protected final void addSubscription(final @NonNull Subscription subscription) {
    subscriptions.add(subscription);
  }
}
