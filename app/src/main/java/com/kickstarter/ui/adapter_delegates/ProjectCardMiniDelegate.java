package com.kickstarter.ui.adapter_delegates;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProjectCardMiniDelegate {

  private final int viewType;
  private LayoutInflater layoutInflater;

  public ProjectCardMiniDelegate(final Activity activity, final int viewType) {
    this.viewType = viewType;
    layoutInflater = activity.getLayoutInflater();
  }

  public int viewType() {
    return viewType;
  }

  public boolean isForViewType(final List items, final int position) {
    return items.get(position) instanceof Project;
  }

  public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent) {
    return new ProjectCardMiniViewHolder(layoutInflater.inflate(R.layout.project_card_mini_view, parent, false));
  }

  public void onBindViewHolder(final List items, final int position, final RecyclerView.ViewHolder viewHolder) {
    final Project project = (Project) items.get(position);
    ((ProjectCardMiniViewHolder) viewHolder).onBind(project);
  }

  static class ProjectCardMiniViewHolder extends RecyclerView.ViewHolder {
    final View view;
    public @InjectView(R.id.time_to_go_text_view) TextView timeToGoTextView;
    public @InjectView(R.id.name) TextView name;
    public @InjectView(R.id.photo) ImageView photo;

    public ProjectCardMiniViewHolder(final View view) {
      super(view);
      this.view = view;
      ButterKnife.inject(this, view);
    }

    public void onBind(final Project project) {
      name.setText(project.name());

      if (project.isLive()) {
        timeToGoTextView.setText(project.timeToGo(view.getContext()));
        timeToGoTextView.setVisibility(View.VISIBLE);
      } else {
        timeToGoTextView.setVisibility(View.GONE);
      }

      Picasso.with(view.getContext()).load(project.photo().med()).into(photo);
    }
  }
}
