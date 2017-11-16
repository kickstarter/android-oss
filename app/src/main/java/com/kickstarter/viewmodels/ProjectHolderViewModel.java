package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ProjectViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface ProjectHolderViewModel {

  interface Inputs {
    void configureWith(Pair<Project, String> projectAndCountry);
  }

  interface Outputs {
    Observable<String> avatarPhotoUrl();
    Observable<String> backersCountTextViewText();
    Observable<String> blurbTextViewText();
    Observable<String> categoryTextViewText();
    Observable<String> commentsCountTextViewText();
    Observable<String> creatorNameTextViewText();
    Observable<String> deadlineCountdownTextViewText();
    Observable<String> locationTextViewText();
    Observable<Integer> percentageFundedProgress();
    Observable<Boolean> playButtonIsGone();
    Observable<Project> projectForDeadlineCountdownTextView();
    Observable<String> projectNameTextViewText();
    Observable<Photo> projectPhoto();
    Observable<String> updatesCountTextViewText();
  }

  final class ViewModel extends ActivityViewModel<ProjectViewHolder> implements Inputs, Outputs {

    public ViewModel(@NonNull Environment environment) {
      super(environment);

      final Observable<Project> project = this.projectAndCountry.map(PairUtils::first);
      final Observable<String> country = this.projectAndCountry.map(PairUtils::second);

      this.avatarPhotoUrl = project.map(p -> p.creator().avatar().medium());
      this.backersCountTextViewText = project.map(Project::backersCount).map(NumberUtils::format);
      this.blurbTextViewText = project.map(Project::blurb);
      this.categoryTextViewText = project.map(Project::category).filter(ObjectUtils::isNotNull).map(Category::name);
      this.commentsCountTextViewText = project.map(Project::commentsCount).filter(ObjectUtils::isNotNull).map(NumberUtils::format);
      this.creatorNameTextViewText = project.map(p -> p.creator().name());
      this.deadlineCountdownTextViewText = project.map(ProjectUtils::deadlineCountdownValue).map(NumberUtils::format);
      this.locationTextViewText = project.map(Project::location).filter(ObjectUtils::isNotNull).map(Location::displayableName);
      this.percentageFundedProgress = project.map(Project::percentageFunded).map(ProgressBarUtils::progress);
      this.playButtonIsGone = project.map(Project::hasVideo).map(BooleanUtils::negate);
      this.projectForDeadlineCountdownTextView = project;
      this.projectNameTextViewText = project.map(Project::name);
      this.projectPhoto = project.map(Project::photo);
      this.updatesCountTextViewText = project.map(Project::updatesCount).filter(ObjectUtils::isNotNull).map(NumberUtils::format);
    }

    private final PublishSubject<Pair<Project, String>> projectAndCountry = PublishSubject.create();

    private final Observable<String> avatarPhotoUrl;
    private final Observable<String> backersCountTextViewText;
    private final Observable<String> blurbTextViewText;
    private final Observable<String> categoryTextViewText;
    private final Observable<String> commentsCountTextViewText;
    private final Observable<String> creatorNameTextViewText;
    private final Observable<String> deadlineCountdownTextViewText;
    private final Observable<String> locationTextViewText;
    private final Observable<Integer> percentageFundedProgress;
    private final Observable<Boolean> playButtonIsGone;
    private final Observable<Project> projectForDeadlineCountdownTextView;
    private final Observable<String> projectNameTextViewText;
    private final Observable<Photo> projectPhoto;
    private final Observable<String> updatesCountTextViewText;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull Pair<Project, String> projectAndCountry) {
      this.projectAndCountry.onNext(projectAndCountry);
    }

    @Override public @NonNull Observable<String> avatarPhotoUrl() {
      return this.avatarPhotoUrl;
    }
    @Override public @NonNull Observable<String> backersCountTextViewText() {
      return this.backersCountTextViewText;
    }
    @Override public @NonNull Observable<String> blurbTextViewText() {
      return this.blurbTextViewText;
    }
    @Override public @NonNull Observable<String> categoryTextViewText() {
      return this.categoryTextViewText;
    }
    @Override public @NonNull Observable<String> commentsCountTextViewText() {
      return this.commentsCountTextViewText;
    }
    @Override public @NonNull Observable<String> creatorNameTextViewText() {
      return this.creatorNameTextViewText;
    }
    @Override public @NonNull Observable<String> deadlineCountdownTextViewText() {
      return this.deadlineCountdownTextViewText;
    }
    @Override public @NonNull Observable<String> locationTextViewText() {
      return this.locationTextViewText;
    }
    @Override public @NonNull Observable<Integer> percentageFundedProgress() {
      return this.percentageFundedProgress;
    }
    @Override public @NonNull Observable<Boolean> playButtonIsGone() {
      return this.playButtonIsGone;
    }
    @Override public @NonNull Observable<Project> projectForDeadlineCountdownTextView() {
      return this.projectForDeadlineCountdownTextView;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public @NonNull Observable<Photo> projectPhoto() {
      return this.projectPhoto;
    }
    @Override public @NonNull Observable<String> updatesCountTextViewText() {
      return this.updatesCountTextViewText;
    }
  }
}
