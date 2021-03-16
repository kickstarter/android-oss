package com.kickstarter.ui.toolbars

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.jakewharton.rxbinding.widget.RxTextView
import com.kickstarter.R
import com.kickstarter.ui.activities.SearchActivity
import com.kickstarter.ui.views.IconButton
import rx.android.schedulers.AndroidSchedulers

class SearchToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : KSToolbar(context, attrs, defStyleAttr) {

    private val clearButton by lazy { findViewById<IconButton>(R.id.clear_button) }
    private val searchEditText by lazy { findViewById<EditText>(R.id.search_edit_text) }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isInEditMode) {
            return
        }

        clearButton.setOnClickListener {
            clearButtonClick()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val text = RxTextView.textChanges(searchEditText)
        val clearable = text.map { it.isNotEmpty() }

        addSubscription(
            clearable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { clearButton.visibility = if (it) VISIBLE else INVISIBLE }
        )

        addSubscription(
            text
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { (context as SearchActivity).viewModel().inputs.search(it.toString()) }
        )
    }

    private fun clearButtonClick() {
        searchEditText.text = null
    }
}
