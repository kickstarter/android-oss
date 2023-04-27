package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSLifecycleEvent
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.viewmodels.projectpage.AudioViewElementViewHolderViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class AudioViewElementViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: AudioViewElementViewHolderViewModel.AudioViewElementViewHolderViewModel

    private val preparePlayerWithUrl = TestSubscriber.create<String>()
    private val stopPlayer = TestSubscriber.create<Unit>()
    private val pausePlayer = TestSubscriber.create<Unit>()

    private val lifecycleObservable = BehaviorSubject.create<KSLifecycleEvent>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = AudioViewElementViewHolderViewModel.AudioViewElementViewHolderViewModel(lifecycleObservable)

        disposables.add(this.vm.outputs.preparePlayerWithUrl().subscribe { preparePlayerWithUrl.onNext(it) })
        disposables.add(this.vm.outputs.stopPlayer().subscribe { stopPlayer.onNext(it) })
        disposables.add(this.vm.outputs.pausePlayer().subscribe { pausePlayer.onNext(it) })
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

        vm.inputs.fragmentLifeCycle(KSLifecycleEvent.STOP)
        stopPlayer.assertValueCount(1)
        stopPlayer.assertValue(Unit)
    }

    @Test
    fun stopPlayerWhenLifecycleEventPause() {
        setUpEnvironment(environment())

        vm.inputs.fragmentLifeCycle(KSLifecycleEvent.PAUSE)
        pausePlayer.assertValueCount(1)
        pausePlayer.assertValue(Unit)
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
}
