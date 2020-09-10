package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import java.util.concurrent.TimeUnit

@RequiresFragmentViewModel(BackingAddOnsFragmentViewModel.ViewModel::class)
class BackingAddOnsFragment : BaseFragment<BackingAddOnsFragmentViewModel.ViewModel>(), ShippingRulesAdapter.Delegate, BackingAddOnViewHolder.ViewListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_backing_addons, container, false)
    }

    private val backingAddonsAdapter = BackingAddOnsAdapter(this)
    private lateinit var shippingRulesAdapter: ShippingRulesAdapter
    private lateinit var errorDialog: AlertDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpShippingAdapter()
        setupRecyclerView()
        setupErrorDialog()

        this.viewModel.outputs.showPledgeFragment()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showPledgeFragment(it.first, it.second) }

        this.viewModel.outputs.addOnsList()
                .compose(bindToLifecycle())
                .throttleWithTimeout(50, TimeUnit.MILLISECONDS)
                .compose(Transformers.observeForUI())
                .subscribe {
                    populateAddOns(it)
                }

        this.viewModel.outputs.isEmptyState()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showEmptyState(it) }

        this.viewModel.outputs.selectedShippingRule()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { fragment_backing_addons_shipping_rules.setText(it.toString()) }

        this.viewModel.outputs.showErrorDialog()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showErrorDialog() }

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

        this.viewModel.outputs.isEnabledCTAButton()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    backing_addons_footer_button.isEnabled = it
                }

        backing_addons_footer_button.setOnClickListener {
            this.viewModel.inputs.continueButtonPressed()
        }
    }

    private fun selectProperString(totalSelected: Int): String {
        val ksString = this.viewModel.environment.ksString()
        return when {
            totalSelected == 0 -> ksString.format(getString(R.string.Skip_add_ons),"","")
            totalSelected == 1 -> ksString.format(getString(R.string.Continue_with_quantity_count_add_ons_one),"quantity_count", totalSelected.toString())
            totalSelected > 1 -> ksString.format(getString(R.string.Continue_with_quantity_count_add_ons_many),"quantity_count", totalSelected.toString())
            else -> ""
        }
    }

    private fun showErrorDialog() {
        Log.d("HELLOWORLD", "SHOW Error")

        if (!errorDialog.isShowing) {
            errorDialog.show()
        }
    }

    private fun dismissErrorDialog() {
        errorDialog.dismiss()
    }

    private fun populateAddOns(projectDataAndAddOnList: Triple<ProjectData, List<Reward>, ShippingRule>) {
        val projectData = projectDataAndAddOnList.first
        val selectedShippingRule = projectDataAndAddOnList.third
        val list = projectDataAndAddOnList
                .second
                .map {
                    Triple(projectData, it, selectedShippingRule)
                }.toList()

        backingAddonsAdapter.populateDataForAddOns(list)
    }

    private fun showEmptyState(isEmptyState: Boolean) {
        backingAddonsAdapter.showEmptyState(isEmptyState)
    }

    private fun setupRecyclerView() {
        fragment_select_addons_recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        fragment_select_addons_recycler.adapter = backingAddonsAdapter
    }

    private fun setUpShippingAdapter() {
        context?.let {
            shippingRulesAdapter = ShippingRulesAdapter(it, R.layout.item_shipping_rule, arrayListOf(), this)
            fragment_backing_addons_shipping_rules.setAdapter(shippingRulesAdapter)
        }
    }

    private fun showPledgeFragment(pledgeData: PledgeData, pledgeReason: PledgeReason) {
        fragmentManager
                ?.beginTransaction()
                ?.setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                ?.add(R.id.fragment_container, PledgeFragment.newInstance(pledgeData, pledgeReason), PledgeFragment::class.java.simpleName)
                ?.addToBackStack(NewCardFragment::class.java.simpleName)
                ?.commit()
    }

    private fun setupErrorDialog() {
        context?.let { context ->
            errorDialog = AlertDialog.Builder(context, R.style.AlertDialog)
                    .setCancelable(false)
                    .setTitle(getString(R.string.Something_went_wrong_please_try_again))
                    .setPositiveButton("             ${getString(R.string.Retry)}") { _, _ -> this.viewModel.inputs.retryButtonPressed() }
                    .setNegativeButton("             ${getString(R.string.close_alert)}") { _, _ -> dismissErrorDialog()}
                    .create()
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

    override fun quantityPerId(quantityPerId: Pair<Int, Long>) {
        this.viewModel.inputs.quantityPerId(quantityPerId)
    }

    override fun onDetach() {
        super.onDetach()
        fragment_select_addons_recycler?.adapter = null
    }
}