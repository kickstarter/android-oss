package com.kickstarter.ui.adapter_delegates;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.view_holders.CommentListViewHolder;

import java.util.List;

public class CommentAdapterDelegate {
  private final int viewType;
  private final Project project;
  private final CommentFeedPresenter presenter;

  public CommentAdapterDelegate(final int viewType, final Project project, final CommentFeedPresenter presenter) {
    this.viewType = viewType;
    this.project = project;
    this.presenter = presenter;
  }

  public int viewType() {
    return viewType;
  }

  public boolean isForViewType(final List items, final int position) {
    return items.get(position) instanceof Comment;  // this doesn't do anything
  }

  public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup) {
    final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
    return new CommentListViewHolder(inflater.inflate(R.layout.comment_card_view, viewGroup, false), presenter);
  }

  public void onBindViewHolder(final List items, final int position, final RecyclerView.ViewHolder viewHolder) {
    final Comment comment = (Comment) items.get(position);  // comments are passed in anyways
    final CommentListViewHolder commentsHolder = (CommentListViewHolder) viewHolder;
    commentsHolder.onBind(comment, project);
  }
}
