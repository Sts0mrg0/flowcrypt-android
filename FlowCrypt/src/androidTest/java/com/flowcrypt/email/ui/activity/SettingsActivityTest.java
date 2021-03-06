/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.activity;

import com.flowcrypt.email.R;
import com.flowcrypt.email.base.BaseTest;
import com.flowcrypt.email.rules.AddAccountToDatabaseRule;
import com.flowcrypt.email.rules.ClearAppSettingsRule;
import com.flowcrypt.email.ui.activity.settings.SettingsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * @author Denis Bondarenko
 * Date: 20.02.2018
 * Time: 15:42
 * E-mail: DenBond7@gmail.com
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest extends BaseTest {

  @Rule
  public TestRule ruleChain = RuleChain
      .outerRule(new ClearAppSettingsRule())
      .around(new AddAccountToDatabaseRule())
      .around(new ActivityTestRule<>(SettingsActivity.class));

  @Test
  public void testShowHelpScreen() {
    testHelpScreen();
  }

  @Test
  public void testShowBackupsScreen() {
    checkIsScreenDisplaying(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string
        .backups));
  }

  @Test
  public void testShowSecurityScreen() {
    checkIsScreenDisplaying(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string
        .security));
  }

  @Test
  public void testShowContactsScreen() {
    checkIsScreenDisplaying(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.contacts));
  }

  @Test
  public void testShowKeysScreen() {
    checkIsScreenDisplaying(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.keys));
  }

  @Test
  public void testShowAttesterScreen() {
    checkIsScreenDisplaying(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.attester));
  }

  @Test
  public void testShowLegalScreen() {
    checkIsScreenDisplaying(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string
            .experimental),
        InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.experimental_settings));
  }

  private void checkIsScreenDisplaying(String screenName) {
    checkIsScreenDisplaying(screenName, screenName);
  }

  private void checkIsScreenDisplaying(String commandName, String screenName) {
    onView(withText(commandName)).check(matches(isDisplayed())).perform(click());
    onView(allOf(withText(screenName), withParent(withId(R.id.toolbar)))).check(matches(isDisplayed()));
  }
}