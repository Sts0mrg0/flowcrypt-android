/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.database.dao.source;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.flowcrypt.email.database.dao.KeysDao;
import com.flowcrypt.email.js.PgpKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class describe creating of table which has name {@link KeysDaoSource#TABLE_NAME_KEYS},
 * add, delete and update rows.
 *
 * @author DenBond7
 * Date: 13.05.2017
 * Time: 12:44
 * E-mail: DenBond7@gmail.com
 */

public class KeysDaoSource extends BaseDaoSource {
  public static final String TABLE_NAME_KEYS = "keys";

  public static final String COL_LONG_ID = "long_id";
  public static final String COL_SOURCE = "source";
  public static final String COL_PUBLIC_KEY = "public_key";
  public static final String COL_PRIVATE_KEY = "private_key";
  public static final String COL_PASSPHRASE = "passphrase";

  public static final String KEYS_TABLE_SQL_CREATE = "CREATE TABLE IF NOT EXISTS " +
      TABLE_NAME_KEYS + " (" +
      BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
      COL_LONG_ID + " VARCHAR(16) NOT NULL, " +
      COL_SOURCE + " VARCHAR(20) NOT NULL, " +
      COL_PUBLIC_KEY + " BLOB NOT NULL, " +
      COL_PRIVATE_KEY + " BLOB NOT NULL, " +
      COL_PASSPHRASE + " varchar(100) DEFAULT NULL " + ");";

  public static final String CREATE_INDEX_LONG_ID_IN_KEYS =
      "CREATE UNIQUE INDEX IF NOT EXISTS " + COL_LONG_ID + "_in_" + TABLE_NAME_KEYS +
          " ON " + TABLE_NAME_KEYS + " (" + COL_LONG_ID + ")";

  @Override
  public String getTableName() {
    return TABLE_NAME_KEYS;
  }

  /**
   * Add information about a key to the database.
   *
   * @param context Interface to global information about an application environment.
   * @param keysDao The {@link KeysDao} object which contain information about a key.
   * @return <tt>{@link Uri}</tt> which contain information about an inserted row or null if the
   * row not inserted.
   */
  public Uri addRow(Context context, KeysDao keysDao) {
    ContentResolver contentResolver = context.getContentResolver();
    if (keysDao != null && contentResolver != null) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(COL_LONG_ID, keysDao.getLongId());
      contentValues.put(COL_SOURCE, keysDao.getPrivateKeySourceType().toString());
      contentValues.put(COL_PUBLIC_KEY, keysDao.getPublicKey());
      contentValues.put(COL_PRIVATE_KEY, keysDao.getPrivateKey());
      contentValues.put(COL_PASSPHRASE, keysDao.getPassphrase());

      return contentResolver.insert(getBaseContentUri(), contentValues);
    } else return null;
  }

  /**
   * Check if the key already exists in the database.
   *
   * @param context Interface to global information about an application environment.
   * @param longId  The key longid parameter.
   * @return <tt>{@link Boolean}</tt> true - if the key already exists in the database, false -
   * otherwise.
   */
  public boolean isKeyExist(Context context, String longId) {
    ContentResolver contentResolver = context.getContentResolver();
    Cursor cursor = contentResolver.query(getBaseContentUri(),
        null, COL_LONG_ID + " = ?", new String[]{longId}, null);

    boolean result = false;

    if (cursor != null) {
      if (cursor.moveToFirst()) {
        result = true;
      }
      cursor.close();
    }

    return result;
  }

  /**
   * Get a list of of avalible keys longids.
   *
   * @param context Interface to global information about an application environment.
   * @return The list of avalible keys longids.
   */
  public List<String> getAllKeysLongIds(Context context) {
    ContentResolver contentResolver = context.getContentResolver();
    Cursor cursor = contentResolver.query(getBaseContentUri(),
        new String[]{COL_LONG_ID}, null, null, null);

    List<String> longIds = new ArrayList<>();

    if (cursor != null) {
      while (cursor.moveToNext()) {
        longIds.add(cursor.getString(cursor.getColumnIndex(COL_LONG_ID)));
      }

      cursor.close();
    }

    return longIds;
  }

  /**
   * Delete information about a private {@link PgpKey}.
   *
   * @param context Interface to global information about an application environment.
   * @param pgpKey  The object which contains information about the private {@link PgpKey}.
   * @return The count of deleted rows. Will be 1 if information about {@link PgpKey} was deleted or -1 otherwise.
   */
  public int removeKey(Context context, PgpKey pgpKey) {
    if (pgpKey != null) {

      ContentResolver contentResolver = context.getContentResolver();
      if (contentResolver != null) {
        return contentResolver.delete(getBaseContentUri(),
            COL_LONG_ID + " = ?", new String[]{pgpKey.getLongid()});
      } else return -1;
    } else return -1;
  }

  /**
   * This method update information about some private keys.
   *
   * @param context           Interface to global information about an application environment.
   * @param keysDaoCollection The list of {@link KeysDao} which contains information about the private keys.
   * @return the {@link ContentProviderResult} array.
   */
  public ContentProviderResult[] updateKeys(Context context, Collection<KeysDao> keysDaoCollection) throws Exception {
    ContentResolver contentResolver = context.getContentResolver();
    if (keysDaoCollection != null && contentResolver != null) {
      ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>();
      for (KeysDao keysDao : keysDaoCollection) {
        contentProviderOperations.add(ContentProviderOperation.newUpdate(getBaseContentUri())
            .withValue(COL_PRIVATE_KEY, keysDao.getPrivateKey())
            .withValue(COL_PASSPHRASE, keysDao.getPassphrase())
            .withSelection(COL_LONG_ID + "= ?", new String[]{keysDao.getLongId()})
            .withYieldAllowed(true)
            .build());
      }
      return contentResolver.applyBatch(getBaseContentUri().getAuthority(), contentProviderOperations);
    } else return new ContentProviderResult[0];
  }
}
