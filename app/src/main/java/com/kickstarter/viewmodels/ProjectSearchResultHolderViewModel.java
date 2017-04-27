package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

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
    void onClick();
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

      this.configData
        .map(ViewModel::projectImage)
        .subscribe(this.projectImage);

      this.configData
        .map(data -> data.project.name())
        .subscribe(this.projectName);

      this.configData
        .map(data ->
          Pair.create((int) data.project.percentageFunded(), ProjectUtils.deadlineCountdownValue(data.project))
        )
        .subscribe(this.projectStats);

      this.configData
        .map(data -> data.project)
        .compose(Transformers.takeWhen(this.onClick))
        .subscribe(this.notifyDelegateOfResultClick);
    }

    @Override public void configureWith(final @NonNull Data data) {
      this.configData.onNext(data);
    }

    @Override public void onClick() {
      this.onClick.onNext(null);
    }

    private final PublishSubject<Data> configData = PublishSubject.create();
    private final PublishSubject<Void> onClick = PublishSubject.create();

    private final BehaviorSubject<Project> notifyDelegateOfResultClick = BehaviorSubject.create();
    private final BehaviorSubject<String> projectImage = BehaviorSubject.create();
    private final BehaviorSubject<String> projectName = BehaviorSubject.create();
    private final BehaviorSubject<Pair<Integer, Integer>> projectStats = BehaviorSubject.create();

    public final ProjectSearchResultHolderViewModel.Inputs inputs = this;
    public final ProjectSearchResultHolderViewModel.Outputs outputs = this;

    @Override  public Observable<Project> notifyDelegateOfResultClick() {
      return this.notifyDelegateOfResultClick;
    }
    @Override public Observable<String> projectImage() {
      return projectImage;
    }
    @Override public Observable<String> projectName() {
      return projectName;
    }
    @Override public Observable<Pair<Integer, Integer>> projectStats() {
      return projectStats;
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
