package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.qualifiers.WebEndpoint;
import com.kickstarter.libs.utils.Secrets;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class KSToolbar extends Toolbar {
  protected @Nullable @Bind(R.id.title_text_view) TextView titleTextView;
  protected @BindDimen(R.dimen.grid_2) float grid_2;

  private Paint backgroundPaint;
  private @WebEndpoint String webEndpoint;

  private final CompositeSubscription subscriptions = new CompositeSubscription();

  public KSToolbar(final @NonNull Context context) {
    super(context);

    init(context);
  }

  public KSToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);

    init(context);
  }

  public KSToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    init(context);
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);

    if (!isInEditMode() && !this.webEndpoint.equals(Secrets.WebEndpoint.PRODUCTION)) {
      canvas.drawRect(0, 0, this.grid_2, getHeight(), this.backgroundPaint);
    }
  }

  @Nullable @OnClick(R.id.back_button)
  protected void backButtonClick() {
    if (getContext() instanceof BaseActivity) {
      ((BaseActivity) getContext()).back();
    } else {
      ((AppCompatActivity) getContext()).onBackPressed();
    }
  }

  protected @NonNull Environment environment() {
    return ((KSApplication) getContext().getApplicationContext()).component().environment();
  }

  /**
   * If the toolbar has a textview with id title_text_view, set its title.
   */
  public void setTitle(final @NonNull String title) {
    if (this.titleTextView != null) {
      this.titleTextView.setText(title);
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

    this.subscriptions.clear();
  }

  protected final void addSubscription(final @NonNull Subscription subscription) {
    this.subscriptions.add(subscription);
  }

  private void init(final @NonNull Context context) {
    if (!isInEditMode()) {
      this.backgroundPaint = new Paint();
      this.backgroundPaint.setStyle(Paint.Style.FILL);
      this.backgroundPaint.setColor(ContextCompat.getColor(context, R.color.kds_trust_500));

      this.webEndpoint = environment().webEndpoint();
    }
  }
}
