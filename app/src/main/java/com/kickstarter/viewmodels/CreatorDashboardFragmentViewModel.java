package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.FragmentViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.ArgumentsKey;
import com.kickstarter.ui.adapters.CreatorDashboardAdapter;
import com.kickstarter.ui.fragments.CreatorDashboardFragment;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;


public interface CreatorDashboardFragmentViewModel {

  interface Inputs extends CreatorDashboardAdapter.Delegate {
    /* project menu clicked */
    void dashboardShowProjectMenuClicked();
  }

  interface Outputs {
    Observable<Pair<Project, ProjectStatsEnvelope>> projectAndStats();
    Observable<Void> toggleBottomSheet();
  }

  final class ViewModel extends FragmentViewModel<CreatorDashboardFragment> implements Inputs, Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<Project> project = arguments()
        .map(args -> args.getParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT))
        .ofType(Project.class);

      final Observable<ProjectStatsEnvelope> projectStatsEnvelope = arguments()
        .map(args -> args.getParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT_STATS))
        .ofType(ProjectStatsEnvelope.class);

      Observable.combineLatest(
        project,
        projectStatsEnvelope,
        Pair::create
      )
        .compose(bindToLifecycle())
        .subscribe(this.projectAndStats);

      this.toggleBottomSheet = this.projectsMenuClick;
    }

    private final PublishSubject<Void> projectsMenuClick = PublishSubject.create();

    private final BehaviorSubject<Pair<Project, ProjectStatsEnvelope>> projectAndStats = BehaviorSubject.create();
    private final Observable<Void> toggleBottomSheet;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void dashboardShowProjectMenuClicked() {
      this.projectsMenuClick.onNext(null);
    }

    @Override public @NonNull Observable<Pair<Project, ProjectStatsEnvelope>> projectAndStats() {
      return this.projectAndStats;
    }

    @Override public @NonNull Observable<Void> toggleBottomSheet() {
      return this.toggleBottomSheet;
    }
  }
}
