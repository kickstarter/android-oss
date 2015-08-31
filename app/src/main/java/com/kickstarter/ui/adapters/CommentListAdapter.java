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
import com.kickstarter.ui.view_holders.ProjectContextViewHolder;

import java.util.List;

public class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  final private List<Comment> comments;
  final private CommentFeedPresenter presenter;
  final private Project project;

  final private int CONTEXT = 0, FEED = 1;

  public CommentListAdapter(final List<Comment> comments, final Project project, final CommentFeedPresenter presenter) {
    this.comments = comments;
    this.presenter = presenter;
    this.project = project;
  }

  @Override
  public int getItemViewType(int position) {
    if(position == CONTEXT) {
      return CONTEXT;
    }
    else if  (comments.get(position) != null){
      return FEED;
    }
    return -1;
  }

  @Override
  public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int i) {
    final Comment comment = comments.get(i);

    switch (viewHolder.getItemViewType()) {
      case CONTEXT:
        ProjectContextViewHolder vh1 = (ProjectContextViewHolder) viewHolder;
        vh1.onBind(project);
        break;
      case FEED:
        CommentListViewHolder vh2 = (CommentListViewHolder) viewHolder;
        vh2.onBind(comment, project);
        break;
      default:
        break;
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
    View view;
    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
    RecyclerView.ViewHolder viewHolder;

    switch (viewType) {
      case CONTEXT:
        view = inflater.inflate(R.layout.project_context_view, viewGroup, false);
        viewHolder = new ProjectContextViewHolder(view);
        break;
      case FEED:
        view = inflater.inflate(R.layout.comment_card_view, viewGroup, false);
        viewHolder = new CommentListViewHolder(view, presenter);
        break;
      // todo: correct default case?
      default:
        view = inflater.inflate(R.layout.comment_card_view, viewGroup, false);
        viewHolder = new CommentListViewHolder(view, presenter);
        break;
    }
    return viewHolder;
  }

  @Override
  public int getItemCount() {
    return comments.size();
  }
}
