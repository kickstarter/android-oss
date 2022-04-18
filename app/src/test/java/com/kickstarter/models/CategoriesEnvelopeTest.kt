package com.kickstarter.models

import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.services.apiresponses.CategoriesEnvelope
import junit.framework.TestCase
import org.junit.Test

class CategoriesEnvelopeTest : TestCase() {
    @Test
    fun testDefaultInit() {

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

        val activityEnvelope = CategoriesEnvelope.builder()
            .categories(categories)
            .build()

        assertEquals(activityEnvelope.categories(), categories)
    }

    @Test
    fun testCategoriesEnvelope_equalFalse() {
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

        val categoriesEnvelope = CategoriesEnvelope.builder().build()
        val categoriesEnvelope2 = CategoriesEnvelope.builder().categories(
            categories
        ).build()

        assertFalse(categoriesEnvelope == categoriesEnvelope2)
    }

    @Test
    fun testCategoriesEnvelope_equalTrue() {
        val categoriesEnvelope1 = CategoriesEnvelope.builder().build()
        val categoriesEnvelope2 = CategoriesEnvelope.builder().build()

        assertEquals(categoriesEnvelope1, categoriesEnvelope2)
    }

    @Test
    fun testCategoriesEnvelopeToBuilder() {
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

        val categoriesEnvelope = CategoriesEnvelope.builder().build()
            .toBuilder()
            .categories(categories)
            .build()

        assertEquals(categoriesEnvelope.categories(), categories)
    }
}
