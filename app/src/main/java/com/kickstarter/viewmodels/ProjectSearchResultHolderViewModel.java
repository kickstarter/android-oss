package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

  final class Data {
    final Project project;
    final boolean isFeatured;

    public Data(final @NonNull Project project, final boolean isFeatured) {
      this.project = project;
      this.isFeatured = isFeatured;
    }
  }

  interface Inputs {
    /** Call to configure the view model with a message. */
    void configureWith(Data data);

    /** Call to say user clicked a project */
    void projectClicked();
  }

  interface Outputs {
    /** Emits key image of project */
    Observable<String> projectImage();

    /** Emits title of project */
    Observable<String> projectName();

    /** Emits a completed / days to go pair */
    Observable<Pair<Integer, Integer>> projectStats();

    /** Emits the project clicked by the user. */
    Observable<Project> notifyDelegateOfResultClick();
  }

  final class ViewModel extends ActivityViewModel<ProjectSearchResultViewHolder> implements
    ProjectSearchResultHolderViewModel.Inputs,
    ProjectSearchResultHolderViewModel.Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.projectImage = this.configData
        .map(ViewModel::projectImage);

      this.projectName = this.configData
        .map(data -> data.project.name());

      this.projectStats = this.configData
        .map(data ->
          Pair.create((int) data.project.percentageFunded(), ProjectUtils.deadlineCountdownValue(data.project))
        );

      this.notifyDelegateOfResultClick = this.configData
        .map(data -> data.project)
        .compose(takeWhen(this.projectClicked));
    }

    private final PublishSubject<Data> configData = PublishSubject.create();
    private final PublishSubject<Void> projectClicked = PublishSubject.create();

    private final Observable<Project> notifyDelegateOfResultClick;
    private final Observable<String> projectImage;
    private final Observable<String> projectName;
    private final Observable<Pair<Integer, Integer>> projectStats;

    public final ProjectSearchResultHolderViewModel.Inputs inputs = this;
    public final ProjectSearchResultHolderViewModel.Outputs outputs = this;

    @Override public void configureWith(final @NonNull Data data) {
      this.configData.onNext(data);
    }
    @Override public void projectClicked() {
      this.projectClicked.onNext(null);
    }

    @Override public Observable<Project> notifyDelegateOfResultClick() {
      return this.notifyDelegateOfResultClick;
    }
    @Override public Observable<String> projectImage() {
      return this.projectImage;
    }
    @Override public Observable<String> projectName() {
      return this.projectName;
    }
    @Override public Observable<Pair<Integer, Integer>> projectStats() {
      return this.projectStats;
    }

    private static @Nullable String projectImage(final @NonNull Data data) {
      final Photo photo = data.project.photo();
      if (photo == null) {
        return null;
      }

      return data.isFeatured ? photo.full() : photo.med();
    }
  }
}
