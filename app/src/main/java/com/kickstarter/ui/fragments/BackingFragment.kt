package com.kickstarter.ui.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.databinding.FragmentBackingBinding
import com.kickstarter.libs.Either
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Reward
import com.kickstarter.ui.activities.BackingActivity
import com.kickstarter.ui.adapters.RewardAndAddOnsAdapter
import com.kickstarter.ui.data.PledgeStatusData
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.BackingFragmentViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class BackingFragment : Fragment() {

    private var rewardsAndAddOnsAdapter = RewardAndAddOnsAdapter()

    private var binding: FragmentBackingBinding? = null

    private lateinit var viewModelFactory: BackingFragmentViewModel.Factory
    private val viewModel: BackingFragmentViewModel.BackingFragmentViewModel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()

    interface BackingDelegate {
        fun refreshProject()
        fun showFixPaymentMethod()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBackingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        this.context?.getEnvironment()?.let { env ->
            viewModelFactory = BackingFragmentViewModel.Factory(env)
        }

        this.viewModel.outputs.backerAvatar()
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setBackerImageView(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.backerName()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.backerName?.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.backerNumber()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setBackerNumberText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.cardLogo()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.rewardCardDetails?.rewardCardLogo?.setImageResource(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.cardExpiration()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setCardExpirationText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.cardIssuer()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setCardIssuerContentDescription(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.cardLastFour()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setCardLastFourText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.fixPaymentMethodButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.fixPaymentMethodButton?.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.fixPaymentMethodButtonIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.fixPaymentMethodMessage?.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.notifyDelegateToRefreshProject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (activity as BackingDelegate?)?.refreshProject() }
            .addToDisposable(disposables)

        this.viewModel.outputs.notifyDelegateToShowFixPledge()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (activity as BackingDelegate?)?.showFixPaymentMethod() }
            .addToDisposable(disposables)

        this.viewModel.outputs.paymentMethodIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.paymentMethod?.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeAmount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.fragmentPledgeSectionSummaryPledge?.pledgeSummaryAmount?.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeDate()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setPledgeDateText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeStatusData()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setPledgeStatusText(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.pledgeSummaryIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.fragmentPledgeSectionSummaryPledge?.pledgeSummary?.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.projectDataAndReward()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bindDataToRewardViewHolder(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.receivedCheckboxChecked()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.receivedSectionLayout?.estimatedDeliveryCheckbox?.isChecked = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.receivedSectionIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.receivedSectionLayout?.receivedSectionLayoutContainer?.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.receivedSectionCreatorIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.estimatedDeliveryLabel2?.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingAmount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.fragmentPledgeSectionSummaryShipping?.shippingSummaryAmount?.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingLocation()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.fragmentPledgeSectionSummaryShipping?.shippingLabel?.text = String.format("%s: %s", getString(R.string.Shipping), it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.shippingSummaryIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.fragmentPledgeSectionSummaryShipping?.shippingSummary?.isGone = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.showUpdatePledgeSuccess()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(view, getString(R.string.Got_it_your_changes_have_been_saved)) }
            .addToDisposable(disposables)

        this.viewModel.outputs.totalAmount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.fragmentBackingSectionSummaryTotal?.totalSummaryAmount?.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.projectDataAndAddOns()
            .filter { it.isNotNull() }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { populateAddOns(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.bonusSupport()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.sectionBonusSupport?.bonusSummaryAmount?.text = it }
            .addToDisposable(disposables)

        this.viewModel.outputs.estimatedDelivery()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { stylizedTextViews(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.deliveryDisclaimerSectionIsGone()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding?.pledgeDetailsLabel?.text = getString(R.string.Pledge_details)
                binding?.deliveryDisclaimerSection?.root?.isGone = it
                binding?.estimatedDeliveryLabel2?.isGone = true
            }
            .addToDisposable(disposables)

        this.viewModel.outputs.swipeRefresherProgressIsVisible()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                binding?.backingSwipeRefreshLayout?.isRefreshing = it
            }
            .addToDisposable(disposables)

        binding?.deliveryDisclaimerSection?.deliveryReminderLabel?.apply {
            val sb = StringBuilder(text.toString())
            sb.append(" " + resources.getString(R.string.Delays_or_changes_are_possible))
            text = sb.toString()

            val boldPortionLength = text.toString().split(".").first().length
            setBoldSpanOnTextView(boldPortionLength, this, resources.getColor(R.color.kds_support_400, null))
        }

        binding?.backingSwipeRefreshLayout?.setColorSchemeResources(R.color.kds_create_700, R.color.kds_create_500, R.color.kds_create_300)

        binding?.backingSwipeRefreshLayout?.setOnRefreshListener {
            this.viewModel.inputs.refreshProject()
        }

        binding?.fixPaymentMethodButton?.setOnClickListener {
            this.viewModel.inputs.fixPaymentMethodButtonClicked()
        }

        binding?.receivedSectionLayout?.estimatedDeliveryCheckbox?.apply {
            viewModel.inputs.receivedCheckboxToggled(this.isChecked)
        }

        binding?.receivedSectionLayout?.estimatedDeliveryCheckbox?.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.inputs.receivedCheckboxToggled(isChecked)
        }
    }

    override fun onStart() {
        super.onStart()
        this.setState(activity is BackingActivity)
    }

    private fun stylizedTextViews(it: String) {
        binding?.receivedSectionLayout?.estimatedDeliveryLabel?.apply {
            val totalCharacters = text.length
            text = viewModel.ksString?.format(
                getString(R.string.estimated_delivery_data),
                "title", text.toString(),
                "estimated_delivery_data", it
            )
            setBoldSpanOnTextView(totalCharacters, this, resources.getColor(R.color.kds_support_400, null))
        }

        binding?.estimatedDeliveryLabel2?.apply {
            text = viewModel.ksString?.format(
                getString(R.string.estimated_delivery_data),
                "title", text.toString(),
                "estimated_delivery_data", it
            )

            val totalCharacters2 = text.length
            setBoldSpanOnTextView(
                totalCharacters2, this, resources.getColor(R.color.kds_support_400, null)
            )
        }
    }

    fun isRefreshing(isRefreshing: Boolean) {
        binding?.backingSwipeRefreshLayout?.isRefreshing = isRefreshing
    }

    private fun setBoldSpanOnTextView(numCharacters: Int, textView: TextView, spanColor: Int) {
        val spannable = SpannableString(textView.text)
        spannable.setSpan(ForegroundColorSpan(spanColor), 0, numCharacters, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0, numCharacters,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }

    fun configureWith(projectData: ProjectData) {
        this.viewModel.inputs.configureWith(projectData)
    }

    fun pledgeSuccessfullyUpdated() {
        this.viewModel.inputs.pledgeSuccessfullyUpdated()
    }

    private fun bindDataToRewardViewHolder(projectAndReward: Pair<ProjectData, Reward>) {
        val project = projectAndReward.first
        val reward = projectAndReward.second

        val projectAndRw = Pair(project, reward)
        rewardsAndAddOnsAdapter.populateDataForReward(projectAndRw)
    }

    private fun populateAddOns(projectAndAddOn: Pair<ProjectData, List<Reward>>) {
        val project = projectAndAddOn.first
        val addOns = projectAndAddOn.second
        val listData = addOns.map {
            Pair(project, it)
        }.toList()
        rewardsAndAddOnsAdapter.populateDataForAddOns(listData)
    }

    private fun setBackerImageView(url: String) {
        activity?.runOnUiThread {
            context?.apply {
                binding?.backingAvatar?.loadCircleImage(url)
            }
        }
    }

    private fun setBackerNumberText(it: String?) {
        binding?.backerNumber?.text = this.viewModel.ksString?.format(getString(R.string.backer_modal_backer_number), "backer_number", it)
    }

    private fun setCardExpirationText(expiration: String) {
        binding?.rewardCardDetails?.rewardCardExpirationDate?.text = this.viewModel.ksString?.format(
            getString(R.string.Credit_card_expiration),
            "expiration_date", expiration
        )
    }

    private fun setCardIssuerContentDescription(cardIssuerOrStringRes: Either<String, Int>) {
        val cardIssuer = cardIssuerOrStringRes.left()
        val stringRes = cardIssuerOrStringRes.right()
        binding?.rewardCardDetails?.rewardCardLogo?.contentDescription = stringRes?.let { getString(it) } ?: cardIssuer
    }

    private fun setCardLastFourText(lastFour: String) {
        binding?.rewardCardDetails?.rewardCardLastFour?.text = this.viewModel.ksString?.format(
            getString(R.string.payment_method_last_four),
            "last_four",
            lastFour
        )
    }

    private fun setPledgeDateText(pledgeDate: String) {
        binding?.backingDate?.text = this.viewModel.ksString?.format(getString(R.string.As_of_pledge_date), "pledge_date", pledgeDate)
    }

    private fun setPledgeStatusText(pledgeStatusData: PledgeStatusData) {
        val pledgeStatusText = pledgeStatusData.statusStringRes?.let {
            when (pledgeStatusData.statusStringRes) {
                R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline_and_receive_proof_of_pledge -> {
                    this.viewModel.ksString?.let { ksString ->
                        ksString.format(
                            getString(it),
                            "total", pledgeStatusData.pledgeTotal,
                            "project_deadline", pledgeStatusData.projectDeadline
                        )
                    }
                }

                R.string.If_your_project_reaches_its_funding_goal_the_backer_will_be_charged_total_on_project_deadline -> {
                    this.viewModel.ksString?.let { ksString ->
                        ksString.format(
                            getString(it),
                            "total", pledgeStatusData.pledgeTotal,
                            "project_deadline", pledgeStatusData.projectDeadline
                        )
                    }
                }

                else -> getString(it)
            }
        }

        pledgeStatusText?.let { statusText ->
            val spannablePledgeStatus = SpannableString(statusText)
            pledgeStatusData.pledgeTotal?.let { ViewUtils.addBoldSpan(spannablePledgeStatus, it) }
            pledgeStatusData.projectDeadline?.let { ViewUtils.addBoldSpan(spannablePledgeStatus, it) }

            binding?.backerPledgeStatus?.text = spannablePledgeStatus
        } ?: run {
            binding?.backerPledgeStatus?.text = null
        }
    }

    fun setState(state: Boolean?) {
        state?.let {
            viewModel.isExpanded(state)
        }
    }

    private fun setupRecyclerView() {
        binding?.rewardAddOnRecycler?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.rewardAddOnRecycler?.adapter = rewardsAndAddOnsAdapter
    }

    override fun onDetach() {
        disposables.clear()
        super.onDetach()
        binding?.rewardAddOnRecycler?.adapter = null
    }

    override fun onDestroyView() {
        binding = null
        disposables.clear()
        super.onDestroyView()
    }
}
