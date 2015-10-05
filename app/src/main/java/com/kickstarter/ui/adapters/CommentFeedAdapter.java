package com.kickstarter.ui.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.CommentViewHolder;
import com.kickstarter.ui.viewholders.EmptyCommentFeedViewHolder;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

import java.util.Collections;
import java.util.List;

import rx.Observable;

public class CommentFeedAdapter extends KsrAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectContextViewHolder.Delegate, EmptyCommentFeedViewHolder.Delegate {}

  public CommentFeedAdapter(@NonNull final Delegate delegate) {
    this.delegate = delegate;
  }

  protected int layout(@NonNull final SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_context_view;
    } else if (sectionRow.section() == 1){
      return R.layout.comment_card_view;
    } else {
      return R.layout.empty_comment_feed_layout;
    }
  }

  public void takeProjectComments(@NonNull final Project project, @NonNull final List<Comment> comments,
    @Nullable final User user) {
    data().clear();

    data().add(Collections.singletonList(project));

    data().add(Observable.from(comments)
      .map(comment -> Pair.create(project, comment))
      .toList().toBlocking().single());

    if (comments.size() == 0) {
      data().add(Collections.singletonList(new Pair<>(project, user)));
    } else {
      data().add(Collections.emptyList());
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
