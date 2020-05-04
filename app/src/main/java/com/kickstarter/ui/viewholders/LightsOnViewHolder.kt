package com.kickstarter.ui.viewholders

import android.view.View
import androidx.core.content.ContextCompat
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ui.adapters.DiscoveryAdapter
import com.kickstarter.ui.data.Editorial
import com.kickstarter.viewmodels.EditorialViewHolderViewModel
import kotlinx.android.synthetic.main.item_lights_on.view.*
import rx.Observable

class LightsOnViewHolder(val view:View, val delegate: DiscoveryAdapter.Delegate): KSViewHolder(view) {
    interface Delegate {
        fun editorialViewHolderClicked(editorial: Editorial)
    }

    private val vm: EditorialViewHolderViewModel.ViewModel = EditorialViewHolderViewModel.ViewModel(environment())

    init {

        this.vm.outputs.ctaTitle()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.itemView.title.setText(it) }

        this.vm.outputs.ctaDescription()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.itemView.description.setText(it) }

        this.vm.graphic()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.itemView.item_background.background = ContextCompat.getDrawable(context(),it) }
    }

    override fun bindData(data: Any?) {
        this.vm.inputs.configureWith(data as Editorial)
    }
}