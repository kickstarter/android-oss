package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnViewHolderViewModel
import kotlinx.android.synthetic.main.add_on_title.view.*
import kotlinx.android.synthetic.main.item_add_on.view.*
import kotlinx.android.synthetic.main.item_lights_on.view.*

class BackingAddOnViewHolder (private val view: View) : KSViewHolder(view) {

    private var viewModel = BackingAddOnViewHolderViewModel.ViewModel(environment())

    init {
        this.viewModel.outputs.titleForAddOn()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe{
                    this.view.add_on_title_no_spannable.text = it
                }

        this.viewModel.outputs.description()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    this.view.add_on_description_text_view.text = it
                }
    }

    override fun bindData(data: Any?) {
        if (data is (Pair<*, *>)) {
            if (data.second is Reward) {
                bindAddonsList(data as Pair<ProjectData, Reward>)
            }
        }
    }

    private fun bindAddonsList(projectDataAndAddOn: Pair<ProjectData, Reward>) {
        this.viewModel.inputs.configureWith(projectDataAndAddOn)
    }
}