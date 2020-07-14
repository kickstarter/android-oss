package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.Reward
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.BackingAddOnsAdapter
import com.kickstarter.ui.adapters.RewardAndAddOnsAdapter
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnsFragmentViewModel
import kotlinx.android.synthetic.main.fragment_backing.*
import kotlinx.android.synthetic.main.fragment_backing_addons.*

@RequiresFragmentViewModel(BackingAddOnsFragmentViewModel.ViewModel::class)
class BackingAddOnsFragment : BaseFragment<BackingAddOnsFragmentViewModel.ViewModel>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_backing_addons, container, false)
    }

    private val backingAddonsAdapter = BackingAddOnsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        this.viewModel.outputs.showPledgeFragment()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showPledgeFragment(it.first, it.second) }

        this.viewModel.outputs.AddOnsList()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    populateAddOns(it)
                }
    }


    private fun populateAddOns(projectDataAndAddOnList: Pair<ProjectData, List<Reward>>) {
        val projectData = projectDataAndAddOnList.first

        val list = projectDataAndAddOnList
                .second
                .map {
                    Pair(projectData,it)
                }.toList()

        backingAddonsAdapter.populateDataForAddOns(list)
    }

    private fun setupRecyclerView() {
        fragment_backing_addons_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        fragment_backing_addons_list.adapter = backingAddonsAdapter
    }

    private fun showPledgeFragment(pledgeData: PledgeData, pledgeReason: PledgeReason) {
        if (this.fragmentManager?.findFragmentByTag(PledgeFragment::class.java.simpleName) == null) {
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

    companion object {
        fun newInstance(pledgeDataAndReason: Pair<PledgeData, PledgeReason>): BackingAddOnsFragment {
            val fragment = BackingAddOnsFragment()
            val argument = Bundle()
            argument.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, pledgeDataAndReason.first)
            argument.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, pledgeDataAndReason.second)
            fragment.arguments = argument
            return fragment
        }
    }
}