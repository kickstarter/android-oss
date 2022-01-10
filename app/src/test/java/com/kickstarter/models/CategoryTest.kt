package com.kickstarter.models

import com.kickstarter.mock.factories.CategoryFactory
import junit.framework.TestCase

class CategoryTest : TestCase() {
    fun testCompareTo() {
        val categories = listOf(
            CategoryFactory.bluesCategory(),
            CategoryFactory.ceramicsCategory(),
            CategoryFactory.worldMusicCategory(),
            CategoryFactory.musicCategory(),
            CategoryFactory.artCategory(),
            CategoryFactory.photographyCategory(),
            CategoryFactory.artCategory(),
            CategoryFactory.textilesCategory()
        )
        val sorted: List<Category> = categories.sorted()

        val expected = listOf(
            CategoryFactory.artCategory(),
            CategoryFactory.artCategory(),
            CategoryFactory.ceramicsCategory(),
            CategoryFactory.textilesCategory(),
            CategoryFactory.musicCategory(),
            CategoryFactory.bluesCategory(),
            CategoryFactory.worldMusicCategory(),
            CategoryFactory.photographyCategory()
        )
        assertEquals(expected, sorted)
    }

    fun testComparableRootCategories() {
        val artCategory = CategoryFactory.artCategory()
        val musicCategory = CategoryFactory.musicCategory()
        assertTrue(artCategory.compareTo(musicCategory) <= -1)
        assertTrue(musicCategory.compareTo(artCategory) >= 1)
    }

    fun testComparableRootAndSelf() {
        val artCategory = CategoryFactory.artCategory()
        assertTrue(artCategory.compareTo(artCategory) == 0)
    }

    fun testComparableChildAndSelf() {
        val bluesCategory = CategoryFactory.bluesCategory()
        assertTrue(bluesCategory.compareTo(bluesCategory) == 0)
    }

    fun testComparableParentAndChildren() {
        val musicCategory = CategoryFactory.musicCategory()
        val bluesCategory = CategoryFactory.bluesCategory()
        val worldMusicCategory = CategoryFactory.worldMusicCategory()
        assertTrue(musicCategory.compareTo(bluesCategory) <= -1)
        assertTrue(bluesCategory.compareTo(musicCategory) >= 1)
        assertTrue(musicCategory.compareTo(worldMusicCategory) <= -1)
        assertTrue(worldMusicCategory.compareTo(musicCategory) >= -1)
    }

    fun testComparableChildrenAndOtherRoot() {
        val photographyCategory = CategoryFactory.photographyCategory()
        val bluesCategory = CategoryFactory.bluesCategory()
        val worldMusicCategory = CategoryFactory.worldMusicCategory()
        assertTrue(bluesCategory.compareTo(photographyCategory) <= -1)
        assertTrue(worldMusicCategory.compareTo(photographyCategory) <= -1)
        assertTrue(photographyCategory.compareTo(bluesCategory) >= 1)
        assertTrue(photographyCategory.compareTo(worldMusicCategory) >= 1)
    }

    fun testComparableChildrenDifferentRoots() {
        val bluesCategory = CategoryFactory.bluesCategory()
        val textilesCategory = CategoryFactory.textilesCategory()
        assertTrue(bluesCategory.compareTo(textilesCategory) >= 1)
        assertTrue(textilesCategory.compareTo(bluesCategory) <= -1)
        val ceramicsCategory = CategoryFactory.ceramicsCategory()
        val worldMusicCategory = CategoryFactory.worldMusicCategory()
        assertTrue(ceramicsCategory.compareTo(worldMusicCategory) <= -1)
        assertTrue(worldMusicCategory.compareTo(ceramicsCategory) >= 1)
    }
}
