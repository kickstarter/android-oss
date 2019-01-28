package com.kickstarter.ui.activities

import android.os.Bundle
import android.util.Pair
import android.widget.TextView
import com.kickstarter.R
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.SwitchCompatUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.User
import com.kickstarter.viewmodels.EditProfileViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(EditProfileViewModel.ViewModel::class)
class EditProfileActivity : BaseActivity<EditProfileViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        this.viewModel.outputs.userAvatarUrl()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { url ->
                    Picasso.with(this).load(url).transform(CircleTransformation()).into(avatar_image_view)
                }

        this.viewModel.outputs.user()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { this.displayPreferences(it) }

        this.viewModel.outputs.userName()
                .compose(bindToLifecycle())
                .compose(Transformers.observeForUI())
                .subscribe { name_edit_text.setText(it, TextView.BufferType.EDITABLE) }

        this.viewModel.outputs.hidePrivateProfileRow()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    ViewUtils.setGone(private_profile_row, it)
                    ViewUtils.setGone(private_profile_text_view, it)
                    ViewUtils.setGone(public_profile_text_view, it)
                }

        private_profile_switch.setOnClickListener {
            this.viewModel.inputs.showPublicProfile(private_profile_switch.isChecked)
        }

    }

    override fun exitTransition(): Pair<Int, Int> = TransitionUtils.slideUpFromBottom()

    private fun displayPreferences(user: User) {
        SwitchCompatUtils.setCheckedWithoutAnimation(private_profile_switch, BooleanUtils.isFalse(user.showPublicProfile()))
    }
}
