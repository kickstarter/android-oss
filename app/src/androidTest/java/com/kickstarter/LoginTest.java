package com.kickstarter;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.kickstarter.ui.activities.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest {
  @Rule
  public ActivityTestRule<LoginActivity> activityRule =
    new ActivityTestRule<>(LoginActivity.class);

  @Test
  public void enteringEmailAndPasswordEnablesLoginButton() {
    onView(withId(R.id.login_button)).check(matches(not(isEnabled())));

    onView(withId(R.id.email)).perform(typeText("test@kickstarter.com"));
    onView(withId(R.id.password)).perform(typeText("password"));

    onView(withId(R.id.login_button)).check(matches(isEnabled()));
  }
}
