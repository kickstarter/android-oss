package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;

import org.joda.time.DateTime;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.coalesce;

public interface ProjectCardHolderViewModel {

  interface Inputs {
    /** Call to configure view model with a project. */
    void configureWith(Pair<Project, DiscoveryParams> projectAndDiscoveryParams);

    /** Call when the project card has been clicked. */
    void projectCardClicked();
  }

  interface Outputs {
    Observable<String> backersCountTextViewText();
    Observable<Boolean> backingViewGroupIsGone();
    Observable<String> deadlineCountdownText();
    Observable<Boolean> featuredViewGroupIsGone();
    Observable<List<User>> friendsForNamepile();
    Observable<Boolean> friendAvatar2IsGone();
    Observable<Boolean> friendAvatar3IsGone();
    Observable<String> friendAvatarUrl1();
    Observable<String> friendAvatarUrl2();
    Observable<String> friendAvatarUrl3();
    Observable<Boolean> imageIsInvisible();
    Observable<Boolean> friendBackingViewIsHidden();
    Observable<Boolean> fundingUnsuccessfulViewGroupIsGone();
    Observable<Boolean> fundingSuccessfulViewGroupIsGone();
    Observable<Boolean> metadataViewGroupIsGone();
    Observable<Integer> metadataViewGroupBackgroundDrawable();
    Observable<Project> projectForDeadlineCountdownDetail();
    Observable<Integer> percentageFundedForProgressBar();
    Observable<Boolean> percentageFundedProgressBarIsGone();
    Observable<String> percentageFundedTextViewText();
    Observable<String> photoUrl();
    Observable<Pair<String, String>> nameAndBlurbText();
    Observable<Project> notifyDelegateOfProjectClick();
    Observable<DateTime> projectCanceledAt();
    Observable<Boolean> projectCardStatsViewGroupIsGone();
    Observable<DateTime> projectFailedAt();
    Observable<Boolean> projectStateViewGroupIsGone();
    Observable<String> projectSubcategory();
    Observable<Boolean> projectSubcategoryIsGone();
    Observable<DateTime> projectSuccessfulAt();
    Observable<DateTime> projectSuspendedAt();
    Observable<String> rootCategoryNameForFeatured();
    Observable<Boolean> savedViewGroupIsGone();
    Observable<Boolean> setDefaultTopPadding();
  }

  final class ViewModel extends ActivityViewModel<ProjectCardViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.projectForDeadlineCountdownDetail = this.project;

      this.backersCountTextViewText = this.project
        .map(Project::backersCount)
        .map(NumberUtils::format);

      this.backingViewGroupIsGone = this.project
        .map(p -> ProjectUtils.metadataForProject(p) != ProjectUtils.Metadata.BACKING);

      this.deadlineCountdownText = this.project
        .map(ProjectUtils::deadlineCountdownValue)
        .map(NumberUtils::format);

      this.featuredViewGroupIsGone = this.project
        .map(p -> ProjectUtils.metadataForProject(p) != ProjectUtils.Metadata.CATEGORY_FEATURED);

      this.friendAvatarUrl1 = this.project
        .filter(Project::isFriendBacking)
        .map(Project::friends)
        .map(friends -> friends.get(0).avatar().small());

      this.friendAvatarUrl2 = this.project
        .filter(Project::isFriendBacking)
        .map(Project::friends)
        .filter(friends -> friends.size() > 1)
        .map(friends -> friends.get(1).avatar().small());

      this.friendAvatarUrl3 = this.project
        .filter(Project::isFriendBacking)
        .map(Project::friends)
        .filter(friends -> friends.size() > 2)
        .map(friends -> friends.get(2).avatar().small());

      this.friendAvatar2IsGone = this.project
        .map(Project::friends)
        .map(friends -> friends != null && friends.size() > 1)
        .map(BooleanUtils::negate);

      this.friendAvatar3IsGone = this.project
        .map(Project::friends)
        .map(friends -> friends != null && friends.size() > 2)
        .map(BooleanUtils::negate);

      this.friendBackingViewIsHidden = this.project
        .map(Project::isFriendBacking)
        .map(BooleanUtils::negate);

      this.friendsForNamepile = this.project
        .filter(Project::isFriendBacking)
        .map(Project::friends);

      this.fundingUnsuccessfulViewGroupIsGone = this.project
        .map(p ->
          !p.state().equals(Project.STATE_CANCELED)
            && !p.state().equals(Project.STATE_FAILED)
            && !p.state().equals(Project.STATE_SUSPENDED));

      this.fundingSuccessfulViewGroupIsGone = this.project
        .map(p -> !p.state().equals(Project.STATE_SUCCESSFUL));

      this.imageIsInvisible = this.project
        .map(Project::photo)
        .map(ObjectUtils::isNull);

