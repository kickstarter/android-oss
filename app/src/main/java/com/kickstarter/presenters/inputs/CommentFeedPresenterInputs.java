package com.kickstarter.presenters.inputs;

import com.kickstarter.ui.viewholders.EmptyCommentFeedViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

public interface CommentFeedPresenterInputs {
  void commentBody(String __);
  void emptyCommentFeedLoginClicked(EmptyCommentFeedViewHolder __);
  void projectContextClicked(ProjectContextViewHolder __);
}
