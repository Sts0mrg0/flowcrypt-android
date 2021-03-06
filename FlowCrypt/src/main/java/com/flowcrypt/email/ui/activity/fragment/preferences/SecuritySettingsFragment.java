/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.activity.fragment.preferences;

import android.os.Bundle;

import com.flowcrypt.email.Constants;
import com.flowcrypt.email.R;
import com.flowcrypt.email.database.dao.source.AccountDao;
import com.flowcrypt.email.database.dao.source.AccountDaoSource;
import com.flowcrypt.email.database.dao.source.UserIdEmailsKeysDaoSource;
import com.flowcrypt.email.ui.activity.ChangePassPhraseActivity;
import com.flowcrypt.email.ui.activity.fragment.base.BasePreferenceFragment;
import com.flowcrypt.email.util.UIUtil;

import androidx.preference.Preference;

/**
 * This fragment contains actions which related to Security options.
 *
 * @author DenBond7
 * Date: 08.08.2018.
 * Time: 10:47.
 * E-mail: DenBond7@gmail.com
 */
public class SecuritySettingsFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener {
  private AccountDao accountDao;

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.preferences_security_settings);

    accountDao = new AccountDaoSource().getActiveAccountInformation(getContext());

    Preference preferenceChangePassPhrase = findPreference(Constants.PREFERENCES_KEY_SECURITY_CHANGE_PASS_PHRASE);
    if (preferenceChangePassPhrase != null) {
      preferenceChangePassPhrase.setOnPreferenceClickListener(this);
    }
  }

  @Override
  public boolean onPreferenceClick(Preference preference) {
    switch (preference.getKey()) {
      case Constants.PREFERENCES_KEY_SECURITY_CHANGE_PASS_PHRASE:
        if (new UserIdEmailsKeysDaoSource().getLongIdsByEmail(getContext(), accountDao.getEmail()).isEmpty()) {
          UIUtil.showInfoSnackbar(getView(), getString(R.string.account_has_no_associated_keys, getString
              (R.string.support_email)));
        } else {
          startActivity(ChangePassPhraseActivity.newIntent(getContext(), accountDao));
        }
        return true;

      default:
        return false;
    }
  }
}
