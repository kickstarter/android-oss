package com.kickstarter.models

import com.kickstarter.mock.factories.PhotoFactory
import junit.framework.TestCase
import org.junit.Test

class PhotoTest : TestCase() {

    @Test
    fun testPhotoInitializationDefault() {
        val photo = Photo.builder()
            .build()

        assertTrue(photo.ed() == "")
        assertTrue(photo.full() == "")
        assertTrue(photo.little() == "")
        assertTrue(photo.med() == "")
        assertTrue(photo.small() == "")
        assertTrue(photo.thumb() == "")
    }

    @Test
    fun testPhotoInitializationNull() {
        val photoUrl = null
        val photo = Photo.builder()
            .ed(photoUrl)
            .full(photoUrl)
            .little(photoUrl)
            .med(photoUrl)
            .small(photoUrl)
            .thumb(photoUrl)
            .build()

        assertTrue(photo.ed() == "")
        assertTrue(photo.full() == "")
        assertTrue(photo.little() == "")
        assertTrue(photo.med() == "")
        assertTrue(photo.small() == "")
        assertTrue(photo.thumb() == "")
    }

    @Test
    fun testPhotoInitializationEquals() {
        val photoUrl = "https://ksr-ugc.imgix.net/assets/012/032/069/46817a8c099133d5bf8b64aad282a696_original.png?crop=faces&w=1552&h=873&fit=crop&v=1463725702&auto=format&q=92&s=72501d155e4a5e399276632687c77959"
        val photo = PhotoFactory.photo()
        val photo2 = Photo.builder()
            .ed(photoUrl)
            .full(photoUrl)
            .little(photoUrl)
            .med(photoUrl)
            .small(photoUrl)
            .thumb(photoUrl)
            .build()

        assertTrue(photo.ed() == photo2.ed())
        assertTrue(photo.full() == photo2.full())
        assertTrue(photo.little() == photo2.little())
        assertTrue(photo.med() == photo2.med())
        assertTrue(photo.small() == photo2.small())
        assertTrue(photo.thumb() == photo2.thumb())
        assertTrue(photo == photo2)

        val photo3 = photo2.toBuilder()
            .full("other url")
            .build()

        assertFalse(photo2 == photo3)
    }
}
