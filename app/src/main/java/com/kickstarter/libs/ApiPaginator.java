package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.services.ApiClientType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

/**
 * An object to facilitate loading pages of data from the API.
 *
 * @param <Data> The type of data returned from the array, e.g. `Project`, `Activity`, etc.
 * @param <Envelope> The type of envelope the API returns for a list of data, e.g. `DiscoverEnvelope`.
 * @param <Params> The type of params that {@link ApiClientType} can use to make a request. Many times this can just be `Void`.
 */
public final class ApiPaginator<Data, Envelope, Params> {
  private final @NonNull Observable<Void> nextPage;
  private final @NonNull Observable<Params> startOverWith;
  private final @NonNull Func1<Envelope, List<Data>> envelopeToListOfData;
  private final @NonNull Func1<Params, Observable<Envelope>> loadWithParams;
  private final @NonNull Func1<String, Observable<Envelope>> loadWithPaginationPath;
  private final @NonNull Func1<Envelope, String> envelopeToMoreUrl;
  private final @NonNull Func1<List<Data>, List<Data>> pageTransformation;
  private final boolean clearWhenStartingOver;
  private final @NonNull Func2<List<Data>, List<Data>, List<Data>> concater;

  private final @NonNull PublishSubject<String> _morePath = PublishSubject.create();
  private final @NonNull PublishSubject<Boolean> _isFetching = PublishSubject.create();

  // Outputs
  public @NonNull Observable<List<Data>> paginatedData() {
    return paginatedData;
  }
  private final @NonNull Observable<List<Data>> paginatedData;
  public @NonNull Observable<Boolean> isFetching() {
    return isFetching;
  }
  private final @NonNull Observable<Boolean> isFetching = _isFetching;
  public @NonNull Observable<Integer> loadingPage() {
    return loadingPage;
  }
  private final @NonNull Observable<Integer> loadingPage;

  private ApiPaginator(
    final @NonNull Observable<Void> nextPage,
    final @NonNull Observable<Params> startOverWith,
    final @NonNull Func1<Envelope, List<Data>> envelopeToListOfData,
    final @NonNull Func1<Params, Observable<Envelope>> loadWithParams,
    final @NonNull Func1<String, Observable<Envelope>> loadWithPaginationPath,
    final @NonNull Func1<Envelope, String> envelopeToMoreUrl,
    final @NonNull Func1<List<Data>, List<Data>> pageTransformation,
    final boolean clearWhenStartingOver,
    final @NonNull Func2<List<Data>, List<Data>, List<Data>> concater
  ) {
    this.nextPage = nextPage;
    this.startOverWith = startOverWith;
    this.envelopeToListOfData = envelopeToListOfData;
    this.loadWithParams = loadWithParams;
    this.envelopeToMoreUrl = envelopeToMoreUrl;
    this.pageTransformation = pageTransformation;
    this.loadWithPaginationPath = loadWithPaginationPath;
    this.clearWhenStartingOver = clearWhenStartingOver;
    this.concater = concater;

    this.paginatedData = this.startOverWith.switchMap(this::dataWithPagination);
    this.loadingPage = this.startOverWith.switchMap(__ -> nextPage.scan(1, (accum, ___) -> accum + 1));
  }

  public final static class Builder<Data, Envelope, Params> {
    private Observable<Void> nextPage;
    private Observable<Params> startOverWith;
    private Func1<Envelope, List<Data>> envelopeToListOfData;
    private Func1<Params, Observable<Envelope>> loadWithParams;
    private Func1<String, Observable<Envelope>> loadWithPaginationPath;
    private Func1<Envelope, String> envelopeToMoreUrl;
    private Func1<List<Data>, List<Data>> pageTransformation;
    private boolean clearWhenStartingOver;
    private Func2<List<Data>, List<Data>, List<Data>> concater = ListUtils::concat;

    /**
     * [Required] An observable that emits whenever a new page of data should be loaded.
     */
    public @NonNull Builder<Data, Envelope, Params> nextPage(final @NonNull Observable<Void> nextPage) {
      this.nextPage = nextPage;
      return this;
    }

    /**
     * [Optional] An observable that emits when a fresh first page should be loaded.
     */
    public @NonNull Builder<Data, Envelope, Params> startOverWith(final @NonNull Observable<Params> startOverWith) {
      this.startOverWith = startOverWith;
      return this;
    }

    /**
     * [Required] A function that takes an `Envelope` instance and returns the list of data embedded in it.
     */
    public @NonNull Builder<Data, Envelope, Params> envelopeToListOfData(final @NonNull Func1<Envelope, List<Data>> envelopeToListOfData) {
      this.envelopeToListOfData = envelopeToListOfData;
      return this;
    }

    /**
     * [Required] A function to extract the more URL from an API response envelope.
     */
    public @NonNull Builder<Data, Envelope, Params> envelopeToMoreUrl(final @NonNull Func1<Envelope, String> envelopeToMoreUrl) {
      this.envelopeToMoreUrl = envelopeToMoreUrl;
      return this;
    }

