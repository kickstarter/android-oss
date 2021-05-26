package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.ThreadActivity

interface ThreadViewModel {

    interface Inputs
    interface Outputs

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ThreadActivity>(environment), Inputs, Outputs
}
