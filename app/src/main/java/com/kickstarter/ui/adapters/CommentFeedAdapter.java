package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
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
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

import java.util.Collections;
import java.util.List;

import rx.Observable;

public final class CommentFeedAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectContextViewHolder.Delegate, EmptyCommentFeedViewHolder.Delegate {}

  public CommentFeedAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_context_view;
    } else if (sectionRow.section() == 1){
      return R.layout.comment_card_view;
    } else {
      return R.layout.empty_comment_feed_layout;
    }
  }

  public void takeProjectComments(final @NonNull Project project, final @NonNull List<Comment> comments,
    final @Nullable User user) {
    sections().clear();

    sections().add(Collections.singletonList(project));

    addSection(Observable.from(comments)
      .map(comment -> Pair.create(project, comment))
      .toList().toBlocking().single());

    if (comments.size() == 0) {
      sections().add(Collections.singletonList(new Pair<>(project, user)));
    } else {
      sections().add(Collections.emptyList());
    }

    notifyDataSetChanged();
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.project_context_view) {
      return new ProjectContextViewHolder(view, delegate);
    } else if (layout == R.layout.empty_comment_feed_layout) {
      return new EmptyCommentFeedViewHolder(view, delegate);
    } else {
      return new CommentViewHolder(view);
    }
  }
}
