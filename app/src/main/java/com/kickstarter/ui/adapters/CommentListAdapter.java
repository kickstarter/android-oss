package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Comment;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.view_holders.CommentListViewHolder;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListViewHolder> {
  private List<Comment> comments;
  private CommentFeedPresenter presenter;

  public CommentListAdapter(final List<Comment> comments, final CommentFeedPresenter presenter) {
    Log.d("TEST", this.toString());
    this.comments = comments;
    this.presenter = presenter;
  }

  @Override
  public void onBindViewHolder(final CommentListViewHolder viewHolder, final int i) {
    Log.d("TEST", this + " onBindViewHolder");
    final Comment comment = comments.get(i);
    viewHolder.onBind(comment);
  }

  @Override
  public CommentListViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
    Log.d("TEST", this + " onCreateViewHolder");
    final View view = LayoutInflater.from(viewGroup.getContext())
      .inflate(R.layout.comment_card_view, viewGroup, false);
    return new CommentListViewHolder(view, presenter);
  }

  // This needs to return greater than 0 in order for view binding to occur!
  @Override
  public int getItemCount() {
    return comments.size();
  }
}