      this.metadataViewGroupIsGone = this.project
        .map(p -> ProjectUtils.metadataForProject(p) == null);

      this.metadataViewGroupBackground = this.backingViewGroupIsGone
        .map(gone -> gone ? R.drawable.rect_white_grey_stroke : R.drawable.rect_green_grey_stroke);

      this.nameAndBlurbText = this.project
        .map(p -> Pair.create(p.name(), p.blurb()));

      this.notifyDelegateOfProjectClick = this.project
        .compose(Transformers.takeWhen(this.projectCardClicked));

      this.percentageFundedForProgressBar = this.project
        .map(p -> (p.state().equals(Project.STATE_LIVE) || p.state().equals(Project.STATE_SUCCESSFUL)) ? p.percentageFunded() : 0.0f)
        .map(ProgressBarUtils::progress);

      this.percentageFundedProgressBarIsGone = this.project
        .map(p -> p.state().equals(Project.STATE_CANCELED));

      this.percentageFundedTextViewText = this.project
        .map(Project::percentageFunded)
        .map(NumberUtils::flooredPercentage);

      this.photoUrl = this.project
        .map(p -> p.photo() == null ? null : p.photo().full());

      this.projectCanceledAt = this.project
        .filter(p -> p.state().equals(Project.STATE_CANCELED))
        .map(Project::stateChangedAt)
        .compose(coalesce(new DateTime()));

      this.projectCardStatsViewGroupIsGone = this.project
        .map(p -> !p.state().equals(Project.STATE_LIVE));

      this.projectFailedAt = this.project
        .filter(p -> p.state().equals(Project.STATE_FAILED))
        .map(Project::stateChangedAt)
        .compose(coalesce(new DateTime()));

      this.projectStateViewGroupIsGone = this.project
        .map(ProjectUtils::isCompleted)
        .map(BooleanUtils::negate);

      this.projectSubcategory = this.project
        .map(Project::category)
        .filter(ObjectUtils::isNotNull)
        .map(Category::name);

      this.projectSubcategoryIsGone = this.category
        .filter(ObjectUtils::isNotNull)
        .map(Category::isRoot)
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

      this.savedViewGroupIsGone = this.project
        .map(p -> ProjectUtils.metadataForProject(p) != ProjectUtils.Metadata.SAVING);

