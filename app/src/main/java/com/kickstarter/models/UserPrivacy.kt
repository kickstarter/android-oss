package com.kickstarter.models

data class UserPrivacy(
    val name: String,
    val email: String,
    val hasPassword: Boolean,
    val isCreator: Boolean,
    val isDeliverable: Boolean,
    val isEmailVerified: Boolean,
    val chosenCurrency: String,
    val enabledFeatures: List<String> = emptyList()
)
