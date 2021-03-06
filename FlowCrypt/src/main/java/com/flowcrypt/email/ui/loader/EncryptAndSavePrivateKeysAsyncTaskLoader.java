/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.ui.loader;

import android.content.Context;
import android.net.Uri;
import android.util.Pair;

import com.eclipsesource.v8.V8Object;
import com.flowcrypt.email.R;
import com.flowcrypt.email.database.dao.KeysDao;
import com.flowcrypt.email.database.dao.source.ContactsDaoSource;
import com.flowcrypt.email.database.dao.source.KeysDaoSource;
import com.flowcrypt.email.database.dao.source.UserIdEmailsKeysDaoSource;
import com.flowcrypt.email.js.Js;
import com.flowcrypt.email.js.PgpContact;
import com.flowcrypt.email.js.PgpKey;
import com.flowcrypt.email.model.KeyDetails;
import com.flowcrypt.email.model.results.LoaderResult;
import com.flowcrypt.email.security.KeyStoreCryptoManager;
import com.flowcrypt.email.util.exception.ExceptionUtil;
import com.flowcrypt.email.util.exception.KeyAlreadyAddedException;

import java.util.ArrayList;
import java.util.List;

import androidx.loader.content.AsyncTaskLoader;

/**
 * This loader try to encrypt and save encrypted key with entered password by
 * {@link KeyStoreCryptoManager} to the database.
 * <p>
 * Return true if one or more key saved, false otherwise;
 *
 * @author DenBond7
 * Date: 03.05.2017
 * Time: 11:47
 * E-mail: DenBond7@gmail.com
 */

public class EncryptAndSavePrivateKeysAsyncTaskLoader extends AsyncTaskLoader<LoaderResult> {
  private static final String KEY_SUCCESS = "success";

  private List<KeyDetails> privateKeyDetailsList;
  private String passphrase;

  private KeysDaoSource keysDaoSource;

  public EncryptAndSavePrivateKeysAsyncTaskLoader(Context context, ArrayList<KeyDetails> privateKeyDetailsList,
                                                  String passphrase) {
    super(context);
    this.privateKeyDetailsList = privateKeyDetailsList;
    this.passphrase = passphrase;
    this.keysDaoSource = new KeysDaoSource();
    onContentChanged();
  }

  @Override
  public LoaderResult loadInBackground() {
    List<KeyDetails> acceptedKeysList = new ArrayList<>();
    try {
      KeyStoreCryptoManager keyStoreCryptoManager = new KeyStoreCryptoManager(getContext());
      Js js = new Js(getContext(), null);
      for (KeyDetails keyDetails : privateKeyDetailsList) {
        String armoredPrivateKey = keyDetails.getValue();
        String normalizedArmoredKey = js.crypto_key_normalize(armoredPrivateKey);

        PgpKey pgpKey = js.crypto_key_read(normalizedArmoredKey);
        V8Object v8Object = js.crypto_key_decrypt(pgpKey, passphrase);

        if (pgpKey.isPrivate()) {
          if (v8Object != null && v8Object.getBoolean(KEY_SUCCESS)) {
            if (!keysDaoSource.isKeyExist(getContext(), pgpKey.getLongid())) {
              Uri uri = keysDaoSource.addRow(getContext(),
                  KeysDao.generateKeysDao(keyStoreCryptoManager, keyDetails, pgpKey, passphrase));

              PgpContact[] pgpContacts = pgpKey.getUserIds();
              List<Pair<String, String>> pairs = new ArrayList<>();
              if (pgpContacts != null) {
                ContactsDaoSource contactsDaoSource = new ContactsDaoSource();

                for (PgpContact pgpContact : pgpContacts) {
                  if (pgpContact != null) {
                    PgpKey publicKey = pgpKey.toPublic();
                    pgpContact.setPubkey(publicKey.armor());
                    if (js.str_is_email_valid(pgpContact.getEmail()) &&
                        contactsDaoSource.getPgpContact(getContext(), pgpContact.getEmail())
                            == null) {
                      new ContactsDaoSource().addRow(getContext(), pgpContact);
                      //todo-DenBond7 Need to resolve a situation with different public keys.
                      //For example we can have a situation when we have to different public
                      // keys with the same email
                    }

                    pairs.add(Pair.create(pgpKey.getLongid(), pgpContact.getEmail()));
                  }
                }
              }

              if (uri != null) {
                acceptedKeysList.add(keyDetails);
                UserIdEmailsKeysDaoSource userIdEmailsKeysDaoSource = new UserIdEmailsKeysDaoSource();

                for (Pair<String, String> pair : pairs) {
                  userIdEmailsKeysDaoSource.addRow(getContext(), pair.first, pair.second);
                }
              }
            } else if (privateKeyDetailsList.size() == 1) {
              return new LoaderResult(null,
                  new KeyAlreadyAddedException(keyDetails,
                      getContext().getString(R.string.the_key_already_added)));
            } else {
              acceptedKeysList.add(keyDetails);
            }
          } else if (privateKeyDetailsList.size() == 1) {
            return new LoaderResult(null,
                new IllegalArgumentException(getContext().getString(R.string.password_is_incorrect)));
          }
        } else if (privateKeyDetailsList.size() == 1) {
          return new LoaderResult(null,
              new IllegalArgumentException(getContext().getString(R.string.not_private_key)));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      ExceptionUtil.handleError(e);
      return new LoaderResult(null, e);
    }

    return new LoaderResult(acceptedKeysList, null);
  }

  @Override
  public void onStartLoading() {
    if (takeContentChanged()) {
      forceLoad();
    }
  }

  @Override
  public void onStopLoading() {
    cancelLoad();
  }
}
