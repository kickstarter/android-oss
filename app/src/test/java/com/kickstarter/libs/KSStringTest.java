package com.kickstarter.libs;

import junit.framework.TestCase;

public class KSStringTest extends TestCase {
  public void testFormat_oneSubstitution() {
    final String string = "by %{name}";
    assertEquals("by christopher", KSString.format(string, "name", "christopher"));
  }

  public void testFormat_twoSubstitutions() {
    final String string = "%{remaining} of %{total}";
    assertEquals("1 of 5", KSString.format(string,
      "remaining", "1",
      "total", "5"
    ));
  }

  public void testFormat_threeSubstitutions() {
    final String string = "%{one}, %{two} and %{three}";
    assertEquals("a, b and c", KSString.format(string,
      "one", "a",
      "two", "b",
      "three", "c"
    ));
  }

  public void testFormat_fourSubstitutions() {
    final String string = "%{one}, %{two}, %{three} and %{four}";
    assertEquals("a, b, c and d", KSString.format(string,
      "one", "a",
      "two", "b",
      "three", "c",
      "four", "d"
    ));
  }

  public void testFormat_replaceWithNullValue() {
    final String string = "search term: %{term}";
    assertEquals("search term: ", KSString.format(string, "term", null));
  }

  public void testFormat_invalidKey() {
    final String string = "by %{name}";
    assertEquals("by %{name}", KSString.format(string, "invalid_key", "foo"));
  }
}
