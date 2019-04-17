package com.kickstarter.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.NonNull
import com.crashlytics.android.Crashlytics
import com.kickstarter.ui.viewholders.KSArrayViewHolder

abstract class KSArrayAdapter<T> (val ctx: Context, private val resourceId: Int, val items: ArrayList<T> = arrayListOf()): ArrayAdapter<T>(ctx, resourceId ,items) {

    private var list = arrayListOf<Any>()
    private var listOfLists = ArrayList<List<Any>>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return convertView?: LayoutInflater.from(context).inflate(resourceId, parent, false)
    }

    fun populateData(viewHolder: KSArrayViewHolder, position: Int) {
        val dataObject = list[position]
        try {
            viewHolder.bindData(dataObject)
        }catch (exception: Exception) {
            Crashlytics.logException(exception)
        }
    }

    fun <T> addSection(@NonNull list: List<T>) {
        this.listOfLists.add(ArrayList<Any>(list))
    }


}