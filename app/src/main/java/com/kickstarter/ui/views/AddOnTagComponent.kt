package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.kickstarter.R
import com.kickstarter.databinding.AddOnTagBinding

class AddOnTagComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding = AddOnTagBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        obtainStyledAttributes(context, attrs, defStyleAttr)
    }

    private fun obtainStyledAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.AddOnTagComponent,
            defStyleAttr = defStyleAttr
        ) {
            getString(R.styleable.AddOnTagComponent_add_on_tag_text)?.also {
                setAddOnTagText(it)
            }
        }
    }

    fun setAddOnTagText(addOnTagText: String) {
        binding.addonTagTv.text = addOnTagText
    }
}
