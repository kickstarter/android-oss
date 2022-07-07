package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.WebViewActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface WebViewViewModel {
    interface Outputs {
        /** Emits a string to display in the toolbar. */
        fun toolbarTitle(): Observable<String>

        /** Emits a URL to load in the web view.  */
        fun url(): Observable<String>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<WebViewActivity >(environment),
        Outputs {

        private val toolbarTitle = BehaviorSubject.create<String>()
        private val url = BehaviorSubject.create<String>()

        val outputs: Outputs = this

        override fun toolbarTitle(): Observable<String> {
            return toolbarTitle
        }

        override fun url(): Observable<String> {
            return url
        }

        init {
            intent()
                .map { it.getStringExtra(IntentKey.TOOLBAR_TITLE) }
                .ofType(String::class.java)
                .compose(bindToLifecycle())
                .subscribe { toolbarTitle.onNext(it) }

            intent()
                .map { it.getStringExtra(IntentKey.URL) }
                .ofType(String::class.java)
                .compose(bindToLifecycle())
                .subscribe { url.onNext(it) }
        }
    }
}
