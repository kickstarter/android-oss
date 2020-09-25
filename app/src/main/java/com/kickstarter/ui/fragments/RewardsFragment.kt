package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.RewardDecoration
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Reward
import com.kickstarter.ui.adapters.RewardsAdapter
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.RewardsFragmentViewModel
import kotlinx.android.synthetic.main.fragment_rewards.*

@RequiresFragmentViewModel(RewardsFragmentViewModel.ViewModel::class)
class RewardsFragment : BaseFragment<RewardsFragmentViewModel.ViewModel>(), RewardsAdapter.Delegate {

    private var rewardsAdapter = RewardsAdapter(this)
    private lateinit var dialog: AlertDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_rewards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        createDialog()

        this.viewModel.outputs.projectData()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { rewardsAdapter.populateRewards(it) }

        this.viewModel.outputs.backedRewardPosition()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { scrollToReward(it) }

        this.viewModel.outputs.showPledgeFragment()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    dialog.dismiss()
                    showPledgeFragment(it.first, it.second)
                }

        this.viewModel.outputs.showAddOnsFragment()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    dialog.dismiss()
                    showAddonsFragment(it)
                }

        this.viewModel.outputs.rewardsCount()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { setRewardsCount(it) }
        
        this.viewModel.outputs.showAlert()
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe {
                    showAlert()
                }

        context?.apply {
            ViewUtils.setGone(rewards_count, ViewUtils.isLandscape(this))
        }
    }

    private fun createDialog() {
        context?.let { context ->
            dialog = AlertDialog.Builder(context, R.style.AlertDialog)
                    .setCancelable(false)
                    .setTitle(getString(R.string.Continue_with_this_reward))
                    .setMessage(getString(R.string.It_may_not_offer_some_or_all_of_your_add_ons))
                    .setNegativeButton(getString(R.string.No_go_back)) { _, _ -> {} }
                    .setPositiveButton(getString(R.string.Yes_continue)) { _, _ ->
                        this.viewModel.inputs.alertButtonPressed()
                    }.create()
        }
    }

    private fun showAlert() {
        if (this.isVisible)
            dialog.show()
    }

    private fun scrollToReward(position: Int) {
        if (position != 0) {
            val recyclerWidth = (rewards_recycler?.width ?: 0)
            val linearLayoutManager = rewards_recycler?.layoutManager as LinearLayoutManager
            val rewardWidth = resources.getDimensionPixelSize(R.dimen.item_reward_width)
            val rewardMargin = resources.getDimensionPixelSize(R.dimen.reward_margin)
            val center = (recyclerWidth - rewardWidth - rewardMargin) / 2
            linearLayoutManager.scrollToPositionWithOffset(position, center)
        }
    }

    private fun setRewardsCount(count: Int) {
        val rewardsCountString = this.viewModel.environment.ksString().format("Rewards_count_rewards", count,
                "rewards_count", NumberUtils.format(count))
        rewards_count.text = rewardsCountString
    }

    override fun onDetach() {
        super.onDetach()
        rewards_recycler?.adapter = null
        this.viewModel = null
    }

    override fun rewardClicked(reward: Reward) {
        this.viewModel.inputs.rewardClicked(reward)
    }

    fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
    }

    private fun addItemDecorator() {
        val margin = resources.getDimension(R.dimen.reward_margin).toInt()
        rewards_recycler.addItemDecoration(RewardDecoration(margin))
    }

    private fun setupRecyclerView() {
        rewards_recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rewards_recycler.adapter = rewardsAdapter
        addItemDecorator()
    }

    private fun showPledgeFragment(pledgeData: PledgeData, pledgeReason: PledgeReason) {
        if (this.isVisible && this.fragmentManager?.findFragmentByTag(PledgeFragment::class.java.simpleName) == null) {
            val pledgeFragment = PledgeFragment.newInstance(pledgeData, pledgeReason)
            this.fragmentManager?.beginTransaction()
                    ?.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                    ?.add(R.id.fragment_container,
                            pledgeFragment,
                            PledgeFragment::class.java.simpleName)
                    ?.addToBackStack(PledgeFragment::class.java.simpleName)
                    ?.commit()
        }
    }

    private fun showAddonsFragment(pledgeDataAndReason: Pair<PledgeData, PledgeReason>) {
        if (this.isVisible && this.fragmentManager?.findFragmentByTag(BackingAddOnsFragment::class.java.simpleName) == null) {
            val addOnsFragment = BackingAddOnsFragment.newInstance(pledgeDataAndReason)
            this.fragmentManager?.beginTransaction()
                    ?.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                    ?.add(R.id.fragment_container,
                            addOnsFragment,
                            BackingAddOnsFragment::class.java.simpleName)
                    ?.addToBackStack(BackingAddOnsFragment::class.java.simpleName)
                    ?.commit()
        }
    }
}