package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.core.content.withStyledAttributes
import com.kickstarter.R
import com.kickstarter.databinding.EnvironmentalCommitmentsCardBinding

class EnvironmentalCommitmentsCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private var binding: EnvironmentalCommitmentsCardBinding =
        EnvironmentalCommitmentsCardBinding.inflate(
            LayoutInflater.from(context),
            this, true
        )

    init {
        obtainStyledAttributes(context, attrs, defStyleAttr)
    }

    private fun obtainStyledAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.EnvironmentalCommitmentsCard,
            defStyleAttr = defStyleAttr
        ) {
            getString(R.styleable.EnvironmentalCommitmentsCard_category_title)?.also {
                setCategoryTitle(it)
            }
            getString(R.styleable.EnvironmentalCommitmentsCard_description)?.also {
                setDescription(it)
            }
        }
    }

    fun setCategoryTitle(categoryTitle: String) {
        binding.sectionHeader.text = categoryTitle
    }

    fun setCategoryTitle(@StringRes categoryTitle: Int) {
        binding.sectionHeader.setText(categoryTitle)
    }
    fun setDescription(description: String) {
        binding.sectionDescription.text = description
    }
}
