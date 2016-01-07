package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Activity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DiscoveryActivityViewHolder extends KSViewHolder {
  @Inject KSString ksString;

  protected @Bind(R.id.avatar) ImageView avatar;
  protected @Bind(R.id.activity_sample) TextView activityTextView;
  protected @Bind(R.id.see_activity_button) Button seeActivityButton;
  protected @BindString(R.string.activity_friend_backed_project_name_by_creator_name) String activityString;

  protected Activity activity;

  private final Delegate delegate;
  public interface Delegate {
    void seeActivityClick(DiscoveryActivityViewHolder viewHolder);
  }

  public DiscoveryActivityViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(final @NonNull Object datum) {
    this.activity = (Activity) datum;
  }

  @OnClick(R.id.see_activity_button)
  protected void seeActivityOnClick() {
    delegate.seeActivityClick(this);
  }
}
