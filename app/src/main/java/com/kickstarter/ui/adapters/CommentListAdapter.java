package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.view_holders.CommentListViewHolder;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListViewHolder> {
  final private List<Comment> comments;
  final private CommentFeedPresenter presenter;
  final private Project project;

  public CommentListAdapter(final List<Comment> comments, final Project project, final CommentFeedPresenter presenter) {
    this.comments = comments;
    this.presenter = presenter;
    this.project = project;
  }

  @Override
  public void onBindViewHolder(final CommentListViewHolder viewHolder, final int i) {
    final Comment comment = comments.get(i);
    viewHolder.onBind(comment, project);
  }

  @Override
  public CommentListViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
    final View view = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.comment_card_view, viewGroup, false);
    return new CommentListViewHolder(view, presenter);
  }

  @Override
  public int getItemCount() {
    return comments.size();
  }
}
