package com.kickstarter.screenshoot.testing.di

import androidx.annotation.NonNull
import com.kickstarter.libs.BuildCheck
import com.kickstarter.services.WebClientType
import com.kickstarter.viewmodels.DiscoveryViewModel

class NoopBuildCheck : BuildCheck {
    @Override
    override fun bind(@NonNull viewModel: DiscoveryViewModel.ViewModel, @NonNull client: WebClientType) {
        // No-op
    }
}
