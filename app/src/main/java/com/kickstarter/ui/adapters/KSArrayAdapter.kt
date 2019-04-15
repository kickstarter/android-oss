package com.kickstarter.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

abstract class KSArrayAdapter<T> (val ctx: Context, private val resourceId: Int, val items: ArrayList<T> = arrayListOf()): ArrayAdapter<T>(ctx, resourceId ,items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return convertView?: LayoutInflater.from(context).inflate(resourceId, parent, false)
    }
}