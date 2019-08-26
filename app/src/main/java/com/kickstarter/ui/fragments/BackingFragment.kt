package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.Project
import com.kickstarter.ui.viewholders.NativeCheckoutRewardViewHolder
import com.kickstarter.viewmodels.BackingFragmentViewModel
import kotlinx.android.synthetic.main.fragment_backing.*
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_pledge.*
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_shipping.*
import kotlinx.android.synthetic.main.fragment_pledge_section_summary_total.*
import kotlinx.android.synthetic.main.item_reward.*

@RequiresFragmentViewModel(BackingFragmentViewModel.ViewModel::class)
class BackingFragment: BaseFragment<BackingFragmentViewModel.ViewModel>()  {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_backing, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.viewModel.outputs.projectAndReward()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    val rewardViewHolder = NativeCheckoutRewardViewHolder(reward_container, delegate = null, inset = true)
                    rewardViewHolder.bindData(Pair(it.first, it.second))
                }

        this.viewModel.outputs.backerNumber()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { backer_number.text = this.viewModel.ksString.format(getString(R.string.backer_modal_backer_number), "backer_number", it) }

        this.viewModel.outputs.backingDate()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { backing_date.text = it }

        this.viewModel.outputs.pledgeAmount()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { pledge_summary_amount.text = it }

        this.viewModel.outputs.shippingAmount()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { shipping_summary_amount.text = it }

        this.viewModel.outputs.totalAmount()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { total_summary_amount.text = it }
    }

    fun takeProject(project: Project) {
        this.viewModel.inputs.project(project)
    }

}
