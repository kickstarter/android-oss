package com.kickstarter.ui.data

sealed class MessageSubject {
    class Backing(val backing: com.kickstarter.models.Backing) : MessageSubject()
    class Project(val project: com.kickstarter.models.Project) : MessageSubject()
    class MessageThread(val messageThread: com.kickstarter.models.MessageThread) : MessageSubject()

    fun <A> value(
        ifBacking: (com.kickstarter.models.Backing) -> A,
        ifMessageThread: (com.kickstarter.models.MessageThread) -> A,
        ifProject: (com.kickstarter.models.Project) -> A
    ): A = when (this) {

        is Backing -> ifBacking(this.backing)
        is MessageThread -> ifMessageThread(this.messageThread)
        is Project -> ifProject(this.project)
    }
}
