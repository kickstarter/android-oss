package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.AddOnsCardBinding
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.ui.adapters.RewardItemsAdapter
import kotlinx.android.synthetic.main.add_on_items.view.*
import rx.Observable
import java.util.*

class AddOnCardComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private var binding = AddOnsCardBinding.inflate(LayoutInflater.from(context), this, true)
    private var listener : AddonCardListener? = null

    init {
        obtainStyledAttributes(context, attrs, defStyleAttr)

        binding.addOnStepper.outputs.display()
            .filter { it != null }
            .subscribe{
                showStepper()
            }

        binding.addOnStepper.outputs.display()
            .filter { it == 0 }
            .subscribe {
                hideStepper()
            }

        binding.initialStateAddOn.setOnClickListener {
            listener?.addButtonClicks()
        }

//        binding.addOnStepper.outputs.display()
//            .subscribe {listener?.displayChanges(it)}
    }

    private fun obtainStyledAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.AddOnCardComponent,
            defStyleAttr = defStyleAttr
        ) {
//            getString(R.styleable.AddOnCardComponent_add_on_tag_text)?.also {
//                setAddOnTagText(it)
//            }
        }
    }

    fun setAddOnDescription(description: String) {
        binding.addOnDescription.text = description
    }

    fun setAddOnItemLayoutVisibility(isVisible: Boolean) {
        binding.itemsContainer.addOnItemLayout.visibility = isVisible.toVisibility()
    }

    fun setDividerVisibility(isVisible: Boolean) {
        binding.divider.visibility = isVisible.toVisibility()
    }

    fun setAddOnTitleText(title: String) {
        binding.titleContainer.addOnTitleTextView.text = title
    }

    fun setAddOnMinimum(minimum : String) {
        binding.addOnMinimum.text = minimum
    }

    fun setAddonConversionVisibility(isVisible: Boolean) {
        binding.addOnConversion.visibility = isVisible.toVisibility()
    }

    fun setAddonConversionText(conversion: String) {
        binding.addOnConversion.text = conversion
    }

    fun setBackerLimitPillVisibility(isVisible: Boolean) {
        binding.addonBackerLimit.visibility = isVisible.toVisibility()
    }

    fun setAddonQuantityRemainingPillVisibility(isVisible: Boolean) {
        binding.addonQuantityRemaining.visibility = isVisible.toVisibility()
    }

    fun setBackerLimitText(backerLimit: String) {
        binding.addonBackerLimit.setAddOnTagText(backerLimit)
    }

    fun setAddonQuantityRemainingText(quantityRemaining: String) {
        binding.addonQuantityRemaining.setAddOnTagText(quantityRemaining)
    }

    fun setTimeLeftVisibility(isVisible: Boolean) {
        binding.addonTimeLeft.visibility = isVisible.toVisibility()
    }

    fun setAddonTimeLeftText(timeLeft: String) {
        binding.addonTimeLeft.setAddOnTagText(timeLeft)
    }

    fun setShippingAmountVisibility(isVisible: Boolean) {
        binding.addOnShippingAmount.visibility = isVisible.toVisibility()
    }

    fun setAddonShippingAmountText(shippingAmount: String) {
        binding.addOnShippingAmount.text = shippingAmount
    }

//    fun setDecreaseQuantityAddonEnabled(isEnabled: Boolean) {
//        binding.decreaseQuantityAddOn.isEnabled = (isEnabled)
//    }
//
//    fun setQuantityAddonText(quantity: String) {
//        binding.quantityAddOn.text = quantity
//    }
//
//    fun setIncreaseQuantityAddonEnabled(isEnabled: Boolean) {
//        binding.increaseQuantityAddOn.isEnabled = isEnabled
//    }

    fun hideStepper() {
        binding.initialStateAddOn.visibility = View.VISIBLE
        binding.addOnStepper.visibility = View.GONE
        binding.initialStateAddOn.isEnabled = true
        binding.addOnStepper.isEnabled = false
    }

    fun showStepper() {
        binding.initialStateAddOn.visibility = View.GONE
        binding.addOnStepper.visibility = View.VISIBLE
        binding.initialStateAddOn.isEnabled = false
        binding.addOnStepper.isEnabled = true
    }

    fun getAddButtonClickListener(): Observable<Void> {
        return RxView.clicks(binding.initialStateAddOn)
    }

    fun stepperDisplay(): Observable<Int> {
        return binding.addOnStepper.outputs.display()
    }

    fun setStepperVariance(quantity: Int) {
        binding.addOnStepper.inputs.setVariance(quantity)
    }
    fun setStepperMax(quantity: Int) {
        binding.addOnStepper.inputs.setMaximum(quantity)
    }
    fun setStepperMin(quantity: Int) {
        binding.addOnStepper.inputs.setMinimum(quantity)
    }

    fun setStepperInitialValue(quantity: Int) {
        binding.addOnStepper.inputs.setInitialValue(quantity)
    }

    fun addbuttonpressed() {
        binding.addOnStepper.inputs.setInitialValue(1)
    }

    fun setAddonCardListener(addonCardListener: AddonCardListener?) {
        this.listener = addonCardListener
    }

//    fun getIncreaseQuantityClickListener(): rx.Observable<Void> {
//        return RxView.clicks(binding.increaseQuantityAddOn)
//    }
//    fun getDecreaseQuantityClickListener(): rx.Observable<Void> {
//        return RxView.clicks(binding.decreaseQuantityAddOn)
//    }

    fun setUpItemsAdapter(rewardItemsAdapter: RewardItemsAdapter, layoutManager: RecyclerView.LayoutManager) {
        binding.addOnCard.add_on_item_recycler_view.apply {
            adapter = rewardItemsAdapter
        }
        binding.addOnCard.add_on_item_recycler_view.layoutManager = layoutManager
    }

    interface AddonCardListener {
        fun addButtonClicks()
//        fun displayChanges(display: Int)
    }
}