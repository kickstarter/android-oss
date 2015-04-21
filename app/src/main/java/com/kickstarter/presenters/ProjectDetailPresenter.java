package com.kickstarter.presenters;

import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.ProjectDetailActivity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class ProjectDetailPresenter {
  private static final KickstarterClient client = new KickstarterClient();
  private final PublishSubject<ProjectDetailActivity> view = PublishSubject.create();

  public static ProjectDetailPresenter create(final Project project) {
    return new ProjectDetailPresenter(client.fetchProject(project));
  }

  public ProjectDetailPresenter(final Observable<Project> project) {
    RxUtils.combineLatestPair(project, view)
      .filter(projectView -> projectView.second != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(projectView -> projectView.second.show(projectView.first));
  }

  public void onTakeView(ProjectDetailActivity v) {
    view.onNext(v);
  }
}
