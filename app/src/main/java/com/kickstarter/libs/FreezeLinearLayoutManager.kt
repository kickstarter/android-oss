package com.kickstarter.libs

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class FreezeLinearLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) : LinearLayoutManager(context, orientation, reverseLayout) {

    private var frozen = false

    fun setFrozen(frozen : Boolean) {
        this.frozen = frozen
    }

    override fun canScrollHorizontally(): Boolean {
        return orientation == HORIZONTAL && !frozen
    }

    override fun canScrollVertically(): Boolean {
        return orientation == VERTICAL && !frozen
    }
}
