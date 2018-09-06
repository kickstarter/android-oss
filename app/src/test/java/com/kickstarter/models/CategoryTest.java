package com.kickstarter.models;

import com.kickstarter.mock.factories.CategoryFactory;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CategoryTest extends TestCase {

  public void testCompareTo() {
    final List<Category> categories = Arrays.asList(
      CategoryFactory.bluesCategory(),
      CategoryFactory.ceramicsCategory(),
      CategoryFactory.worldMusicCategory(),
      CategoryFactory.musicCategory(),
      CategoryFactory.artCategory(),
      CategoryFactory.photographyCategory(),
      CategoryFactory.artCategory(),
      CategoryFactory.textilesCategory()
    );


    final List<Category> sorted = new ArrayList<>(categories);
    Collections.sort(sorted);

    final List<Category> expected = Arrays.asList(
      CategoryFactory.artCategory(),
      CategoryFactory.artCategory(),
      CategoryFactory.ceramicsCategory(),
      CategoryFactory.textilesCategory(),
      CategoryFactory.musicCategory(),
      CategoryFactory.bluesCategory(),
      CategoryFactory.worldMusicCategory(),
      CategoryFactory.photographyCategory()
    );

    assertEquals(expected, sorted);
  }

  public void testComparableRootCategories() {
    final Category artCategory = CategoryFactory.artCategory();
    final Category musicCategory = CategoryFactory.musicCategory();

    assertTrue(artCategory.compareTo(musicCategory) <= -1);
    assertTrue(musicCategory.compareTo(artCategory) >= 1);
  }

  public void testComparableRootAndSelf() {
    final Category artCategory = CategoryFactory.artCategory();
    assertTrue(artCategory.compareTo(artCategory) == 0);
  }

  public void testComparableChildAndSelf() {
    final Category bluesCategory = CategoryFactory.bluesCategory();
    assertTrue(bluesCategory.compareTo(bluesCategory) == 0);
  }

  public void testComparableParentAndChildren() {
    final Category musicCategory = CategoryFactory.musicCategory();
    final Category bluesCategory = CategoryFactory.bluesCategory();
    final Category worldMusicCategory = CategoryFactory.worldMusicCategory();

    assertTrue(musicCategory.compareTo(bluesCategory) <= -1);
    assertTrue(bluesCategory.compareTo(musicCategory) >= 1);

    assertTrue(musicCategory.compareTo(worldMusicCategory) <= -1);
    assertTrue(worldMusicCategory.compareTo(musicCategory) >= -1);
  }

  public void testComparableChildrenAndOtherRoot() {
    final Category photographyCategory = CategoryFactory.photographyCategory();
    final Category bluesCategory = CategoryFactory.bluesCategory();
    final Category worldMusicCategory = CategoryFactory.worldMusicCategory();

    assertTrue(bluesCategory.compareTo(photographyCategory) <= -1);
    assertTrue(worldMusicCategory.compareTo(photographyCategory) <= -1);

    assertTrue(photographyCategory.compareTo(bluesCategory) >= 1);
    assertTrue(photographyCategory.compareTo(worldMusicCategory) >= 1);
  }

  public void testComparableChildrenDifferentRoots() {
    final Category bluesCategory = CategoryFactory.bluesCategory();
    final Category textilesCategory = CategoryFactory.textilesCategory();

    assertTrue(bluesCategory.compareTo(textilesCategory) >= 1);
    assertTrue(textilesCategory.compareTo(bluesCategory) <= -1);

    final Category ceramicsCategory = CategoryFactory.ceramicsCategory();
    final Category worldMusicCategory = CategoryFactory.worldMusicCategory();

    assertTrue(ceramicsCategory.compareTo(worldMusicCategory) <= -1);
    assertTrue(worldMusicCategory.compareTo(ceramicsCategory) >= 1);
  }
}
