package com.kickstarter.viewmodels;

import android.content.Intent;
import android.net.Uri;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.preferences.BooleanPreferenceType;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.DiscoveryDrawerUtils;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.libs.utils.IntegerUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.UrlUtils;
import com.kickstarter.libs.utils.extensions.ConfigExtension;
import com.kickstarter.libs.utils.extensions.StringExtKt;
import com.kickstarter.libs.utils.extensions.UriExt;
import com.kickstarter.models.Category;
import com.kickstarter.models.QualtricsIntercept;
import com.kickstarter.models.QualtricsResult;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.WebClientType;
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter;
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.intentmappers.DiscoveryIntentMapper;
import com.kickstarter.ui.intentmappers.IntentMapper;
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import okhttp3.Response;
import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.errors;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface DiscoveryViewModel {

  interface Inputs extends DiscoveryDrawerAdapter.Delegate, DiscoveryPagerAdapter.Delegate {
    /** Call when a new build is available. */
    void newerBuildIsAvailable(final InternalBuildEnvelope envelope);

    /** Call when you want to open or close the drawer. */
    void openDrawer(final boolean open);

    /** Call when the users confirms they want to take the Qualtrics survey. */
    void qualtricsConfirmClicked();

    /** Call when the users dismisses the Qualtrics prompt. */
    void qualtricsDismissClicked();

    /** Call when you receive a {@link com.qualtrics.digital.TargetingResult} from Qualtrics. */
    void qualtricsResult(final QualtricsResult qualtricsResult);

    /** Call when the user selects a sort tab. */
    void sortClicked(final int sortPosition);
  }

  interface Outputs {
    /** Emits a boolean that determines if the drawer is open or not. */
    Observable<Boolean> drawerIsOpen();

    /** Emits the drawable resource ID of the drawer menu icon.  */
    Observable<Integer> drawerMenuIcon();

    /** Emits a boolean that determines if the sort tab layout should be expanded/collapsed. */
    Observable<Boolean> expandSortTabLayout();

    /** Emits a boolean that determines if the Qualtrics prompt should be visible. */
    Observable<Boolean> qualtricsPromptIsGone();

    /** Emits when params change so that the tool bar can adjust accordingly. */
    Observable<DiscoveryParams> updateToolbarWithParams();

    /** Emits when the params of a particular page should be updated. The page will be responsible for
     * taking those params and creating paginating projects from it. */
    Observable<DiscoveryParams> updateParamsForPage();

    Observable<NavigationDrawerData> navigationDrawerData();

    /** Emits the root categories and position. Position is used to determine the appropriate fragment
     * to pass the categories to. */
    Observable<Pair<List<Category>, Integer>> rootCategoriesAndPosition();

    /** Emits a list of pages that should be cleared of all their content. */
    Observable<List<Integer>> clearPages();

    /** Emits when we should set up {@link com.qualtrics.digital.Qualtrics} with first app session boolean. */
    Observable<Boolean> setUpQualtrics();

    /** Emits when a newer build is available and an alert should be shown. */
    Observable<InternalBuildEnvelope> showBuildCheckAlert();

    /** Start activity feed activity. */
    Observable<Void> showActivityFeed();

    /** Start creator dashboard activity. */
    Observable<Void> showCreatorDashboard();

    /** Start help activity. */
    Observable<Void> showHelp();

    /** Start internal tools activity. */
    Observable<Void> showInternalTools();

    /** Start login tout activity for result. */
    Observable<Void> showLoginTout();

    /** Start {@link com.kickstarter.ui.activities.MessageThreadsActivity}. */
    Observable<Void> showMessages();

    /** Start profile activity. */
    Observable<Void> showProfile();

    /** Start the {@link com.qualtrics.digital.QualtricsSurveyActivity} with the survey url. */
    Observable<String> showQualtricsSurvey();

    /** Start settings activity. */
    Observable<Void> showSettings();

    /** Emits a {@link QualtricsIntercept} whose impression count property should be incremented. */
    Observable<QualtricsIntercept> updateImpressionCount();

    /** Emits the success message from verify endpoint */
    Observable<String> showSuccessMessage();

    /** Emits the error message from verify endpoint */
    Observable<String> showErrorMessage();
  }

  final class ViewModel extends ActivityViewModel<DiscoveryActivity> implements Inputs, Outputs {
    private final ApiClientType apiClient;
    private final BuildCheck buildCheck;
    private final CurrentUserType currentUserType;
    private final CurrentConfigType currentConfigType;
    private final BooleanPreferenceType firstSessionPreference;
    private final WebClientType webClient;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.apiClient = environment.apiClient();
      this.buildCheck = environment.buildCheck();
      this.currentConfigType = environment.currentConfig();
      this.currentUserType = environment.currentUser();
      this.firstSessionPreference = environment.firstSessionPreference();
      this.webClient = environment.webClient();

      this.buildCheck.bind(this, this.webClient);

      this.showActivityFeed = this.activityFeedClick;
      this.showBuildCheckAlert = this.newerBuildIsAvailable;
      this.showCreatorDashboard = this.creatorDashboardClick;
      this.showHelp = this.loggedOutSettingsClick;
      this.showInternalTools = this.internalToolsClick;
      this.showLoginTout = this.loggedOutLoginToutClick;
      this.showMessages = this.messagesClick;
      this.showProfile = this.profileClick;
      this.showSettings = this.settingsClick;

      final Observable<User> currentUser = this.currentUserType.observable();

      final Observable<User> changedUser = currentUser
        .distinctUntilChanged();

      changedUser
        .compose(bindToLifecycle())
        .subscribe(updatedUser ->
        this.apiClient.config()
          .compose(Transformers.neverError())
          .subscribe(this.currentConfigType::config));

      // Seed params when we are freshly launching the app with no data.
      final Observable<DiscoveryParams> paramsFromInitialIntent = intent()
        .take(1)
        .map(Intent::getAction)
        .filter(Intent.ACTION_MAIN::equals)
        .compose(combineLatestPair(changedUser))
        .map(intentAndUser -> DiscoveryParams.getDefaultParams(intentAndUser.second))
        .share();

      final Observable<Config> currentConfig = this.currentConfigType.observable()
              .distinctUntilChanged();

      final Observable<Uri> uriFromVerification = intent()
        .map(Intent::getData)
        .ofType(Uri.class)
        .compose(combineLatestPair(currentConfig))
        .filter(it -> ConfigExtension.isFeatureFlagEnabled(it.second, ConfigExtension.EMAIL_VERIFICATION_FLOW))
        .map(it -> it.first)
        .filter(KSUri::isVerificationEmailUrl);

      final Observable<Notification<EmailVerificationEnvelope>> verification = uriFromVerification
              .map(UriExt::getTokenFromQueryParams)
              .switchMap(this.apiClient::verifyEmail)
              .materialize()
              .share()
              .distinctUntilChanged();

      verification
              .compose(values())
              .map(EmailVerificationEnvelope::message)
              .compose(bindToLifecycle())
              .subscribe(this.sucessMessage);

      verification
              .compose(errors())
              .map(ErrorEnvelope::fromThrowable)
              .map(ErrorEnvelope::errorMessage)
              .compose(bindToLifecycle())
              .subscribe(this.messageError);

      final Observable<DiscoveryParams> paramsFromIntent = intent()
        .flatMap(i -> DiscoveryIntentMapper.params(i, this.apiClient));

      final Observable<DiscoveryParams> drawerParamsClicked = this.childFilterRowClick
        .mergeWith(this.topFilterRowClick)
        .map(NavigationDrawerData.Section.Row::params);

      // Merge various param data sources.
      final Observable<DiscoveryParams> params = Observable.merge(
        paramsFromInitialIntent,
        paramsFromIntent,
        drawerParamsClicked
      );

      final Observable<Integer> pagerSelectedPage = this.pagerSetPrimaryPage.distinctUntilChanged();

      // Combine params with the selected sort position.
      final Observable<DiscoveryParams> paramsWithSort = Observable.combineLatest(
        params,
        pagerSelectedPage.map(DiscoveryUtils::sortFromPosition),
        (p, s) -> p.toBuilder().sort(s).build()
      );

      paramsWithSort
        .compose(bindToLifecycle())
        .subscribe(this.updateParamsForPage);

      params
        .compose(takePairWhen(this.sortClicked.map(DiscoveryUtils::sortFromPosition)))
        .map(paramsAndSort -> paramsAndSort.first.toBuilder().sort(paramsAndSort.second).build())
        .compose(bindToLifecycle())
        .subscribe(this.lake::trackExploreSortClicked);

      paramsWithSort
        .compose(takeWhen(drawerParamsClicked))
        .compose(bindToLifecycle())
        .subscribe(this.lake::trackFilterClicked);

      final Observable<List<Category>> categories = this.apiClient.fetchCategories()
        .compose(neverError())
        .flatMap(Observable::from)
        .toSortedList()
        .share();

      // Combine root categories with the selected sort position.
      Observable.combineLatest(
        categories
          .flatMap(Observable::from)
          .filter(Category::isRoot)
          .toList(),
        pagerSelectedPage,
        Pair::create
      )
        .compose(bindToLifecycle())
        .subscribe(this.rootCategoriesAndPosition);

      final Observable<Category> drawerClickedParentCategory = this.parentFilterRowClick
        .map(NavigationDrawerData.Section.Row::params)
        .map(DiscoveryParams::category);

      final Observable<Category> expandedCategory = Observable.merge(
        this.topFilterRowClick.map(__ -> (Category) null),
        drawerClickedParentCategory
      )
        .scan(null, (previous, next) -> {
          if (previous != null && next != null && previous.equals(next)) {
            return null;
          }
          return next;
        });

      // Accumulate a list of pages to clear when the params or user changes,
      // to avoid displaying old data.
      pagerSelectedPage
        .compose(takeWhen(params))
        .compose(combineLatestPair(changedUser))
        .map(pageAndUser -> pageAndUser.first)
        .flatMap(currentPage -> Observable.from(DiscoveryParams.Sort.defaultSorts)
          .map(DiscoveryUtils::positionFromSort)
          .filter(sortPosition -> !sortPosition.equals(currentPage))
          .toList()
        )
        .compose(bindToLifecycle())
        .subscribe(this.clearPages);

      params.distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.updateToolbarWithParams);

      this.updateParamsForPage.map(__ -> true)
        .compose(bindToLifecycle())
        .subscribe(this.expandSortTabLayout);

      Observable.combineLatest(
        categories,
        params,
        expandedCategory,
        currentUser,
        DiscoveryDrawerUtils::deriveNavigationDrawerData
      )
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.navigationDrawerData);

      drawerParamsClicked
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackDiscoveryFilterSelected);

      final List<Observable<Boolean>> drawerOpenObservables = Arrays.asList(
        this.openDrawer,
        this.childFilterRowClick.map(__ -> false),
        this.topFilterRowClick.map(__ -> false),
        this.internalToolsClick.map(__ -> false),
        this.loggedOutLoginToutClick.map(__ -> false),
        this.loggedOutSettingsClick.map(__ -> false),
        this.activityFeedClick.map(__ -> false),
        this.messagesClick.map(__ -> false),
        this.creatorDashboardClick.map(__ -> false),
        this.profileClick.map(__ -> false),
        this.settingsClick.map(__ -> false)
      );

      Observable.merge(drawerOpenObservables)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.drawerIsOpen);

      final Observable<Boolean> drawerOpened = this.openDrawer
        .filter(BooleanUtils::isTrue);

      drawerOpened
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackDiscoveryFilters());

      paramsWithSort
        .compose(takeWhen(drawerOpened))
        .compose(bindToLifecycle())
        .subscribe(this.lake::trackHamburgerMenuClicked);

      intent()
        .filter(IntentMapper::appBannerIsSet)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackOpenedAppBanner());

      currentUser
        .map(this::currentDrawerMenuIcon)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.drawerMenuIcon::onNext);

      Observable.just(this.firstSessionPreference)
        .map(pref -> {
          if (pref.isSet()) {
            pref.set(false);
          } else {
            pref.set(true);
          }
          return pref.get();
        })
        .compose(bindToLifecycle())
        .subscribe(this.setUpQualtrics::onNext);

      this.qualtricsResult
        .map(QualtricsResult::resultPassed)
        .map(BooleanUtils::negate)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.qualtricsPromptIsGone);

      Observable.merge(this.qualtricsConfirmClicked, this.qualtricsDismissClicked)
        .map(__ -> true)
        .compose(bindToLifecycle())
        .subscribe(this.qualtricsPromptIsGone);

      final Observable<QualtricsResult> passedQualtricsResult = this.qualtricsResult
        .filter(QualtricsResult::resultPassed)
        .distinctUntilChanged();

      passedQualtricsResult
        .map(__ -> QualtricsIntercept.NATIVE_APP_FEEDBACK)
        .compose(bindToLifecycle())
        .subscribe(this.updateImpressionCount);

      passedQualtricsResult
        .map(result -> {
          result.recordImpression();
          return result;
        })
        .compose(combineLatestPair(currentUser))
        .compose(takeWhen(this.qualtricsConfirmClicked))
        .map(resultAndUser -> {
          final QualtricsResult qualtricsResult = resultAndUser.first;
          qualtricsResult.recordClick();
          final User user = resultAndUser.second;
          return Pair.create(qualtricsResult.surveyUrl(), user);
        })
        .filter(surveyAndUser -> StringExtKt.isPresent(surveyAndUser.first))
        .map(surveyAndUser -> {
          final String surveyUrl = surveyAndUser.first;
          final User user = surveyAndUser.second;
          final boolean userLoggedIn = user != null;
          String url = UrlUtils.INSTANCE.appendQueryParameter(surveyUrl, "logged_in", Boolean.toString(userLoggedIn));
          if (userLoggedIn) {
            url = UrlUtils.INSTANCE.appendQueryParameter(url, "user_uid", Long.toString(user.id()));
          }
          return url;
        })
        .compose(bindToLifecycle())
        .subscribe(this.showQualtricsSurvey);
    }

    private Boolean isSameResponse(final @NonNull Response first, final @NonNull Response second) {
      return first.code() == second.code() && first.message() == second.message();
    }

    private int currentDrawerMenuIcon(final @Nullable User user) {
      if (ObjectUtils.isNull(user)) {
        return R.drawable.ic_menu;
      }

      final int erroredBackingsCount = IntegerUtils.intValueOrZero(user.erroredBackingsCount());
      final int unreadMessagesCount = IntegerUtils.intValueOrZero(user.unreadMessagesCount());
      final int unseenActivityCount = IntegerUtils.intValueOrZero(user.unseenActivityCount());

      if (!IntegerUtils.isZero(erroredBackingsCount)) {
        return R.drawable.ic_menu_error_indicator;
      } else if (!(IntegerUtils.isZero(unreadMessagesCount + unseenActivityCount + erroredBackingsCount))) {
        return R.drawable.ic_menu_indicator;
      } else {
        return R.drawable.ic_menu;
      }
    }

    private final PublishSubject<Void> activityFeedClick = PublishSubject.create();
    private final PublishSubject<NavigationDrawerData.Section.Row> childFilterRowClick = PublishSubject.create();
    private final PublishSubject<Void> creatorDashboardClick = PublishSubject.create();
    private final PublishSubject<Void> internalToolsClick = PublishSubject.create();
    private final PublishSubject<Void> loggedOutLoginToutClick = PublishSubject.create();
    private final PublishSubject<Void> loggedOutSettingsClick = PublishSubject.create();
    private final PublishSubject<Void> messagesClick = PublishSubject.create();
    private final PublishSubject<InternalBuildEnvelope> newerBuildIsAvailable = PublishSubject.create();
    private final PublishSubject<Boolean> openDrawer = PublishSubject.create();
    private final PublishSubject<Integer> pagerSetPrimaryPage = PublishSubject.create();
    private final PublishSubject<NavigationDrawerData.Section.Row> parentFilterRowClick = PublishSubject.create();
    private final PublishSubject<Void> profileClick = PublishSubject.create();
    private final PublishSubject<Void> qualtricsConfirmClicked = PublishSubject.create();
    private final PublishSubject<Void> qualtricsDismissClicked = PublishSubject.create();
    private final PublishSubject<QualtricsResult> qualtricsResult = PublishSubject.create();
    private final PublishSubject<Void> settingsClick = PublishSubject.create();
    private final PublishSubject<Integer> sortClicked = PublishSubject.create();
    private final PublishSubject<NavigationDrawerData.Section.Row> topFilterRowClick = PublishSubject.create();

    private final BehaviorSubject<List<Integer>> clearPages = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> drawerIsOpen = BehaviorSubject.create();
    private final BehaviorSubject<Integer> drawerMenuIcon = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> expandSortTabLayout = BehaviorSubject.create();
    private final BehaviorSubject<NavigationDrawerData> navigationDrawerData = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> qualtricsPromptIsGone = BehaviorSubject.create();
    private final BehaviorSubject<Pair<List<Category>, Integer>> rootCategoriesAndPosition = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> setUpQualtrics = BehaviorSubject.create();
    private final Observable<Void> showActivityFeed;
    private final Observable<InternalBuildEnvelope> showBuildCheckAlert;
    private final Observable<Void> showCreatorDashboard;
    private final Observable<Void> showHelp;
    private final Observable<Void> showInternalTools;
    private final Observable<Void> showLoginTout;
    private final Observable<Void> showMessages;
    private final Observable<Void> showProfile;
    private final PublishSubject<String> showQualtricsSurvey = PublishSubject.create();
    private final Observable<Void> showSettings;
    private final PublishSubject<QualtricsIntercept> updateImpressionCount = PublishSubject.create();
    private final BehaviorSubject<DiscoveryParams> updateParamsForPage = BehaviorSubject.create();
    private final BehaviorSubject<DiscoveryParams> updateToolbarWithParams = BehaviorSubject.create();
    private final PublishSubject<String> sucessMessage = PublishSubject.create();
    private final PublishSubject<String> messageError = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void childFilterViewHolderRowClick(final @NonNull ChildFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row) {
      this.childFilterRowClick.onNext(row);
    }
    @Override public void discoveryPagerAdapterSetPrimaryPage(final @NonNull DiscoveryPagerAdapter adapter, final int position) {
      this.pagerSetPrimaryPage.onNext(position);
    }
    @Override public void loggedInViewHolderActivityClick(final @NonNull LoggedInViewHolder viewHolder) {
      this.activityFeedClick.onNext(null);
    }
    @Override public void loggedInViewHolderDashboardClick(final @NonNull LoggedInViewHolder viewHolder) {
      this.creatorDashboardClick.onNext(null);
    }
    @Override public void loggedInViewHolderInternalToolsClick(final @NonNull LoggedInViewHolder viewHolder) {
      this.internalToolsClick.onNext(null);
    }
    @Override public void loggedInViewHolderMessagesClick(final @NonNull LoggedInViewHolder viewHolder) {
      this.messagesClick.onNext(null);
    }
    @Override public void loggedInViewHolderProfileClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user) {
      this.profileClick.onNext(null);
    }
    @Override public void loggedInViewHolderSettingsClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user) {
      this.settingsClick.onNext(null);
    }
    @Override public void loggedOutViewHolderActivityClick(final @NonNull LoggedOutViewHolder viewHolder) {
      this.activityFeedClick.onNext(null);
    }
    @Override public void loggedOutViewHolderInternalToolsClick(final @NonNull LoggedOutViewHolder viewHolder) {
      this.internalToolsClick.onNext(null);
    }
    @Override public void loggedOutViewHolderLoginToutClick(final @NonNull LoggedOutViewHolder viewHolder) {
      this.loggedOutLoginToutClick.onNext(null);
    }
    @Override public void loggedOutViewHolderHelpClick(final @NonNull LoggedOutViewHolder viewHolder) {
      this.loggedOutSettingsClick.onNext(null);
    }
    @Override public void newerBuildIsAvailable(final @NonNull InternalBuildEnvelope envelope) {
      this.newerBuildIsAvailable.onNext(envelope);
    }
    @Override public void openDrawer(final boolean open) {
      this.openDrawer.onNext(open);
    }
    @Override public void parentFilterViewHolderRowClick(final @NonNull ParentFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row) {
      this.parentFilterRowClick.onNext(row);
    }
    @Override public void qualtricsConfirmClicked() {
      this.qualtricsConfirmClicked.onNext(null);
    }
    @Override public void qualtricsDismissClicked() {
      this.qualtricsDismissClicked.onNext(null);
    }
    @Override public void qualtricsResult(final @NonNull QualtricsResult qualtricsResult) {
      this.qualtricsResult.onNext(qualtricsResult);
    }
    @Override public void sortClicked(final @NonNull int position) {
      this.sortClicked.onNext(position);
    }
    @Override public void topFilterViewHolderRowClick(final @NonNull TopFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row) {
      this.topFilterRowClick.onNext(row);
    }

    @Override public @NonNull Observable<List<Integer>> clearPages() {
      return this.clearPages;
    }
    @Override public @NonNull Observable<Boolean> drawerIsOpen() {
      return this.drawerIsOpen;
    }
    @Override public @NonNull Observable<Integer> drawerMenuIcon() {
      return this.drawerMenuIcon;
    }
    @Override public @NonNull Observable<Boolean> expandSortTabLayout() {
      return this.expandSortTabLayout;
    }
    @Override public @NonNull Observable<NavigationDrawerData> navigationDrawerData() {
      return this.navigationDrawerData;
    }
    @Override public @NonNull Observable<Boolean> qualtricsPromptIsGone() {
      return this.qualtricsPromptIsGone;
    }
    @Override public @NonNull Observable<Pair<List<Category>, Integer>> rootCategoriesAndPosition() {
      return this.rootCategoriesAndPosition;
    }
    @Override public @NonNull Observable<Boolean> setUpQualtrics() {
      return this.setUpQualtrics;
    }
    @Override public @NonNull Observable<Void> showActivityFeed() {
      return this.showActivityFeed;
    }
    @Override public @NonNull Observable<InternalBuildEnvelope> showBuildCheckAlert() {
      return this.showBuildCheckAlert;
    }
    @Override public @NonNull Observable<Void> showCreatorDashboard() {
      return this.showCreatorDashboard;
    }
    @Override public @NonNull Observable<Void> showHelp() {
      return this.showHelp;
    }
    @Override public @NonNull Observable<Void> showInternalTools() {
      return this.showInternalTools;
    }
    @Override public @NonNull Observable<Void> showLoginTout() {
      return this.showLoginTout;
    }
    @Override public @NonNull Observable<Void> showMessages() {
      return this.showMessages;
    }
    @Override public @NonNull Observable<Void> showProfile() {
      return this.showProfile;
    }
    @Override public @NonNull Observable<String> showQualtricsSurvey() {
      return this.showQualtricsSurvey;
    }
    @Override public @NonNull Observable<Void> showSettings() {
      return this.showSettings;
    }
    @Override public @NonNull Observable<QualtricsIntercept> updateImpressionCount() {
      return this.updateImpressionCount;
    }
    @Override public @NonNull Observable<DiscoveryParams> updateParamsForPage() {
      return this.updateParamsForPage;
    }
    @Override public @NonNull Observable<DiscoveryParams> updateToolbarWithParams() {
      return this.updateToolbarWithParams;
    }
    @Override
    public Observable<String> showSuccessMessage() {
      return this.sucessMessage;
    }
    @Override
    public Observable<String> showErrorMessage() {
      return this.messageError;
    }
  }
}
