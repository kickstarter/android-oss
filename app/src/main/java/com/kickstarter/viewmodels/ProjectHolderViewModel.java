package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.UserCurrency;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.I18nUtils;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.ProjectViewHolder;

import org.joda.time.DateTime;

import java.math.RoundingMode;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface ProjectHolderViewModel {

  interface Inputs {
    /** Call to configure view holder with a project and the config country. */
    void configureWith(Pair<Project, String> projectAndCountry);

    /** Call when the project social view group is clicked. */
    void projectSocialViewGroupClicked();
  }

  interface Outputs {
    /** Emits the creator's avatar photo url for display. */
    Observable<String> avatarPhotoUrl();

    /** Emits the backers count string for display. */
    Observable<String> backersCountTextViewText();

    /** Emits when the backing view group should be gone. */
    Observable<Boolean> backingViewGroupIsGone();

    /** Emits the project blurb for display. */
    Observable<String> blurbTextViewText();

    /** Emits the project category for display. */
    Observable<String> categoryTextViewText();

    /** Emits teh comments count for display. */
    Observable<String> commentsCountTextViewText();

    /** Emits the project creator's name for display. */
    Observable<String> creatorNameTextViewText();

    /** Emits the deadline countdown text for display. */
    Observable<String> deadlineCountdownTextViewText();

    /** Emits root category to display in the featured metadata. */
    Observable<String> featuredTextViewRootCategory();

    /** Emits the featured view group should be gone. */
    Observable<Boolean> featuredViewGroupIsGone();

    /** Emits the goal string for display. */
    Observable<String> goalStringForTextView();

    /** Emits the location for display. */
    Observable<String> locationTextViewText();

    /** Emits the percentage funded amount for display in the progress bar. */
    Observable<Integer> percentageFundedProgress();

    /** Emits when the progress bar should be gone. */
    Observable<Boolean> percentageFundedProgressBarIsGone();

    /** Emits when the play button should be gone. */
    Observable<Boolean> playButtonIsGone();

    /** Emits the pledged amount for display. */
    Observable<String> pledgedTextViewText();

    /** Emits the date time to be displayed in the disclaimer. */
    Observable<DateTime> projectDisclaimerGoalReachedDateTime();

    /** Emits a string and date time for an unsuccessful project disclaimer. */
    Observable<Pair<String, DateTime>> projectDisclaimerGoalNotReachedString();

    /** Emits when the disclaimer view should be gone. */
    Observable<Boolean> projectDisclaimerTextViewIsGone();

    /** Emits the background drawable for the metadata view group. */
    Observable<Integer> projectMetadataViewGroupBackgroundDrawableInt();

    /** Emits when the metadata view group should be gone. */
    Observable<Boolean> projectMetadataViewGroupIsGone();

    /** Emits the project name for display. */
    Observable<String> projectNameTextViewText();

    /** Emits the project for display. */
    Observable<Project> projectOutput();

    /** Emits the project photo for display. */
    Observable<Photo> projectPhoto();

    /** Emits when the social image view should be gone. */
    Observable<Boolean> projectSocialImageViewIsGone();

    /** Emits the social image view url for display. */
    Observable<String> projectSocialImageViewUrl();

    /** Emits the list of friends to display display in the facepile.*/
    Observable<List<User>> projectSocialTextViewFriends();

    /** Emits when the social view group should be gone. */
    Observable<Boolean> projectSocialViewGroupIsGone();

    /** Emits the state background color int for display. */
    Observable<Integer> projectStateViewGroupBackgroundColorInt();

    /** Emits when the project state view group should be gone. */
    Observable<Boolean> projectStateViewGroupIsGone();

    /** Emits when we should set default stats margins. */
    Observable<Boolean> shouldSetDefaultStatsMargins();

    /** Emits when we should set the canceled state view. */
    Observable<Void> setCanceledProjectStateView();

    /** Emits when we should set an on click listener to the social view. */
    Observable<Void> setProjectSocialClickListener();

    /** Emits when we should set the successful state view. */
    Observable<DateTime> setSuccessfulProjectStateView();

    /** Emits when we should set the suspended state view. */
    Observable<Void> setSuspendedProjectStateView();

    /** Emits when we should set the unsuccessful state view. */
    Observable<DateTime> setUnsuccessfulProjectStateView();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectSocialActivity}. */
    Observable<Project> startProjectSocialActivity();

    /** Emits the updates count for display. */
    Observable<String> updatesCountTextViewText();

    /** Emits the usd conversion text for display. */
    Observable<Pair<String, String>> usdConversionPledgedAndGoalText();

    /** Emits when the usd conversion view should be gone. */
    Observable<Boolean> usdConversionTextViewIsGone();
  }

  final class ViewModel extends ActivityViewModel<ProjectViewHolder> implements Inputs, Outputs {
    private final KSCurrency ksCurrency;
    private final UserCurrency userCurrency;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.ksCurrency = environment.ksCurrency();
      this.userCurrency = environment.userCurrency();

      final Observable<Project> project = this.projectAndCountry.map(PairUtils::first);
      final Observable<ProjectUtils.Metadata> projectMetadata = project.map(ProjectUtils::metadataForProject);

      this.avatarPhotoUrl = project.map(p -> p.creator().avatar().medium());
      this.backersCountTextViewText = project.map(Project::backersCount).map(NumberUtils::format);

      this.backingViewGroupIsGone = projectMetadata
        .map(ProjectUtils.Metadata.BACKING::equals)
        .map(BooleanUtils::negate);

      this.blurbTextViewText = project.map(Project::blurb);
      this.categoryTextViewText = project.map(Project::category).filter(ObjectUtils::isNotNull).map(Category::name);

      this.commentsCountTextViewText = project
        .map(Project::commentsCount)
        .filter(ObjectUtils::isNotNull)
        .map(NumberUtils::format);

      this.creatorNameTextViewText = project.map(p -> p.creator().name());
      this.deadlineCountdownTextViewText = project.map(ProjectUtils::deadlineCountdownValue).map(NumberUtils::format);

      this.featuredViewGroupIsGone = projectMetadata
        .map(ProjectUtils.Metadata.CATEGORY_FEATURED::equals)
        .map(BooleanUtils::negate);

      this.featuredTextViewRootCategory = this.featuredViewGroupIsGone
        .filter(BooleanUtils::isFalse)
        .compose(combineLatestPair(project))
        .map(bp -> bp.second.category())
        .filter(ObjectUtils::isNotNull)
        .map(Category::root)
        .filter(ObjectUtils::isNotNull)
        .map(Category::name);

      this.goalStringForTextView = project
        .map(p -> this.userCurrency.format(p.goal(), p, false, RoundingMode.DOWN,
          p.current_currency()));

      this.locationTextViewText = project
        .map(Project::location)
        .filter(ObjectUtils::isNotNull)
        .map(Location::displayableName);

      this.percentageFundedProgress = project.map(Project::percentageFunded).map(ProgressBarUtils::progress);

      this.percentageFundedProgressBarIsGone = project
        .map(p -> p.isSuccessful() || p.isCanceled() || p.isFailed() || p.isSuspended());

      this.playButtonIsGone = project.map(Project::hasVideo).map(BooleanUtils::negate);

      this.pledgedTextViewText = project
        .map(p -> this.userCurrency.format(p.pledged(), p,
          false, RoundingMode.DOWN, p.current_currency()));

      this.projectDisclaimerGoalReachedDateTime = project
        .filter(Project::isFunded)
        .map(Project::deadline);

      this.projectDisclaimerGoalNotReachedString = project
        .filter(p -> p.deadline() != null && p.isLive() && !p.isFunded())
        .map(p -> Pair.create(this.ksCurrency.format(p.goal(), p, true), p.deadline()));

      this.projectDisclaimerTextViewIsGone = project.map(p -> p.deadline() == null || !p.isLive());

      this.projectMetadataViewGroupBackgroundDrawableInt = projectMetadata
        .filter(ProjectUtils.Metadata.BACKING::equals)
        .map(__ -> R.drawable.rect_green_grey_stroke);

      this.projectMetadataViewGroupIsGone = projectMetadata
        .map(m -> m != ProjectUtils.Metadata.CATEGORY_FEATURED && m != ProjectUtils.Metadata.BACKING);

      this.projectNameTextViewText = project.map(Project::name);
      this.projectOutput = project;
      this.projectPhoto = project.map(Project::photo);

      this.projectSocialImageViewUrl = project
        .filter(Project::isFriendBacking)
        .map(Project::friends)
        .map(ListUtils::first)
        .map(f -> f.avatar().small());

      this.projectSocialTextViewFriends = project
        .filter(Project::isFriendBacking)
        .map(Project::friends);

      this.projectSocialViewGroupIsGone = project.map(Project::isFriendBacking).map(BooleanUtils::negate);

      this.projectStateViewGroupBackgroundColorInt = project
        .filter(p -> !p.isLive())
        .map(p -> p.state().equals(Project.STATE_SUCCESSFUL) ? R.color.green_alpha_50 : R.color.ksr_grey_400);

      this.projectStateViewGroupIsGone = project.map(Project::isLive);

      this.projectSocialImageViewIsGone = this.projectSocialViewGroupIsGone;
      this.shouldSetDefaultStatsMargins = this.projectSocialViewGroupIsGone;
      this.setCanceledProjectStateView = project.filter(Project::isCanceled).compose(ignoreValues());

      this.setProjectSocialClickListener = project
        .filter(Project::isFriendBacking)
        .map(Project::friends)
        .filter(fs -> fs.size() > 2)
        .compose(ignoreValues());

      this.setSuccessfulProjectStateView = project
        .filter(Project::isSuccessful)
        .map(p -> ObjectUtils.coalesce(p.stateChangedAt(), new DateTime()));

      this.setSuspendedProjectStateView = project.filter(Project::isSuspended).compose(ignoreValues());

      this.setUnsuccessfulProjectStateView = project
        .filter(Project::isFailed)
        .map(p -> ObjectUtils.coalesce(p.stateChangedAt(), new DateTime()));

      this.startProjectSocialActivity = project.compose(takeWhen(this.projectSocialViewGroupClicked));

      this.updatesCountTextViewText = project
        .map(Project::updatesCount)
        .filter(ObjectUtils::isNotNull)
        .map(NumberUtils::format);

      this.usdConversionTextViewIsGone = this.projectAndCountry
        .map(pc -> I18nUtils.isCountryUS(pc.second) && !I18nUtils.isCountryUS(pc.first.country()))
        .map(BooleanUtils::negate);

      this.usdConversionPledgedAndGoalText = project
        .map(p -> {
          final String pledged = this.ksCurrency.format(p.pledged(), p);
          final String goal = this.ksCurrency.format(p.goal(), p);
          return Pair.create(pledged, goal);
        });
    }

    private final PublishSubject<Pair<Project, String>> projectAndCountry = PublishSubject.create();
    private final PublishSubject<Void> projectSocialViewGroupClicked = PublishSubject.create();

    private final Observable<String> avatarPhotoUrl;
    private final Observable<String> backersCountTextViewText;
    private final Observable<Boolean> backingViewGroupIsGone;
    private final Observable<String> blurbTextViewText;
    private final Observable<String> categoryTextViewText;
    private final Observable<String> commentsCountTextViewText;
    private final Observable<String> creatorNameTextViewText;
    private final Observable<String> deadlineCountdownTextViewText;
    private final Observable<String> featuredTextViewRootCategory;
    private final Observable<Boolean> featuredViewGroupIsGone;
    private final Observable<String> goalStringForTextView;
    private final Observable<String> locationTextViewText;
    private final Observable<Integer> percentageFundedProgress;
    private final Observable<Boolean> percentageFundedProgressBarIsGone;
    private final Observable<Boolean> playButtonIsGone;
    private final Observable<String> pledgedTextViewText;
    private final Observable<DateTime> projectDisclaimerGoalReachedDateTime;
    private final Observable<Pair<String, DateTime>> projectDisclaimerGoalNotReachedString;
    private final Observable<Boolean> projectDisclaimerTextViewIsGone;
    private final Observable<Integer> projectMetadataViewGroupBackgroundDrawableInt;
    private final Observable<Boolean> projectMetadataViewGroupIsGone;
    private final Observable<String> projectNameTextViewText;
    private final Observable<Project> projectOutput;
    private final Observable<Photo> projectPhoto;
    private final Observable<Boolean> projectSocialImageViewIsGone;
    private final Observable<String> projectSocialImageViewUrl;
    private final Observable<List<User>> projectSocialTextViewFriends;
    private final Observable<Boolean> projectSocialViewGroupIsGone;
    private final Observable<Integer> projectStateViewGroupBackgroundColorInt;
    private final Observable<Boolean> projectStateViewGroupIsGone;
    private final Observable<Void> setCanceledProjectStateView;
    private final Observable<Void> setProjectSocialClickListener;
    private final Observable<DateTime> setSuccessfulProjectStateView;
    private final Observable<Void> setSuspendedProjectStateView;
    private final Observable<DateTime> setUnsuccessfulProjectStateView;
    private final Observable<Project> startProjectSocialActivity;
    private final Observable<Boolean> shouldSetDefaultStatsMargins;
    private final Observable<String> updatesCountTextViewText;
    private final Observable<Boolean> usdConversionTextViewIsGone;
    private final Observable<Pair<String, String>> usdConversionPledgedAndGoalText;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull Pair<Project, String> projectAndCountry) {
      this.projectAndCountry.onNext(projectAndCountry);
    }
    @Override public void projectSocialViewGroupClicked() {
      this.projectSocialViewGroupClicked.onNext(null);
    }
    @Override public @NonNull Observable<String> avatarPhotoUrl() {
      return this.avatarPhotoUrl;
    }
    @Override public @NonNull Observable<Boolean> backingViewGroupIsGone() {
      return this.backingViewGroupIsGone;
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
    @Override public @NonNull Observable<String> featuredTextViewRootCategory() {
      return this.featuredTextViewRootCategory;
    }
    @Override public @NonNull Observable<Boolean> featuredViewGroupIsGone() {
      return this.featuredViewGroupIsGone;
    }
    @Override public @NonNull Observable<String> goalStringForTextView() {
      return this.goalStringForTextView;
    }
    @Override public @NonNull Observable<String> locationTextViewText() {
      return this.locationTextViewText;
    }
    @Override public @NonNull Observable<Integer> percentageFundedProgress() {
      return this.percentageFundedProgress;
    }
    @Override public @NonNull Observable<Boolean> percentageFundedProgressBarIsGone() {
      return this.percentageFundedProgressBarIsGone;
    }
    @Override public @NonNull Observable<Boolean> playButtonIsGone() {
      return this.playButtonIsGone;
    }
    @Override public @NonNull Observable<String> pledgedTextViewText() {
      return this.pledgedTextViewText;
    }
    @Override public @NonNull Observable<DateTime> projectDisclaimerGoalReachedDateTime() {
      return this.projectDisclaimerGoalReachedDateTime;
    }
    @Override public @NonNull Observable<Pair<String, DateTime>> projectDisclaimerGoalNotReachedString() {
      return this.projectDisclaimerGoalNotReachedString;
    }
    @Override public @NonNull Observable<Boolean> projectDisclaimerTextViewIsGone() {
      return this.projectDisclaimerTextViewIsGone;
    }
    @Override public @NonNull Observable<Integer> projectMetadataViewGroupBackgroundDrawableInt() {
      return this.projectMetadataViewGroupBackgroundDrawableInt;
    }
    @Override public @NonNull Observable<Boolean> projectMetadataViewGroupIsGone() {
      return this.projectMetadataViewGroupIsGone;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public @NonNull Observable<Project> projectOutput() {
      return this.projectOutput;
    }
    @Override public @NonNull Observable<Photo> projectPhoto() {
      return this.projectPhoto;
    }
    @Override public @NonNull Observable<Boolean> projectSocialImageViewIsGone() {
      return this.projectSocialImageViewIsGone;
    }
    @Override public @NonNull Observable<String> projectSocialImageViewUrl() {
      return this.projectSocialImageViewUrl;
    }
    @Override public @NonNull Observable<List<User>> projectSocialTextViewFriends() {
      return this.projectSocialTextViewFriends;
    }
    @Override public @NonNull Observable<Boolean> projectSocialViewGroupIsGone() {
      return this.projectSocialViewGroupIsGone;
    }
    @Override public @NonNull Observable<Integer> projectStateViewGroupBackgroundColorInt() {
      return this.projectStateViewGroupBackgroundColorInt;
    }
    @Override public @NonNull Observable<Boolean> projectStateViewGroupIsGone() {
      return this.projectStateViewGroupIsGone;
    }
    @Override public @NonNull Observable<Project> startProjectSocialActivity() {
      return this.startProjectSocialActivity;
    }
    @Override public @NonNull Observable<Void> setCanceledProjectStateView() {
      return this.setCanceledProjectStateView;
    }
    @Override public @NonNull Observable<Void> setProjectSocialClickListener() {
      return this.setProjectSocialClickListener;
    }
    @Override public @NonNull Observable<DateTime> setSuccessfulProjectStateView() {
      return this.setSuccessfulProjectStateView;
    }
    @Override public @NonNull Observable<Void> setSuspendedProjectStateView() {
      return this.setSuspendedProjectStateView;
    }
    @Override public @NonNull Observable<DateTime> setUnsuccessfulProjectStateView() {
      return this.setUnsuccessfulProjectStateView;
    }
    @Override public @NonNull Observable<Boolean> shouldSetDefaultStatsMargins() {
      return this.shouldSetDefaultStatsMargins;
    }
    @Override public @NonNull Observable<String> updatesCountTextViewText() {
      return this.updatesCountTextViewText;
    }
    @Override public @NonNull Observable<Boolean> usdConversionTextViewIsGone() {
      return this.usdConversionTextViewIsGone;
    }
    @Override public @NonNull Observable<Pair<String, String>> usdConversionPledgedAndGoalText() {
      return this.usdConversionPledgedAndGoalText;
    }
  }
}