    /**
     * [Required] A function that makes an API request with a pagination URL.
     */
    public @NonNull Builder<Data, Envelope, Params> loadWithPaginationPath(final @NonNull Func1<String, Observable<Envelope>> loadWithPaginationPath) {
      this.loadWithPaginationPath = loadWithPaginationPath;
      return this;
    }

    /**
     * [Required] A function that takes a `Params` and performs the associated network request
     * and returns an `Observable<Envelope>`
     */
    public @NonNull Builder<Data, Envelope, Params> loadWithParams(final @NonNull Func1<Params, Observable<Envelope>> loadWithParams) {
      this.loadWithParams = loadWithParams;
      return this;
    }

    /**
     * [Optional] Function to transform every page of data that is loaded.
     */
    public @NonNull Builder<Data, Envelope, Params> pageTransformation(final @NonNull Func1<List<Data>, List<Data>> pageTransformation) {
      this.pageTransformation = pageTransformation;
      return this;
    }

    /**
     * [Optional] Determines if the list of loaded data is cleared when starting over from the first page.
     */
    public @NonNull Builder<Data, Envelope, Params> clearWhenStartingOver(final boolean clearWhenStartingOver) {
      this.clearWhenStartingOver = clearWhenStartingOver;
      return this;
    }

    /**
     * [Optional] Determines how two lists are concatenated together while paginating. A regular `ListUtils::concat` is probably
     * sufficient, but sometimes you may want `ListUtils::concatDistinct`
     */
    public @NonNull Builder<Data, Envelope, Params> concater(final @NonNull Func2<List<Data>, List<Data>, List<Data>> concater) {
      this.concater = concater;
      return this;
    }

    public @NonNull ApiPaginator<Data, Envelope, Params> build() throws RuntimeException {
      // Early error when required field is not set
      if (nextPage == null) {
        throw new RuntimeException("`nextPage` is required");
      }
      if (envelopeToListOfData == null) {
        throw new RuntimeException("`envelopeToListOfData` is required");
      }
      if (loadWithParams == null) {
        throw new RuntimeException("`loadWithParams` is required");
      }
      if (loadWithPaginationPath == null) {
        throw new RuntimeException("`loadWithPaginationPath` is required");
      }
      if (envelopeToMoreUrl == null) {
        throw new RuntimeException("`envelopeToMoreUrl` is required");
      }

      // Default params for optional fields
      if (startOverWith == null) {
        startOverWith = Observable.just(null);
      }
      if (pageTransformation == null) {
        pageTransformation = x -> x;
      }
      if (concater == null) {
        concater = ListUtils::concat;
      }

      return new ApiPaginator<>(nextPage, startOverWith, envelopeToListOfData, loadWithParams,
        loadWithPaginationPath, envelopeToMoreUrl, pageTransformation, clearWhenStartingOver, concater);
    }
  }

  public @NonNull static <Data, Envelope, FirstPageParams> Builder<Data, Envelope, FirstPageParams> builder() {
    return new Builder<>();
  }

  /**
   * Returns an observable that emits the accumulated list of paginated data each time a new page is loaded.
   */
  private @NonNull Observable<List<Data>> dataWithPagination(final @NonNull Params firstPageParams) {
    final Observable<List<Data>> data = paramsAndMoreUrlWithPagination(firstPageParams)
      .concatMap(this::fetchData)
      .takeUntil(List::isEmpty);

    if (clearWhenStartingOver) {
      return data.scan(new ArrayList<>(), concater);
    }
    return data.scan(concater);
  }

  /**
   * Returns an observable that emits the params for the next page of data *or* the more URL for the next page.
   */
  private @NonNull Observable<Pair<Params, String>> paramsAndMoreUrlWithPagination(final @NonNull Params firstPageParams) {

    return _morePath
      .map(path -> new Pair<Params, String>(null, path))
      .compose(Transformers.takeWhen(nextPage))
      .startWith(new Pair<>(firstPageParams, null));
  }

  private @NonNull Observable<List<Data>> fetchData(final @NonNull Pair<Params, String> paginatingData) {

    return (paginatingData.second != null ? loadWithPaginationPath.call(paginatingData.second) : loadWithParams.call(paginatingData.first))
      .retry(2)
      .compose(Transformers.neverError())
      .doOnNext(this::keepMorePath)
      .map(envelopeToListOfData)
      .map(pageTransformation)
      .doOnSubscribe(() -> _isFetching.onNext(true))
      .doAfterTerminate(() -> _isFetching.onNext(false));
  }

  private void keepMorePath(final @NonNull Envelope envelope) {
    try {
      final URL url = new URL(envelopeToMoreUrl.call(envelope));
      _morePath.onNext(pathAndQueryFromURL(url));
    } catch (MalformedURLException ignored) {}
  }

  private @NonNull String pathAndQueryFromURL(final @NonNull URL url) {
    return url.getPath() + "?" + url.getQuery();
  }
}

