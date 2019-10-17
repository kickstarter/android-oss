package com.kickstarter.ui.viewholders

import android.view.View
import androidx.core.content.ContextCompat
import com.kickstarter.R
import kotlinx.android.synthetic.main.item_feature_flag.view.*

class FeatureFlagViewHolder(val view: View) : KSViewHolder(view) {


    override fun bindData(data: Any?) {
        @Suppress("UNCHECKED_CAST")
        val flag = data as Map.Entry<String, Boolean>

        this.view.flag_name.text = flag.key
        this.view.flag_value.text = flag.value.toString()
        val color = ContextCompat.getColor(context(), if (flag.value) R.color.text_secondary else R.color.text_primary)
        this.view.flag_value.setTextColor(color)
    }
}