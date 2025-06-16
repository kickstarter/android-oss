package com.kickstarter.mock.factories

import com.kickstarter.models.CheckoutWave

class CheckoutWaveFactory {
    companion object {

        // - Active CheckoutWave
        fun checkoutWaveActive(): CheckoutWave {
            return CheckoutWave.builder()
                .id(-1)
                .active(true)
                .build()
        }
    }
}