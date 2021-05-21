package com.kickstarter.services.mutations

import android.os.Parcelable
import com.kickstarter.libs.qualifiers.AutoGson
import com.kickstarter.libs.utils.encodeRelayId
import com.kickstarter.models.Project
import kotlinx.android.parcel.Parcelize

@AutoGson
@Parcelize
class PostCommentData(
    val body: String?,
    val parentId: String?,
    val project: Project?,
    val clientMutationId: String?
) : Parcelable {

    fun commentableId() = this.project?.let { encodeRelayId(it) }

    @AutoGson
    @Parcelize
    data class Builder(
        var body: String? = null,
        var project: Project? = null,
        var parentId: String? = null,
        var clientMutationId: String? = null
    ) : Parcelable {
        fun body(body: String) = apply { this.body = body }
        fun project(project: Project) = apply { this.project = project }
        fun parentId(parentId: String?) = apply { this.parentId = parentId }
        fun clientMutationId(clientMutationId: String) = apply { this.clientMutationId = clientMutationId }
        fun build() = PostCommentData(body, parentId, project, clientMutationId)
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        this.body,
        this.project,
        this.parentId,
        this.clientMutationId
    )
}
