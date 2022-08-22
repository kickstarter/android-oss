package com.kickstarter.ui.data

import androidx.annotation.StringRes
import com.kickstarter.R

enum class ResetPasswordScreenState(
    @StringRes val title: Int,
    @StringRes val hint: Int?
) {
    ResetPassword(
        title = R.string.FPO_reset_your_password,
        hint = R.string.FPO_we_discontinued_logging_in_through_Facebook
    ),
    ForgetPassword(
        title = R.string.forgot_password_title,
        hint = null
    )
}
