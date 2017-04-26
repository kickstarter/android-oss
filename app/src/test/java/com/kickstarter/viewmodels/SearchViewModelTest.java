package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

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
}
