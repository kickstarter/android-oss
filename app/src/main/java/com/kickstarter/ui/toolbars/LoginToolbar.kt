package com.kickstarter.ui.toolbars

import android.content.Context
import android.util.AttributeSet
import android.view.View
import butterknife.ButterKnife
import com.kickstarter.R
import com.kickstarter.ui.views.LoginPopupMenu

class LoginToolbar @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : KSToolbar(context, attrs, defStyleAttr) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        ButterKnife.bind(this)
        findViewById<View>(R.id.help_button).setOnClickListener {
            helpButtonClick(it)
        }
    }
    
    fun helpButtonClick(view: View) {
        LoginPopupMenu(context, view).show()
    }
}
