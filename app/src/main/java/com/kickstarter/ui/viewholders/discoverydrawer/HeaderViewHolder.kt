package com.kickstarter.ui.viewholders.discoverydrawer

import android.view.View
import androidx.annotation.NonNull
import com.kickstarter.libs.utils.ObjectUtils.requireNonNull
import com.kickstarter.ui.viewholders.KSViewHolder
import kotlinx.android.synthetic.main.discovery_drawer_header.view.*


class HeaderViewHolder(@NonNull view: View) : KSViewHolder(view) {
    private var item: Int = 0

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        this.item = requireNonNull<Int>(data as Int, Int::class.java)
        this.itemView.discovery_drawer_header_title.setText(item)
    }
}

