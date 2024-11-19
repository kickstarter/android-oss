package com.kickstarter.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.BuildConfig
import com.kickstarter.libs.utils.ExceptionUtils
import com.kickstarter.ui.viewholders.KSViewHolder
import java.util.ArrayList

abstract class KSListAdapter(
    diffUtil: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any) = oldItem == newItem

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any) = oldItem == newItem

        override fun getChangePayload(oldItem: Any, newItem: Any) = false
    }
) : ListAdapter<Any, KSViewHolder>(diffUtil) {
    private val sections = ArrayList<List<Any>>()

    fun sections(): List<List<Any>> {
        return this.sections
    }

    fun clearSections() {
        this.sections.clear()
    }

    fun <T> addSection(section: List<T>) {
        this.sections.add(ArrayList<Any>(section))
    }

    fun <T> addSections(sections: List<List<T>>) {
        for (section in sections) {
            addSection(section)
        }
    }

    protected fun items(): MutableList<Any> {
        val items = ArrayList<Any>()
        for (section in sections) {
            items.addAll(section)
        }

        return items
    }

    fun <T> setSection(location: Int, section: List<T>) {
        this.sections[location] = ArrayList<Any>(section)
    }

    fun <T> insertSection(location: Int, section: List<T>) {
        this.sections.add(location, ArrayList<Any>(section))
    }

    /**
     * Fetch the layout id associated with a sectionRow.
     */
    protected abstract fun layout(sectionRow: SectionRow?): Int

    /**
     * Returns a new KSViewHolder given a layout and view.
     */
    protected abstract fun viewHolder(@LayoutRes layout: Int, view: ViewGroup): KSViewHolder

    override fun onViewDetachedFromWindow(holder: KSViewHolder) {
        super.onViewDetachedFromWindow(holder)

        if (!hasObservers()) {
            holder.destroy()
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, @LayoutRes layout: Int): KSViewHolder {
        val viewHolder = viewHolder(layout, viewGroup)
        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: KSViewHolder, position: Int) {
        val data = objectFromPosition(position)

        try {
            data?.let {
                viewHolder.bindData(data)
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                ExceptionUtils.rethrowAsRuntimeException(e)
            } else {
                // TODO: alter the exception message to say we are just reporting it and it's not a real crash.
                FirebaseCrashlytics.getInstance().setCustomKey("KSListAdapter", this::class.java.simpleName)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return layout(sectionRowFromPosition(position))
    }

    override fun getItemCount(): Int {
        return items().size
    }

    /**
     * Gets the data object associated with a sectionRow.
     */
    protected fun objectFromSectionRow(sectionRow: SectionRow): Any {
        return this.sections[sectionRow.section()][sectionRow.row()]
    }

    protected fun sectionCount(section: Int): Int {
        return if (section > sections().size - 1) {
            0
        } else sections()[section].size
    }

    /**
     * Gets the data object associated with a position.
     */
    protected fun objectFromPosition(position: Int): Any? {
        sectionRowFromPosition(position)?.let {
            return objectFromSectionRow(it)
        }
        return null
    }

    private fun sectionRowFromPosition(position: Int): SectionRow? {
        val sectionRow = SectionRow()
        var cursor = 0
        for (section in this.sections) {
            for (item in section) {
                if (cursor == position) {
                    return sectionRow
                }
                cursor++
                sectionRow.nextRow()
            }
            sectionRow.nextSection()
        }
        return null
    }

    private fun inflateView(viewGroup: ViewGroup, @LayoutRes viewType: Int): View {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        return layoutInflater.inflate(viewType, viewGroup, false)
    }

    /**
     * SectionRows allow RecyclerViews to be structured into sections of rows.
     */
    protected inner class SectionRow {
        private var section: Int = 0
        private var row: Int = 0

        constructor() {
            this.section = 0
            this.row = 0
        }

        constructor(section: Int, row: Int) {
            this.section = section
            this.row = row
        }

        fun section(): Int {
            return this.section
        }

        fun row(): Int {
            return this.row
        }

        fun nextRow() {
            this.row++
        }

        fun nextSection() {
            this.section++
            this.row = 0
        }
    }
}
