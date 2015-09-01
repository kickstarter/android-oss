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
  private int viewType;
  private Project project;
  private CommentFeedPresenter presenter;

  public CommentAdapterDelegate(final int viewType, final Project project, final CommentFeedPresenter presenter) {
    this.viewType = viewType;
    this.project = project;
    this.presenter = presenter;
  }

  public int viewType() {
    return viewType;
  }

  public boolean isForViewType(List items, int position) {
    return items.get(position) instanceof Comment;  // this doesn't do anything
  }

  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
    return new CommentListViewHolder(inflater.inflate(R.layout.comment_card_view, viewGroup, false), presenter);
  }

  public void onBindViewHolder(List items, int position, RecyclerView.ViewHolder viewHolder) {
    Comment comment = (Comment) items.get(position);  // comments are passed in anyways
    CommentListViewHolder commentsHolder = (CommentListViewHolder) viewHolder;
    commentsHolder.onBind(comment, project);
  }
}
