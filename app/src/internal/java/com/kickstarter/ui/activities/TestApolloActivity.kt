package com.kickstarter.ui.activities

import android.os.Bundle
import android.widget.Toast
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.ui.viewmodels.TestApolloViewModel
import kotlinx.android.synthetic.main.activity_test_apollo.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(TestApolloViewModel.ViewModel::class)
class TestApolloActivity : BaseActivity<TestApolloViewModel.ViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_apollo)

        make_graphql_call.text = "Make GraphQL call"
        make_graphql_call.setOnClickListener {
            this.viewModel.inputs.makeNetworkCallClicked()
        }

        make_graphql_call_with_errors.text = "Make GraphQL call with errors"
        make_graphql_call_with_errors.setOnClickListener {
            this.viewModel.inputs.makeNetworkCallWithErrorsClicked()
        }

        this.viewModel.outputs.email()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { email.text = it }

        this.viewModel.outputs.name()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { name.text = it }

        this.viewModel.errors.error()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    }
}
