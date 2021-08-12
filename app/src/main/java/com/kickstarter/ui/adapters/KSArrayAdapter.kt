package com.kickstarter.ui.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.viewbinding.ViewBinding
import com.kickstarter.ui.viewholders.KSArrayViewHolder

abstract class KSArrayAdapter<T>(val ctx: Context, private val resourceId: Int, items: ArrayList<T> = arrayListOf()) : ArrayAdapter<T>(ctx, resourceId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewBinding = getViewBinding(resourceId, parent)
        val viewHolder = viewHolder(viewBinding)
        viewHolder?.bindData(getItem(position))
        return viewBinding.root
    }

    /**
     * Returns a new KSArrayViewHolder given a viewBinding.
     */
    @NonNull
    protected abstract fun viewHolder(@NonNull viewBinding: ViewBinding): KSArrayViewHolder?
    /**
     * Returns a new ViewBinding given a layout and viewGroup.
     */
    @NonNull
    protected abstract fun getViewBinding(@LayoutRes layout: Int, @NonNull viewGroup: ViewGroup): ViewBinding
}
