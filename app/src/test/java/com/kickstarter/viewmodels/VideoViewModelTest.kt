package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.VideoFactory.hlsVideo
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.VideoViewModel.Factory
import com.kickstarter.viewmodels.VideoViewModel.VideoViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class VideoViewModelTest : KSRobolectricTestCase() {

    lateinit var vm: VideoViewModel
    private val disposables = CompositeDisposable()
    val preparePlayerWithUrl = TestSubscriber<String>()
    val preparePlayerWithUrlAndPosition = TestSubscriber<Pair<String, Long>>()

    @After
    fun cleanUp() {
        disposables.clear()
    }

    private fun setUpEnvironment(environment: Environment, intent: Intent) {
        vm = Factory(environment(), intent).create(VideoViewModel::class.java)
        vm.outputs.preparePlayerWithUrl().subscribe { preparePlayerWithUrl.onNext(it) }.addToDisposable(disposables)
        vm.outputs.preparePlayerWithUrlAndPosition().subscribe { preparePlayerWithUrlAndPosition.onNext(it) }.addToDisposable(disposables)
    }
    @Test
    fun testVideoViewModel_EmitsVideoUrl_WhenHls() {

        val project = project().toBuilder().video(hlsVideo()).build()
        // Configure the view model with a project intent.
        val intent = Intent().putExtra(IntentKey.PROJECT, project)
        setUpEnvironment(environment(), intent)

        preparePlayerWithUrl.assertValue(project.video()!!.hls())
        preparePlayerWithUrlAndPosition.assertNoValues()
    }

/*
  @Test
  public void testVideoViewModel_EmitsVideoUrl_WhenNotHls() {
    final VideoViewModel.ViewModel vm = new VideoViewModel.ViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> preparePlayerWithUrl = new TestSubscriber<>();
    vm.outputs.preparePlayerWithUrl().subscribe(preparePlayerWithUrl);

    // Configure the view model with a project intent.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    preparePlayerWithUrl.assertValue(project.video().high());
  }

  @Test
  public void testVideoViewModel_VideoPlayStart() {
    final VideoViewModel.ViewModel vm = new VideoViewModel.ViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> preparePlayerWithUrl = new TestSubscriber<>();
    vm.outputs.preparePlayerWithUrl().subscribe(preparePlayerWithUrl);
    vm.inputs.onVideoStarted(100, 0);

    // Configure the view model with a project intent.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    preparePlayerWithUrl.assertValue(project.video().high());
    this.segmentTrack.assertValues(EventName.VIDEO_PLAYBACK_STARTED.getEventName());
  }

  @Test
  public void testVideoViewModel_VideoPlayCompleted() {
    final VideoViewModel.ViewModel vm = new VideoViewModel.ViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> preparePlayerWithUrl = new TestSubscriber<>();
    vm.inputs.onVideoCompleted(10, 2);

    // Configure the view model with a project intent.
    vm.intent(new Intent().putExtra(IntentKey.PROJECT, project));

    this.segmentTrack.assertValues(EventName.VIDEO_PLAYBACK_COMPLETED.getEventName());
  }*/
}
