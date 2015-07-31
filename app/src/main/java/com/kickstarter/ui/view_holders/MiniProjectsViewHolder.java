package com.kickstarter.ui.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.delegates.MiniProjectsDelegate;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MiniProjectsViewHolder extends RecyclerView.ViewHolder {
  protected @InjectView(R.id.name) TextView name;
  protected @InjectView(R.id.photo) ImageView photo;

  protected View view;
  protected Project project;
  protected MiniProjectsDelegate delegate;

  public MiniProjectsViewHolder(final View view, final MiniProjectsDelegate delegate) {
    super(view);
    this.view = view;
    this.delegate = delegate;

    ButterKnife.inject(this, view);

    view.setOnClickListener((View v) -> {
      this.delegate.onProjectClicked(project, this);
    });
  }

  public void onBind(final Project project) {
    this.project = project;
    name.setText(project.name());

    Picasso.with(view.getContext()).
      load(project.photo().med()).
      into(photo);
  }
}
