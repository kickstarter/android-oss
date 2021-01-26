package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.HelpSettingsActivity
import rx.subjects.PublishSubject

interface HelpSettingsViewModel {

    interface Inputs {
        /**  Call when the user clicks the contact row.  */
        fun contactClicked()
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<HelpSettingsActivity>(environment), HelpSettingsViewModel.Inputs {

        private val contactClicked = PublishSubject.create<Void>()

        val inputs: Inputs = this

        init { }

        override fun contactClicked() = this.contactClicked.onNext(null)
    }
}
