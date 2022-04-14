package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.adapters.data.NavigationDrawerData
import org.junit.Test

class NavigationDrawerDataTest : KSRobolectricTestCase() {
    @Test
    fun testDefaultInit() {
        val user = User.builder().build()

        val expandedCategory = Category.builder().build()

        val selectedParams = DiscoveryParams.builder().build()

        val params = DiscoveryParams.builder().build()

        val row = NavigationDrawerData.Section.Row.builder().params(params)
            .rootIsExpanded(true)
            .selected(true)
            .build()

        val rows = listOf(row)

        val section = NavigationDrawerData.Section.builder().expandable(true).expanded(true)
            .rows(rows).build()

        val sections = listOf(section)

        val navigationDrawerData = NavigationDrawerData.builder()
            .user(user)
            .expandedCategory(expandedCategory)
            .sections(sections)
            .selectedParams(selectedParams).build()

        assertEquals(navigationDrawerData.user(), user)
        assertEquals(navigationDrawerData.expandedCategory(), expandedCategory)
        assertEquals(navigationDrawerData.sections(), sections)
        assertEquals(navigationDrawerData.selectedParams(), selectedParams)
        
        assertTrue(section.expandable())
        assertTrue(section.expanded())
        assertEquals(section.rows(), rows)
        
        assertEquals(row.params(), params)
        assertTrue(row.selected())
        assertTrue(row.rootIsExpanded())
    }
    
    @Test
    fun testNavigationDrawerData_equalFalse() {
        val user = User.builder().build()

        val expandedCategory = Category.builder().build()

        val selectedParams = DiscoveryParams.builder().build()

        val params = DiscoveryParams.builder().build()

        val row = NavigationDrawerData.Section.Row.builder().params(params)
            .rootIsExpanded(true)
            .selected(true)
            .build()

        val rows = listOf(row)

        val section = NavigationDrawerData.Section.builder().expandable(true).expanded(true)
            .rows(rows).build()

        val sections = listOf(section)

        val navigationDrawerData = NavigationDrawerData.builder().build()
        val navigationDrawerData2 = NavigationDrawerData.builder()
            .user(user).build()
        val navigationDrawerData3 = NavigationDrawerData.builder().expandedCategory(expandedCategory)
            .selectedParams(selectedParams).build()
        val navigationDrawerData4 = NavigationDrawerData.builder()
            .sections(sections)
            .selectedParams(selectedParams).build()

        assertFalse(navigationDrawerData == navigationDrawerData2)
        assertFalse(navigationDrawerData == navigationDrawerData3)
        assertFalse(navigationDrawerData == navigationDrawerData4)

        assertFalse(navigationDrawerData3 == navigationDrawerData2)
        assertFalse(navigationDrawerData3 == navigationDrawerData4)
    }

    @Test
    fun testNavigationDrawerData_equalTrue() {
        val navigationDrawerData1 = NavigationDrawerData.builder().build()
        val navigationDrawerData2 = NavigationDrawerData.builder().build()

        assertEquals(navigationDrawerData1, navigationDrawerData2)
    }

    @Test
    fun testNavigationDrawerDataToBuilder() {

        val user = User.builder().build()

        val expandedCategory = Category.builder().build()

        val selectedParams = DiscoveryParams.builder().build()

        val params = DiscoveryParams.builder().build()

        val row = NavigationDrawerData.Section.Row.builder().build().toBuilder().params(params)
            .rootIsExpanded(true)
            .selected(true)
            .build()

        val rows = listOf(row)

        val section = NavigationDrawerData.Section.builder().build().toBuilder().expandable(true).expanded(true)
            .rows(rows).build()

        val sections = listOf(section)

        val navigationDrawerData = NavigationDrawerData.builder().build().toBuilder()
            .user(user)
            .expandedCategory(expandedCategory)
            .sections(sections)
            .selectedParams(selectedParams).build()

        assertEquals(navigationDrawerData.user(), user)
        assertEquals(navigationDrawerData.expandedCategory(), expandedCategory)
        assertEquals(navigationDrawerData.sections(), sections)
        assertEquals(navigationDrawerData.selectedParams(), selectedParams)

        assertTrue(section.expandable())
        assertTrue(section.expanded())
        assertEquals(section.rows(), rows)

        assertEquals(row.params(), params)
        assertTrue(row.selected())
        assertTrue(row.rootIsExpanded())
    }

    @Test
    fun testNavigationDrawerDataSections_isCategoryFilter() {
        val params = DiscoveryParams.builder().build()
        val row = NavigationDrawerData.Section.Row.builder().build().toBuilder()
            .params(params)
            .rootIsExpanded(true)
            .selected(true)
            .build()

        val rows = listOf(row)

        val section = NavigationDrawerData.Section.builder().build().toBuilder().expandable(true).expanded(true)
            .rows(rows).build()

        assertFalse(section.isCategoryFilter)

        val section2 = NavigationDrawerData.Section.builder().build()
        assertFalse(section2.isCategoryFilter)

        val params2 = DiscoveryParams.builder().category(Category.builder().build()).build()
        val row2 = NavigationDrawerData.Section.Row.builder()
            .params(params2)
            .rootIsExpanded(true)
            .selected(true)
            .build()

        val rows2 = listOf(row2)

        val section3 = NavigationDrawerData.Section.builder().build().toBuilder().expandable(true).expanded(true)
            .rows(rows2).build()

        assertTrue(section3.isCategoryFilter)
    }

    @Test
    fun testNavigationDrawerDataSections_isTopFilter() {
        val section2 = NavigationDrawerData.Section.builder().build()
        assertTrue(section2.isTopFilter)

        val params2 = DiscoveryParams.builder().category(Category.builder().build()).build()
        val row2 = NavigationDrawerData.Section.Row.builder()
            .params(params2)
            .rootIsExpanded(true)
            .selected(true)
            .build()

        val rows2 = listOf(row2)

        val section3 = NavigationDrawerData.Section.builder().build().toBuilder().expandable(true).expanded(true)
            .rows(rows2).build()

        assertFalse(section3.isTopFilter)
    }
}
