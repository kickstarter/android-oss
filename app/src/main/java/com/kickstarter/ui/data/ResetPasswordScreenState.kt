package com.kickstarter.ui.data

import androidx.annotation.StringRes
import com.kickstarter.R

enum class ResetPasswordScreenState(
    @StringRes val title: Int,
    @StringRes val hint: Int?
) {
    ResetPassword(
        title = R.string.Reset_your_password,
        hint = R.string.We_re_simplifying_our_login_process_To_log_in
    ),
    ForgetPassword(
        title = R.string.forgot_password_title,
        hint = null
    )
}
