package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface ProjectSearchResultHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a project and isFeatured data. */
    void configureWith(Pair<Project, Boolean> projectAndIsFeatured);

    /** Call to say user clicked a project */
    void projectClicked();
  }

  interface Outputs {
    /** Emits title of project. */
    Observable<String> projectNameTextViewText();

    /** Emits the project photo url to be displayed. */
    Observable<String> projectPhotoUrl();

    /** Emits a completed / days to go pair. */
    Observable<Pair<Integer, Integer>> projectStats();

    /** Emits the project clicked by the user. */
    Observable<Project> notifyDelegateOfResultClick();
  }

  final class ViewModel extends ActivityViewModel<ProjectSearchResultViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.projectPhotoUrl = this.projectAndIsFeatured
        .map(ViewModel::photoUrl);

      this.projectNameTextViewText = this.projectAndIsFeatured
        .map(projectAndIsFeatured -> projectAndIsFeatured.first.name());

      this.projectStats = this.projectAndIsFeatured
        .map(projectAndIsFeatured ->
          Pair.create(
            (int) projectAndIsFeatured.first.percentageFunded(),
            ProjectUtils.deadlineCountdownValue(projectAndIsFeatured.first)
          )
        );

      this.notifyDelegateOfResultClick = this.projectAndIsFeatured
        .map(projectAndIsFeatured -> projectAndIsFeatured.first)
        .compose(takeWhen(this.projectClicked));
    }

    private final PublishSubject<Pair<Project, Boolean>> projectAndIsFeatured = PublishSubject.create();
    private final PublishSubject<Void> projectClicked = PublishSubject.create();

    private final Observable<Project> notifyDelegateOfResultClick;
    private final Observable<String> projectNameTextViewText;
    private final Observable<String> projectPhotoUrl;
    private final Observable<Pair<Integer, Integer>> projectStats;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull Pair<Project, Boolean> projectAndIsFeatured) {
      this.projectAndIsFeatured.onNext(projectAndIsFeatured);
    }
    @Override public void projectClicked() {
      this.projectClicked.onNext(null);
    }

    @Override public Observable<Project> notifyDelegateOfResultClick() {
      return this.notifyDelegateOfResultClick;
    }
    @Override public Observable<String> projectPhotoUrl() {
      return this.projectPhotoUrl;
    }
    @Override public Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public Observable<Pair<Integer, Integer>> projectStats() {
      return this.projectStats;
    }

    private static @NonNull String photoUrl(final @NonNull Pair<Project, Boolean> projectAndIsFeatured) {
      final Photo photo = projectAndIsFeatured.first.photo();
      if (photo == null) {
        return null;
      }

      return projectAndIsFeatured.second ? photo.full() : photo.med();
    }
  }
}
