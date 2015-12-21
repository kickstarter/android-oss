package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;

import org.junit.Test;
import org.robolectric.annotation.Config;

public class KSStringTest extends KSRobolectricTestCase {
  @Test
  public void testFormat_oneSubstitution() {
    final String string = "by %{name}";
    assertEquals("by christopher", ksString().format(string, "name", "christopher"));
  }

  @Test
  public void testFormat_twoSubstitutions() {
    final String string = "%{remaining} of %{total}";
    assertEquals("1 of 5", ksString().format(string,
      "remaining", "1",
      "total", "5"
    ));
  }

  @Test
  public void testFormat_threeSubstitutions() {
    final String string = "%{one}, %{two} and %{three}";
    assertEquals("a, b and c", ksString().format(string,
      "one", "a",
      "two", "b",
      "three", "c"
    ));
  }

  @Test
  public void testFormat_fourSubstitutions() {
    final String string = "%{one}, %{two}, %{three} and %{four}";
    assertEquals("a, b, c and d", ksString().format(string,
      "one", "a",
      "two", "b",
      "three", "c",
      "four", "d"
    ));
  }

  @Test
  public void testFormat_replaceWithNullValue() {
    final String string = "search term: %{term}";
    assertEquals("search term: ", ksString().format(string, "term", null));
  }

  @Test
  public void testFormat_invalidKey() {
    final String string = "by %{name}";
    assertEquals("by %{name}", ksString().format(string, "invalid_key", "foo"));
  }

  /**
   * Catch issue with regexp substitution where `$` needs to be escaped.
   */
  @Test
  public void testFormat_replaceWithValueContainingDollarSign() {
    final String string = "pledged of %{goal}";
    assertEquals("pledged of $100", ksString().format(string, "goal", "$100"));
  }

  @Test
  public void testFormat_replaceStringContainingHtml() {
    final String string = "by <u>%{creator_name}</u>";
    assertEquals("by <u>Christopher</u>", ksString().format(string, "creator_name", "Christopher"));
  }

  @Test
  public void testFormat_count() {
    final String keyPath = "dates_time_days";
    final KSString ksString = ksString();

    assertEquals("10 days", ksString.format(keyPath, 10, "time_count", "10"));
    assertEquals("3 days", ksString.format(keyPath, 3, "time_count", "3"));
    assertEquals("1 day", ksString.format(keyPath, 1, "time_count", "1"));
    assertEquals("2 days", ksString.format(keyPath, 2, "time_count", "2"));
    assertEquals("0 days", ksString.format(keyPath, 0, "time_count", "0"));
  }

  @Test
  public void testFormat_countWithNoResource() {
    final String keyPath = "dates_time_days";
    final KSString ksString = ksString();

    assertEquals("", ksString.format(keyPath, -1, "time_count", "-1"));
  }

  @Test
  @Config(qualifiers="de")
  public void testFormat_german() {
    assertEquals("von Kristof", ksString().format(application().getString(R.string.project_creator_by_creator),
      "creator_name", "Kristof"));
  }
}
