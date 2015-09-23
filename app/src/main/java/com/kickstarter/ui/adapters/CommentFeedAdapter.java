package com.kickstarter.ui.adapters;

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
  public CommentFeedAdapter(final Project project, final List<Pair<Project, Comment>> projectAndComments) {
    data().add(Collections.singletonList(project));
    data().add(projectAndComments);
  }

  protected int layout(final SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_context_view;
    } else {
      return R.layout.comment_card_view;
    }
  }

  protected KsrViewHolder viewHolder(final int layout, final View view) {
    if (layout == R.layout.project_context_view) {
      return new ProjectContextViewHolder(view);
    }
    return new CommentViewHolder(view);
  }
}
