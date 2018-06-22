/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.activity;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.flowcrypt.email.R;
import com.flowcrypt.email.assertions.RecyclerViewItemCountAssertion;
import com.flowcrypt.email.base.BaseTest;
import com.flowcrypt.email.database.dao.source.ContactsDaoSource;
import com.flowcrypt.email.js.PgpContact;
import com.flowcrypt.email.rules.AddAccountToDatabaseRule;
import com.flowcrypt.email.rules.ClearAppSettingsRule;
import com.flowcrypt.email.util.TestGeneralUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

/**
 * @author Denis Bondarenko
 * Date: 21.05.2018
 * Time: 14:50
 * E-mail: DenBond7@gmail.com
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class PreviewImportPgpContactActivityTest extends BaseTest {
    private ActivityTestRule activityTestRule = new ActivityTestRule<PreviewImportPgpContactActivity>
            (PreviewImportPgpContactActivity.class, false, false) {
    };

    @Rule
    public TestRule ruleChain = RuleChain
            .outerRule(new ClearAppSettingsRule())
            .around(new AddAccountToDatabaseRule())
            .around(activityTestRule);

    @Test
    public void testShowHelpScreen() {
        activityTestRule.launchActivity(PreviewImportPgpContactActivity.newIntent(InstrumentationRegistry
                .getTargetContext(), getSinglePublicKeyForUnsavedContact()));
        testHelpScreen();
    }

    @Test
    public void testIsDisplayedSingleItem() {
        PgpContact pgpContact = new PgpContact("default@denbond7.com", null,
                getSinglePublicKeyForUnsavedContact(), true, null, false, null, null, null, 0);
        new ContactsDaoSource().addRow(InstrumentationRegistry.getTargetContext(), pgpContact);
        activityTestRule.launchActivity(PreviewImportPgpContactActivity.newIntent(InstrumentationRegistry
                .getTargetContext(), getSinglePublicKeyForUnsavedContact()));
        onView(withId(R.id.textViewAlreadyImported)).check(matches(isDisplayed()));
    }

    @Test
    public void testIsDisplayedLabelAlreadyImported() {
        activityTestRule.launchActivity(PreviewImportPgpContactActivity.newIntent(InstrumentationRegistry
                .getTargetContext(), getSinglePublicKeyForUnsavedContact()));
        onView(withId(R.id.recyclerViewContacts)).check(new RecyclerViewItemCountAssertion(1));
    }

    @Test
    public void testSaveButtonForSingleContact() {
        activityTestRule.launchActivity(PreviewImportPgpContactActivity.newIntent(InstrumentationRegistry
                .getTargetContext(), getSinglePublicKeyForUnsavedContact()));
        onView(withId(R.id.recyclerViewContacts)).check(new RecyclerViewItemCountAssertion(1));
        onView(withId(R.id.buttonSaveContact)).check(matches(isDisplayed())).perform(click());
        checkIsToastDisplayed(activityTestRule.getActivity(),
                InstrumentationRegistry.getTargetContext().getString(R.string.contact_successfully_saved));
        onView(withId(R.id.buttonSaveContact)).check(matches(not(isDisplayed())));
        onView(withId(R.id.textViewAlreadyImported)).check(matches(isDisplayed()));
    }

    @Test
    public void testIsImportAllButtonDisplayed() {
        activityTestRule.launchActivity(PreviewImportPgpContactActivity.newIntent(InstrumentationRegistry
                .getTargetContext(), get10PublicKeysForUnsavedContacts()));
        onView(withId(R.id.buttonImportAll)).check(matches(isDisplayed()));
    }

    @Test
    public void testLoadLotOfContacts() {
        int countOfKeys = 10;

        activityTestRule.launchActivity(PreviewImportPgpContactActivity.newIntent(InstrumentationRegistry
                .getTargetContext(), get10PublicKeysForUnsavedContacts()));
        onView(withId(R.id.recyclerViewContacts)).check(new RecyclerViewItemCountAssertion(countOfKeys))
                .perform(RecyclerViewActions.scrollToPosition(countOfKeys - 1));
    }

    private String getSinglePublicKeyForUnsavedContact() {
        try {
            return TestGeneralUtil.readFileFromAssetsAsString(InstrumentationRegistry.getContext(),
                    "pgp/default@denbond7.com_pub.asc");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String get10PublicKeysForUnsavedContacts() {
        try {
            return TestGeneralUtil.readFileFromAssetsAsString(InstrumentationRegistry.getContext(),
                    "pgp/ten_public_keys.asc");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}