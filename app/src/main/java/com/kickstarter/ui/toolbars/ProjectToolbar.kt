package com.kickstarter.ui.toolbars

import android.content.Context
import android.util.AttributeSet
import androidx.activity.ComponentActivity
import com.kickstarter.R
import com.kickstarter.ui.views.IconButton

class ProjectToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : KSToolbar(context, attrs, defStyleAttr) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<IconButton>(R.id.back_icon)?.setOnClickListener {
            backIconClick()
        }
    }

    private fun backIconClick() {
        (context as ComponentActivity).onBackPressedDispatcher.onBackPressed()
    }
}
