package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.IntegerUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.ProfileActivity;
import com.kickstarter.ui.adapters.ProfileAdapter;
import com.kickstarter.ui.viewholders.EmptyProfileViewHolder;
import com.kickstarter.ui.viewholders.ProfileCardViewHolder;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface ProfileViewModel {

  interface Inputs {
    /** Call when the Explore Projects button in the empty state has been clicked. */
    void exploreProjectsButtonClicked();

    /** Call when the messages button has been clicked. */
    void messsagesButtonClicked();

    /** Call when the next page has been invoked. */
    void nextPage();

    /** Call when a project card has been clicked. */
    void projectCardClicked(final @NonNull Project project);
  }

  interface Outputs {
    /** Emits the user avatar image to be displayed. */
    Observable<String> avatarImageViewUrl();

    /** Emits when the backed projects count should be hidden. */
    Observable<Boolean> backedCountTextViewHidden();

    /** Emits the backed projects count to be displayed. */
    Observable<String> backedCountTextViewText();

    /** Emits when the backed projects text view should be hidden. */
    Observable<Boolean> backedTextViewHidden();

    /** Emits when the created projects count should be hidden. */
    Observable<Boolean> createdCountTextViewHidden();

    /** Emits the created projects count to be displayed. */
    Observable<String> createdCountTextViewText();

    /** Emits when the created projects text view should be hidden. */
    Observable<Boolean> createdTextViewHidden();

    /** Emits when the divider view should be hidden. */
    Observable<Boolean> dividerViewHidden();

    /** Emits a list of projects to display in the profile. */
    Observable<List<Project>> projects();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.MessageThreadsActivity}. */
    Observable<Void> startMessageThreadsActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Project> startProjectActivity();

    /** Emits when we should resume the {@link com.kickstarter.ui.activities.DiscoveryActivity}. */
    Observable<Void> resumeDiscoveryActivity();

    /** Emits the user name to be displayed. */
    Observable<String> userNameTextViewText();
  }

  final class ViewModel extends ActivityViewModel<ProfileActivity> implements ProfileAdapter.Delegate, Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<User> freshUser = this.client.fetchCurrentUser()
        .retry(2)
        .compose(Transformers.neverError());
      freshUser.subscribe(this.currentUser::refresh);

      final DiscoveryParams params = DiscoveryParams.builder()
        .backed(1)
        .perPage(18)
        .sort(DiscoveryParams.Sort.ENDING_SOON)
        .build();

      final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
        ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
          .nextPage(this.nextPage)
          .envelopeToListOfData(DiscoverEnvelope::projects)
          .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
          .loadWithParams(__ -> this.client.fetchProjects(params))
          .loadWithPaginationPath(this.client::fetchProjects)
          .build();

      final Observable<User> loggedInUser = this.currentUser.loggedInUser();

      this.avatarImageViewUrl = loggedInUser.map(u -> u.avatar().medium());

      this.backedCountTextViewHidden = loggedInUser
        .map(u -> IntegerUtils.isZero(u.backedProjectsCount()));
      this.backedTextViewHidden = this.backedCountTextViewHidden;

      this.backedCountTextViewText = loggedInUser
        .map(User::backedProjectsCount)
        .filter(IntegerUtils::isNonZero)
        .map(NumberUtils::format);

      this.createdCountTextViewHidden = loggedInUser
        .map(u -> IntegerUtils.isZero(u.createdProjectsCount()));
      this.createdTextViewHidden = this.createdCountTextViewHidden;

      this.createdCountTextViewText = loggedInUser
        .map(User::createdProjectsCount)
        .filter(IntegerUtils::isNonZero)
        .map(NumberUtils::format);

      this.dividerViewHidden = Observable.combineLatest(
        this.backedTextViewHidden,
        this.createdTextViewHidden,
        Pair::create
      )
        .map(p -> p.first || p.second);

      this.projects = paginator.paginatedData();
      this.resumeDiscoveryActivity = this.exploreProjectsButtonClicked;
      this.startProjectActivity = this.projectCardClicked;
      this.startMessageThreadsActivity = this.messsagesButtonClicked;
      this.userNameTextViewText = loggedInUser.map(User::name);

      this.koala.trackProfileView();
    }

    private final PublishSubject<Void> exploreProjectsButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> messsagesButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Project> projectCardClicked = PublishSubject.create();

    private final Observable<String> avatarImageViewUrl;
    private final Observable<Boolean> backedCountTextViewHidden;
    private final Observable<String> backedCountTextViewText;
    private final Observable<Boolean> backedTextViewHidden;
    private final Observable<Boolean> createdCountTextViewHidden;
    private final Observable<String> createdCountTextViewText;
    private final Observable<Boolean> createdTextViewHidden;
    private final Observable<Boolean> dividerViewHidden;
    private final Observable<List<Project>> projects;
    private final Observable<Void> resumeDiscoveryActivity;
    private final Observable<Project> startProjectActivity;
    private final Observable<Void> startMessageThreadsActivity;
    private final Observable<String> userNameTextViewText;

    public final ProfileViewModel.Inputs inputs = this;
    public final ProfileViewModel.Outputs outputs = this;

    @Override public void emptyProfileViewHolderExploreProjectsClicked(final @NonNull EmptyProfileViewHolder viewHolder) {
      this.exploreProjectsButtonClicked();
    }
    @Override public void exploreProjectsButtonClicked() {
      this.exploreProjectsButtonClicked.onNext(null);
    }
    @Override public void messsagesButtonClicked() {
      this.messsagesButtonClicked.onNext(null);
    }
    @Override public void nextPage() {
      this.nextPage.onNext(null);
    }
    @Override public void profileCardViewHolderClicked(final @NonNull ProfileCardViewHolder viewHolder, final @NonNull Project project) {
      this.projectCardClicked(project);
    }
    @Override public void projectCardClicked(final @NonNull Project project) {
      this.projectCardClicked.onNext(project);
    }

    @Override public @NonNull Observable<String> avatarImageViewUrl() {
      return this.avatarImageViewUrl;
    }
    @Override public @NonNull Observable<String> backedCountTextViewText() {
      return this.backedCountTextViewText;
    }
    @Override public @NonNull Observable<Boolean> backedCountTextViewHidden() {
      return this.backedCountTextViewHidden;
    }
    @Override public @NonNull Observable<Boolean> backedTextViewHidden() {
      return this.backedTextViewHidden;
    }
    @Override public @NonNull Observable<Boolean> createdCountTextViewHidden() {
      return this.createdCountTextViewHidden;
    }
    @Override public @NonNull Observable<String> createdCountTextViewText() {
      return this.createdCountTextViewText;
    }
    @Override public @NonNull Observable<Boolean> createdTextViewHidden() {
      return this.createdTextViewHidden;
    }
    @Override public @NonNull Observable<Boolean> dividerViewHidden() {
      return this.dividerViewHidden;
    }
    @Override public @NonNull Observable<List<Project>> projects() {
      return this.projects;
    }
    @Override public @NonNull Observable<Void> resumeDiscoveryActivity() {
      return this.resumeDiscoveryActivity;
    }
    @Override public @NonNull Observable<Project> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<Void> startMessageThreadsActivity() {
      return this.startMessageThreadsActivity;
    }
    @Override public @NonNull Observable<String> userNameTextViewText() {
      return this.userNameTextViewText;
    }
  }
}
