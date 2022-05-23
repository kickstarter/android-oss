package com.kickstarter.ui.fragments

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.databinding.FragmentBackingAddonsBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.BackingAddOnsAdapter
import com.kickstarter.ui.adapters.ShippingRulesAdapter
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.hideKeyboard
import com.kickstarter.ui.viewholders.BackingAddOnViewHolder
import com.kickstarter.viewmodels.BackingAddOnsFragmentViewModel
import java.util.concurrent.TimeUnit

@RequiresFragmentViewModel(BackingAddOnsFragmentViewModel.ViewModel::class)
class BackingAddOnsFragment : BaseFragment<BackingAddOnsFragmentViewModel.ViewModel>(), ShippingRulesAdapter.Delegate, BackingAddOnViewHolder.ViewListener {
    private var binding: FragmentBackingAddonsBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBackingAddonsBinding.inflate(inflater, container, false)
        return binding?.root
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
            .subscribe { binding?.fragmentBackingAddonsShippingRules?.setText(it.toString()) }

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
                binding?.fragmentBackingAddonsSectionFooterLayout?.backingAddonsFooterButton ?.text = selectProperString(total)
            }

        this.viewModel.outputs.shippingSelectorIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.fragmentBackingAddonsShippingRules?.isGone = it
                binding?.fragmentBackingAddonsCallOut?.isGone = it
            }

        this.viewModel.outputs.isEnabledCTAButton()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.fragmentBackingAddonsSectionFooterLayout?.backingAddonsFooterButton ?.isEnabled = it
            }

        binding?.fragmentBackingAddonsSectionFooterLayout?.backingAddonsFooterButton ?.setOnClickListener {
            this.viewModel.inputs.continueButtonPressed()
        }
    }

    private fun selectProperString(totalSelected: Int): String {
        val ksString = requireNotNull(this.viewModel.environment.ksString())
        return when {
            totalSelected == 0 -> ksString.format(getString(R.string.Skip_add_ons), "", "")
            totalSelected == 1 -> ksString.format(getString(R.string.Continue_with_quantity_count_add_ons_one), "quantity_count", totalSelected.toString())
            totalSelected > 1 -> ksString.format(getString(R.string.Continue_with_quantity_count_add_ons_many), "quantity_count", totalSelected.toString())
            else -> ""
        }
    }

    private fun showErrorDialog() {
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
        binding?.fragmentSelectAddonsRecycler?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.fragmentSelectAddonsRecycler?.adapter = backingAddonsAdapter
    }

    private fun setUpShippingAdapter() {
        activity?.let {
            shippingRulesAdapter = ShippingRulesAdapter(it, R.layout.item_shipping_rule, arrayListOf(), this)
            binding?.fragmentBackingAddonsShippingRules?.setAdapter(shippingRulesAdapter)
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
                .setPositiveButton(getString(R.string.Retry)) { _, _ ->
                    this.viewModel.inputs.retryButtonPressed()
                }
                .setNegativeButton(getString(R.string.general_navigation_buttons_close)) { _, _ -> dismissErrorDialog() }
                .create()
        }
    }

    private fun displayShippingRules(shippingRules: List<ShippingRule>, project: Project) {
        binding?.fragmentBackingAddonsShippingRules?.isEnabled = true
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
        binding?.fragmentBackingAddonsShippingRules?.clearFocus()
    }

    override fun quantityPerId(quantityPerId: Pair<Int, Long>) {
        this.viewModel.inputs.quantityPerId(quantityPerId)
    }

    override fun onDetach() {
        super.onDetach()
        binding?.fragmentSelectAddonsRecycler?.adapter = null
        this.viewModel = null
    }
}
