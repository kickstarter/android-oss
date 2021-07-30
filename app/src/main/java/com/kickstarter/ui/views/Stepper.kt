package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.kickstarter.databinding.StepperUiBinding
import rx.Observable

class Stepper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: StepperUiBinding = StepperUiBinding.inflate(LayoutInflater.from(context), this, true)
    private var amount: Int = 0

    val stepperListener = object : OnStepperUpdatedListener {
        override fun onStepperUpdated(): Observable<Int> {
            return Observable.just(amount)
        }
    }

    var minimum: Int = 0
    var maximum: Int = 10
    var initialValue: Int = 0
        set(value) {
            field = value
            amount = value
        }

    interface OnStepperUpdatedListener {
        fun onStepperUpdated(): Observable<Int>
    }

    init {
        updateAmountUI(amount)
        setListenerForDecreaseButton()
        setListenerForIncreaseButton()
    }

    private fun updateAmountUI(amount: Int) {
        binding.quantityAddOn.text = amount.toString()
    }

    private fun setListenerForIncreaseButton() {
        binding.increaseQuantityAddOn.setOnClickListener {
            updateAmountUI(increase())
            stepperListener.onStepperUpdated()
        }
    }

    private fun setListenerForDecreaseButton() {
        binding.decreaseQuantityAddOn.setOnClickListener {
            updateAmountUI(decrease())
            stepperListener.onStepperUpdated()
        }
    }

    private fun decrease() = amount - 1
    private fun increase() = amount + 1
}
