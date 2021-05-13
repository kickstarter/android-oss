package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.ProjectCommentsActivity

interface ProjectCommentsViewModel {

    interface Inputs {}
    interface Outputs {}

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ProjectCommentsActivity>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this
    }
}