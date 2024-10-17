package com.kickstarter.services

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.extensions.isDiscoverCategoriesPath
import com.kickstarter.libs.utils.extensions.isDiscoverPlacesPath
import com.kickstarter.libs.utils.extensions.isDiscoverScopePath
import com.kickstarter.libs.utils.extensions.isDiscoverSortParam
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.toInteger
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Project
import com.kickstarter.models.User
import kotlinx.parcelize.Parcelize
import java.util.Locale
import kotlin.jvm.JvmOverloads

@Parcelize
class DiscoveryParams private constructor(
    private val backed: Int?,
    private val category: Category?,
    private val categoryParam: String?,
    private val location: Location?,
    private val locationParam: String?,
    private val page: Int?,
    private val perPage: Int?,
    private val pledged: Int?,
    private val staffPicks: Boolean?,
    private val starred: Int?,
    private val social: Int?,
    private val sort: Sort?,
    private val recommended: Boolean?,
    private val similarTo: Project?,
    private val state: State?,
    private val tagId: Int?,
    private val term: String?
) : Parcelable {

    fun backed() = this.backed
    fun category() = this.category
    fun categoryParam() = this.categoryParam
    fun location() = this.location
    fun locationParam() = this.locationParam
    fun page() = this.page
    fun perPage() = this.perPage
    fun pledged() = this.pledged
    fun staffPicks() = this.staffPicks
    fun starred() = this.starred
    fun social() = this.social
    fun sort() = this.sort
    fun recommended() = this.recommended
    fun similarTo() = this.similarTo
    fun state() = this.state
    fun tagId() = this.tagId
    fun term() = this.term
    fun nextPage(): DiscoveryParams {
        val page = page()
        return if (page != null) toBuilder().page(page + 1).build() else this
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is DiscoveryParams) {
            equals = backed() == other.backed() &&
                category() == other.category() &&
                categoryParam() == other.categoryParam() &&
                location() == other.location() &&
                locationParam() == other.locationParam() &&
                page() == other.page() &&
                perPage() == other.perPage() &&
                staffPicks() == other.staffPicks() &&
                starred() == other.starred() &&
                social() == other.social() &&
                sort() == other.sort() &&
                recommended() == other.recommended() &&
                similarTo() == other.similarTo() &&
                state() == other.state() &&
                tagId() == other.tagId() &&
                term() == other.term()
        }
        return equals
    }

    @Parcelize
    data class Builder(
        private var backed: Int? = null,
        private var category: Category? = null,
        private var categoryParam: String? = null,
        private var location: Location? = null,
        private var locationParam: String? = null,
        private var page: Int? = null,
        private var perPage: Int? = null,
        private var pledged: Int? = null,
        private var staffPicks: Boolean? = null,
        private var starred: Int? = null,
        private var social: Int? = null,
        private var sort: Sort? = null,
        private var recommended: Boolean? = null,
        private var similarTo: Project? = null,
        private var state: State? = null,
        private var tagId: Int? = null,
        private var term: String? = null
    ) : Parcelable {
        fun backed(backed: Int?) = apply { this.backed = backed }
        fun category(category: Category?) = apply { this.category = category }
        fun categoryParam(categoryParam: String?) = apply { this.categoryParam = categoryParam }
        fun location(location: Location?) = apply { this.location = location }
        fun locationParam(locationParam: String?) = apply { this.locationParam = locationParam }
        fun page(page: Int?) = apply { this.page = page }
        fun perPage(perPage: Int?) = apply { this.perPage = perPage }
        fun pledged(pledged: Int?) = apply { this.pledged = pledged }
        fun sort(sort: Sort?) = apply { this.sort = sort }
        fun staffPicks(staffPicks: Boolean?) = apply { this.staffPicks = staffPicks }
        fun starred(starred: Int?) = apply { this.starred = starred }
        fun social(social: Int?) = apply { this.social = social }
        fun recommended(recommended: Boolean?) = apply { this.recommended = recommended }
        fun similarTo(similarTo: Project?) = apply { this.similarTo = similarTo }
        fun state(state: State?) = apply { this.state = state }
        fun tagId(tagId: Int?) = apply { this.tagId = tagId }
        fun term(term: String?) = apply { this.term = term }
        fun build() = DiscoveryParams(
            backed = backed,
            category = category,
            categoryParam = categoryParam,
            location = location,
            locationParam = locationParam,
            page = page,
            perPage = perPage,
            pledged = pledged,
            staffPicks = staffPicks,
            starred = starred,
            social = social,
            sort = sort,
            recommended = recommended,
            similarTo = similarTo,
            state = state,
            tagId = tagId,
            term = term
        )

        /**
         * Returns a builder containing the contents of this builder and `otherBuilder`. If a value for the same property
         * exists in both builders, the returned builder will contain the value from `otherBuilder`.
         */
        fun mergeWith(otherBuilder: Builder): Builder {
            val other = otherBuilder.build()
            var retVal = this
            if (other.backed() != null) {
                retVal = retVal.backed(other.backed())
            }
            if (other.category() != null) {
                retVal = retVal.category(other.category())
            }
            if (other.categoryParam() != null) {
                retVal = retVal.categoryParam(other.categoryParam())
            }
            if (other.location() != null) {
                retVal = retVal.location(other.location())
            }
            if (other.page() != null) {
                retVal = retVal.page(other.page())
            }
            if (other.perPage() != null) {
                retVal = retVal.perPage(other.perPage())
            }
            if (other.pledged() != null) {
                retVal = retVal.pledged(other.pledged())
            }
            if (other.social() != null) {
                retVal = retVal.social(other.social())
            }
            if (other.staffPicks() != null) {
                retVal = retVal.staffPicks(other.staffPicks())
            }
            if (other.starred() != null) {
                retVal = retVal.starred(other.starred())
            }
            if (other.state() != null) {
                retVal = retVal.state(other.state())
            }
            if (other.sort() != null) {
                retVal = retVal.sort(other.sort())
            }
            if (other.recommended() != null) {
                retVal = retVal.recommended(other.recommended())
            }
            if (other.similarTo() != null) {
                retVal = retVal.similarTo(other.similarTo())
            }
            if (other.tagId() != null) {
                retVal = retVal.tagId(other.tagId())
            }
            if (other.term() != null) {
                retVal = retVal.term(other.term())
            }
            return retVal
        }
    }

    fun toBuilder() = Builder(
        backed = backed,
        category = category,
        categoryParam = categoryParam,
        location = location,
        locationParam = locationParam,
        page = page,
        perPage = perPage,
        pledged = pledged,
        staffPicks = staffPicks,
        starred = starred,
        social = social,
        sort = sort,
        recommended = recommended,
        similarTo = similarTo,
        state = state,
        tagId = tagId,
        term = term
    )

    fun queryParams(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map.apply {
            if (backed() != null) {
                put("backed", backed().toString())
            }
            category()?.let {
                put("category_id", it.id().toString())
            }
            categoryParam()?.let {
                put("category_id", it)
            }
            location()?.let {
                put("woe_id", it.id().toString())
            }
            locationParam()?.let {
                put("woe_id", it)
            }
            if (page() != null) {
                put("page", page().toString())
            }
            if (perPage() != null) {
                put("per_page", perPage().toString())
            }
            if (pledged() != null) {
                put("pledged", pledged().toString())
            }
            if (recommended() != null) {
                put("recommended", recommended().toString())
            }
            similarTo()?.let {
                put("similar_to", it.id().toString())
            }
            if (starred() != null) {
                put("starred", starred().toString())
            }
            if (social() != null) {
                put("social", social().toString())
            }
            val sort = sort()
            if (sort != null) {
                put("sort", sort.toString())
            }
            if (staffPicks() != null) {
                put("staff_picks", staffPicks().toString())
            }
            val state = state()
            if (state != null) {
                put("state", state.toString())
            }
            val tagId = tagId()
            if (tagId != null) {
                put("tag_id", tagId.toString())
            }

            term()?.let { put("q", it) }
            if (shouldIncludeFeatured()) {
                put("include_featured", "true")
            }
        }

        return map
    }

    /**
     * Determines if the `include_featured` flag should be included in a discovery request so that we guarantee that the
     * featured project for the category comes back.
     */
    fun shouldIncludeFeatured(): Boolean {
        return category() != null && category()?.parent() == null && page() != null && page() == 1 && (sort() == null || sort() == Sort.MAGIC)
    }

    override fun toString(): String {
        return queryParams().toString()
    }

    /**
     * Determines the correct string to display for a filter depending on where it is shown.
     *
     * @param context           context
     * @param ksString          ksString for string formatting
     * @param isToolbar         true if string is being displayed in the [com.kickstarter.ui.toolbars.DiscoveryToolbar]
     * @param isParentFilter    true if string is being displayed as a [com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder]
     *
     * @return the appropriate filter string
     */
    @JvmOverloads
    fun filterString(
        context: Context,
        ksString: KSString,
        isToolbar: Boolean = false,
        isParentFilter: Boolean = false
    ): String {
        return if (staffPicks().isTrue()) {
            context.getString(R.string.Projects_We_Love)
        } else if (starred() != null && starred() == 1) {
            context.getString(R.string.Saved)
        } else if (backed() != null && backed() == 1) {
            context.getString(R.string.discovery_backing)
        } else if (social() != null && social() == 1) {
            if (isToolbar) context.getString(R.string.Following) else context.getString(R.string.Backed_by_people_you_follow)
        } else if (category() != null) {
            if (category()?.isRoot == true && !isParentFilter && !isToolbar) ksString.format(
                context.getString(
                    R.string.All_category_name_Projects
                ),
                "category_name", category()?.name()
            ) else category()?.name() ?: ""
        } else if (location() != null) {
            location()?.displayableName() ?: ""
        } else if (recommended().isTrue()) {
            if (isToolbar) context.getString(R.string.Recommended) else context.getString(R.string.discovery_recommended_for_you)
        } else {
            context.getString(R.string.All_Projects)
        }
    }

    /**
     * Determines if params are for All Projects, i.e. discovery without params.
     * @return true if is All Projects.
     */
    val isAllProjects: Boolean
        get() = (
            (staffPicks().isFalse() || staffPicks().isNull()) &&
                (starred() == null || starred() != 1) && (backed() == null || backed() != 1) &&
                (social() == null || social() != 1) && category() == null && location() == null &&
                tagId() == null
            )

    /**
     * Determines if params are for Saved Projects, i.e. discovery with starred params.
     * @return true if is Saved Projects.
     */
    val isSavedProjects: Boolean
        get() = (
            (starred() != null && starred() == 1).isTrue() && staffPicks().isFalse() && (backed() == null || backed() != 1) &&
                (social() == null || social() != 1) && category() == null && location() == null && recommended().isFalse() &&
                tagId() == null
            )
    val isCategorySet: Boolean
        get() = category() != null

    companion object {
        /**
         * Returns a [DiscoveryParams] constructed by parsing data out of the given [Uri].
         */
        @JvmStatic
        fun fromUri(uri: Uri): DiscoveryParams {
            var builder = builder()
            if (uri.isDiscoverCategoriesPath()) {
                builder = builder.categoryParam(uri.lastPathSegment)
            }
            if (uri.isDiscoverPlacesPath()) {
                builder = builder.locationParam(uri.lastPathSegment)
            }
            if (uri.isDiscoverScopePath("ending-soon")) {
                builder = builder.sort(Sort.ENDING_SOON)
            }
            if (uri.isDiscoverScopePath("newest")) {
                builder = builder.sort(Sort.NEWEST).staffPicks(true)
            }
            if (uri.isDiscoverSortParam()) {
                builder = builder.sort(
                    uri.getQueryParameter("sort")?.let {
                        Sort.fromString(it)
                    }
                )
            }
            if (uri.isDiscoverScopePath("popular")) {
                builder = builder.sort(Sort.POPULAR)
            }
            if (uri.isDiscoverScopePath("recently-launched")) {
                builder = builder.sort(Sort.NEWEST)
            }
            if (uri.isDiscoverScopePath("small-projects")) {
                builder = builder.pledged(0)
            }
            if (uri.isDiscoverScopePath("social")) {
                builder = builder.social(0)
            }
            if (uri.isDiscoverScopePath("successful")) {
                builder = builder.sort(Sort.ENDING_SOON).state(State.SUCCESSFUL)
            }
            val backed = uri.getQueryParameter("backed").toInteger()
            if (backed != null) {
                builder = builder.backed(backed)
            }
            val categoryParam = uri.getQueryParameter("category_id")
            if (categoryParam != null) {
                builder = builder.categoryParam(categoryParam)
            }
            val locationParam = uri.getQueryParameter("woe_id")
            if (locationParam != null) {
                builder = builder.locationParam(locationParam)
            }
            val page = uri.getQueryParameter("page").toInteger()
            if (page != null) {
                builder = builder.page(page)
            }
            val perPage = uri.getQueryParameter("per_page").toInteger()
            if (perPage != null) {
                builder = builder.perPage(perPage)
            }
            val pledged = uri.getQueryParameter("pledged").toInteger()
            if (pledged != null) {
                builder = builder.pledged(pledged)
            }
            val recommended = uri.getBooleanQueryParameter("recommended", false)
            if (recommended) {
                builder = builder.recommended(recommended)
            }
            val social = uri.getQueryParameter("social").toInteger()
            if (social != null) {
                builder = builder.social(social)
            }
            val staffPicks = uri.getBooleanQueryParameter("staff_picks", false)
            if (staffPicks) {
                builder = builder.staffPicks(staffPicks)
            }
            val sortParam = uri.getQueryParameter("sort")
            if (sortParam != null) {
                builder = builder.sort(Sort.fromString(sortParam))
            }
            val starred = uri.getQueryParameter("starred").toInteger()
            if (starred != null) {
                builder = builder.starred(starred)
            }
            val stateParam = uri.getQueryParameter("state")
            if (stateParam != null) {
                builder = builder.state(State.fromString(stateParam))
            }
            val tagId = uri.getQueryParameter("tag_id").toInteger()
            if (tagId != null) {
                builder = builder.tagId(tagId)
            }
            val term = uri.getQueryParameter("term")
            if (term != null) {
                builder = builder.term(term)
            }
            return builder.build()
        }

        @JvmStatic
        fun builder(): Builder {
            return Builder()
                .page(1)
                .perPage(15)
        }

        @JvmStatic
        fun getDefaultParams(user: User?): DiscoveryParams {
            val builder = builder()
            if (user != null && user.optedOutOfRecommendations().isFalse()) {
                builder.recommended(true).backed(-1)
            }
            return builder
                .sort(Sort.MAGIC)
                .build()
        }
    }

    enum class Sort {
        MAGIC, POPULAR, NEWEST, ENDING_SOON, DISTANCE;

        override fun toString(): String {
            return when (this) {
                MAGIC -> "magic"
                POPULAR -> "popularity"
                NEWEST -> "newest"
                ENDING_SOON -> "end_date"
                DISTANCE -> "distance"
            }
        }

        fun refTagSuffix(): String {
            return when (this) {
                MAGIC -> ""
                POPULAR -> "_popular"
                NEWEST -> "_newest"
                ENDING_SOON -> "_ending_soon"
                DISTANCE -> "_distance"
            }
        }

        companion object {
            @JvmField
            var defaultSorts = listOf(MAGIC, POPULAR, NEWEST, ENDING_SOON)
            fun fromString(string: String): Sort {
                when (string) {
                    "magic" -> return MAGIC
                    "popularity" -> return POPULAR
                    "newest" -> return NEWEST
                    "end_date" -> return ENDING_SOON
                    "distance" -> return DISTANCE
                }
                return MAGIC
            }
        }
    }

    enum class State {
        STARTED, SUBMITTED, LIVE, SUCCESSFUL, CANCELED, FAILED, UNKNOWN;

        override fun toString(): String {
            return name.lowercase(Locale.getDefault())
        }

        companion object {
            fun fromString(string: String): State {
                return when (string) {
                    "started" -> STARTED
                    "submitted" -> SUBMITTED
                    "live" -> LIVE
                    "successful" -> SUCCESSFUL
                    "canceled" -> CANCELED
                    "failed" -> FAILED
                    else -> { UNKNOWN }
                }
            }
        }
    }
}
