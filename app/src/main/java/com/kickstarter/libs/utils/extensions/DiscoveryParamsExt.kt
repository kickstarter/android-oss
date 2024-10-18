package com.kickstarter.libs.utils.extensions

import android.util.Pair
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.ListUtils
import com.kickstarter.models.Category
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.type.ProjectSort
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import java.util.TreeMap

/**
 * A `ref_tag` representation of some discovery params. This tag can be used to attribute a checkout when a user
 * pledges from discovery using these particular params.
 */
fun DiscoveryParams.refTag(): RefTag {
    if (this.isCategorySet) {
        val sort = this.sort()
        return if (sort != null) {
            RefTag.category(sort)
        } else
            RefTag.category()
    }

    if (this.location() != null) {
        return RefTag.city()
    }

    val staffPicks: Boolean = this.staffPicks().isTrue()

    if (staffPicks) {
        val sort = this.sort()
        return if (sort != null) {
            RefTag.recommended(sort)
        } else
            RefTag.recommended()
    }

    this.tagId()?.let {
        return RefTag.collection(it)
    }

    if (this.social().isNonZero()) {
        return RefTag.social()
    }

    return if (this.term() != null) {
        RefTag.search()
    } else RefTag.discovery()
}

/**
 * Converts all the disparate data representing the state of the menu data into a `NavigationDrawerData` object
 * that can be used to populate a view.
 *
 * @param categories The full list of categories that can be displayed.
 * @param selected The params that correspond to what is currently selected in the menu.
 * @param expandedCategory The category that correspond to what is currently expanded in the menu.
 * @param user The currently logged in user.
 */
fun DiscoveryParams.deriveNavigationDrawerData(
    categories: List<Category> = listOf(),
    expandedCategory: Category? = null,
    user: User
): NavigationDrawerData {
    val builder = NavigationDrawerData.builder()

    val visible = categories
        .filter { isVisible(it, expandedCategory) }
        .flatMap { doubleRootIfExpanded(it, expandedCategory) }
        .map { DiscoveryParams.builder().category(it).build() }
        .toList()
    val sectionsForVisible = paramsGroupedByRootCategory(visible)
    val sectionsFromAllParamsVisible = sectionsFromAllParams(sectionsForVisible, expandedCategory)
    val topSections = if (user == User.builder().build()) {
        topSections(null)
    } else {
        topSections(user)
    }

    val allSections = topSections.toMutableList()
    allSections.addAll(sectionsFromAllParamsVisible)
    allSections.toList()

    return builder
        .sections(allSections)
        .user(if (user == User.builder().build()) null else user)
        .selectedParams(this)
        .expandedCategory(expandedCategory)
        .build()
}

/**
 * Determines if a category is visible given what is the currently expanded category.
 * @param category The category to determine its visibility.
 * @param expandedCategory The category that is currently expandable, possible `null`.
 */
private fun isVisible(category: Category, expandedCategory: Category?): Boolean {
    if (expandedCategory == null || category.id() == 0L) {
        return category.isRoot
    }
    return if (category.isRoot) {
        true
    } else category.root()?.id() == expandedCategory.id()
}

/**
 * Given a doubly nested list of all possible category params and an (optional) expanded category this will
 * create a list of sections that can be used in the drawer.
 */
private fun sectionsFromAllParams(
    sections: List<List<DiscoveryParams>>,
    expandedCategory: Category?
): List<NavigationDrawerData.Section> {
    return sections
        .map {
            rowsFromParams(it)
        }.map {
            Pair.create(
                it,
                rowsAreExpanded(it, expandedCategory)
            )
        }
        .map {
            NavigationDrawerData.Section.builder()
                .rows(it.first)
                .expanded(it.second)
                .build()
        }
        .toList()
}

/**
 * Converts a list of params into a list of rows that the drawer can use to display rows.
 */
private fun rowsFromParams(params: List<DiscoveryParams>): List<NavigationDrawerData.Section.Row> {
    return params
        .map {
            NavigationDrawerData.Section.Row.builder().params(it).build()
        }
        .toList()
}

