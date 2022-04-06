package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.viewmodels.projectpage.AudioViewElementViewHolderViewModel
import com.trello.rxlifecycle.FragmentEvent
import org.junit.Test
import rx.observers.TestSubscriber

class AudioViewElementViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: AudioViewElementViewHolderViewModel.ViewModel

    private val preparePlayerWithUrl = TestSubscriber.create<String>()
    private val stopPlayer = TestSubscriber.create<Void>()
    private val pausePlayer = TestSubscriber.create<Void>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = AudioViewElementViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.preparePlayerWithUrl().subscribe(preparePlayerWithUrl)
        this.vm.outputs.stopPlayer().subscribe(stopPlayer)
        this.vm.outputs.pausePlayer().subscribe(pausePlayer)
    }

    @Test
    fun initializePlayerTest() {
        setUpEnvironment(environment())
        val url = "https://d15chbti7ht62o.cloudfront.net/assets/002/236/466/f17de99e2a9e76a4954418c16d963f9b_mp3.mp3"
        val element = AudioViewElement(url)

        vm.inputs.configureWith(element)
        preparePlayerWithUrl.assertValue(element.sourceUrl)
    }

    @Test
    fun initializePlayerInvalidUrlTest() {
        setUpEnvironment(environment())
        val url = ""
        val element = AudioViewElement(url)

        vm.inputs.configureWith(element)
        preparePlayerWithUrl.assertNoValues()

        val url2 = "https://SomeInvalidUrl"
        val element2 = AudioViewElement(url2)
        vm.inputs.configureWith(element2)
        preparePlayerWithUrl.assertNoValues()
    }

    @Test
    fun stopPlayerWhenLifecycleEventStop() {
        setUpEnvironment(environment())

        vm.inputs.fragmentLifeCycle(FragmentEvent.STOP)
        stopPlayer.assertValueCount(1)
        stopPlayer.assertValue(null)
    }

    @Test
    fun stopPlayerWhenLifecycleEventPause() {
        setUpEnvironment(environment())

        vm.inputs.fragmentLifeCycle(FragmentEvent.PAUSE)
        pausePlayer.assertValueCount(1)
        pausePlayer.assertValue(null)
    }
}
