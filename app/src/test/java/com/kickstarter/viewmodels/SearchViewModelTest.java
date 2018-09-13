package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.DiscoverEnvelopeFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

public class SearchViewModelTest extends KSRobolectricTestCase {
  private SearchViewModel.ViewModel vm;
  private final TestSubscriber<Project> goToProject = new TestSubscriber<>();
  private final TestSubscriber<RefTag> goToRefTag = new TestSubscriber<>();
  private final TestSubscriber<List<Project>> popularProjects = new TestSubscriber<>();
  private final TestSubscriber<Boolean> popularProjectsPresent = new TestSubscriber<>();
  private final TestSubscriber<List<Project>> searchProjects = new TestSubscriber<>();
  private final TestSubscriber<Boolean> searchProjectsPresent = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new SearchViewModel.ViewModel(environment);

    this.vm.outputs.startProjectActivity().map(p -> p.first).subscribe(this.goToProject);
    this.vm.outputs.startProjectActivity().map(p -> p.second).subscribe(this.goToRefTag);
    this.vm.outputs.popularProjects().subscribe(this.popularProjects);
    this.vm.outputs.searchProjects().subscribe(this.searchProjects);
    this.vm.outputs.popularProjects().map(ps -> ps.size() > 0).subscribe(this.popularProjectsPresent);
    this.vm.outputs.searchProjects().map(ps -> ps.size() > 0).subscribe(this.searchProjectsPresent);
  }

  @Test
  public void testPopularProjectsLoadImmediately() {
    setUpEnvironment(environment());

    this.popularProjectsPresent.assertValues(true);
    this.searchProjectsPresent.assertNoValues();
  }

  @Test
  public void testSearchProjectsWhenEnterSearchTerm() {
    final TestScheduler scheduler = new TestScheduler();
    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .build();

    setUpEnvironment(env);

    // Popular projects emit immediately.
    this.popularProjectsPresent.assertValues(true);
    this.searchProjectsPresent.assertNoValues();
    this.koalaTest.assertValues(KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY);

    // Searching shouldn't emit values immediately
    this.vm.inputs.search("hello");
    this.searchProjectsPresent.assertNoValues();
    this.koalaTest.assertValues(KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY);

    // Waiting a small amount time shouldn't emit values
    scheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS);
    this.searchProjectsPresent.assertNoValues();
    this.koalaTest.assertValues(KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY);

    // Waiting the rest of the time makes the search happen
    scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
    this.searchProjectsPresent.assertValues(false, true);
    this.koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY
    );

    // Typing more search terms doesn't emit more values
    this.vm.inputs.search("hello world!");
    this.searchProjectsPresent.assertValues(false, true);
    this.koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY
    );

    // Waiting enough time emits search results
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    this.searchProjectsPresent.assertValues(false, true, false, true);
    this.koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY
    );

    // Clearing search terms brings back popular projects.
    this.vm.inputs.search("");
    this.searchProjectsPresent.assertValues(false, true, false, true, false);
    this.popularProjectsPresent.assertValues(true, false, true);
    this.koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY,
      KoalaEvent.CLEARED_SEARCH_TERM);
  }

  @Test
  public void testSearchPagination() {
    final TestScheduler scheduler = new TestScheduler();
    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .build();

    setUpEnvironment(env);

    this.searchProjectsPresent.assertNoValues();
    this.koalaTest.assertValues(KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY);

    this.vm.inputs.search("cats");

    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);

    this.searchProjectsPresent.assertValues(false, true);
    this.koalaTest.assertValues(
      KoalaEvent.VIEWED_SEARCH, KoalaEvent.DISCOVER_SEARCH_LEGACY,
      KoalaEvent.LOADED_SEARCH_RESULTS, KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY
    );

    this.vm.inputs.nextPage();

    this.searchProjectsPresent.assertValues(false, true);
    this.koalaTest.assertValues(
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
      @Override public @NonNull
      Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    setUpEnvironment(env);

    this.vm.inputs.search("cat");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    this.vm.inputs.projectClicked(projects.get(0));

    this.goToRefTag.assertValues(RefTag.searchFeatured());
    this.goToProject.assertValues(projects.get(0));
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
      @Override public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    setUpEnvironment(env);

    // populate search and overcome debounce
    this.vm.inputs.search("cat");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    this.vm.inputs.projectClicked(projects.get(1));

    this.goToRefTag.assertValues(RefTag.search());
    this.goToProject.assertValues(projects.get(1));
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
      @Override public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    setUpEnvironment(env);

    // populate search and overcome debounce
    this.vm.inputs.search("");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    this.vm.inputs.projectClicked(projects.get(0));

    this.goToRefTag.assertValues(RefTag.searchPopularFeatured());
    this.goToProject.assertValues(projects.get(0));
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
      @Override public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    setUpEnvironment(env);

    // populate search and overcome debounce
    this.vm.inputs.search("");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    this.vm.inputs.projectClicked(projects.get(2));

    this.goToRefTag.assertValues(RefTag.searchPopular());
    this.goToProject.assertValues(projects.get(2));
  }

  @Test
  public void testNoResults() {
    final TestScheduler scheduler = new TestScheduler();

    final List<Project> projects = Arrays.asList(
    );

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(projects));
      }
    };

    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .apiClient(apiClient)
      .build();

    setUpEnvironment(env);

    // populate search and overcome debounce
    this.vm.inputs.search("__");
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);

    this.searchProjects.assertValueCount(2);
  }
}
