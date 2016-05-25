package com.kickstarter.libs.rx.transformers;

import android.os.Looper;

import com.kickstarter.KSRobolectricTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowLooper;

import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

public final class ObserveForUITransformerTest extends KSRobolectricTestCase {
  @Before
  public void setUp() {
    RxAndroidPlugins.getInstance().reset();
    ShadowLooper.pauseMainLooper();
  }

  @After
  public void tearDown() {
    RxAndroidPlugins.getInstance().reset();
    ShadowLooper.unPauseMainLooper();
  }

  @Test
  public void test() {
    final Scheduler scheduler = AndroidSchedulers.from(Looper.getMainLooper());
    final AtomicInteger x = new AtomicInteger();

    Observable.just(1)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(x::set);

    // Main looper is paused, so value should not change.
    assertEquals(0, x.get());

    Observable.just(2)
      .observeOn(Schedulers.immediate())
      .subscribe(x::set);

    // Since the work used the immediate scheduler, it is unaffected by the main looper being paused.
    assertEquals(2, x.get());

    Observable.just(3)
      .compose(observeForUI())
      .subscribe(x::set);

    // The main looper is paused but the code is executing on the main thread, so observeForUI() should schedule the
    // work immediately rather than queueing it up.
    assertEquals(3, x.get());

    // Run the queued work.
    ShadowLooper.runUiThreadTasks();

    // Code observed using `AndroidSchedulers.mainThread()` is now run.
    assertEquals(1, x.get());
  }
}
