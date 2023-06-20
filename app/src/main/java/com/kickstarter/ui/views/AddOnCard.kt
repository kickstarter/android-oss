package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.databinding.AddOnCardBinding
import com.kickstarter.libs.utils.extensions.toVisibility
import com.kickstarter.ui.adapters.RewardItemsAdapter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AddOnCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private var binding = AddOnCardBinding.inflate(LayoutInflater.from(context), this, true)
    private var addButtonIsVisible = PublishSubject.create<Boolean>()

    interface Outputs {
        /**
         * Observable that will emmit the current value on the stepper display every time it changes.
         */
        fun stepperQuantity(): Observable<Int>

        /**
         * Observable that will emmit everytime the add button is clicked.
         */
        fun addButtonIsVisible(): Observable<Boolean>
    }

    init {
        obtainStyledAttributes(context, attrs, defStyleAttr)

        binding.addOnStepper.outputs.display()
            .filter { it != null }
            .subscribe {
                showStepper()
                addButtonIsVisible.onNext(false)
            }

        binding.addOnStepper.outputs.display()
            .filter { it == 0 }
            .subscribe {
                hideStepper()
                addButtonIsVisible.onNext(true)
            }

        binding.initialStateAddOn.setOnClickListener {
            binding.addOnStepper.inputs.setInitialValue(1)
        }
    }

    val outputs: Outputs = object : Outputs {
        override fun stepperQuantity(): Observable<Int> = binding.addOnStepper.outputs.display()
        override fun addButtonIsVisible(): Observable<Boolean> = addButtonIsVisible
    }

    private fun obtainStyledAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.AddOnCardComponent,
            defStyleAttr = defStyleAttr
        ) {
            getString(R.styleable.AddOnCardComponent_add_on_description_text)?.also {
                setAddOnDescription(it)
            }
            getBoolean(R.styleable.AddOnCardComponent_add_on_description_visibility, false).also {
                setAddonDescriptionVisibility(it)
            }
            getBoolean(R.styleable.AddOnCardComponent_add_on_items_container_visibility, false).also {
                setAddOnItemLayoutVisibility(it)
            }
            getBoolean(R.styleable.AddOnCardComponent_add_on_divider_visibility, false).also {
                setDividerVisibility(it)
            }
            getString(R.styleable.AddOnCardComponent_add_on_title_text)?.also {
                setAddOnTitleText(it)
            }
            getString(R.styleable.AddOnCardComponent_add_on_minimum_text)?.also {
                setAddOnMinimumText(it)
            }
            getBoolean(R.styleable.AddOnCardComponent_add_on_conversion_visibility, false).also {
                setAddonConversionVisibility(it)
            }
            getString(R.styleable.AddOnCardComponent_add_on_conversion_text)?.also {
                setAddonConversionText(it)
            }
            getBoolean(R.styleable.AddOnCardComponent_add_on_backer_limit_pill_visibility, false).also {
                setBackerLimitPillVisibility(it)
            }
            getString(R.styleable.AddOnCardComponent_add_on_backer_limit_pill_text)?.also {
                setBackerLimitText(it)
            }
            getBoolean(R.styleable.AddOnCardComponent_add_on_quantity_remaining_visibility, false).also {
                setAddonQuantityRemainingPillVisibility(it)
            }
            getString(R.styleable.AddOnCardComponent_add_on_quantity_remaining_text)?.also {
                setAddonQuantityRemainingText(it)
            }
            getString(R.styleable.AddOnCardComponent_add_on_backer_limit_pill_text)?.also {
                setBackerLimitText(it)
            }
            getBoolean(R.styleable.AddOnCardComponent_add_on_time_left_visibility, false).also {
                setTimeLeftVisibility(it)
            }
            getString(R.styleable.AddOnCardComponent_add_on_time_left_text)?.also {
                setTimeLeftText(it)
            }
            getBoolean(R.styleable.AddOnCardComponent_add_on_shipping_amount_visibility, false).also {
                setShippingAmountVisibility(it)
            }
            getString(R.styleable.AddOnCardComponent_add_on_shipping_amount_text)?.also {
                setShippingAmountText(it)
            }
            getInt(R.styleable.AddOnCardComponent_add_on_stepper_initial_value, 0).also {
                setStepperInitialValue(it)
            }
            getInt(R.styleable.AddOnCardComponent_add_on_stepper_max, 10).also {
                setStepperMax(it)
            }
            getBoolean(R.styleable.AddOnCardComponent_add_on_local_pickup_is_gone, true).also {
                setLocalPickUpIsGone(it)
            }
        }
    }

    fun setAddOnDescription(description: String) {
        binding.addOnDescription.text = description
    }

    fun setAddonDescriptionVisibility(isVisible: Boolean) {
        binding.addOnDescription.visibility = isVisible.toVisibility()
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

    fun setAddOnMinimumText(minimum: String) {
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

    fun setBackerLimitText(backerLimit: String) {
        binding.addonBackerLimit.setAddOnTagText(backerLimit)
    }

    fun setAddonQuantityRemainingPillVisibility(isVisible: Boolean) {
        binding.addonQuantityRemaining.visibility = isVisible.toVisibility()
    }

    fun setAddonQuantityRemainingText(quantityRemaining: String) {
        binding.addonQuantityRemaining.setAddOnTagText(quantityRemaining)
    }

    fun setTimeLeftVisibility(isVisible: Boolean) {
        binding.addonTimeLeft.visibility = isVisible.toVisibility()
    }

    fun setTimeLeftText(timeLeft: String) {
        binding.addonTimeLeft.setAddOnTagText(timeLeft)
    }

    fun setShippingAmountVisibility(isVisible: Boolean) {
        binding.addOnShippingAmount.visibility = isVisible.toVisibility()
    }

    fun setShippingAmountText(shippingAmount: String) {
        binding.addOnShippingAmount.text = shippingAmount
    }

    fun setLocalPickUpName(localPickupName: String) {
        binding.localPickupContainer.localPickupLocation.text = localPickupName
    }

    fun setLocalPickUpIsGone(isGone: Boolean) {
        binding.localPickupContainer.localPickupGroup.isGone = isGone
    }

    private fun hideStepper() {
        binding.initialStateAddOn.visibility = View.VISIBLE
        binding.addOnStepper.visibility = View.GONE
        binding.initialStateAddOn.isEnabled = true
        binding.addOnStepper.isEnabled = false
    }

    private fun showStepper() {
        binding.initialStateAddOn.visibility = View.GONE
        binding.addOnStepper.visibility = View.VISIBLE
        binding.initialStateAddOn.isEnabled = false
        binding.addOnStepper.isEnabled = true
    }

    fun stepperDisplay(): Observable<Int> {
        return binding.addOnStepper.outputs.display()
    }

    fun setStepperMax(quantity: Int) {
        binding.addOnStepper.inputs.setMaximum(quantity)
    }

    fun setStepperInitialValue(quantity: Int) {
        binding.addOnStepper.inputs.setInitialValue(quantity)
    }

    fun setUpItemsAdapter(rewardItemsAdapter: RewardItemsAdapter, layoutManager: RecyclerView.LayoutManager) {
        binding.itemsContainer.addOnItemRecyclerView.apply {
            adapter = rewardItemsAdapter
            this.layoutManager = layoutManager
        }
    }
}
