/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.scenarios.setup;


import com.flowcrypt.email.R;
import com.flowcrypt.email.TestConstants;
import com.flowcrypt.email.util.PrivateKeysManager;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * This class tests the condition when a user has some private key backups (email).
 *
 * @author Denis Bondarenko
 * Date: 27.12.2017
 * Time: 13:05
 * E-mail: DenBond7@gmail.com
 */

public abstract class SignInWithBackupStandardAuthTest extends SignInWithStandardAuthTest {
  @Test
  public void testAllConditionsTrue() throws Exception {
    onView(withId(R.id.buttonOtherEmailProvider)).perform(click());
    fillAllFields();
    onView(withId(R.id.buttonTryToConnect)).perform(click());

    checkRightHeader();
    typeAndCheckPrivateKeyPassword(TestConstants.DEFAULT_PASSWORD);
    onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
  }

  @Test
  public void testPasswordIncorrect() throws Exception {
    onView(withId(R.id.buttonOtherEmailProvider)).perform(click());
    fillAllFields();
    onView(withId(R.id.buttonTryToConnect)).perform(click());

    checkRightHeader();
    typeAndCheckPrivateKeyPassword("password");
    onView(withText(R.string.password_is_incorrect)).check(matches(isDisplayed()));
  }

  @Test
  public void testUseAnotherAccount() throws Exception {
    onView(withId(R.id.buttonOtherEmailProvider)).perform(click());
    fillAllFields();
    onView(withId(R.id.buttonTryToConnect)).perform(click());

    checkRightHeader();
    onView(withId(R.id.editTextKeyPassword)).perform(closeSoftKeyboard());
    onView(withText(R.string.use_another_account)).perform(click());
    onView(withId(R.id.buttonOtherEmailProvider)).check(matches(isDisplayed()));
  }

  @Test
  public void testUseExistingKey() throws Exception {
    PrivateKeysManager.addTempPrivateKey();

    onView(withId(R.id.buttonOtherEmailProvider)).perform(click());
    fillAllFields();
    onView(withId(R.id.buttonTryToConnect)).perform(click());

    checkRightHeader();
    onView(withId(R.id.editTextKeyPassword)).perform(closeSoftKeyboard());
    onView(withText(R.string.use_existing_keys)).perform(click());

    onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
  }

  private void typeAndCheckPrivateKeyPassword(String password) {
    onView(withId(R.id.editTextKeyPassword)).perform(clearText(), typeText(password),
        closeSoftKeyboard());
    onView(withId(R.id.buttonPositiveAction)).perform(click());
  }

  private void checkRightHeader() {
    onView(withId(R.id.textViewSubTitle)).check(matches(isDisplayed()));
  }
}