/**
 * From a list of rows and the currently expanded category figures out if the rows are expanded.
 */
private fun rowsAreExpanded(
    rows: List<NavigationDrawerData.Section.Row>,
    expandedCategory: Category?
): Boolean {
    val sectionCategory = rows[0].params().category()
    return sectionCategory != null && expandedCategory != null && expandedCategory.id() != 0L && sectionCategory.rootId() == expandedCategory.rootId()
}

/**
 * Since there are two rows that correspond to a root category in an expanded section (e.g. "Art" & "All of Art"),
 * this method will double up that root category in such a situation.
 * @param category The category that might potentially be doubled up.
 * @param expandedCategory The currently expanded category.
 */
private fun doubleRootIfExpanded(
    category: Category,
    expandedCategory: Category?
): List<Category> {
    if (expandedCategory == null) {
        return listOf(category)
    }
    return if (category.isRoot && category.id() == expandedCategory.id()) {
        listOf(category, category)
    } else listOf(category)
}

/**
 * Returns a list of top-level section filters that can be used based on the current user, which could be `null`.
 * Each filter is its own section containing one single row.
 *
 * @param user The currently logged in user, can be `null`.
 */
private fun topSections(user: User?): List<NavigationDrawerData.Section> {
    val filters = ListUtils.empty<DiscoveryParams>()
    val userIsLoggedIn = user != null

    if (userIsLoggedIn && user?.optedOutOfRecommendations()?.isFalse() == true) {
        filters.add(DiscoveryParams.builder().recommended(true).backed(-1).build())
    }

    filters.add(DiscoveryParams.builder().build())
    filters.add(DiscoveryParams.builder().staffPicks(true).build())

    if (userIsLoggedIn) {
        filters.add(DiscoveryParams.builder().starred(1).build())
        /*if (user?.social()?.isTrue() == true) { //TODO: Bring back once social is available on graphql
            filters.add(DiscoveryParams.builder().social(1).build())
        }*/
    }

    return filters
        .map {
            NavigationDrawerData.Section.Row.builder().params(it).build()
        }
        .map { listOf(it) }
        .map { NavigationDrawerData.Section.builder().rows(it).build() }
        .toList()
}

/**
 * Converts the full list of category discovery params into a grouped list of params. A group corresponds to a root
 * category, and the list contains all subcategories.
 */
private fun paramsGroupedByRootCategory(ps: List<DiscoveryParams>): List<List<DiscoveryParams>> {
    val grouped: MutableMap<String, MutableList<DiscoveryParams>> = TreeMap()
    for (p in ps) {
        p.category()?.root()?.name()?.let {
            if (!grouped.containsKey(it)) {
                grouped[it] = ArrayList()
            }
            grouped[it]?.add(p)
        }
    }
    return ArrayList<List<DiscoveryParams>>(grouped.values)
}

/**
 * Return the corresponding tab position for a given sort param.
 */
fun DiscoveryParams.Sort?.positionFromSort(): Int {
    return if (this == null) {
        0
    } else when (this) {
        DiscoveryParams.Sort.MAGIC -> 0
        DiscoveryParams.Sort.POPULAR -> 1
        DiscoveryParams.Sort.NEWEST -> 2
        DiscoveryParams.Sort.ENDING_SOON -> 3
        else -> 0
    }
}

/**
 * From the current Sort param, to the GraphQL Project.Sort param
 */
fun DiscoveryParams.Sort.toProjectSort(): ProjectSort {
    return when (this) {
        DiscoveryParams.Sort.MAGIC -> ProjectSort.MAGIC
        DiscoveryParams.Sort.DISTANCE -> ProjectSort.DISTANCE
        DiscoveryParams.Sort.POPULAR -> ProjectSort.POPULARITY
        DiscoveryParams.Sort.ENDING_SOON -> ProjectSort.END_DATE
        DiscoveryParams.Sort.NEWEST -> ProjectSort.NEWEST
    }
}
