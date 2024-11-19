package com.kickstarter.ui.viewholders

import android.content.Context
import android.view.View
import com.kickstarter.KSApplication
import com.kickstarter.libs.Environment

abstract class KSArrayViewHolder(private val view: View) {
    abstract fun bindData(any: Any?)

    /**
     * Called when the ViewHolder is being detached. Subclasses should override if they need to do any work
     * when the ViewHolder is being de-allocated.
     */
    protected open fun destroy() {}

    protected fun view(): View {
        return this.view
    }

    protected fun context(): Context {
        return this.view.context
    }

    protected fun environment(): Environment {
        return (context().applicationContext as KSApplication).component().environment()
    }
}
