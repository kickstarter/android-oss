package com.kickstarter.presenters;

import com.kickstarter.models.Project;
import com.kickstarter.ui.activities.ProjectDetailActivity;

public class ProjectDetailPresenter {
  private ProjectDetailActivity view;
  private Project project;

  public ProjectDetailPresenter(Project project) {
    this.project = project;
  }

  public void onTakeView(ProjectDetailActivity view) {
    if (view != null) {
      view.show(project);
    }
  }
}
