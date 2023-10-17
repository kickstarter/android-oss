package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject

interface HelpSettingsViewModel {

    interface Inputs {
        /**  Call when the user clicks the contact row.  */
        fun contactClicked()
    }

    class HelpSettingsViewModel : ViewModel(), Inputs {

        private val contactClicked = PublishSubject.create<Unit>()

        val inputs: Inputs = this

        override fun contactClicked() = this.contactClicked.onNext(Unit)
    }
}
