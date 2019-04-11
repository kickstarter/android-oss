package com.kickstarter.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.RewardDecoration
import com.kickstarter.models.Project
import com.kickstarter.ui.adapters.HorizontalRewardsAdapter
import com.kickstarter.viewmodels.RewardFragmentViewModel
import kotlinx.android.synthetic.main.fragment_rewards.*

@RequiresFragmentViewModel(RewardFragmentViewModel.ViewModel::class)
class RewardsFragment : BaseFragment<RewardFragmentViewModel.ViewModel>() {

    private var rewardsAdapter = HorizontalRewardsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_rewards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        this.viewModel.outputs.project()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { rewardsAdapter.populateRewards(it) }
    }

    override fun onDetach() {
        super.onDetach()
        rewards_recycler?.adapter = null
    }

    fun takeProject(project: Project) {
        this.viewModel.inputs.project(project)
    }

    private fun addItemDecorator() {
        val radius = resources.getDimensionPixelSize(R.dimen.circle_radius).toFloat()
        val inactiveColor = ContextCompat.getColor(rewards_recycler.context, R.color.ksr_dark_grey_400)
        val activeColor = ContextCompat.getColor(rewards_recycler.context, R.color.ksr_soft_black)
        val margin = resources.getDimension(R.dimen.reward_margin).toInt()
        val padding = radius * 2
        rewards_recycler.addItemDecoration(RewardDecoration(margin, activeColor, inactiveColor, radius, padding))
    }

    private fun addSnapHelper() {
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rewards_recycler)
    }

    private fun setupRecyclerView() {
        rewards_recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rewards_recycler.adapter = rewardsAdapter
        addItemDecorator()
        addSnapHelper()
    }
}
