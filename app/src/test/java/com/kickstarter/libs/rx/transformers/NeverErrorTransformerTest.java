package com.kickstarter.libs.rx.transformers;

import com.kickstarter.BuildConfig;
import com.kickstarter.KSRobolectricGradleTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
public class NeverErrorTransformerTest {

  @Test
  public void testNeverError_emitsSameValuesAsSource() {

    final Observable<Integer> source = Observable.just(1, 2, 3, 4);
    final Observable<Integer> result = source.compose(Transformers.neverError());

    final TestSubscriber<Integer> resultTest = TestSubscriber.create();
    result.subscribe(resultTest);

    resultTest.assertValues(1, 2, 3, 4);
    resultTest.assertCompleted();
  }

  @Test
  public void testNeverError_emitsSameValuesAndSkipsError() {

    final Observable<Integer> errorsOnLast = Observable.just(1, 2, 3, 4)
      .flatMap(i -> i < 4 ? Observable.just(i) : Observable.error(new RuntimeException()));
    final Observable<Integer> result = errorsOnLast.compose(Transformers.neverError());

    final TestSubscriber<Integer> resultTest = TestSubscriber.create();
    result.subscribe(resultTest);

    resultTest.assertValues(1, 2, 3);
    resultTest.assertCompleted();
    resultTest.assertNoErrors();
  }

  @Test
  public void testNeverError_pipesErrorsToPublishSubject() {

    final RuntimeException exception = new RuntimeException();
    final Observable<Integer> errorsOnLast = Observable.just(1, 2, 3, 4)
      .flatMap(i -> i < 4 ? Observable.just(i) : Observable.error(exception));
    final PublishSubject<Throwable> error = PublishSubject.create();
    final Observable<Integer> result = errorsOnLast.compose(Transformers.pipeErrorsTo(error));

    final TestSubscriber<Throwable> errorTest = TestSubscriber.create();
    error.subscribe(errorTest);
    final TestSubscriber<Integer> resultTest = TestSubscriber.create();
    result.subscribe(resultTest);

    resultTest.assertValues(1, 2, 3);
    resultTest.assertCompleted();
    resultTest.assertNoErrors();

    errorTest.assertValues(exception);
    errorTest.assertNotCompleted();
    errorTest.assertNoErrors();
  }
}
