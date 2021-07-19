package com.kickstarter.libs

import androidx.annotation.NonNull
import com.kickstarter.services.WebClientType
import com.kickstarter.viewmodels.DiscoveryViewModel

class NoopBuildCheck : BuildCheck {
    @Override
    override fun bind(@NonNull viewModel: DiscoveryViewModel.ViewModel, @NonNull client: WebClientType) {
        // No-op
    }
}
