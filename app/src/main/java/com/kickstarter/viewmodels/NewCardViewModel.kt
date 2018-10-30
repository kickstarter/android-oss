package com.kickstarter.viewmodels

import android.support.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.ui.activities.NewCardActivity

interface NewCardViewModel {
    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<NewCardActivity>(environment)
}
