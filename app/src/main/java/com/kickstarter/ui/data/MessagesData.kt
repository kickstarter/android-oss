package com.kickstarter.ui.data

import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.User

data class MessagesData(val backing: Backing?, val project: Project, val participant: User, val currentUser: User)
