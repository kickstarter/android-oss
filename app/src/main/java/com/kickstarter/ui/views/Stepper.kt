package com.kickstarter.ui.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.kickstarter.databinding.StepperUiBinding
import rx.Observable
import rx.subjects.PublishSubject

class Stepper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: StepperUiBinding = StepperUiBinding.inflate(LayoutInflater.from(context), this, true)

    interface Inputs {
        fun setVariance(const: Int)
        fun setMinimum(min: Int)
        fun setMaximum(max: Int)
        fun setInitialValue(firstVal: Int)
    }

    interface Outputs {
        fun display(): Observable<Int>
    }

    private var variance: Int = 1
    private var minimum: Int = 0
    private var maximum: Int = 10

    /**
     * The initial loaded value need to be within min-max range or it will use the minimum instead
     */
    private var initialValue: Int = 0
        set(value) {
            field = if (value in minimum..maximum) {
                value
            } else minimum
            updateDisplayUI(value)
        }

    private var displayAmount = PublishSubject.create<Int>()

    val inputs: Inputs = object : Inputs {
        override fun setVariance(const: Int) {
            variance
        }

        override fun setMinimum(min: Int) {
            minimum = min
        }

        override fun setMaximum(max: Int) {
            maximum = max
        }

        override fun setInitialValue(firstVal: Int) {
            initialValue = firstVal
        }
    }

    val outputs: Outputs = object : Outputs {
        override fun display(): Observable<Int> = displayAmount
    }

    init {
        setListenerForDecreaseButton()
        setListenerForIncreaseButton()
        setListenerForAmountChanged()
    }

    /**
     * Check the amount is between the min-max range before updating the UI.
     * If the amount is in a valid range it updates the UI with the new value
     */
    private fun updateDisplayUI(amount: Int) {
        if (amount in minimum..maximum) {
            binding.quantityAddOn.text = amount.toString()
        }
    }

    private fun setListenerForIncreaseButton() {
        binding.increaseQuantityAddOn.setOnClickListener {
            updateDisplayUI(increase())
        }
    }

    private fun getDisplayInt() = binding.quantityAddOn.text.toString().toInt()

    private fun setListenerForDecreaseButton() {
        binding.decreaseQuantityAddOn.setOnClickListener {
            updateDisplayUI(decrease())
        }
    }

    private fun setListenerForAmountChanged() {
        binding.quantityAddOn.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                displayAmount.onNext(s.toString().toInt())
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun decrease() = getDisplayInt() - variance
    private fun increase() = getDisplayInt() + variance
}
