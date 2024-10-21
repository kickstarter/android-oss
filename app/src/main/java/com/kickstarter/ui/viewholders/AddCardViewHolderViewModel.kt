package com.kickstarter.ui.viewholders

import com.kickstarter.libs.utils.extensions.addToDisposable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

interface AddCardViewHolderViewModel {

    interface Inputs {
        fun configureWith(state: State)
    }

    interface Outputs {
        fun setLoadingState(): Observable<State>
        fun setDefaultState(): Observable<State>
    }

    class ViewModel :
        Inputs,
        Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        val state = PublishSubject.create<State>()
        val loading = PublishSubject.create<State>()
        val default = PublishSubject.create<State>()

        private val disposables = CompositeDisposable()

        init {

            state
                .filter {
                    it == State.DEFAULT
                }
                .subscribe { this.default.onNext(it) }
                .addToDisposable(disposables)

            state
                .filter {
                    it == State.LOADING
                }
                .subscribe { this.loading.onNext(it) }
                .addToDisposable(disposables)
        }

        // inputs
        override fun configureWith(state: State) = this.state.onNext(state)

        // output
        override fun setDefaultState() = this.default
        override fun setLoadingState() = this.loading

        fun clear() = disposables.clear()
    }
}
