package com.kickstarter.viewmodels;


import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.adapters.data.ProjectDashboardData;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface CreatorDashboardHeaderHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with ProjectDashboardData. */
    void configureWith(ProjectDashboardData projectDashboardData);

    /** Call when the messages button is clicked. */
    void messagesButtonClicked();

    /** Call when the project button is clicked. */
    void projectButtonClicked();
  }

  interface Outputs {
    /** project that is currently being viewed */
    Observable<Project> currentProject();

    /** Emits when the messages button should be gone. */
    Observable<Boolean> messagesButtonIsGone();

    /** Emits when the other projects button should be gone. */
    Observable<Boolean> otherProjectsButtonIsGone();

    /** string number with the percentage of a projects funding */
    Observable<String> percentageFunded();

    /** Emits the percentage funded amount for display in the progress bar. */
    Observable<Integer> percentageFundedProgress();

    /** Emits color of progress bar based on project state. */
    Observable<Integer> progressBarBackground();

    /** localized count of number of backers */
    Observable<String> projectBackersCountText();

    /** current project's name */
    Observable<String> projectNameTextViewText();
    /** Emits when we should start the {@link com.kickstarter.ui.activities.MessageThreadsActivity}. */
    Observable<Pair<Project, RefTag>> startMessageThreadsActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Pair<Project, RefTag>> startProjectActivity();

    /** time remaining for latest project (no units) */
    Observable<String> timeRemainingText();

    /** Emits a boolean determining if the view project button should be gone. */
    Observable<Boolean> viewProjectButtonIsGone();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardHeaderViewHolder> implements Inputs, Outputs {
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.currentUser = environment.currentUser();

      final Observable<User> user = this.currentUser.observable();

      final Observable<Boolean> singleProjectView = this.projectDashboardData
        .map(ProjectDashboardData::getSingleProjectView);

      this.otherProjectsButtonIsGone = user
        .map(User::memberProjectsCount)
        .map(count -> ObjectUtils.coalesce(count, 0))
        .map(count -> count <= 1)
        .compose(combineLatestPair(singleProjectView))
        .map(onlyOneProjectAndSingleProjectView -> onlyOneProjectAndSingleProjectView.first || onlyOneProjectAndSingleProjectView.second)
        .compose(bindToLifecycle());

      this.currentProject = this.projectDashboardData
        .map(ProjectDashboardData::getProject)
        .compose(bindToLifecycle());

      this.messagesButtonIsGone = this.currentProject
        .compose(combineLatestPair(user))
        .map(projectAndUser -> projectAndUser.first.creator().id() != projectAndUser.second.id())
        .compose(bindToLifecycle());

      this.percentageFunded = this.currentProject
        .map(p -> NumberUtils.flooredPercentage(p.percentageFunded()))
        .compose(bindToLifecycle());

      this.percentageFundedProgress = this.currentProject
        .map(p -> ProgressBarUtils.progress(p.percentageFunded()))
        .compose(bindToLifecycle());

      this.progressBarBackground = this.currentProject
        .map(p -> p.isLive() || p.isStarted() || p.isSubmitted() || p.isSuccessful())
        .map(liveStartedSubmittedSuccessful -> liveStartedSubmittedSuccessful ? R.drawable.progress_bar_green_horizontal : R.drawable.progress_bar_grey_horizontal)
        .compose(bindToLifecycle());

      this.projectBackersCountText = this.currentProject
        .map(Project::backersCount)
        .map(NumberUtils::format)
        .compose(bindToLifecycle());

      this.projectNameTextViewText = this.currentProject
        .map(Project::name)
        .distinctUntilChanged()
        .compose(bindToLifecycle());

      this.startMessageThreadsActivity = this.currentProject
        .compose(takeWhen(this.messagesButtonClicked))
        .map(p -> Pair.create(p, RefTag.dashboard()))
        .compose(bindToLifecycle());

      this.startProjectActivity = this.currentProject
        .compose(takeWhen(this.projectButtonClicked))
        .map(p -> Pair.create(p, RefTag.dashboard()))
        .compose(bindToLifecycle());

      this.timeRemainingText = this.currentProject
        .map(ProjectUtils::deadlineCountdownValue)
        .map(NumberUtils::format)
        .compose(bindToLifecycle());

      this.viewProjectButtonIsGone = singleProjectView
        .compose(bindToLifecycle());
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Void> messagesButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> projectButtonClicked = PublishSubject.create();
    private final PublishSubject<ProjectDashboardData> projectDashboardData = PublishSubject.create();

    private final Observable<Project> currentProject;
    private final Observable<Boolean> messagesButtonIsGone;
    private final Observable<Boolean> otherProjectsButtonIsGone;
    private final Observable<String> percentageFunded;
    private final Observable<Integer> percentageFundedProgress;
    private final Observable<Integer> progressBarBackground;
    private final Observable<String> projectBackersCountText;
    private final Observable<String> projectNameTextViewText;
    private final Observable<Pair<Project, RefTag>> startMessageThreadsActivity;
    private final Observable<Pair<Project, RefTag>> startProjectActivity;
    private final Observable<String> timeRemainingText;
    private final Observable<Boolean> viewProjectButtonIsGone;

    @Override public void configureWith(final @NonNull ProjectDashboardData projectDashboardData) {
      this.projectDashboardData.onNext(projectDashboardData);
    }
    @Override public void messagesButtonClicked() {
      this.messagesButtonClicked.onNext(null);
    }
    @Override public void projectButtonClicked() {
      this.projectButtonClicked.onNext(null);
    }

    @Override public @NonNull Observable<Project> currentProject() {
      return this.currentProject;
    }
    @Override public @NonNull Observable<Boolean> messagesButtonIsGone() {
      return this.messagesButtonIsGone;
    }
    @Override public @NonNull Observable<Boolean> otherProjectsButtonIsGone() {
      return this.otherProjectsButtonIsGone;
    }
    @Override public @NonNull Observable<String> percentageFunded() {
      return this.percentageFunded;
    }
    @Override public @NonNull Observable<Integer> percentageFundedProgress() {
      return this.percentageFundedProgress;
    }
    @Override public @NonNull Observable<Integer> progressBarBackground() {
      return this.progressBarBackground;
    }
    @Override public @NonNull Observable<String> projectBackersCountText() {
      return this.projectBackersCountText;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startMessageThreadsActivity() {
      return this.startMessageThreadsActivity;
    }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<String> timeRemainingText() {
      return this.timeRemainingText;
    }
    @Override public @NonNull Observable<Boolean> viewProjectButtonIsGone() {
      return this.viewProjectButtonIsGone;
    }
  }
}
