package com.kickstarter.libs.rx.transformers;

import com.kickstarter.KSRobolectricTestCase;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

public class NeverErrorTransformerTest extends KSRobolectricTestCase {
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
}
