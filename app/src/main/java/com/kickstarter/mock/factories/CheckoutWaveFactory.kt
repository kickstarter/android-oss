package com.kickstarter.mock.factories

import com.kickstarter.models.CheckoutWave

class CheckoutWaveFactory {
    companion object {
        fun checkoutWaveActive(): CheckoutWave {
            return CheckoutWave.builder()
                .id(-1)
                .active(true)
                .build()
        }
        fun checkoutWaveInactive(): CheckoutWave {
            return CheckoutWave.builder()
                .id(-1)
                .active(false)
                .build()
        }
    }
}
