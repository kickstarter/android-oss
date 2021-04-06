package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.EventContextValues;
import com.kickstarter.libs.utils.IntegerUtils;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.extensions.StringExt;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.SearchActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;

public interface SearchViewModel {

  interface Inputs {
    /** Call when the next page has been invoked. */
    void nextPage();

    /** Call when a project is tapped in search results. */
    void projectClicked(final @NonNull Project project);

    /** Call when text changes in search box. */
    void search(final @NonNull String s);
  }

  interface Outputs {
    /** Emits a boolean indicating whether projects are being fetched from the API. */
    Observable<Boolean> isFetchingProjects();

    /** Emits list of popular projects. */
    Observable<List<Project>> popularProjects();

    /** Emits list of projects matching criteria. */
    Observable<List<Project>> searchProjects();

    /** Emits a project and ref tag when we should start a project activity. */
    Observable<Pair<Project, RefTag>> startProjectActivity();
  }

  final class ViewModel extends ActivityViewModel<SearchActivity> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final ApiClientType apiClient = environment.apiClient();
      final Scheduler scheduler = environment.scheduler();

      final Observable<DiscoveryParams> searchParams = this.search
        .filter(ObjectUtils::isNotNull)
        .filter(StringExt::isPresent)
        .debounce(300, TimeUnit.MILLISECONDS, scheduler)
        .map(s -> DiscoveryParams.builder().term(s).build());

      final Observable<DiscoveryParams> popularParams = this.search
        .filter(ObjectUtils::isNotNull)
        .filter(StringExt::isTrimmedEmpty)
        .map(__ -> defaultParams)
        .startWith(defaultParams);

      final Observable<DiscoveryParams> params = Observable.merge(searchParams, popularParams);

      final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
        ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
          .nextPage(this.nextPage)
          .startOverWith(params)
          .envelopeToListOfData(DiscoverEnvelope::projects)
          .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
          .clearWhenStartingOver(true)
          .concater(ListUtils::concatDistinct)
          .loadWithParams(apiClient::fetchProjects)
          .loadWithPaginationPath(apiClient::fetchProjects)
          .build();

      paginator.isFetching()
        .compose(bindToLifecycle())
        .subscribe(this.isFetchingProjects);

      this.search
        .filter(ObjectUtils::isNotNull)
        .filter(StringExt:: isTrimmedEmpty)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.searchProjects.onNext(ListUtils.empty()));

      params
        .compose(takePairWhen(paginator.paginatedData()))
        .compose(bindToLifecycle())
        .subscribe(paramsAndProjects -> {
          if (paramsAndProjects.first.sort() == defaultSort) {
            this.popularProjects.onNext(paramsAndProjects.second);
          } else {
            this.searchProjects.onNext(paramsAndProjects.second);
          }
        });

      final Observable<Integer> pageCount = paginator.loadingPage();
      final Observable<String> query = params
        .map(DiscoveryParams::term);

      final Observable<List<Project>> projects = Observable.merge(this.popularProjects, this.searchProjects);

      this.projectClicked
              .compose(bindToLifecycle())
              .subscribe(p ->
                      this.lake.trackProjectCardClicked(
                              p,
                              EventContextValues.ContextPageName.SEARCH.getContextName()));

      this.startProjectActivity = Observable.combineLatest(this.search, projects, Pair::create)
        .compose(takePairWhen(this.projectClicked))
        .map(searchTermAndProjectsAndProjectClicked -> {
          final String searchTerm = searchTermAndProjectsAndProjectClicked.first.first;
          final List<Project> currentProjects = searchTermAndProjectsAndProjectClicked.first.second;
          final Project projectClicked = searchTermAndProjectsAndProjectClicked.second;

          return this.projectAndRefTag(searchTerm, currentProjects, projectClicked);
        });

      params
        .compose(takePairWhen(pageCount))
        .filter(paramsAndPageCount -> paramsAndPageCount.first.sort() != defaultSort && IntegerUtils.intValueOrZero(paramsAndPageCount.second) == 1)
        .map(paramsAndPageCount -> paramsAndPageCount.first)
        .observeOn(Schedulers.io())
        .compose(bindToLifecycle())
        .subscribe(this.lake::trackSearchResultsLoaded);

      this.lake.trackSearchButtonClicked();
      this.lake.trackSearchCTAButtonClicked();
      this.lake.trackSearchPageViewed(defaultParams);
    }

    private static final DiscoveryParams.Sort defaultSort = DiscoveryParams.Sort.POPULAR;
    private static final DiscoveryParams defaultParams = DiscoveryParams.builder().sort(defaultSort).build();

    /**
     * Returns a project and its appropriate ref tag given its location in a list of popular projects or search results.
     *
     * @param searchTerm        The search term entered to determine list of search results.
     * @param projects          The list of popular or search result projects.
     * @param selectedProject   The project selected by the user.
     * @return                  The project and its appropriate ref tag.
     */
    private @NonNull Pair<Project, RefTag> projectAndRefTag(final @NonNull String searchTerm,
      final @NonNull List<Project> projects, final @NonNull Project selectedProject) {

      final boolean isFirstResult = selectedProject == projects.get(0);

      if (searchTerm.length() == 0) {
        return isFirstResult
          ? Pair.create(selectedProject, RefTag.searchPopularFeatured())
          : Pair.create(selectedProject, RefTag.searchPopular());
      } else {
        return isFirstResult
          ? Pair.create(selectedProject, RefTag.searchFeatured())
          : Pair.create(selectedProject, RefTag.search());
      }
    }

    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Project> projectClicked = PublishSubject.create();
    private final PublishSubject<String> search = PublishSubject.create();

    private final BehaviorSubject<Boolean> isFetchingProjects = BehaviorSubject.create();
    private final BehaviorSubject<List<Project>> popularProjects = BehaviorSubject.create();
    private final BehaviorSubject<List<Project>> searchProjects = BehaviorSubject.create();
    private final Observable<Pair<Project, RefTag>> startProjectActivity;

    public final SearchViewModel.Inputs inputs = this;
    public final SearchViewModel.Outputs outputs = this;

    @Override public void nextPage() {
      this.nextPage.onNext(null);
    }
    @Override public void projectClicked(final @NonNull Project project) {
      this.projectClicked.onNext(project);
    }
    @Override public void search(final @NonNull String s) {
      this.search.onNext(s);
    }

    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<Boolean> isFetchingProjects() {
      return this.isFetchingProjects;
    }
    @Override public @NonNull Observable<List<Project>> popularProjects() {
      return this.popularProjects;
    }
    @Override public @NonNull Observable<List<Project>> searchProjects() {
      return this.searchProjects;
    }
  }
}
