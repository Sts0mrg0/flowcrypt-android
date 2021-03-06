/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.flowcrypt.email.database.dao.source.AccountAliasesDaoSource;
import com.flowcrypt.email.database.dao.source.AccountDaoSource;
import com.flowcrypt.email.database.dao.source.ActionQueueDaoSource;
import com.flowcrypt.email.database.dao.source.ContactsDaoSource;
import com.flowcrypt.email.database.dao.source.KeysDaoSource;
import com.flowcrypt.email.database.dao.source.UserIdEmailsKeysDaoSource;
import com.flowcrypt.email.database.dao.source.imap.AttachmentDaoSource;
import com.flowcrypt.email.database.dao.source.imap.ImapLabelsDaoSource;
import com.flowcrypt.email.database.dao.source.imap.MessageDaoSource;
import com.flowcrypt.email.service.actionqueue.actions.FillUserIdEmailsKeysTableAction;


/**
 * A helper class to manage database creation and version management.
 *
 * @author DenBond7
 * Date: 13.05.2017
 * Time: 12:20
 * E-mail: DenBond7@gmail.com
 */
public class FlowCryptSQLiteOpenHelper extends SQLiteOpenHelper {
  public static final String COLUMN_NAME_COUNT = "COUNT(*)";
  public static final String DB_NAME = "flowcrypt.db";
  public static final int DB_VERSION = 12;

  private static final String TAG = FlowCryptSQLiteOpenHelper.class.getSimpleName();
  private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

  private Context context;

  public FlowCryptSQLiteOpenHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    this.context = context;
  }

  public static void dropTable(SQLiteDatabase sqLiteDatabase, String tableName) {
    sqLiteDatabase.execSQL(DROP_TABLE + tableName);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.execSQL(KeysDaoSource.KEYS_TABLE_SQL_CREATE);
    sqLiteDatabase.execSQL(KeysDaoSource.CREATE_INDEX_LONG_ID_IN_KEYS);

    sqLiteDatabase.execSQL(ContactsDaoSource.CONTACTS_TABLE_SQL_CREATE);
    sqLiteDatabase.execSQL(ContactsDaoSource.CREATE_INDEX_EMAIL_IN_CONTACT);
    sqLiteDatabase.execSQL(ContactsDaoSource.CREATE_INDEX_NAME_IN_CONTACT);
    sqLiteDatabase.execSQL(ContactsDaoSource.CREATE_INDEX_HAS_PGP_IN_CONTACT);
    sqLiteDatabase.execSQL(ContactsDaoSource.CREATE_INDEX_LONG_ID_IN_CONTACT);
    sqLiteDatabase.execSQL(ContactsDaoSource.CREATE_INDEX_LAST_USE_IN_CONTACT);

    sqLiteDatabase.execSQL(ImapLabelsDaoSource.IMAP_LABELS_TABLE_SQL_CREATE);

    sqLiteDatabase.execSQL(MessageDaoSource.IMAP_MESSAGES_INFO_TABLE_SQL_CREATE);
    sqLiteDatabase.execSQL(MessageDaoSource.CREATE_INDEX_EMAIL_IN_MESSAGES);
    sqLiteDatabase.execSQL(MessageDaoSource.CREATE_INDEX_EMAIL_UID_FOLDER_IN_MESSAGES);

    sqLiteDatabase.execSQL(AccountDaoSource.ACCOUNTS_TABLE_SQL_CREATE);
    sqLiteDatabase.execSQL(AccountDaoSource.CREATE_INDEX_EMAIL_TYPE_IN_ACCOUNTS);

    sqLiteDatabase.execSQL(AttachmentDaoSource.ATTACHMENT_TABLE_SQL_CREATE);
    sqLiteDatabase.execSQL(AttachmentDaoSource.CREATE_INDEX_EMAIL_UID_FOLDER_IN_ATTACHMENT);

    sqLiteDatabase.execSQL(AccountAliasesDaoSource.ACCOUNTS_ALIASES_TABLE_SQL_CREATE);
    sqLiteDatabase.execSQL(AccountAliasesDaoSource.CREATE_INDEX_EMAIL_TYPE_IN_ACCOUNTS_ALIASES);

    sqLiteDatabase.execSQL(ActionQueueDaoSource.ACTION_QUEUE_TABLE_SQL_CREATE);

    sqLiteDatabase.execSQL(UserIdEmailsKeysDaoSource.SQL_CREATE_TABLE);
    sqLiteDatabase.execSQL(UserIdEmailsKeysDaoSource.INDEX_LONG_ID__USER_ID_EMAIL);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    switch (oldVersion) {
      case 1:
        upgradeDatabaseFrom1To2Version(sqLiteDatabase);
        upgradeDatabaseFrom2To3Version(sqLiteDatabase);
        upgradeDatabaseFrom3To4Version(sqLiteDatabase);
        upgradeDatabaseFrom4To5Version(sqLiteDatabase);
        upgradeDatabaseFrom5To6Version(sqLiteDatabase);
        upgradeDatabaseFrom6To7Version(sqLiteDatabase);
        upgradeDatabaseFrom7To8Version(sqLiteDatabase);
        upgradeDatabaseFrom8To9Version(sqLiteDatabase);
        upgradeDatabaseFrom9To10Version(sqLiteDatabase);
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 2:
        upgradeDatabaseFrom2To3Version(sqLiteDatabase);
        upgradeDatabaseFrom3To4Version(sqLiteDatabase);
        upgradeDatabaseFrom4To5Version(sqLiteDatabase);
        upgradeDatabaseFrom5To6Version(sqLiteDatabase);
        upgradeDatabaseFrom6To7Version(sqLiteDatabase);
        upgradeDatabaseFrom7To8Version(sqLiteDatabase);
        upgradeDatabaseFrom8To9Version(sqLiteDatabase);
        upgradeDatabaseFrom9To10Version(sqLiteDatabase);
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 3:
        upgradeDatabaseFrom3To4Version(sqLiteDatabase);
        upgradeDatabaseFrom4To5Version(sqLiteDatabase);
        upgradeDatabaseFrom5To6Version(sqLiteDatabase);
        upgradeDatabaseFrom6To7Version(sqLiteDatabase);
        upgradeDatabaseFrom7To8Version(sqLiteDatabase);
        upgradeDatabaseFrom8To9Version(sqLiteDatabase);
        upgradeDatabaseFrom9To10Version(sqLiteDatabase);
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 4:
        upgradeDatabaseFrom4To5Version(sqLiteDatabase);
        upgradeDatabaseFrom5To6Version(sqLiteDatabase);
        upgradeDatabaseFrom6To7Version(sqLiteDatabase);
        upgradeDatabaseFrom7To8Version(sqLiteDatabase);
        upgradeDatabaseFrom8To9Version(sqLiteDatabase);
        upgradeDatabaseFrom9To10Version(sqLiteDatabase);
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 5:
        upgradeDatabaseFrom5To6Version(sqLiteDatabase);
        upgradeDatabaseFrom6To7Version(sqLiteDatabase);
        upgradeDatabaseFrom7To8Version(sqLiteDatabase);
        upgradeDatabaseFrom8To9Version(sqLiteDatabase);
        upgradeDatabaseFrom9To10Version(sqLiteDatabase);
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 6:
        upgradeDatabaseFrom6To7Version(sqLiteDatabase);
        upgradeDatabaseFrom7To8Version(sqLiteDatabase);
        upgradeDatabaseFrom8To9Version(sqLiteDatabase);
        upgradeDatabaseFrom9To10Version(sqLiteDatabase);
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 7:
        upgradeDatabaseFrom7To8Version(sqLiteDatabase);
        upgradeDatabaseFrom8To9Version(sqLiteDatabase);
        upgradeDatabaseFrom9To10Version(sqLiteDatabase);
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 8:
        upgradeDatabaseFrom8To9Version(sqLiteDatabase);
        upgradeDatabaseFrom9To10Version(sqLiteDatabase);
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 9:
        upgradeDatabaseFrom9To10Version(sqLiteDatabase);
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 10:
        upgradeDatabaseFrom10To11Version(sqLiteDatabase);
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;

      case 11:
        upgradeDatabaseFrom11To12Version(sqLiteDatabase);
        break;
    }

    Log.d(TAG, "Database updated from OLD_VERSION = " + Integer.toString(oldVersion)
        + " to NEW_VERSION = " + Integer.toString(newVersion));
  }

  private void upgradeDatabaseFrom1To2Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL(AttachmentDaoSource.ATTACHMENT_TABLE_SQL_CREATE);
      sqLiteDatabase.execSQL(AttachmentDaoSource.CREATE_INDEX_EMAIL_UID_FOLDER_IN_ATTACHMENT);

      sqLiteDatabase.execSQL("ALTER TABLE " + MessageDaoSource.TABLE_NAME_MESSAGES +
          " ADD COLUMN " + MessageDaoSource.COL_IS_MESSAGE_HAS_ATTACHMENTS
          + " INTEGER DEFAULT 0;");

      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_IS_ENABLE
          + " INTEGER DEFAULT 1;");

      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_IS_ACTIVE
          + " INTEGER DEFAULT 0;");
      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom2To3Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      dropTable(sqLiteDatabase, AttachmentDaoSource.TABLE_NAME_ATTACHMENT);
      sqLiteDatabase.execSQL(AttachmentDaoSource.ATTACHMENT_TABLE_SQL_CREATE);
      sqLiteDatabase.execSQL(AttachmentDaoSource.CREATE_INDEX_EMAIL_UID_FOLDER_IN_ATTACHMENT);

      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom3To4Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_USERNAME + " TEXT NOT NULL DEFAULT '';");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_PASSWORD + " TEXT NOT NULL DEFAULT '';");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_IMAP_SERVER + " TEXT NOT NULL DEFAULT '';");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_IMAP_PORT + " INTEGER DEFAULT 143;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_IMAP_IS_USE_SSL_TLS + " INTEGER DEFAULT 0;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_IMAP_IS_USE_STARTTLS + " INTEGER DEFAULT 0;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_IMAP_AUTH_MECHANISMS + " TEXT;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_SMTP_SERVER + " TEXT NOT NULL DEFAULT '';");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_SMTP_PORT + " INTEGER DEFAULT 25;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_SMTP_IS_USE_SSL_TLS + " INTEGER DEFAULT 0;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_SMTP_IS_USE_STARTTLS + " INTEGER DEFAULT 0;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_SMTP_AUTH_MECHANISMS + " TEXT;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_SMTP_IS_USE_CUSTOM_SIGN + " INTEGER DEFAULT 0;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_SMTP_USERNAME + " TEXT DEFAULT NULL;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_SMTP_PASSWORD + " TEXT DEFAULT NULL;");

      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom4To5Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL(AccountAliasesDaoSource.ACCOUNTS_ALIASES_TABLE_SQL_CREATE);
      sqLiteDatabase.execSQL(AccountAliasesDaoSource.CREATE_INDEX_EMAIL_TYPE_IN_ACCOUNTS_ALIASES);
      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom5To6Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL("DROP INDEX IF EXISTS email_account_type_in_accounts_aliases");
      sqLiteDatabase.execSQL(AccountAliasesDaoSource.CREATE_INDEX_EMAIL_TYPE_IN_ACCOUNTS_ALIASES);
      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom6To7Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL(ActionQueueDaoSource.ACTION_QUEUE_TABLE_SQL_CREATE);
      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom7To8Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_IS_CONTACTS_LOADED
          + " INTEGER DEFAULT 0;");
      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom8To9Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL("ALTER TABLE " + MessageDaoSource.TABLE_NAME_MESSAGES +
          " ADD COLUMN " + MessageDaoSource.COL_IS_ENCRYPTED
          + " INTEGER DEFAULT -1;");
      sqLiteDatabase.execSQL("ALTER TABLE " + MessageDaoSource.TABLE_NAME_MESSAGES +
          " ADD COLUMN " + MessageDaoSource.COL_CC_ADDRESSES + " TEXT DEFAULT NULL;");
      sqLiteDatabase.execSQL("ALTER TABLE " + MessageDaoSource.TABLE_NAME_MESSAGES +
          " ADD COLUMN " + MessageDaoSource.COL_IS_NEW + " INTEGER DEFAULT 0;");

      sqLiteDatabase.execSQL("ALTER TABLE " + AccountDaoSource.TABLE_NAME_ACCOUNTS +
          " ADD COLUMN " + AccountDaoSource.COL_IS_SHOW_ONLY_ENCRYPTED + " INTEGER DEFAULT 0;");

      sqLiteDatabase.execSQL(UserIdEmailsKeysDaoSource.SQL_CREATE_TABLE);
      sqLiteDatabase.execSQL(UserIdEmailsKeysDaoSource.INDEX_LONG_ID__USER_ID_EMAIL);
      new ActionQueueDaoSource().addAction(sqLiteDatabase, new FillUserIdEmailsKeysTableAction());

      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom9To10Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL("ALTER TABLE " + MessageDaoSource.TABLE_NAME_MESSAGES +
          " ADD COLUMN " + MessageDaoSource.COL_STATE + " INTEGER DEFAULT -1;");
      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom10To11Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL("ALTER TABLE " + AttachmentDaoSource.TABLE_NAME_ATTACHMENT +
          " ADD COLUMN " + AttachmentDaoSource.COL_FORWARDED_FOLDER + " TEXT;");
      sqLiteDatabase.execSQL("ALTER TABLE " + AttachmentDaoSource.TABLE_NAME_ATTACHMENT +
          " ADD COLUMN " + AttachmentDaoSource.COL_FORWARDED_UID + " INTEGER DEFAULT -1;");

      sqLiteDatabase.execSQL("ALTER TABLE " + MessageDaoSource.TABLE_NAME_MESSAGES +
          " ADD COLUMN " + MessageDaoSource.COL_ATTACHMENTS_DIRECTORY + " TEXT;");
      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }

  private void upgradeDatabaseFrom11To12Version(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.beginTransaction();
    try {
      sqLiteDatabase.execSQL("ALTER TABLE " + MessageDaoSource.TABLE_NAME_MESSAGES +
          " ADD COLUMN " + MessageDaoSource.COL_ERROR_MSG + " TEXT DEFAULT NULL;");
      sqLiteDatabase.setTransactionSuccessful();
    } finally {
      sqLiteDatabase.endTransaction();
    }
  }
}
