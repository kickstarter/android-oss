package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.ProjectActivity

interface FeatureFlagsViewModel {
    interface Inputs
    interface Outputs

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ProjectActivity>(environment), Inputs, Outputs {

    }
}