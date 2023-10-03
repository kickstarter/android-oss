package com.kickstarter.libs.utils

import java.util.Objects

class KsOptional<T> {
    private var value: T?

    private constructor() {
        value = null
    }

    private constructor(value: T) {
        this.value = Objects.requireNonNull(value)
    }

    interface Action<T> {
        fun apply(value: T)
    }

    fun ifPresent(action: Action<T>) {
        if (value != null) {
            action.apply(value!!)
        }
    }

    fun isPresent() = value != null

    fun getValue() = value

    companion object {
        fun <T> empty(): KsOptional<T> {
            return KsOptional()
        }

        fun <T> of(value: T): KsOptional<T> {
            if (value == null) return empty()
            return KsOptional(value)
        }
    }
}
