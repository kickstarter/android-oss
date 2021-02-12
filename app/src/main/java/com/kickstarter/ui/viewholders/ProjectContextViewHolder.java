package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public final class ProjectContextViewHolder extends KSViewHolder {
  private final Context context;
  private final Delegate delegate;
  private final KSString ksString;
  private Project project;

  protected @Bind(R.id.project_context_image_view) ImageView projectContextImageView;
  protected @Bind(R.id.project_context_project_name) TextView projectNameTextView;
  protected @Bind(R.id.project_context_creator_name) TextView creatorNameTextView;
  protected @BindString(R.string.project_creator_by_creator) String projectCreatorByCreatorString;

  public interface Delegate {
    void projectContextClicked(ProjectContextViewHolder viewHolder);
  }

  public ProjectContextViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();
    this.ksString = environment().ksString();
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.project = ObjectUtils.requireNonNull((Project) data, Project.class);
  }

  public void onBind() {
    final Photo photo = this.project.photo();

    if (photo != null) {
      this.projectContextImageView.setVisibility(View.VISIBLE);
      Picasso.get().load(photo.full()).into(this.projectContextImageView);
    } else {
      this.projectContextImageView.setVisibility(View.INVISIBLE);
    }

    this.projectNameTextView.setText(this.project.name());
    this.creatorNameTextView.setText(this.ksString.format(
      this.projectCreatorByCreatorString,
      "creator_name",
      this.project.creator().name()
    ));
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.delegate.projectContextClicked(this);
  }
}
