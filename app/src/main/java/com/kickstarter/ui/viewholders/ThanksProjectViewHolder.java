package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class ThanksProjectViewHolder extends KSViewHolder {
  private Project project;
  private final Context context;
  private final Delegate delegate;

  protected @Bind(R.id.time_to_go_text_view) TextView timeToGoTextView;
  protected @Bind(R.id.name) TextView nameTextView;
  protected @Bind(R.id.photo) ImageView photoImageView;
  protected @BindString(R.string.discovery_baseball_card_time_left_to_go) String timeLeftToGoString;

  protected @Inject KSString ksString;

  public interface Delegate {
    void projectClick(ThanksProjectViewHolder viewHolder, Project project);
  }

  public ThanksProjectViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();
    ((KSApplication) this.context.getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.project = requireNonNull((Project) data, Project.class);
  }

  public void onBind() {
    this.nameTextView.setText(this.project.name());

    if (this.project.isLive()) {
      this.timeToGoTextView.setText(this.ksString.format(
        this.timeLeftToGoString,
        "time_left",
        ProjectUtils.deadlineCountdown(this.project, this.context)
      ));
      this.timeToGoTextView.setVisibility(View.VISIBLE);
    } else {
      this.timeToGoTextView.setVisibility(View.GONE);
    }

    final Photo photo = this.project.photo();
    if (photo != null) {
      this.photoImageView.setVisibility(View.VISIBLE);
      Picasso.with(this.context).load(photo.med()).into(this.photoImageView);
    } else {
      this.photoImageView.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.delegate.projectClick(this, this.project);
  }
}
