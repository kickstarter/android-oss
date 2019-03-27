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
import com.kickstarter.libs.utils.RewardDecoration
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey.PROJECT
import com.kickstarter.ui.adapters.HorizontalRewardsAdapter
import com.kickstarter.viewmodels.RewardFragmentViewModel
import kotlinx.android.synthetic.main.fragment_rewards.*

@RequiresFragmentViewModel(RewardFragmentViewModel.ViewModel::class)
class RewardsFragment : BaseFragment<RewardFragmentViewModel.ViewModel>() {

    private lateinit var project: Project

    companion object {
        fun newInstance(project: Project): RewardsFragment {
            val args = Bundle()
            args.putParcelable(PROJECT, project)
            val fragment = RewardsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args != null) {
            project = args.getParcelable(PROJECT) as Project
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_rewards, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupRecyclerView()
    }
    
    private fun setupRecyclerView() {
        setRewardsAdapter(this.project)
        addItemDecorator()
        addSnapHelper()
    }

    private fun setRewardsAdapter(project: Project) {
        val rewardsAdapter = HorizontalRewardsAdapter(this.viewModel)
        rewards_recycler.adapter = rewardsAdapter
        rewards_recycler.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rewardsAdapter.populateRewards(project)
    }

    private fun addSnapHelper() {
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rewards_recycler)
    }

    private fun addItemDecorator() {
        val radius = resources.getDimensionPixelSize(R.dimen.circle_radius).toFloat()
        val inactiveColor = ContextCompat.getColor(rewards_recycler.context, R.color.ksr_dark_grey_400)
        val activeColor = ContextCompat.getColor(rewards_recycler.context, R.color.ksr_soft_black)
        val margin = resources.getDimension(R.dimen.reward_margin).toInt()
        val padding = radius * 2
        rewards_recycler.addItemDecoration(RewardDecoration(margin, activeColor, inactiveColor, radius, padding))
    }
}
