package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.BackingAddOnViewHolder
import rx.subjects.PublishSubject

class BackingAddOnViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward].
         * @param projectData we get the Project for currency
         * @param reward the actual addOn loading on the ViewHolder
         */
        fun configureWith(projectData: ProjectData, reward: Reward)
    }

    interface Outputs {
    }

    /**
     *  Logic to handle the UI for backing `Add On` card
     *  Configuring the View for [BackingAddOnViewHolder]
     *  - No interaction with the user just displaying information
     *  - Loading in [BackingAddOnViewHolder] -> [BackingAddOnsAdapter] -> [BackingAddOnsFragment]
     */
    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<BackingAddOnViewHolder>(environment), Inputs, Outputs {
        private val projectDataAndAddOn = PublishSubject.create<Pair<ProjectData, Reward>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

        }

        override fun configureWith(projectData: ProjectData, reward: Reward) = this.projectDataAndAddOn.onNext(Pair.create(projectData, reward))
    }
}