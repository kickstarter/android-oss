package com.kickstarter.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.isGone
import com.kickstarter.databinding.ActivityEditProfileBinding
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.models.User
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.EditProfileViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class EditProfileActivity : ComponentActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModelFactory: EditProfileViewModel.Factory
    private val viewModel: EditProfileViewModel.EditProfileViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getEnvironment()?.let { env ->
            viewModelFactory = EditProfileViewModel.Factory(env)
        }

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        setContentView(binding.root)
        setUpConnectivityStatusCheck(lifecycle)

        this.viewModel.outputs.userAvatarUrl()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { url ->
                binding.avatarImageView.loadCircleImage(url)
            }.addToDisposable(disposables)

        this.viewModel.outputs.user()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.displayPreferences(it) }
            .addToDisposable(disposables)

        this.viewModel.outputs.userName()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.nameEditText.setText(it, TextView.BufferType.EDITABLE) }
            .addToDisposable(disposables)

        this.viewModel.outputs.hidePrivateProfileRow()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.privateProfileRow.isGone = it
                binding.privateProfileTextView.isGone = it
                binding.publicProfileTextView.isGone = it
            }.addToDisposable(disposables)

        this.viewModel.unableToSavePreferenceError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSnackbar(binding.newSettingsLayout, it) }
            .addToDisposable(disposables)

        binding.privateProfileSwitch.setOnClickListener {
            this.viewModel.inputs.privateProfileChecked(binding.privateProfileSwitch.isChecked)
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun displayPreferences(user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(
            binding.privateProfileSwitch,
            user.showPublicProfile().isFalse()
        )
    }
}
