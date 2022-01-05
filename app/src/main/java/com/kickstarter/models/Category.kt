package com.kickstarter.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Category internal constructor(
    private val analyticsName: String,
    private val color: Int,
    private val id: Long,
    private val name: String,
    private val parent: Category?,
    private val parentId: Long,
    private val parentName: String,
    private val position: Int,
    private val projectsCount: Int,
    private val slug: String
) : Parcelable, Comparable<Category> {

    fun analyticsName() = this.analyticsName
    fun color() = this.color
    fun id() = this.id
    fun name() = this.name
    fun parent() = this.parent
    fun parentId() = this.parentId
    fun parentName() = this.parentName
    fun position() = this.position
    fun projectsCount() = this.projectsCount
    fun slug() = this.slug

    @Parcelize
    data class Builder(
        private var analyticsName: String = "",
        private var color: Int = 0,
        private var id: Long = 0L,
        private var name: String = "",
        private var parent: Category? = null,
        private var parentId: Long = 0L,
        private var parentName: String = "",
        private var position: Int = 0,
        private var projectsCount: Int = 0,
        private var slug: String = ""
    ) : Parcelable {
        fun analyticsName(analyticsName: String?) = apply { analyticsName?.let { this.analyticsName = it } }
        fun color(color: Int?) = apply { color?.let { this.color = it } }
        fun id(id: Long?) = apply { id?.let { this.id = it } }
        fun name(name: String?) = apply { name?.let { this.name = it } }
        fun parent(parent: Category?) = apply { parent?.let { this.parent = it } }
        fun parentId(parentId: Long?) = apply { parentId?.let { this.parentId = it } }
        fun parentName(parentName: String?) = apply { parentName?.let { this.parentName = it } }
        fun position(position: Int) = apply { position?.let { this.position = it } }
        fun projectsCount(projectsCount: Int?) = apply { projectsCount?.let { this.projectsCount = it } }
        fun slug(slug: String?) = apply { slug?.let { this.slug = it } }
        fun build() = Category(
            analyticsName = analyticsName,
            color = color,
            id = id,
            name = name,
            parent = parent,
            parentId = parentId,
            parentName = parentName,
            position = position,
            projectsCount = projectsCount,
            slug = slug
        )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        analyticsName = analyticsName,
        color = color,
        id = id,
        name = name,
        parent = parent,
        parentId = parentId,
        parentName = parentName,
        position = position,
        projectsCount = projectsCount,
        slug = slug
    )

    override fun compareTo(other: Category): Int {
        if (id() == other.id()) {
            return 0
        }
        if (isRoot && id() == other.rootId()) {
            return -1
        } else if (!isRoot && rootId() == other.id()) {
            return 1
        }
        return other.root()?.let { root()?.name()?.compareTo(it.name()) } ?: 0
    }

    val isRoot: Boolean
        get() = parentId() == 0L

    fun root(): Category? {
        return if (isRoot) this else parent()
    }

    fun rootId(): Long {
        return if (isRoot) id() else parentId()
    }
}
