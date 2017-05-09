package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;

import org.joda.time.DateTime;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.coalesce;

public interface ProjectCardHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a project. */
    void configureWith(Project project);
    void projectClicked();
  }

  interface Outputs {
    /** Emits the string name of the projects category. */
    Observable<String> backersCountText();
    Observable<Boolean> backingViewGroupIsGone();
    Observable<String> categoryNameText();
    Observable<String> deadlineCountdownText();
    Observable<Boolean> featuredViewGroupIsGone();
    Observable<List<User>> friendsForNamepile();
    Observable<String> friendAvatarUrl();
    Observable<Boolean> imageIsInvisible();
    Observable<Boolean> friendBackingViewIsHidden();
    Observable<Boolean> fundingUnsuccessfulTextViewIsGone();
    Observable<Boolean> metadataViewGroupIsGone();
    Observable<Project> projectOutput();
    Observable<Integer> percentageFunded();
    Observable<Boolean> percentageFundedProgressBarIsGone();
    Observable<String> percentageFundedText();
    Observable<String> photoUrl();
    Observable<String> blurbText();
    Observable<String> nameText();
    Observable<Project> notifyDelegateOfProjectClick();
    Observable<Boolean> potdViewGroupIsGone();
    Observable<DateTime> projectCanceledAt();
    Observable<DateTime> projectFailedAt();
    Observable<Boolean> projectStateViewGroupIsGone();
    Observable<DateTime> projectSuccessfulAt();
    Observable<DateTime> projectSuspendedAt();
    Observable<String> rootCategoryNameForFeatured();
    Observable<Void> setDefaultTopPadding();
    Observable<Void> setMetadataTopPadding();
    Observable<Boolean> starredViewGroupIsGone();
    Observable<Boolean> successfullyFundedTextViewIsGone();
  }

  final class ViewModel extends ActivityViewModel<ProjectCardViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.projectOutput = this.project;

      this.backersCountText = this.project
        .map(Project::backersCount)
        .map(NumberUtils::format);

      this.backingViewGroupIsGone = this.project
        .map(p -> p.isBacking() || p.isStarred() || p.isPotdToday() || p.isFeaturedToday());

      this.blurbText = this.project
        .map(Project::blurb);

      this.categoryNameText = this.project
        .map(Project::category)
        .map(Category::name)
        .compose(coalesce(""));

      this.deadlineCountdownText = this.project
        .map(ProjectUtils::deadlineCountdownValue)
        .map(NumberUtils::format);

      this.featuredViewGroupIsGone = this.project
        .map(p -> p.isBacking() || p.isStarred() || p.isPotdToday() || !p.isFeaturedToday());

      this.friendAvatarUrl = this.project
        .filter(Project::isFriendBacking)
        .map(Project::friends)
        .map(friends -> friends.get(0).avatar().small());

      this.friendBackingViewIsHidden = this.project
        .map(Project::isFriendBacking)
        .map(BooleanUtils::negate);

      this.friendsForNamepile = this.project
        .filter(Project::isFriendBacking)
        .map(Project::friends);

      this.fundingUnsuccessfulTextViewIsGone = this.project
        .map(p ->
          !p.state().equals(Project.STATE_CANCELED) &&
            !p.state().equals(Project.STATE_FAILED) &&
            !p.state().equals(Project.STATE_SUSPENDED));

      this.imageIsInvisible = this.project
        .map(Project::photo)
        .map(ObjectUtils::isNull);

      this.metadataViewGroupIsGone = this.backingViewGroupIsGone
        .map(BooleanUtils::isFalse);

      this.nameText = this.project
        .map(Project::name);

      this.notifyDelegateOfProjectClick = this.project
        .compose(Transformers.takeWhen(this.projectClicked));

      this.percentageFunded = this.project
        .map(Project::percentageFunded)
        .map(ProgressBarUtils::progress);

      this.percentageFundedProgressBarIsGone = this.project
        .map(ProjectUtils::isCompleted);

      this.percentageFundedText = this.project
        .map(Project::percentageFunded)
        .map(NumberUtils::flooredPercentage);

      this.photoUrl = this.project
        .map(Project::photo)
        .filter(ObjectUtils::isNotNull)
        .map(Photo::med);

      this.potdViewGroupIsGone = this.project
        .map(p -> p.isBacking() || p.isStarred() || !p.isPotdToday() || p.isFeaturedToday());

      this.projectCanceledAt = this.project
        .filter(p -> p.state().equals(Project.STATE_CANCELED))
        .map(Project::stateChangedAt)
        .compose(coalesce(new DateTime()));

      this.projectFailedAt = this.project
        .filter(p -> p.state().equals(Project.STATE_FAILED))
        .map(Project::stateChangedAt)
        .compose(coalesce(new DateTime()));

      this.projectStateViewGroupIsGone = this.project
        .map(ProjectUtils::isCompleted)
        .map(BooleanUtils::negate);

      this.projectSuccessfulAt = this.project
        .filter(p -> p.state().equals(Project.STATE_SUCCESSFUL))
        .map(Project::stateChangedAt)
        .compose(coalesce(new DateTime()));

      this.projectSuspendedAt = this.project
        .filter(p -> p.state().equals(Project.STATE_SUSPENDED))
        .map(Project::stateChangedAt)
        .compose(coalesce(new DateTime()));

      this.rootCategoryNameForFeatured = this.project
        .map(Project::category)
        .filter(ObjectUtils::isNotNull)
        .map(Category::root)
        .filter(ObjectUtils::isNotNull)
        .map(Category::name);

      this.setDefaultTopPadding = this.metadataViewGroupIsGone
        .filter(b -> b)
        .compose(Transformers.ignoreValues());

      this.setMetadataTopPadding = this.metadataViewGroupIsGone
        .filter(b -> !b)
        .compose(Transformers.ignoreValues());

      this.starredViewGroupIsGone = this.project
        .map(p -> p.isBacking() || !p.isStarred());

      this.successfullyFundedTextViewIsGone = this.project
        .map(p -> !p.state().equals(Project.STATE_SUCCESSFUL));
    }

    private final PublishSubject<Project> project = PublishSubject.create();
    private final PublishSubject<Void> projectClicked = PublishSubject.create();

    private final Observable<String> backersCountText;
    private final Observable<Boolean> backingViewGroupIsGone;
    private final Observable<String> blurbText;
    private final Observable<String> categoryNameText;
    private final Observable<String> deadlineCountdownText;
    private final Observable<Boolean> featuredViewGroupIsGone;
    private final Observable<String> friendAvatarUrl;
    private final Observable<Boolean> friendBackingViewIsHidden;
    private final Observable<List<User>> friendsForNamepile;
    private final Observable<Boolean> fundingUnsuccessfulTextViewIsGone;
    private final Observable<Boolean> imageIsInvisible;
    private final Observable<Boolean> metadataViewGroupIsGone;
    private final Observable<String> nameText;
    private final Observable<Project> notifyDelegateOfProjectClick;
    private final Observable<Integer> percentageFunded;
    private final Observable<Boolean> percentageFundedProgressBarIsGone;
    private final Observable<String> percentageFundedText;
    private final Observable<String> photoUrl;
    private final Observable<Boolean> potdViewGroupIsGone;
    private final Observable<Project> projectOutput;
    private final Observable<Boolean> projectStateViewGroupIsGone;
    private final Observable<DateTime> projectCanceledAt;
    private final Observable<DateTime> projectFailedAt;
    private final Observable<DateTime> projectSuccessfulAt;
    private final Observable<DateTime> projectSuspendedAt;
    private final Observable<String> rootCategoryNameForFeatured;
    private final Observable<Void> setDefaultTopPadding;
    private final Observable<Void> setMetadataTopPadding;
    private final Observable<Boolean> starredViewGroupIsGone;
    private final Observable<Boolean> successfullyFundedTextViewIsGone;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void configureWith(final @NonNull Project project) {
      this.project.onNext(project);
    }

    @Override
    public void projectClicked() { this.projectClicked.onNext(null); }

    @Override public @NonNull Observable<String> backersCountText() { return this.backersCountText; }
    @Override public @NonNull Observable<Boolean> backingViewGroupIsGone() { return this.backingViewGroupIsGone; }
    @Override public @NonNull Observable<String> blurbText() { return this.blurbText; }
    @Override public @NonNull Observable<String> categoryNameText() { return this.categoryNameText; }
    @Override public @NonNull Observable<String> deadlineCountdownText() { return this.deadlineCountdownText; }
    @Override public @NonNull Observable<Boolean> featuredViewGroupIsGone() { return this.featuredViewGroupIsGone; }
    @Override public @NonNull Observable<String> friendAvatarUrl() { return this.friendAvatarUrl; }
    @Override public @NonNull Observable<Boolean> friendBackingViewIsHidden() { return this.friendBackingViewIsHidden; }
    @Override public @NonNull Observable<List<User>> friendsForNamepile() { return this.friendsForNamepile; }
    @Override public @NonNull Observable<Boolean> fundingUnsuccessfulTextViewIsGone() { return this.fundingUnsuccessfulTextViewIsGone; }
    @Override public @NonNull Observable<Boolean> imageIsInvisible() { return this.imageIsInvisible; }
    @Override public @NonNull Observable<Boolean> metadataViewGroupIsGone() { return this.metadataViewGroupIsGone; }
    @Override public @NonNull Observable<String> nameText() { return this.nameText; }
    @Override public @NonNull Observable<Project> notifyDelegateOfProjectClick() { return this.notifyDelegateOfProjectClick; }
    @Override public @NonNull Observable<Integer> percentageFunded() { return this.percentageFunded; }
    @Override public @NonNull Observable<Boolean> percentageFundedProgressBarIsGone() { return this.percentageFundedProgressBarIsGone; }
    @Override public @NonNull Observable<String> percentageFundedText() { return this.percentageFundedText; }
    @Override public @NonNull Observable<String> photoUrl() { return this.photoUrl; }
    @Override public @NonNull Observable<Boolean> potdViewGroupIsGone() { return this.potdViewGroupIsGone; }
    @Override public @NonNull Observable<Project> projectOutput() { return this.projectOutput; }
    @Override public @NonNull Observable<Boolean> projectStateViewGroupIsGone() { return this.projectStateViewGroupIsGone; }
    @Override public @NonNull Observable<DateTime> projectCanceledAt() { return this.projectCanceledAt; }
    @Override public @NonNull Observable<DateTime> projectFailedAt() { return this.projectFailedAt; }
    @Override public @NonNull Observable<DateTime> projectSuccessfulAt() { return this.projectSuccessfulAt; }
    @Override public @NonNull Observable<DateTime> projectSuspendedAt() { return this.projectSuspendedAt; }
    @Override public @NonNull Observable<String> rootCategoryNameForFeatured() { return this.rootCategoryNameForFeatured; }
    @Override public @NonNull Observable<Void> setDefaultTopPadding() { return this.setDefaultTopPadding; }
    @Override public @NonNull Observable<Void> setMetadataTopPadding() { return this.setMetadataTopPadding; }
    @Override public @NonNull Observable<Boolean> starredViewGroupIsGone() { return this.starredViewGroupIsGone; }
    @Override public @NonNull Observable<Boolean> successfullyFundedTextViewIsGone() { return this.successfullyFundedTextViewIsGone; }
  }
}
