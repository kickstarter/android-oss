package com.kickstarter.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.kickstarter.R
import com.kickstarter.extensions.onChange
import com.kickstarter.extensions.showSnackbar
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.viewmodels.NewCardFragmentViewModel
import com.stripe.android.view.CardInputListener
import kotlinx.android.synthetic.main.fragment_new_card.*

@RequiresFragmentViewModel(NewCardFragmentViewModel.ViewModel::class)
class NewCardFragment : BaseFragment<NewCardFragmentViewModel.ViewModel>() {
    interface OnCardSavedListener {
        fun cardSaved()
    }

    private var saveEnabled = false
    private var onCardSavedListener: OnCardSavedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_new_card, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(new_card_toolbar)
        setHasOptionsMenu(true)

        this.viewModel.outputs.allowedCardWarningIsVisible()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { ViewUtils.setGone(allowed_card_warning, !it) }

        this.viewModel.outputs.cardWidgetFocusDrawable()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { card_focus.setImageResource(it) }

        this.viewModel.outputs.progressBarIsVisible()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe {
                    ViewUtils.setGone(progress_bar, !it)
                    updateMenu(!it)
                }

        this.viewModel.outputs.saveButtonIsEnabled()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { updateMenu(it) }

        this.viewModel.outputs.success()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { onCardSavedListener?.cardSaved() }

        this.viewModel.outputs.error()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { showSnackbar(new_card_toolbar, it) }

        cardholder_name.onChange { this.viewModel.inputs.name(it) }
        postal_code.onChange { this.viewModel.inputs.postalCode(it) }
        addListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                this.viewModel.inputs.saveCardClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onCardSavedListener = context as? OnCardSavedListener
        if (onCardSavedListener == null) {
            throw ClassCastException("$context must implement OnArticleSelectedListener")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val save = menu.findItem(R.id.save)
        save.isEnabled = saveEnabled
    }

    private fun addListeners() {
        card_input_widget.clearFocus()
        cardholder_name.onFocusChangeListener = cardFocusChangeListener
        postal_code.onFocusChangeListener = cardFocusChangeListener
        card_input_widget.setCardNumberTextWatcher(cardNumberWatcher)
        card_input_widget.setCvcNumberTextWatcher(cardValidityWatcher)
        card_input_widget.setExpiryDateTextWatcher(cardValidityWatcher)
        card_input_widget.setCardInputListener(object : CardInputListener {
            override fun onFocusChange(focusField: String?) {
                this@NewCardFragment.viewModel.inputs.cardFocus(true)
            }

            override fun onPostalCodeComplete() {
            }

            override fun onCardComplete() {
                cardChanged()
            }

            override fun onExpirationComplete() {
                cardChanged()
            }

            override fun onCvcComplete() {
                cardChanged()
            }
        })
    }

    private fun cardChanged() {
        this.viewModel.inputs.card(card_input_widget.card)
    }

    private fun updateMenu(saveEnabled: Boolean) {
        this.saveEnabled = saveEnabled
        activity?.invalidateOptionsMenu()
    }

    private val cardValidityWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            cardChanged()
        }
    }

    private val cardNumberWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            this@NewCardFragment.viewModel.inputs.cardNumber(s?.toString() ?: "")
        }
    }

    private val cardFocusChangeListener = View.OnFocusChangeListener { _, _ -> this@NewCardFragment.viewModel.inputs.cardFocus(false) }
}
