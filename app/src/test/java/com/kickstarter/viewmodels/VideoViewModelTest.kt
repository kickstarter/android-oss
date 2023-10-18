package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventName
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
    private val preparePlayerWithUrl = TestSubscriber<String>()
    private val preparePlayerWithUrlAndPosition = TestSubscriber<Pair<String, Long>>()

    @After
    fun cleanUp() {
        disposables.clear()
    }

    private fun setUpEnvironment(environment: Environment, intent: Intent) {
        vm = Factory(environment, intent).create(VideoViewModel::class.java)
        vm.outputs.preparePlayerWithUrl().subscribe { preparePlayerWithUrl.onNext(it) }.addToDisposable(disposables)
        vm.outputs.preparePlayerWithUrlAndPosition().subscribe { preparePlayerWithUrlAndPosition.onNext(it) }.addToDisposable(disposables)
    }
    @Test
    fun testVideoViewModel_EmitsVideoUrl_WhenHls() {

        val project = project().toBuilder().video(hlsVideo()).build()
        val videoUrl = project.video()!!.hls()

        // Configure the view model with a project intent.
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.VIDEO_URL_SOURCE, videoUrl)

        setUpEnvironment(environment(), intent)

        vm.inputs.resume()
//        vm.inputs.onVideoStarted(0,0)
//        vm.inputs.onVideoCompleted(100, 100)

        preparePlayerWithUrl.assertValue(videoUrl)
        preparePlayerWithUrlAndPosition.assertNoValues()
    }

    @Test
    fun testVideoViewModel_EmitsVideoUrl_WhenNotHls() {
        val project = project()
        val videoUrl = project.video()?.high() ?: ""
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.VIDEO_URL_SOURCE, videoUrl)

        setUpEnvironment(environment(), intent)

        vm.inputs.resume()
        preparePlayerWithUrl.assertValue(videoUrl)
        preparePlayerWithUrlAndPosition.assertNoValues()
    }

    @Test
    fun testVideoViewModel_StartVideo_WhenAdvancedPosition() {
        val project = project()
        val videoUrl = project.video()?.high() ?: ""
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.VIDEO_URL_SOURCE, videoUrl)
            .putExtra(IntentKey.VIDEO_SEEK_POSITION, 20L)

        setUpEnvironment(environment(), intent)

        vm.inputs.resume()
        preparePlayerWithUrl.assertNoValues()
        preparePlayerWithUrlAndPosition.assertValue(Pair(videoUrl, 20L))
    }

    @Test
    fun testVideoViewModel_SendEvents_WhenStartEnd() {
        val project = project()
        val videoUrl = project.video()?.high() ?: ""
        val intent = Intent()
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.VIDEO_URL_SOURCE, videoUrl)
            .putExtra(IntentKey.VIDEO_SEEK_POSITION, 20L)

        setUpEnvironment(environment(), intent)

        vm.inputs.resume()
        vm.inputs.onVideoStarted(0L, 0L)

        preparePlayerWithUrl.assertNoValues()
        preparePlayerWithUrlAndPosition.assertValue(Pair(videoUrl, 20L))

        vm.inputs.onVideoCompleted(100L, 100L)

        this.segmentTrack.assertValues(
            EventName.VIDEO_PLAYBACK_STARTED.eventName,
            EventName.VIDEO_PLAYBACK_COMPLETED.eventName
        )
    }
}
