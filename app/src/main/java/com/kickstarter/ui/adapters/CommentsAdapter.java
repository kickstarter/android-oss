package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.adapter_delegates.CommentAdapterDelegate;
import com.kickstarter.ui.adapter_delegates.ProjectContextAdapterDelegate;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  final private List<Comment> comments;

  final private int VIEW_TYPE_CONTEXT = 0;
  final private int VIEW_TYPE_COMMENT = 1;

  CommentAdapterDelegate commentFeedDelegate;
  ProjectContextAdapterDelegate projectContextDelegate;

  // comments is the data source of the adapter
  public CommentsAdapter(final List<Comment> comments, final Project project, final CommentFeedPresenter presenter) {
    this.comments = comments;
    commentFeedDelegate = new CommentAdapterDelegate(VIEW_TYPE_COMMENT, project, presenter);
    projectContextDelegate = new ProjectContextAdapterDelegate(VIEW_TYPE_CONTEXT, project);
  }

  @Override
  public int getItemViewType(int position) {
    if (projectContextDelegate.isForViewType(position)) {
      return projectContextDelegate.viewType();
    }
    else {
      return commentFeedDelegate.viewType();
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
    switch (viewType) {
      case VIEW_TYPE_CONTEXT:
        return projectContextDelegate.onCreateViewHolder(viewGroup);
      case VIEW_TYPE_COMMENT:
        return commentFeedDelegate.onCreateViewHolder(viewGroup);
      default:
        throw new IllegalArgumentException("No delegate found");
    }
  }

  @Override
  public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
    int viewType = viewHolder.getItemViewType();
    switch (viewType) {
      case VIEW_TYPE_CONTEXT:
        projectContextDelegate.onBindViewHolder(viewHolder);
        break;
      case VIEW_TYPE_COMMENT:
        commentFeedDelegate.onBindViewHolder(comments, position, viewHolder);
        break;
    }
  }

  // we want to add the header as a data source here so as to not return size of comments
  @Override
  public int getItemCount() {
    return comments.size();
  }
}
