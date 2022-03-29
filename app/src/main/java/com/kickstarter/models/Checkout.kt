package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Checkout private constructor(
    private val id: Long?,
    private val backing: Backing,
) : Parcelable {
    fun id() = this.id
    fun backing() = this.backing

    @Parcelize
    data class Builder(
        private var id: Long? = null,
        private var backing: Backing = Backing.builder().build()
    ) : Parcelable {
        fun id(id: Long?) = apply { this.id = id }
        fun backing(backing: Backing) = apply { this.backing = backing }
        fun build() = Checkout(
            id = id,
            backing = backing
        )
    }

    fun toBuilder() = Builder(
        id = id,
        backing = backing
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is Checkout) {
            equals = id() == obj.id() &&
                backing() == obj.backing()
        }
        return equals
    }

    @Parcelize
    data class Backing private constructor(
        private val clientSecret: String?,
        private val requiresAction: Boolean,
    ) : Parcelable {

        fun clientSecret() = this.clientSecret
        fun requiresAction() = this.requiresAction

        @Parcelize
        data class Builder(
            private var clientSecret: String? = null,
            private var requiresAction: Boolean = false,
        ) : Parcelable {
            fun clientSecret(clientSecret: String?) = apply { this.clientSecret = clientSecret }
            fun requiresAction(requiresAction: Boolean) = apply { this.requiresAction = requiresAction }
            fun build() = Backing(
                clientSecret = clientSecret,
                requiresAction = requiresAction
            )
        }

        override fun equals(obj: Any?): Boolean {
            var equals = super.equals(obj)
            if (obj is Backing) {
                equals = clientSecret() == obj.clientSecret() &&
                    requiresAction() == obj.requiresAction()
            }
            return equals
        }
        fun toBuilder() = Builder(
            clientSecret = clientSecret,
            requiresAction = requiresAction

        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }
    }
}
