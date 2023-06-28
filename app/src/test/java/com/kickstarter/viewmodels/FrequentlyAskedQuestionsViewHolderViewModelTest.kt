package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber

class FrequentlyAskedQuestionsViewHolderViewModelTest : KSRobolectricTestCase() {

    private val disposables = CompositeDisposable()
    private val projectFaqInput = TestSubscriber.create<String>()
}