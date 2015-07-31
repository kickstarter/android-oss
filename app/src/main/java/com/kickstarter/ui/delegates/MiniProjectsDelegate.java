package com.kickstarter.ui.delegates;

import com.kickstarter.models.Project;
import com.kickstarter.ui.view_holders.MiniProjectsViewHolder;

public interface MiniProjectsDelegate {
  void onProjectClicked(final Project project, final MiniProjectsViewHolder viewHolder);
}
