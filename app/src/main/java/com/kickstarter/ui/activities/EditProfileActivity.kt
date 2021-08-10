package com.kickstarter.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import com.kickstarter.databinding.ActivityEditProfileBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.models.User
import com.kickstarter.viewmodels.EditProfileViewModel
import com.squareup.picasso.Picasso
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(EditProfileViewModel.ViewModel::class)
class EditProfileActivity : BaseActivity<EditProfileViewModel.ViewModel>() {
    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)

        this.viewModel.outputs.userAvatarUrl()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { url ->
                Picasso.get().load(url).transform(CircleTransformation()).into(binding.avatarImageView)
            }

        this.viewModel.outputs.user()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { this.displayPreferences(it) }

        this.viewModel.outputs.userName()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.nameEditText.setText(it, TextView.BufferType.EDITABLE) }

        this.viewModel.outputs.hidePrivateProfileRow()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.privateProfileRow.isGone = it
                binding.privateProfileTextView.isGone = it
                binding.publicProfileTextView.isGone = it
            }
        binding.privateProfileSwitch.setOnClickListener {
            this.viewModel.inputs.showPublicProfile(binding.privateProfileSwitch.isChecked)
        }
    }

    private fun displayPreferences(user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(binding.privateProfileSwitch, BooleanUtils.isFalse(user.showPublicProfile()))
    }
}
