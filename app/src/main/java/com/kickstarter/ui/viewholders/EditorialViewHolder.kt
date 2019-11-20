package com.kickstarter.ui.viewholders

import android.view.View
import androidx.core.content.ContextCompat
import com.facebook.FacebookSdk.getApplicationContext
import com.kickstarter.R
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.ui.data.Editorial
import com.kickstarter.viewmodels.EditorialViewHolderViewModel
import kotlinx.android.synthetic.main.item_editorial.view.*

class EditorialViewHolder(val view: View, val delegate: Delegate) : KSViewHolder(view) {

    interface Delegate {
        fun editorialViewHolderClicked(viewHolder: EditorialViewHolder, tagId: Int)
    }

    private val vm: EditorialViewHolderViewModel.ViewModel = EditorialViewHolderViewModel.ViewModel(environment())

    init {

        this.vm.outputs.backgroundTint()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.itemView.backgroundTintList = ContextCompat.getColorStateList(getApplicationContext(), R.color.trust_700) }

        this.vm.outputs.ctaDescription()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.itemView.editorial_description.setText(it) }

        this.vm.outputs.ctaTitle()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.itemView.editorial_title.setText(it) }

        this.vm.outputs.graphic()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { this.itemView.editorial_graphic.setImageResource(it) }

        this.vm.outputs.tagId()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { delegate.editorialViewHolderClicked(this, it) }

        this.itemView.setOnClickListener { this.vm.inputs.editorialClicked() }

    }

    override fun bindData(data: Any?) {
        this.vm.inputs.configureWith(data as Editorial)
    }
}