package com.kickstarter.libs.utils;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

import rx.functions.Func2;

public class ListUtilsTest extends TestCase {

  public void testAllReplaced() {
    final List<Integer> xs = Arrays.asList(1, 2, 2, 4, 5);
    final List<Integer> expected = Arrays.asList(1, 9, 9, 4, 5);

    assertEquals(expected, ListUtils.allReplaced(xs, 2, 9));
  }

  public void testContains() {
    final List<Integer> xs = Arrays.asList(1, 2, 3, 4, 5);
    final Func2<Integer, Integer, Boolean> equality = (x, y) -> (x % 2 == 0 && y % 2 == 0);

    assertTrue(ListUtils.contains(xs, 8, equality));
    assertFalse(ListUtils.contains(xs, 1, equality));
    assertFalse(ListUtils.contains(xs, 7, equality));
  }

  public void testDifference() {
    final List<Integer> xs = Arrays.asList(1, 2, 3, 4, 5);
    final List<Integer> ys = Arrays.asList(1, 4, 6, 4, 5, 7);
    final List<Integer> expected = Arrays.asList(2, 3);

    assertEquals(expected, ListUtils.difference(xs, ys));
  }

  public void testIndexOf() {
    final List<Integer> xs = Arrays.asList(1, 2, 3, 4, 5);
    final Func2<Integer, Integer, Boolean> equality = (x, y) -> (x % 2 == 0 && y % 2 == 0);

    assertEquals(1, ListUtils.indexOf(xs, 2, equality));
    assertEquals(1, ListUtils.indexOf(xs, 4, equality));
    assertEquals(-1, ListUtils.indexOf(xs, 1, equality));
  }

  public void testIntersection() {
    final List<Integer> xs = Arrays.asList(1, 2, 3, 4, 5);
    final List<Integer> ys = Arrays.asList(1, 4, 6, 4, 5, 7);
    final List<Integer> expected = Arrays.asList(1, 4, 5);

    assertEquals(expected, ListUtils.intersection(xs, ys));
  }

  public void testFind() {
    final List<Integer> xs = Arrays.asList(1, 2, 3, 4, 5);
    final Func2<Integer, Integer, Boolean> equality = (x, y) -> (x % 2 == 0 && y % 2 == 0);

    assertEquals(Integer.valueOf(2), ListUtils.find(xs, 2, equality));
    assertEquals(Integer.valueOf(2), ListUtils.find(xs, 4, equality));
    assertEquals(null, ListUtils.find(xs, 1, equality));
  }

  public void testFlatten() {
    final List<List<Integer>> xss = Arrays.asList(
      Arrays.asList(1, 2, 3),
      Arrays.asList(4, 5, 6),
      Arrays.asList(7, 8, 9)
    );
    final List<Integer> xs = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

    assertEquals(xs, ListUtils.flatten(xss));
  }
}
