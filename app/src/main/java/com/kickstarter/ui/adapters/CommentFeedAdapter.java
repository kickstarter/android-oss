package com.kickstarter.ui.adapters;

import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CommentViewHolder;
import com.kickstarter.ui.viewholders.EmptyCommentFeedViewHolder;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

import java.util.Collections;
import java.util.List;

import rx.Observable;

public class CommentFeedAdapter extends KsrAdapter {
  private final Delegate delegate;
  private final Project project;

  public interface Delegate extends ProjectContextViewHolder.Delegate, EmptyCommentFeedViewHolder.Delegate {}

  public CommentFeedAdapter(final Delegate delegate, final Project project) {
    this.delegate = delegate;
    this.project = project;
  }

  protected int layout(final SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_context_view;
    } else if (!project.hasComments()){
      return R.layout.empty_comment_feed_layout;
    } else {
      return R.layout.comment_card_view;
    }
  }

  public void takeProjectComments(@NonNull final Project project, @NonNull final List<Comment> comments) {
    data().clear();
    data().add(Collections.singletonList(project));

    if (project.hasComments()) {
      data().add(Observable.from(comments)
        .map(comment -> Pair.create(project, comment))
        .toList().toBlocking().single()
      );
    } else {
      // ViewHolder will not initialize unless there is data.
      data().add(Collections.singletonList(project));
    }
    notifyDataSetChanged();
  }

  protected KsrViewHolder viewHolder(final int layout, @NonNull final View view) {
    if (layout == R.layout.project_context_view) {
      return new ProjectContextViewHolder(view, delegate);
    } else if (layout == R.layout.empty_comment_feed_layout) {
      return new EmptyCommentFeedViewHolder(view, delegate);
    } else {
      return new CommentViewHolder(view);
    }
  }
}
