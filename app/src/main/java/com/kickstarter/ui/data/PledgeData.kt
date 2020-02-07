package com.kickstarter.ui.data

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.models.Reward

@AutoParcel
abstract class PledgeData : Parcelable {
    abstract fun reward(): Reward
    abstract fun screenLocation(): ScreenLocation?
    abstract fun projectTracking(): ProjectTracking

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun reward(reward: Reward): Builder
        abstract fun screenLocation(screenLocation: ScreenLocation?): Builder
        abstract fun projectTracking(projectTracking: ProjectTracking): Builder
        abstract fun build(): PledgeData
    }

    abstract fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_PledgeData.Builder()
        }
    }
}
