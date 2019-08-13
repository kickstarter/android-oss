package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.ui.fragments.BackingFragment

interface BackingFragmentViewModel {
    interface Inputs {

    }

    interface Outputs {

    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<BackingFragment>(environment), Inputs, Outputs {

        init {

        }
    }
}