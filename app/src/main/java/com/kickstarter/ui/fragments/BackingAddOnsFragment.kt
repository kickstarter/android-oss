package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.extensions.hideKeyboard
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.BackingAddOnsAdapter
import com.kickstarter.ui.adapters.ShippingRulesAdapter
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.BackingAddOnViewHolder
import com.kickstarter.viewmodels.BackingAddOnsFragmentViewModel
import kotlinx.android.synthetic.main.fragment_backing_addons.*
import kotlinx.android.synthetic.main.fragment_backing_addons_section_footer.*
import kotlinx.android.synthetic.main.fragment_backing_addons_section_footer.view.*
import rx.Observable

@RequiresFragmentViewModel(BackingAddOnsFragmentViewModel.ViewModel::class)
class BackingAddOnsFragment : BaseFragment<BackingAddOnsFragmentViewModel.ViewModel>(), ShippingRulesAdapter.Delegate, BackingAddOnViewHolder.ViewListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_backing_addons, container, false)
    }

    private val backingAddonsAdapter = BackingAddOnsAdapter(this)
    private lateinit var shippingRulesAdapter: ShippingRulesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpShippingAdapter()
        setupRecyclerView()

        this.viewModel.outputs.showPledgeFragment()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showPledgeFragment(it.first, it.second) }

        this.viewModel.outputs.addOnsList()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    populateAddOns(it)
                }

        this.viewModel.outputs.selectedShippingRule()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { fragment_backing_addons_shipping_rules.setText(it.toString()) }

        this.viewModel.outputs.shippingRulesAndProject()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .filter { ObjectUtils.isNotNull(context) }
                .subscribe { displayShippingRules(it.first, it.second) }

        this.viewModel.outputs.totalSelectedAddOns()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .filter { ObjectUtils.isNotNull(it) }
                .subscribe { total ->
                    backing_addons_footer_button.text = selectProperString(total)
                }
      
        this.viewModel.outputs.shippingSelectorIsGone()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    ViewUtils.setGone(fragment_backing_addons_shipping_rules, it)
                    ViewUtils.setGone(fragment_backing_addons_call_out, it)
                }

        backing_addons_footer_button.setOnClickListener {
            this.viewModel.inputs.continueButtonPressed()
        }
    }

    private fun selectProperString(totalSelected: Int): String {
        val ksString = this.viewModel.environment.ksString()
        return when {
            totalSelected == 0 -> ksString.format(getString(R.string.Skip_add_ons),"","")
            totalSelected == 1 -> ksString.format(getString(R.string.Continue_with_quantity_add_ons_one),"quantity", totalSelected.toString())
            totalSelected > 1 -> ksString.format(getString(R.string.Continue_with_quantity_add_ons_many),"quantity", totalSelected.toString())
            else -> ""
        }
    }

    private fun populateAddOns(projectDataAndAddOnList: Triple<ProjectData, List<Reward>, ShippingRule>) {
        val projectData = projectDataAndAddOnList.first
        val selectedShippingRule = projectDataAndAddOnList.third
        val list = projectDataAndAddOnList
                .second
                .map {
                    Triple(projectData,it, selectedShippingRule)
                }.toList()

        backingAddonsAdapter.populateDataForAddOns(list)
    }

    private fun setupRecyclerView() {
        fragment_backing_addons_list.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        fragment_backing_addons_list.adapter = backingAddonsAdapter
    }

    private fun setUpShippingAdapter() {
        context?.let {
            shippingRulesAdapter = ShippingRulesAdapter(it, R.layout.item_shipping_rule, arrayListOf(), this)
            fragment_backing_addons_shipping_rules.setAdapter(shippingRulesAdapter)
        }
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

    private fun displayShippingRules(shippingRules: List<ShippingRule>, project: Project) {
        fragment_backing_addons_shipping_rules.isEnabled = true
        shippingRulesAdapter.populateShippingRules(shippingRules, project)
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

    override fun ruleSelected(rule: ShippingRule) {
        this.viewModel.inputs.shippingRuleSelected(rule)
        activity?.hideKeyboard()
        fragment_backing_addons_shipping_rules.clearFocus()
    }

    override fun quantityHasChanged(quantity: Int) {
        this.viewModel.inputs.selectedAddonsQuantity(quantity)
    }
}