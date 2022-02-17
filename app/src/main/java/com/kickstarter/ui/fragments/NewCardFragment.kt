package com.kickstarter.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Pair
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.R
import com.kickstarter.databinding.FragmentNewCardBinding
import com.kickstarter.databinding.ModalFragmentNewCardBinding
import com.kickstarter.libs.BaseFragment
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.extensions.onChange
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.NewCardFragmentViewModel
import com.stripe.android.ApiResultCallback
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputListener
import rx.android.schedulers.AndroidSchedulers

@RequiresFragmentViewModel(NewCardFragmentViewModel.ViewModel::class)
class NewCardFragment : BaseFragment<NewCardFragmentViewModel.ViewModel>() {
    interface OnCardSavedListener {
        fun cardSaved(storedCard: StoredCard)
    }

    private var saveEnabled = false
    private var onCardSavedListener: OnCardSavedListener? = null
    private var fragmentNewCardBinding: FragmentNewCardBinding? = null
    private var modalFragmentNewCardBinding: ModalFragmentNewCardBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        if (modal()) {
            modalFragmentNewCardBinding = ModalFragmentNewCardBinding.inflate(inflater, container, false)
            fragmentNewCardBinding = modalFragmentNewCardBinding?.fragmentNewCard
            return modalFragmentNewCardBinding?.root
        }
        fragmentNewCardBinding = FragmentNewCardBinding.inflate(inflater, container, false)
        return fragmentNewCardBinding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(fragmentNewCardBinding?.formNewCardLayout?.newCardToolbar)
        setHasOptionsMenu(true)

        this.viewModel.outputs.allowedCardWarning()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { setAllowedCardWarning(it) }

