package com.kickstarter.ui.viewholders

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import rx.Observable
import rx.subjects.PublishSubject

interface AddCardViewHolderViewModel {

    interface Inputs {
        fun configureWith(state: State)
    }

    interface Outputs {
        fun setLoadingState(): Observable<State>
        fun setDefaultState(): Observable<State>
    }

    class ViewModel(@NonNull environment: Environment) :
        ActivityViewModel<BackingAddOnViewHolder>(environment),
        Inputs,
        Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        val state = PublishSubject.create<State>()
        val loading = PublishSubject.create<State>()
        val default = PublishSubject.create<State>()

        init {

            state
                .filter {
                    it == State.DEFAULT
                }
                .compose(bindToLifecycle())
                .subscribe { this.default.onNext(it) }

            state
                .filter {
                    it == State.LOADING
                }
                .compose(bindToLifecycle())
                .subscribe { this.loading.onNext(it) }
        }

        // inputs
        override fun configureWith(state: State) = this.state.onNext(state)

        // output
        override fun setDefaultState() = this.default
        override fun setLoadingState() = this.loading
    }
}
