package com.kickstarter.ui.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.kickstarter.databinding.StepperBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class Stepper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: StepperBinding = StepperBinding.inflate(LayoutInflater.from(context), this, true)

    interface Inputs {
        /**
         * Increment value to add/substract, by default 1:
         * Ie: if this value is set by 2, the increment/decrement showed on the display will change by 2
         */
        fun setVariance(const: Int)

        /**
         * Min value allowed by the stepper, by default 0, when that value reached the decrease button will be disabled
         */
        fun setMinimum(min: Int)

        /**
         * Max value allowed by the stepper, by default 10, when that value reached the increase button will be disabled
         */
        fun setMaximum(max: Int)

        /**
         * The initial value to be displayed, by default 0. The value need to be within min-max range or it will use the minimum instead
         */
        fun setInitialValue(firstVal: Int)
    }

    interface Outputs {
        /**
         * Observable that will emmit every time the displays changes with the value present on the display
         */
        fun display(): Observable<Int>
    }

    private var variance: Int = 1
    private var minimum: Int = 0
    private var maximum: Int = 10
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
            variance = const
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
        updateDisplayUI(initialValue)
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
            binding.stepperDisplay.text = amount.toString()
        }

        // - update enabled state for the buttons everytime the display changes
        binding.decreaseQuantity.isEnabled = amount > minimum
        binding.increaseQuantity.isEnabled = amount < maximum
    }

    private fun setListenerForIncreaseButton() {
        binding.increaseQuantity.setOnClickListener {
            updateDisplayUI(increase())
        }
    }

    private fun getDisplayInt() = binding.stepperDisplay.text.toString().toInt()

    private fun setListenerForDecreaseButton() {
        binding.decreaseQuantity.setOnClickListener {
            updateDisplayUI(decrease())
        }
    }

    private fun setListenerForAmountChanged() {
        binding.stepperDisplay.addTextChangedListener(object : TextWatcher {
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
