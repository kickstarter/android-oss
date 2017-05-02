package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.DiscoverEnvelopeFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

public class SearchViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testPopularProjectsLoadImmediately() {
    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(environment());

    final TestSubscriber<Boolean> popularProjectsPresent = new TestSubscriber<>();
    viewModel.outputs.popularProjects().map(ps -> ps.size() > 0).subscribe(popularProjectsPresent);

    final TestSubscriber<Boolean> searchProjectsPresent = new TestSubscriber<>();
    viewModel.outputs.searchProjects().map(ps -> ps.size() > 0).subscribe(searchProjectsPresent);

    popularProjectsPresent.assertValues(true);
    searchProjectsPresent.assertNoValues();
  }

  @Test
  public void testSearchProjectsWhenEnterSearchTerm() {
    final TestScheduler scheduler = new TestScheduler();
    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .build();

    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(env);

    final TestSubscriber<Boolean> searchProjectsPresent = new TestSubscriber<>();
    viewModel.outputs.searchProjects().map(ps -> ps.size() > 0).subscribe(searchProjectsPresent);

    final TestSubscriber<Boolean> popularProjectsPresent = new TestSubscriber<>();
    viewModel.outputs.popularProjects().map(ps -> ps.size() > 0).subscribe(popularProjectsPresent);

    // Popular projects emit immediately.
    popularProjectsPresent.assertValues(true);
    searchProjectsPresent.assertNoValues();
    koalaTest.assertValues(KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY);

    // Searching shouldn't emit values immediately
    viewModel.inputs.search("hello");
    searchProjectsPresent.assertNoValues();
    koalaTest.assertValues(KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY);

    // Waiting a small amount time shouldn't emit values
    scheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS);
    searchProjectsPresent.assertNoValues();
    koalaTest.assertValues(KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY);

    // Waiting the rest of the time makes the search happen
    scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
    searchProjectsPresent.assertValues(false, true);
    koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY
    );

    // Typing more search terms doesn't emit more values
    viewModel.inputs.search("hello world!");
    searchProjectsPresent.assertValues(false, true);
    koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY
    );

    // Waiting enough time emits search results
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    searchProjectsPresent.assertValues(false, true, false, true);
    koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY
    );

    // Clearing search terms brings back popular projects.
    viewModel.inputs.search("");
    searchProjectsPresent.assertValues(false, true, false, true, false);
    popularProjectsPresent.assertValues(true, false, true);
    koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY,
      KoalaEvent.CLEARED_SEARCH_TERM);
  }

  void testSearchPagination() {
    final TestScheduler scheduler = new TestScheduler();
    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .build();

    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(env);

    final TestSubscriber<Boolean> searchProjectsPresent = new TestSubscriber<>();
    viewModel.outputs.searchProjects().map(ps -> ps.size() > 0).subscribe(searchProjectsPresent);

    searchProjectsPresent.assertNoValues();
    koalaTest.assertValues(KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY);

    viewModel.inputs.search("cats");

    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);

    searchProjectsPresent.assertValues(false, true);
    koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY
    );

    viewModel.inputs.nextPage();

    searchProjectsPresent.assertValues(false, true, true);
    koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY,
      KoalaEvent.LOADED_MORE_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LOAD_MORE_LEGACY
    );
  }

  @Test
  public void testFeaturedSearchRefTags() {
    final TestScheduler scheduler = new TestScheduler();

    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject(),
      ProjectFactory.almostCompletedProject(),
      ProjectFactory.backedProject()
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public
      @NonNull
      Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(env);

    final TestSubscriber<Project> goToProject = new TestSubscriber<>();
    final TestSubscriber<RefTag> goToRefTag = new TestSubscriber<>();
    viewModel.outputs.goToProject().map(p -> p.first).subscribe(goToProject);
    viewModel.outputs.goToProject().map(p -> p.second).subscribe(goToRefTag);

    viewModel.inputs.search("cat");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    viewModel.inputs.projectClicked(projects.get(0));

    goToRefTag.assertValues(RefTag.searchFeatured());
    goToProject.assertValues(projects.get(0));
  }

  @Test
  public void testSearchRefTags() {
    final TestScheduler scheduler = new TestScheduler();

    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject(),
      ProjectFactory.almostCompletedProject(),
      ProjectFactory.backedProject()
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public
      @NonNull
      Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(env);

    final TestSubscriber<Project> goToProject = new TestSubscriber<>();
    final TestSubscriber<RefTag> goToRefTag = new TestSubscriber<>();
    viewModel.outputs.goToProject().map(p -> p.first).subscribe(goToProject);
    viewModel.outputs.goToProject().map(p -> p.second).subscribe(goToRefTag);

    // populate search and overcome debounce
    viewModel.inputs.search("cat");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    viewModel.inputs.projectClicked(projects.get(1));

    goToRefTag.assertValues(RefTag.search());
    goToProject.assertValues(projects.get(1));
  }

  @Test
  public void testFeaturedPopularRefTags() {
    final TestScheduler scheduler = new TestScheduler();

    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject(),
      ProjectFactory.almostCompletedProject(),
      ProjectFactory.backedProject()
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public
      @NonNull
      Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(env);

    final TestSubscriber<Project> goToProject = new TestSubscriber<>();
    final TestSubscriber<RefTag> goToRefTag = new TestSubscriber<>();
    viewModel.outputs.goToProject().map(p -> p.first).subscribe(goToProject);
    viewModel.outputs.goToProject().map(p -> p.second).subscribe(goToRefTag);

    // populate search and overcome debounce
    viewModel.inputs.search("");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    viewModel.inputs.projectClicked(projects.get(0));

    goToRefTag.assertValues(RefTag.searchPopularFeatured());
    goToProject.assertValues(projects.get(0));
  }

  @Test
  public void testPopularRefTags() {
    final TestScheduler scheduler = new TestScheduler();

    final List<Project> projects = Arrays.asList(
      ProjectFactory.allTheWayProject(),
      ProjectFactory.almostCompletedProject(),
      ProjectFactory.backedProject()
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public
      @NonNull
      Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(env);

    final TestSubscriber<Project> goToProject = new TestSubscriber<>();
    final TestSubscriber<RefTag> goToRefTag = new TestSubscriber<>();
    viewModel.outputs.goToProject().map(p -> p.first).subscribe(goToProject);
    viewModel.outputs.goToProject().map(p -> p.second).subscribe(goToRefTag);

    // populate search and overcome debounce
    viewModel.inputs.search("");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    viewModel.inputs.projectClicked(projects.get(2));

    goToRefTag.assertValues(RefTag.searchPopular());
    goToProject.assertValues(projects.get(2));
  }

  @Test
  public void testNoResuts() {
    final TestScheduler scheduler = new TestScheduler();

    final List<Project> projects = Arrays.asList(
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public
      @NonNull
      Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    final SearchViewModel.ViewModel viewModel = new SearchViewModel.ViewModel(env);

    final TestSubscriber<List<Project>> projectList = new TestSubscriber<>();
    viewModel.outputs.searchProjects().subscribe(projectList);

    // populate search and overcome debounce
    viewModel.inputs.search("__");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);

    projectList.assertValueCount(2);

  }
}
