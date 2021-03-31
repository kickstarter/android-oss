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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.FragmentBackingBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.Either
import com.kickstarter.libs.SwipeRefresher
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.libs.utils.extensions.setGone
import com.kickstarter.models.Reward
import com.kickstarter.ui.activities.BackingActivity
import com.kickstarter.ui.adapters.RewardAndAddOnsAdapter
import com.kickstarter.ui.data.PledgeStatusData
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.BackingFragmentViewModel
import com.squareup.picasso.Picasso
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers.io

@RequiresFragmentViewModel(BackingFragmentViewModel.ViewModel::class)
class BackingFragment : BaseFragment<BackingFragmentViewModel.ViewModel>() {

    private var rewardsAndAddOnsAdapter = RewardAndAddOnsAdapter()

    private var binding: FragmentBackingBinding? = null

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

        this.viewModel.outputs.backerAvatar()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setBackerImageView(it) }

        this.viewModel.outputs.backerName()
            .observeOn(io())
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { binding?.backerName?.text = it }

        this.viewModel.outputs.backerNumber()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setBackerNumberText(it) }

        this.viewModel.outputs.cardLogo()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding?.rewardCardDetails?.rewardCardLogo?.setImageResource(it) }

        this.viewModel.outputs.cardExpiration()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setCardExpirationText(it) }

        this.viewModel.outputs.cardIssuer()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setCardIssuerContentDescription(it) }

        this.viewModel.outputs.cardLastFour()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setCardLastFourText(it) }

        this.viewModel.outputs.fixPaymentMethodButtonIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.fixPaymentMethodButton?.setGone(it)
            }

        this.viewModel.outputs.fixPaymentMethodButtonIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.fixPaymentMethodMessage?.setGone(it)
            }

        this.viewModel.outputs.notifyDelegateToRefreshProject()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { (activity as BackingDelegate?)?.refreshProject() }

        this.viewModel.outputs.notifyDelegateToShowFixPledge()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { (activity as BackingDelegate?)?.showFixPaymentMethod() }

        this.viewModel.outputs.paymentMethodIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.paymentMethod?.setGone(it)
            }

        this.viewModel.outputs.pledgeAmount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding?.fragmentPledgeSectionSummaryPledge?.pledgeSummaryAmount?.text = it }

        this.viewModel.outputs.pledgeDate()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setPledgeDateText(it) }

        this.viewModel.outputs.pledgeStatusData()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setPledgeStatusText(it) }

        this.viewModel.outputs.pledgeSummaryIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding?.fragmentPledgeSectionSummaryPledge?.pledgeSummary?.setGone(it) }

        this.viewModel.outputs.projectDataAndReward()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                bindDataToRewardViewHolder(it)
            }

        this.viewModel.outputs.receivedCheckboxChecked()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding?.receivedSectionLayout?.estimatedDeliveryCheckbox?.isChecked = it }

        this.viewModel.outputs.receivedSectionIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.receivedSectionLayout?.root?.setGone(it)
            }

        this.viewModel.outputs.receivedSectionCreatorIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.estimatedDeliveryLabel2?.setGone(it)
            }

        this.viewModel.outputs.shippingAmount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding?.fragmentPledgeSectionSummaryShipping?.shippingSummaryAmount?.text = it }

        this.viewModel.outputs.shippingLocation()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding?.fragmentPledgeSectionSummaryShipping?.shippingLabel?.text = String.format("%s: %s", getString(R.string.Shipping), it) }

        this.viewModel.outputs.shippingSummaryIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding?.fragmentPledgeSectionSummaryShipping?.shippingSummary?.setGone(it) }

        this.viewModel.outputs.showUpdatePledgeSuccess()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { showSnackbar(view, getString(R.string.Got_it_your_changes_have_been_saved)) }

        this.viewModel.outputs.totalAmount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding?.fragmentBackingSectionSummaryTotal?.totalSummaryAmount?.text = it }

        this.viewModel.outputs.projectDataAndAddOns()
            .filter { ObjectUtils.isNotNull(it) }
            .distinctUntilChanged()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { populateAddOns(it) }

        this.viewModel.outputs.bonusSupport()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding?.sectionBonusSupport?.bonusSummaryAmount?.text = it }

        this.viewModel.outputs.estimatedDelivery()
            .distinctUntilChanged()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                stylizedTextViews(it)
            }

        this.viewModel.outputs.deliveryDisclaimerSectionIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe {
                binding?.pledgeDetailsLabel?.text = getString(R.string.Pledge_details)
                binding?.receivedSectionLayout?.root?.setGone(true)
                binding?.deliveryDisclaimerSection?.root?.setGone(it)
                binding?.estimatedDeliveryLabel2?.setGone(true)
            }

        binding?.deliveryDisclaimerSection?.deliveryReminderLabel?.apply {
            val sb = StringBuilder(text.toString())
            sb.append(" " + resources.getString(R.string.Delays_or_changes_are_possible))
            text = sb.toString()

            val boldPortionLength = text.toString().split(".").first().length
            setBoldSpanOnTextView(boldPortionLength, this, resources.getColor(R.color.kds_support_400, null))
        }

        binding?.backingSwipeRefreshLayout?.let {
            SwipeRefresher(
                this, it, { this.viewModel.inputs.refreshProject() }, { this.viewModel.outputs.swipeRefresherProgressIsVisible() }
            )
        }

        binding?.fixPaymentMethodButton?.let {
            RxView.clicks(it)
                .compose(bindToLifecycle())
                .subscribe { this.viewModel.inputs.fixPaymentMethodButtonClicked() }
        }

        binding?.receivedSectionLayout?.estimatedDeliveryCheckbox?.apply {
            RxView.clicks(this)
                .compose(bindToLifecycle())
                .subscribe { viewModel.inputs.receivedCheckboxToggled(this.isChecked) }
        }
    }

    override fun onStart() {
        super.onStart()
        this.setState(activity is BackingActivity)
    }

    private fun stylizedTextViews(it: String) {
        binding?.receivedSectionLayout?.estimatedDeliveryLabel?.apply {
            val totalCharacters = text.length
            text = viewModel.ksString.format(
                getString(R.string.estimated_delivery_data),
                "title", text.toString(),
                "estimated_delivery_data", it
            )
            setBoldSpanOnTextView(totalCharacters, this, resources.getColor(R.color.kds_support_400, null))
        }

        binding?.estimatedDeliveryLabel2?.apply {
            text = viewModel.ksString.format(
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

    fun pageViewed() = this.viewModel.pageViewed()

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
        context?.apply {
            Picasso.get().load(url)
                .transform(CircleTransformation())
                .into(binding?.backingAvatar)
        }
    }

    private fun setBackerNumberText(it: String?) {
        binding?.backerNumber?.text = this.viewModel.ksString.format(getString(R.string.backer_modal_backer_number), "backer_number", it)
    }

    private fun setCardExpirationText(expiration: String) {
        binding?.rewardCardDetails?.rewardCardExpirationDate?.text = this.viewModel.ksString.format(
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
        binding?.rewardCardDetails?.rewardCardLastFour?.text = this.viewModel.ksString.format(
            getString(R.string.payment_method_last_four),
            "last_four",
            lastFour
        )
    }

    private fun setPledgeDateText(pledgeDate: String) {
        binding?.backingDate?.text = this.viewModel.ksString.format(getString(R.string.As_of_pledge_date), "pledge_date", pledgeDate)
    }

    private fun setPledgeStatusText(pledgeStatusData: PledgeStatusData) {
        val ksString = this.viewModel.ksString
        val pledgeStatusText = pledgeStatusData.statusStringRes?.let {
            when (pledgeStatusData.statusStringRes) {
                R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline -> {
                    ksString.format(
                        getString(it),
                        "total", pledgeStatusData.pledgeTotal,
                        "project_deadline", pledgeStatusData.projectDeadline
                    )
                }

                R.string.If_your_project_reaches_its_funding_goal_the_backer_will_be_charged_total_on_project_deadline -> {
                    ksString.format(
                        getString(it),
                        "total", pledgeStatusData.pledgeTotal,
                        "project_deadline", pledgeStatusData.projectDeadline
                    )
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

    private fun setupRecyclerView() {
        binding?.rewardAddOnRecycler?.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding?.rewardAddOnRecycler?.adapter = rewardsAndAddOnsAdapter
    }

    override fun onDetach() {
        super.onDetach()
        binding?.rewardAddOnRecycler?.adapter = null
        this.viewModel = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
