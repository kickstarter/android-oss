package com.kickstarter.libs.utils;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

import rx.functions.Func2;

public class DiffUtilsTest extends TestCase {

  public void testDiff_WithExpandingSection() {
    final List<Integer> oldItems = Arrays.asList(1, 2, 3, 4, 5);
    final List<Integer> newItems = Arrays.asList(1, 2, 3, 31, 32, 33, 4, 5);

    final DiffUtils.Diff expected = DiffUtils.Diff.builder()
      .insertions(Arrays.asList(3, 4, 5))
      .build();

    assertEquals(expected, DiffUtils.diff(oldItems, newItems));
  }

  public void testDiff_WithCollapsingSection() {
    final List<Integer> oldItems = Arrays.asList(1, 2, 3, 31, 32, 33, 4, 5);
    final List<Integer> newItems = Arrays.asList(1, 2, 3, 4, 5);

    final DiffUtils.Diff expected = DiffUtils.Diff.builder()
      .deletions(Arrays.asList(3, 4, 5))
      .build();

    assertEquals(expected, DiffUtils.diff(oldItems, newItems));
  }

  public void testDiff_WithChangedSingleItem() {
    final List<Integer> oldItems = Arrays.asList(1, 2, 3, 4, 5);
    final List<Integer> newItems = Arrays.asList(1, 2, 6, 4, 5);

    final DiffUtils.Diff expected = DiffUtils.Diff.builder()
      .insertions(Arrays.asList(2))
      .deletions(Arrays.asList(2))
      .build();

    assertEquals(expected, DiffUtils.diff(oldItems, newItems));
  }

  public void testDiff_WithTheWholeEnchilada() {
    final List<Integer> oldItems = Arrays.asList(1, 2, 3, 40, 41, 42, 43, 5);
    final List<Integer> newItems = Arrays.asList(1, 2, 6, 61, 62, 63, 4, 5);
    final Func2<Integer, Integer, Boolean> equality = (x, y) -> x.equals(y) || (x == 4 && y == 40) || (x == 40 && y == 4);

    final DiffUtils.Diff expected = DiffUtils.Diff.builder()
      .insertions(Arrays.asList(2, 3, 4, 5))
      .deletions(Arrays.asList(2, 4, 5, 6))
      .updates(Arrays.asList(3))
      .build();

    assertEquals(expected, DiffUtils.diff(oldItems, newItems, equality));
  }
}
