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
import io.reactivex.Observable
import kotlinx.android.synthetic.main.add_on_items.view.*
import java.util.*

class AddOnCardComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private var binding = AddOnsCardBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        obtainStyledAttributes(context, attrs, defStyleAttr)
    }

    private fun obtainStyledAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.AddOnCardComponent,
            defStyleAttr = defStyleAttr
        ) {
            getString(R.styleable.AddOnCardComponent_add_on_tag_text)?.also {
//                setAddOnTagText(it)
            }
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
        binding.addonBackerLimit.text = backerLimit
    }

    fun setAddonQuantityRemainingText(quantityRemaining: String) {
        binding.addonQuantityRemaining.text = quantityRemaining
    }

    fun setTimeLeftVisibility(isVisible: Boolean) {
        binding.addonTimeLeft.visibility = isVisible.toVisibility()
    }

    fun setAddonTimeLeftText(timeLeft: String) {
        binding.addonTimeLeft.text = timeLeft
    }

    fun setShippingAmountVisibility(isVisible: Boolean) {
        binding.addOnShippingAmount.visibility = isVisible.toVisibility()
    }

    fun setAddonShippingAmountText(shippingAmount: String) {
        binding.addOnShippingAmount.text = shippingAmount
    }

    fun setDecreaseQuantityAddonEnabled(isEnabled: Boolean) {
        binding.decreaseQuantityAddOn.isEnabled = (isEnabled)
    }

    fun setQuantityAddonText(quantity: String) {
        binding.quantityAddOn.text = quantity
    }

    fun setIncreaseQuantityAddonEnabled(isEnabled: Boolean) {
        binding.increaseQuantityAddOn.isEnabled = isEnabled
    }

    fun hideStepper() {
        binding.initialStateAddOn.visibility = View.VISIBLE
        binding.stepperContainerAddOn.visibility = View.GONE
        binding.initialStateAddOn.isEnabled = true
        binding.increaseQuantityAddOn.isEnabled = false
    }

    fun showStepper() {
        binding.initialStateAddOn.visibility = View.GONE
        binding.stepperContainerAddOn.visibility = View.VISIBLE
        binding.initialStateAddOn.isEnabled = false
        binding.increaseQuantityAddOn.isEnabled = true
    }

    fun getAddButtonClickListener(): rx.Observable<Void> {
        return RxView.clicks(binding.initialStateAddOn)
    }

    fun getIncreaseQuantityClickListener(): rx.Observable<Void> {
        return RxView.clicks(binding.increaseQuantityAddOn)
    }
    fun getDecreaseQuantityClickListener(): rx.Observable<Void> {
        return RxView.clicks(binding.decreaseQuantityAddOn)
    }

    fun setUpItemsAdapter(rewardItemsAdapter: RewardItemsAdapter, layoutManager: RecyclerView.LayoutManager) {
        binding.addOnCard.add_on_item_recycler_view.apply {
            adapter = rewardItemsAdapter
        }
        binding.addOnCard.add_on_item_recycler_view.layoutManager = layoutManager
    }
}