      this.setDefaultTopPadding = this.metadataViewGroupIsGone;
    }

    private final PublishSubject<Category> category = PublishSubject.create();
    private final PublishSubject<Project> project = PublishSubject.create();
    private final PublishSubject<Void> projectCardClicked = PublishSubject.create();

    private final Observable<String> backersCountTextViewText;
    private final Observable<Boolean> backingViewGroupIsGone;
    private final Observable<String> deadlineCountdownText;
    private final Observable<Boolean> featuredViewGroupIsGone;
    private final Observable<Boolean> friendAvatar2IsGone;
    private final Observable<Boolean> friendAvatar3IsGone;
    private final Observable<String> friendAvatarUrl1;
    private final Observable<String> friendAvatarUrl2;
    private final Observable<String> friendAvatarUrl3;
    private final Observable<Boolean> friendBackingViewIsHidden;
    private final Observable<List<User>> friendsForNamepile;
    private final Observable<Boolean> fundingSuccessfulViewGroupIsGone;
    private final Observable<Boolean> fundingUnsuccessfulViewGroupIsGone;
    private final Observable<Boolean> imageIsInvisible;
    private final Observable<Integer> metadataViewGroupBackground;
    private final Observable<Boolean> metadataViewGroupIsGone;
    private final Observable<Pair<String, String>> nameAndBlurbText;
    private final Observable<Project> notifyDelegateOfProjectClick;
    private final Observable<Integer> percentageFundedForProgressBar;
    private final Observable<Boolean> percentageFundedProgressBarIsGone;
    private final Observable<String> percentageFundedTextViewText;
    private final Observable<String> photoUrl;
    private final Observable<Project> projectForDeadlineCountdownDetail;
    private final Observable<Boolean> projectCardStatsViewGroupIsGone;
    private final Observable<Boolean> projectStateViewGroupIsGone;
    private final Observable<DateTime> projectCanceledAt;
    private final Observable<DateTime> projectFailedAt;
    private final Observable<String> projectSubcategory;
    private final Observable<Boolean> projectSubcategoryIsGone;
    private final Observable<DateTime> projectSuccessfulAt;
    private final Observable<DateTime> projectSuspendedAt;
    private final Observable<String> rootCategoryNameForFeatured;
    private final Observable<Boolean> savedViewGroupIsGone;
    private final Observable<Boolean> setDefaultTopPadding;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull Pair<Project, DiscoveryParams> projectAndCategory) {
      this.project.onNext(projectAndCategory.first);
      this.category.onNext(projectAndCategory.second.category());
    }
    @Override public void projectCardClicked() {
      this.projectCardClicked.onNext(null);
    }

    @Override public @NonNull Observable<String> backersCountTextViewText() {
      return this.backersCountTextViewText;
    }
    @Override public @NonNull Observable<Boolean> backingViewGroupIsGone() {
      return this.backingViewGroupIsGone;
    }
    @Override public @NonNull Observable<String> deadlineCountdownText() {
      return this.deadlineCountdownText;
    }
    @Override public @NonNull Observable<Boolean> featuredViewGroupIsGone() {
      return this.featuredViewGroupIsGone;
    }
    @Override public @NonNull Observable<Boolean> friendAvatar2IsGone() {
      return this.friendAvatar2IsGone;
    }
    @Override public @NonNull Observable<Boolean> friendAvatar3IsGone() {
      return this.friendAvatar3IsGone;
    }
    @Override public @NonNull Observable<String> friendAvatarUrl1() {
      return this.friendAvatarUrl1;
    }
    @Override public @NonNull Observable<String> friendAvatarUrl2() {
      return this.friendAvatarUrl2;
    }
    @Override public @NonNull Observable<String> friendAvatarUrl3() {
      return this.friendAvatarUrl3;
    }
    @Override public @NonNull Observable<Boolean> friendBackingViewIsHidden() {
      return this.friendBackingViewIsHidden;
    }
    @Override public @NonNull Observable<List<User>> friendsForNamepile() {
      return this.friendsForNamepile;
    }
    @Override public @NonNull Observable<Boolean> fundingSuccessfulViewGroupIsGone() {
      return this.fundingSuccessfulViewGroupIsGone;
    }
    @Override public @NonNull Observable<Boolean> fundingUnsuccessfulViewGroupIsGone() {
      return this.fundingUnsuccessfulViewGroupIsGone;
    }
    @Override public @NonNull Observable<Boolean> imageIsInvisible() {
      return this.imageIsInvisible;
    }
    @Override
    public Observable<Integer> metadataViewGroupBackgroundDrawable() {
      return this.metadataViewGroupBackground;
    }
    @Override public @NonNull Observable<Boolean> metadataViewGroupIsGone() {
      return this.metadataViewGroupIsGone;
    }
    @Override
    public Observable<Pair<String, String>> nameAndBlurbText() {
      return this.nameAndBlurbText;
    }
    @Override public @NonNull Observable<Project> notifyDelegateOfProjectClick() {
      return this.notifyDelegateOfProjectClick;
    }
    @Override public @NonNull Observable<Integer> percentageFundedForProgressBar() {
      return this.percentageFundedForProgressBar;
    }
    @Override public @NonNull Observable<Boolean> percentageFundedProgressBarIsGone() {
      return this.percentageFundedProgressBarIsGone;
    }
    @Override public @NonNull Observable<String> percentageFundedTextViewText() {
      return this.percentageFundedTextViewText;
    }
    @Override public @NonNull Observable<String> photoUrl() {
      return this.photoUrl;
    }
    @Override public @NonNull Observable<Boolean> projectCardStatsViewGroupIsGone() {
      return this.projectCardStatsViewGroupIsGone;
    }
    @Override public @NonNull Observable<Project> projectForDeadlineCountdownDetail() {
      return this.projectForDeadlineCountdownDetail;
    }
    @Override public @NonNull Observable<Boolean> projectStateViewGroupIsGone() {
      return this.projectStateViewGroupIsGone;
    }
    @Override
    public Observable<String> projectSubcategory() {
      return this.projectSubcategory;
    }
    @Override
    public Observable<Boolean> projectSubcategoryIsGone() {
      return this.projectSubcategoryIsGone;
    }
    @Override public @NonNull Observable<DateTime> projectCanceledAt() {
      return this.projectCanceledAt;
    }
    @Override public @NonNull Observable<DateTime> projectFailedAt() {
      return this.projectFailedAt;
    }
    @Override public @NonNull Observable<DateTime> projectSuccessfulAt() {
      return this.projectSuccessfulAt;
    }
    @Override public @NonNull Observable<DateTime> projectSuspendedAt() {
      return this.projectSuspendedAt;
    }
    @Override public @NonNull Observable<String> rootCategoryNameForFeatured() {
      return this.rootCategoryNameForFeatured;
    }
    @Override public @NonNull Observable<Boolean> setDefaultTopPadding() {
      return this.setDefaultTopPadding;
    }
    @Override public @NonNull Observable<Boolean> savedViewGroupIsGone() {
      return this.savedViewGroupIsGone;
    }
  }
}
