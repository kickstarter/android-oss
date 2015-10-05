package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CommentViewHolder;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

import java.util.Collections;
import java.util.List;

public class CommentFeedAdapter extends KsrAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectContextViewHolder.Delegate {}

  public CommentFeedAdapter(@NonNull final Project project, @NonNull final List<Pair<Project, Comment>> projectAndComments,
    @NonNull final Delegate delegate) {
    data().add(Collections.singletonList(project));
    data().add(projectAndComments);
    this.delegate = delegate;
  }

  protected @LayoutRes int layout(@NonNull final SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_context_view;
    } else {
      return R.layout.comment_card_view;
    }
  }

  protected KsrViewHolder viewHolder(final @LayoutRes int layout, @NonNull final View view) {
    if (layout == R.layout.project_context_view) {
      return new ProjectContextViewHolder(view, delegate);
    }
    return new CommentViewHolder(view);
  }
}