        this.viewModel.outputs.allowedCardWarningIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                fragmentNewCardBinding?.formNewCardLayout?.allowedCardWarning?.isGone = !it
            }

        this.viewModel.outputs.cardWidgetFocusDrawable()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { fragmentNewCardBinding?.formNewCardLayout?.cardFocus?.setImageResource(it) }

        this.viewModel.outputs.progressBarIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .filter { this.activity != null && isVisible }
            .subscribe {
                fragmentNewCardBinding?.formNewCardLayout?.progressBar?.isGone = !it
                updateMenu(!it)
            }

        this.viewModel.outputs.saveButtonIsEnabled()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { updateMenu(it) }

        this.viewModel.outputs.success()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.onCardSavedListener?.cardSaved(it) }

        this.viewModel.outputs.error()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                fragmentNewCardBinding?.formNewCardLayout?.newCardToolbar?.let {
                    showSnackbar(it, getString(R.string.Something_went_wrong_please_try_again))
                }
            }

        this.viewModel.outputs.modalError()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                modalFragmentNewCardBinding?.modalNewCardSnackbarAnchor?.let { modalNewCardSnackbarAnchor ->
                    showSnackbar(
                        modalNewCardSnackbarAnchor, getString(R.string.Something_went_wrong_please_try_again)
                    )
                }
            }

        this.viewModel.outputs.reusableContainerIsVisible()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                fragmentNewCardBinding?.formNewCardLayout?.reusableContainer ?.isGone = !it
            }

        this.viewModel.outputs.appBarLayoutHasElevation()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                if (!it)
                    fragmentNewCardBinding?.formNewCardLayout?.newCardAppBarLayout ?.stateListAnimator = null
            }

        this.viewModel.outputs.dividerIsVisible()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { fragmentNewCardBinding?.formNewCardLayout?.formContainer ?.showDividers = if (it) LinearLayout.SHOW_DIVIDER_END else LinearLayout.SHOW_DIVIDER_NONE }

        this.viewModel.outputs.createStripeToken()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { createStripeToken(it) }

        fragmentNewCardBinding?.formNewCardLayout?.reusableSwitch?.let { reusableSwitch ->
            RxView.clicks(reusableSwitch)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.viewModel.inputs.reusable(reusableSwitch.isChecked) }
        }

        fragmentNewCardBinding?.formNewCardLayout?.cardholderName?.onChange { this.viewModel.inputs.name(it) }
        fragmentNewCardBinding?.formNewCardLayout?.postalCode?.onChange { this.viewModel.inputs.postalCode(it) }
        addListeners()
        fragmentNewCardBinding?.formNewCardLayout?.cardholderName?.requestFocus()
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
        this.onCardSavedListener = context as? OnCardSavedListener
        if (this.onCardSavedListener == null) {
            throw ClassCastException("$context must implement OnCardSavedListener")
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
        fragmentNewCardBinding?.formNewCardLayout?.cardInputWidget?.clearFocus()
        fragmentNewCardBinding?.formNewCardLayout?.cardholderName?.onFocusChangeListener = cardFocusChangeListener
        fragmentNewCardBinding?.formNewCardLayout?.postalCode?.onFocusChangeListener = cardFocusChangeListener
        fragmentNewCardBinding?.formNewCardLayout?.cardInputWidget?.postalCodeEnabled = false
        fragmentNewCardBinding?.formNewCardLayout?.cardInputWidget?.setCardNumberTextWatcher(cardNumberWatcher)
        fragmentNewCardBinding?.formNewCardLayout?.cardInputWidget?.setCvcNumberTextWatcher(cardValidityWatcher)
        fragmentNewCardBinding?.formNewCardLayout?.cardInputWidget?.setExpiryDateTextWatcher(cardValidityWatcher)
        fragmentNewCardBinding?.formNewCardLayout?.cardInputWidget?.setCardInputListener(object : CardInputListener {
            override fun onFocusChange(focusField: CardInputListener.FocusField) {
                this@NewCardFragment.viewModel.inputs.cardFocus(true)
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

            override fun onPostalCodeComplete() {
                cardChanged()
            }
        })
    }

    private fun cardChanged() {
        this.viewModel.inputs.card(fragmentNewCardBinding?.formNewCardLayout?.cardInputWidget?.cardParams)
    }

    private fun createStripeToken(card: CardParams) {
        this.viewModel.environment.stripe().createCardToken(
            cardParams = card,
            callback = object : ApiResultCallback<Token> {
                override fun onSuccess(result: Token) {
                    this@NewCardFragment.viewModel.inputs.stripeTokenResultSuccessful(result)
                }

                override fun onError(e: Exception) {
                    this@NewCardFragment.viewModel.inputs.stripeTokenResultUnsuccessful(e)
                }
            }
        )
    }

    private fun modal(): Boolean {
        return arguments?.getBoolean(ArgumentsKey.NEW_CARD_MODAL) ?: false
    }

    private fun setAllowedCardWarning(warningAndProject: Pair<Int?, Project?>) {
        val warning = warningAndProject.first
        val project = warningAndProject.second

        warning?.let {
            if (project == null) {
                fragmentNewCardBinding?.formNewCardLayout?.allowedCardWarning ?.setText(it)
            } else {
                val ksString = this.viewModel.environment.ksString()
                val country = project.location()?.expandedCountry()
                fragmentNewCardBinding?.formNewCardLayout?.allowedCardWarning ?.text = ksString.format(getString(it), "project_country", country)
            }
        }
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
            cardChanged()
        }
    }

    private val cardFocusChangeListener = View.OnFocusChangeListener { _, _ -> this@NewCardFragment.viewModel.inputs.cardFocus(false) }

    companion object {

        fun newInstance(modal: Boolean = false, project: Project): NewCardFragment {
            val fragment = NewCardFragment()
            val argument = Bundle()
            argument.putBoolean(ArgumentsKey.NEW_CARD_MODAL, modal)
            argument.putParcelable(ArgumentsKey.NEW_CARD_PROJECT, project)
            fragment.arguments = argument
            return fragment
        }
    }
}
