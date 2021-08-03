package com.kickstarter.ui.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
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

    // - inputs
    var minimum: Int = 0
    var maximum: Int = 10
    var initialValue: Int = 0
        set(value) {
            field = value
            updateAmountUI(value)
        }

    // - output
    private var displayAmount: Observable<Int> = Observable.just(initialValue) // initial value or minimum

    init {
        setListenerForDecreaseButton()
        setListenerForIncreaseButton()
        setListenerForAmountChanged()
    }

    private fun updateAmountUI(amount: Int) {
        binding.quantityAddOn.text = amount.toString()
    }

    private fun setListenerForIncreaseButton() {
        binding.increaseQuantityAddOn.setOnClickListener {
            if (getDisplayInt() <= maximum) {
                updateAmountUI(increase())
            }
        }
    }

    private fun getDisplayInt() = binding.quantityAddOn.text.toString().toInt()

    private fun setListenerForDecreaseButton() {
        binding.decreaseQuantityAddOn.setOnClickListener {
            if (getDisplayInt() >= minimum) {
                updateAmountUI(decrease())
            }
        }
    }

    private fun setListenerForAmountChanged() {
        binding.quantityAddOn.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                displayAmount = Observable.just(s.toString().toInt())
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

    private fun decrease() = getDisplayInt() - 1
    private fun increase() = getDisplayInt() + 1
}
