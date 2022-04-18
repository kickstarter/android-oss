package com.kickstarter.ui.adapters.data

import android.os.Parcelable
import com.kickstarter.models.Category
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import kotlinx.parcelize.Parcelize

@Parcelize
class NavigationDrawerData private constructor(
    private val user: User?,
    private val sections: List<Section>,
    private val expandedCategory: Category?,
    private val selectedParams: DiscoveryParams?
) : Parcelable {

    fun user() = this.user
    fun sections() = this.sections
    fun expandedCategory() = this.expandedCategory
    fun selectedParams() = this.selectedParams

    @Parcelize
    data class Builder(
        private var user: User? = null,
        private var sections: List<Section> = emptyList(),
        private var expandedCategory: Category? = null,
        private var selectedParams: DiscoveryParams ? = null
    ) : Parcelable {
        fun user(user: User?) = apply { this.user = user }
        fun sections(sections: List<Section>) = apply { this.sections = sections }
        fun expandedCategory(expandedCategory: Category?) = apply { this.expandedCategory = expandedCategory }
        fun selectedParams(selectedParams: DiscoveryParams?) = apply { this.selectedParams = selectedParams }
        fun build() = NavigationDrawerData(
            user = user,
            sections = sections,
            expandedCategory = expandedCategory,
            selectedParams = selectedParams
        )
    }

    fun toBuilder() = Builder(
        user = user,
        sections = sections,
        expandedCategory = expandedCategory,
        selectedParams = selectedParams
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is NavigationDrawerData) {
            equals = user() == obj.user() &&
                sections() == obj.sections() &&
                expandedCategory() == obj.expandedCategory() &&
                selectedParams() == obj.selectedParams()
        }
        return equals
    }

    @Parcelize
    class Section private constructor(
        private val expandable: Boolean,
        private val expanded: Boolean,
        private val rows: List<Row>
    ) : Parcelable {
        fun expandable() = this.expandable
        fun expanded() = this.expanded
        fun rows() = this.rows

        @Parcelize
        data class Builder(
            private var expandable: Boolean = false,
            private var expanded: Boolean = false,
            private var rows: List<Row> = emptyList()
        ) : Parcelable {
            fun expandable(expandable: Boolean) = apply { this.expandable = expandable }
            fun expanded(expanded: Boolean) = apply { this.expanded = expanded }
            fun rows(rows: List<Row>) = apply { this.rows = rows }
            fun build() = Section(
                expandable = expandable,
                expanded = expanded,
                rows = rows
            )
        }

        fun toBuilder() = Builder(
            expandable = expandable,
            expanded = expanded,
            rows = rows
        )

        companion object {
            @JvmStatic
            fun builder() = Builder()
        }

        override fun equals(obj: Any?): Boolean {
            var equals = super.equals(obj)
            if (obj is Section) {
                equals = expandable() == obj.expandable() &&
                    expanded() == obj.expanded() &&
                    rows() == obj.rows()
            }
            return equals
        }

        val isCategoryFilter: Boolean
            get() = rows().isNotEmpty() && rows()[0].params().isCategorySet

        val isTopFilter: Boolean
            get() = !isCategoryFilter

        @Parcelize
        class Row private constructor(
            private val params: DiscoveryParams,
            private val selected: Boolean,
            private val rootIsExpanded: Boolean,
        ) : Parcelable {
            fun params() = this.params
            fun selected() = this.selected
            fun rootIsExpanded() = this.rootIsExpanded

            @Parcelize
            data class Builder(
                private var params: DiscoveryParams = DiscoveryParams.builder().build(),
                private var selected: Boolean = false,
                private var rootIsExpanded: Boolean = false,
            ) : Parcelable {
                fun rootIsExpanded(rootIsExpanded: Boolean) = apply { this.rootIsExpanded = rootIsExpanded }
                fun selected(selected: Boolean) = apply { this.selected = selected }
                fun params(params: DiscoveryParams) = apply { this.params = params }
                fun build() = Row(
                    params = params,
                    selected = selected,
                    rootIsExpanded = rootIsExpanded
                )
            }

            fun toBuilder() = Builder(
                params = params,
                selected = selected,
                rootIsExpanded = rootIsExpanded
            )

            companion object {
                @JvmStatic
                fun builder() = Builder()
            }

            override fun equals(obj: Any?): Boolean {
                var equals = super.equals(obj)
                if (obj is Row) {
                    equals = params() == obj.params() &&
                        selected() == obj.selected() &&
                        rootIsExpanded() == obj.rootIsExpanded()
                }
                return equals
            }
        }
    }
}
