package com.kickstarter.viewmodels

import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.viewholders.RepliesStatusCellType
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

interface RepliesStatusCellViewHolderViewModel {
    interface Inputs {
        fun configureWith(configureCellWith: RepliesStatusCellType)
    }

    interface Outputs {
        fun isViewMoreRepliesPaginationVisible(): Observable<Boolean>
        fun isErrorPaginationVisible(): Observable<Boolean>
    }

    class ViewModel : Inputs, Outputs {
        private val isViewMoreRepliesPaginationVisible = BehaviorSubject.create<Boolean>()
        private val isErrorPaginationVisible = BehaviorSubject.create<Boolean>()
        private val initCellConfig = BehaviorSubject.create<RepliesStatusCellType>()

        val inputs = this
        val outputs = this

        private val disposables = CompositeDisposable()

        init {
            this.initCellConfig
                .subscribe {
                    when (it) {
                        RepliesStatusCellType.VIEW_MORE -> {
                            this.isErrorPaginationVisible.onNext(false)
                            this.isViewMoreRepliesPaginationVisible.onNext(true)
                        }
                        RepliesStatusCellType.PAGINATION_ERROR, RepliesStatusCellType.INITIAL_ERROR -> {
                            this.isErrorPaginationVisible.onNext(true)
                            this.isViewMoreRepliesPaginationVisible.onNext(false)
                        }
                        RepliesStatusCellType.EMTPY -> {
                            this.isErrorPaginationVisible.onNext(false)
                            this.isViewMoreRepliesPaginationVisible.onNext(false)
                        }
                    }
                }.addToDisposable(disposables)
        }

        // - Inputs
        override fun configureWith(configureCellWith: RepliesStatusCellType) = this.initCellConfig.onNext(configureCellWith)

        // - Outputs
        override fun isViewMoreRepliesPaginationVisible(): Observable<Boolean> = this.isViewMoreRepliesPaginationVisible
        override fun isErrorPaginationVisible(): Observable<Boolean> = this.isErrorPaginationVisible

        fun clear() {
            disposables.clear()
        }
    }
}
