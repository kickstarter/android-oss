package com.kickstarter.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import com.kickstarter.ui.viewholders.KSArrayViewHolder

abstract class KSArrayAdapter<T>(val ctx: Context, private val resourceId: Int, items: ArrayList<T> = arrayListOf()) : ArrayAdapter<T>(ctx, resourceId, items){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resourceId, parent, false)
        val viewHolder = viewHolder(resourceId, view)
        viewHolder.bindData(getItem(position))
        return view
    }

    /**
     * Returns a new KSViewHolder given a layout and view.
     */
    @NonNull
    protected abstract fun viewHolder(@LayoutRes layout: Int, @NonNull view: View): KSArrayViewHolder

}