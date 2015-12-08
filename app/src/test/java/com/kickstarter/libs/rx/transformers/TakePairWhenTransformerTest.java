package com.kickstarter.libs.rx.transformers;

import android.util.Pair;

import com.kickstarter.BuildConfig;
import com.kickstarter.KSRobolectricGradleTestRunner;
import com.kickstarter.KSRobolectricTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public class TakePairWhenTransformerTest extends KSRobolectricTestCase {

  @Test
  public void testPairTakeWhen_sourceEmitsFirst() {

    final PublishSubject<Integer> source = PublishSubject.create();
    final PublishSubject<String> sample = PublishSubject.create();
    final Observable<Pair<Integer, String>> pair = source.compose(Transformers.takePairWhen(sample));

    final TestSubscriber<Pair<Integer, String>> resultTest = TestSubscriber.create();
    pair.subscribe(resultTest);

    source.onNext(1);
    resultTest.assertNoValues();

    source.onNext(2);
    resultTest.assertNoValues();

    sample.onNext("a");
    resultTest.assertValues(Pair.create(2, "a"));

    sample.onNext("b");
    resultTest.assertValues(Pair.create(2, "a"), Pair.create(2, "b"));

    source.onNext(3);
    resultTest.assertValues(Pair.create(2, "a"), Pair.create(2, "b"));

    sample.onNext("c");
    resultTest.assertValues(Pair.create(2, "a"), Pair.create(2, "b"), Pair.create(3, "c"));
  }

  @Test
  public void testPairTakeWhen_sourceEmitsSecond() {

    final PublishSubject<Integer> source = PublishSubject.create();
    final PublishSubject<String> sample = PublishSubject.create();
    final Observable<Pair<Integer, String>> pair = source.compose(Transformers.takePairWhen(sample));

    final TestSubscriber<Pair<Integer, String>> resultTest = TestSubscriber.create();
    pair.subscribe(resultTest);

    sample.onNext("a");
    resultTest.assertNoValues();

    sample.onNext("b");
    resultTest.assertNoValues();

    source.onNext(1);
    resultTest.assertNoValues();

    sample.onNext("c");
    resultTest.assertValues(Pair.create(1, "c"));

    source.onNext(2);
    resultTest.assertValues(Pair.create(1, "c"));

    sample.onNext("d");
    resultTest.assertValues(Pair.create(1, "c"), Pair.create(2, "d"));
  }
}
