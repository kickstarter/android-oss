package com.kickstarter.libs.utils.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.fragments.CrowdfundCheckoutFragment
import com.kickstarter.ui.fragments.PledgeFragment

fun Fragment.selectPledgeFragment(
    pledgeData: PledgeData,
    pledgeReason: PledgeReason,
    ffEnabled: Boolean = false
): Fragment {
    val fragment = when (pledgeReason) {
        PledgeReason.FIX_PLEDGE ->
            if (ffEnabled) CrowdfundCheckoutFragment()
            else PledgeFragment()
        else -> CrowdfundCheckoutFragment()
    }

    return fragment.withData(pledgeData, pledgeReason)
}

fun Fragment.withData(pledgeData: PledgeData?, pledgeReason: PledgeReason?): Fragment {
    val argument = Bundle()

    pledgeData?.let {
        argument.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, it)
    }
    pledgeReason?.let {
        argument.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, it)
    }

    this.arguments = argument
    return this
}
