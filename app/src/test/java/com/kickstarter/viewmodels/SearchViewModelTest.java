package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.mock.factories.DiscoverEnvelopeFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.libs.utils.EventName;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
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
  public void testSearchResultPageViewed () {

    final CurrentUserType currentUser = new MockCurrentUser();
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<DiscoverEnvelope> fetchProjects(final @NonNull DiscoveryParams params) {
        if (params.isSavedProjects()) {
          return Observable.just(DiscoverEnvelopeFactory.discoverEnvelope(new ArrayList<>()));
        } else {
          return super.fetchProjects(params);
        }
      }
    };

    final Environment environment = environment().toBuilder()
            .apiClient(apiClient)
            .currentUser(currentUser)
            .build();

    setUpEnvironment(environment);

    this.vm.search("hello");
    this.lakeTest.assertValues("Search Button Clicked", EventName.CTA_CLICKED.getEventName(), EventName.PAGE_VIEWED.getEventName(), "Search Results Loaded");

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
    this.lakeTest.assertValues("Search Button Clicked", EventName.CTA_CLICKED.getEventName(), "Search Page Viewed");

    // Searching shouldn't emit values immediately
    this.vm.inputs.search("hello");
    this.searchProjectsPresent.assertNoValues();
    this.lakeTest.assertValues("Search Button Clicked", EventName.CTA_CLICKED.getEventName(), "Search Page Viewed");

    // Waiting a small amount time shouldn't emit values
    scheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS);
    this.searchProjectsPresent.assertNoValues();
    this.lakeTest.assertValues("Search Button Clicked", EventName.CTA_CLICKED.getEventName(), "Search Page Viewed");

    // Waiting the rest of the time makes the search happen
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    this.searchProjectsPresent.assertValues(false, true);
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    this.lakeTest.assertValues("Search Button Clicked", EventName.CTA_CLICKED.getEventName(), "Search Page Viewed");

    // Typing more search terms doesn't emit more values
    this.vm.inputs.search("hello world!");
    this.searchProjectsPresent.assertValues(false, true);
    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
    this.lakeTest.assertValues("Search Button Clicked", EventName.CTA_CLICKED.getEventName(), "Search Page Viewed", "Search Results Loaded", EventName.PAGE_VIEWED.getEventName());

    // Waiting enough time emits search results
    scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
    this.searchProjectsPresent.assertValues(false, true, false, true);

    // Clearing search terms brings back popular projects.
    this.vm.inputs.search("");

    this.searchProjectsPresent.assertValues(false, true, false, true, false);
    this.popularProjectsPresent.assertValues(true, false, true);
  }

  @Test
  public void testSearchPagination() {
    final TestScheduler scheduler = new TestScheduler();
    final Environment env = environment().toBuilder()
      .scheduler(scheduler)
      .build();

    setUpEnvironment(env);

    this.searchProjectsPresent.assertNoValues();
    this.lakeTest.assertValues("Search Button Clicked",  EventName.CTA_CLICKED.getEventName(),  "Search Page Viewed");

    this.vm.inputs.search("cats");

    scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);

    this.searchProjectsPresent.assertValues(false, true);

    this.vm.inputs.nextPage();
    this.searchProjectsPresent.assertValues(false, true);
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

  @Test
  public void init_whenProjectCardClicked_shouldTrackProjectEvent() {
    final TestScheduler scheduler = new TestScheduler();
    final Environment env = environment().toBuilder()
            .scheduler(scheduler)
            .build();

    setUpEnvironment(env);

    this.vm.inputs.search("hello");
    this.lakeTest.assertValues("Search Button Clicked",  EventName.CTA_CLICKED.getEventName(), "Search Page Viewed");
    this.segmentTrack.assertValues("Search Button Clicked",  EventName.CTA_CLICKED.getEventName(), "Search Page Viewed");

    this.vm.inputs.projectClicked(ProjectFactory.project());
    this.lakeTest.assertValues("Search Button Clicked",  EventName.CTA_CLICKED.getEventName(), "Search Page Viewed", EventName.CARD_CLICKED.getEventName());
    this.segmentTrack.assertValues("Search Button Clicked",  EventName.CTA_CLICKED.getEventName(), "Search Page Viewed", EventName.CARD_CLICKED.getEventName());
  }
}
