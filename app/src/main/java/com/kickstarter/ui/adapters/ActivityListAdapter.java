package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.Presenter;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.ViewHolder> {
  private List<Activity> activities;
  private ActivityFeedPresenter presenter;

  private static final int VIEW_TYPE_DEFAULT = 0;
  private static final int VIEW_TYPE_FRIEND_BACKING = 1;
  private static final int VIEW_TYPE_FRIEND_FOLLOW = 2;

  public ActivityListAdapter(final List<Activity> activities, final ActivityFeedPresenter presenter) {
    this.activities = activities;
    this.presenter = presenter;
  }

  @Override
  public int getItemViewType(final int position) {
    switch(activities.get(position).category()) {
      case UPDATE:
        return VIEW_TYPE_DEFAULT;
      case SUCCESS:
      case LAUNCH:
      case FAILURE:
      case CANCELLATION:
      case SUSPENSION:
      case RESUME:
        return VIEW_TYPE_DEFAULT;
      case FOLLOW:
        return VIEW_TYPE_FRIEND_FOLLOW;
      case BACKING:
        return VIEW_TYPE_FRIEND_BACKING;
      default:
        // TODO: Should raise RuntimeException?
        return VIEW_TYPE_DEFAULT;
    }
  }

  @Override
  public void onBindViewHolder(final ViewHolder view_holder, final int i) {
    final Activity activity = activities.get(i);
    view_holder.onBind(activity);
  }

  @Override
  public ViewHolder onCreateViewHolder(final ViewGroup view_group, final int view_type) {
    LayoutInflater layout_inflater = LayoutInflater.from(view_group.getContext());

    final View view;
    switch (view_type) {
      case VIEW_TYPE_FRIEND_BACKING:
        view = layout_inflater.inflate(R.layout.activity_friend_backing_view, view_group, false);
        return new FriendBackingViewHolder(view, presenter);
      case VIEW_TYPE_FRIEND_FOLLOW:
        view = layout_inflater.inflate(R.layout.activity_friend_follow_view, view_group, false);
        return new FriendFollowViewHolder(view, presenter);
      default:
        view = layout_inflater.inflate(R.layout.activity_view, view_group, false);
        return new DefaultViewHolder(view, presenter);
    }
  }

  @Override
  public int getItemCount() {
    return activities.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    protected Activity activity;
    protected View view;
    protected Presenter presenter;

    public ViewHolder(final View view, final ActivityFeedPresenter presenter) {
      super(view);

      this.view = view;
      this.presenter = presenter;
    }

    // Subclasses should override this
    // TODO: Make it an abstract class
    public void onBind(final Activity activity) {
      this.activity = activity;
    }
  }

  public static class DefaultViewHolder extends ViewHolder {
    @Optional @InjectView(R.id.id) TextView id;

    public DefaultViewHolder(final View view, final ActivityFeedPresenter presenter) {
      super(view, presenter);
      ButterKnife.inject(this, view);
    }
  }

  public static class FriendBackingViewHolder extends ViewHolder {
    @InjectView(R.id.project_name) TextView project_name;

    public FriendBackingViewHolder(final View view, final ActivityFeedPresenter presenter) {
      super(view, presenter);
      ButterKnife.inject(this, view);
    }

    @Override
    public void onBind(final Activity activity) {
      super.onBind(activity);
      project_name.setText(activity.category().toString());
    }
  }

  public static class FriendFollowViewHolder extends ViewHolder {
    public FriendFollowViewHolder(final View view, final ActivityFeedPresenter presenter) {
      super(view, presenter);
      ButterKnife.inject(this, view);
    }

    @Override
    public void onBind(final Activity activity) {
      super.onBind(activity);
    }
  }
}
