package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.SearchActivity;
import com.kickstarter.ui.adapters.SearchAdapter;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
public interface SearchViewModel {

  interface Inputs {
    /** Call on text change in search box **/
    void search(final @NonNull String s);

    /** Load more data **/
    void nextPage();
  }

  interface Outputs {
    /** Emits list of popular projects **/
    Observable<List<Project>> popularProjects();

    /** Emits list of projects matching critera **/
    Observable<List<Project>> searchProjects();
  }

  final class ViewModel extends ActivityViewModel<SearchActivity> implements SearchAdapter.Delegate, Inputs, Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final ApiClientType apiClient = environment.apiClient();
      final Scheduler scheduler = environment.scheduler();

      final Observable<DiscoveryParams> searchParams = search
        .filter(StringUtils::isPresent)
        .debounce(300, TimeUnit.MILLISECONDS, scheduler)
        .map(s -> DiscoveryParams.builder().term(s).build());

      final Observable<DiscoveryParams> popularParams = search
        .filter(StringUtils::isEmpty)
        .map(__ -> defaultParams)
        .startWith(defaultParams);

      final Observable<DiscoveryParams> params = Observable.merge(searchParams, popularParams);

      final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
        ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
          .nextPage(nextPage)
          .startOverWith(params)
          .envelopeToListOfData(DiscoverEnvelope::projects)
          .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
          .clearWhenStartingOver(true)
          .concater(ListUtils::concatDistinct)
          .loadWithParams(apiClient::fetchProjects)
          .loadWithPaginationPath(apiClient::fetchProjects)
          .build();

      search
        .filter(StringUtils::isEmpty)
        .compose(bindToLifecycle())
        .subscribe(__ -> searchProjects.onNext(ListUtils.empty()));

      params
        .compose(Transformers.takePairWhen(paginator.paginatedData()))
        .compose(bindToLifecycle())
        .subscribe(paramsAndProjects -> {
          if (paramsAndProjects.first.sort() == defaultSort) {
            popularProjects.onNext(paramsAndProjects.second);
          } else {
            searchProjects.onNext(paramsAndProjects.second);
          }
        });

      // Track us viewing this page
      koala.trackSearchView();

      // Track search results and pagination
      final Observable<Integer> pageCount = paginator.loadingPage();
      final Observable<String> query = params
        .map(DiscoveryParams::term);
      query
        .compose(Transformers.takePairWhen(pageCount))
        .filter(qp -> StringUtils.isPresent(qp.first))
        .compose(bindToLifecycle())
        .subscribe(qp -> koala.trackSearchResults(qp.first, qp.second));
    }

    private final PublishSubject<String> search = PublishSubject.create();
    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<Project> projectCardClicked = PublishSubject.create();

    private final BehaviorSubject<List<Project>> popularProjects = BehaviorSubject.create();
    private final BehaviorSubject<List<Project>> searchProjects = BehaviorSubject.create();

    public final SearchViewModel.Inputs inputs = this;
    public final SearchViewModel.Outputs outputs = this;

    @Override public void search(final @NonNull String s) {
      search.onNext(s);
    }

    @Override public void nextPage() {
      nextPage.onNext(null);
    }

    @Override public Observable<List<Project>> popularProjects() {
      return popularProjects;
    }

    @Override public Observable<List<Project>> searchProjects() {
      return searchProjects;
    }

    private static final DiscoveryParams.Sort defaultSort = DiscoveryParams.Sort.POPULAR;
    private static final DiscoveryParams defaultParams = DiscoveryParams.builder().sort(defaultSort).build();

    @Override
    public void projectSearchResultClick(final ProjectSearchResultViewHolder viewHolder, final Project project) {
      this.projectCardClicked.onNext(project);
    }
  }
}
