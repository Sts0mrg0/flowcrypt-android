/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.database.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.flowcrypt.email.database.FlowCryptDatabaseManager;
import com.flowcrypt.email.database.FlowCryptSQLiteOpenHelper;
import com.flowcrypt.email.database.dao.source.AccountAliasesDaoSource;
import com.flowcrypt.email.database.dao.source.AccountDaoSource;
import com.flowcrypt.email.database.dao.source.ActionQueueDaoSource;
import com.flowcrypt.email.database.dao.source.ContactsDaoSource;
import com.flowcrypt.email.database.dao.source.KeysDaoSource;
import com.flowcrypt.email.database.dao.source.UserIdEmailsKeysDaoSource;
import com.flowcrypt.email.database.dao.source.imap.AttachmentDaoSource;
import com.flowcrypt.email.database.dao.source.imap.ImapLabelsDaoSource;
import com.flowcrypt.email.database.dao.source.imap.MessageDaoSource;
import com.flowcrypt.email.util.exception.ExceptionUtil;

import java.util.ArrayList;

import androidx.annotation.NonNull;


/**
 * This class encapsulate data and provide it to the application through the single
 * {@link ContentResolver} interface.
 *
 * @author Denis Bondarenko
 * Date: 13.05.2017
 * Time: 10:32
 * E-mail: DenBond7@gmail.com
 */
public class SecurityContentProvider extends ContentProvider {
  private static final int MATCHED_CODE_KEYS_TABLE = 1;
  private static final int MATCHED_CODE_KEYS_TABLE_SINGLE_ROW = 2;
  private static final int MATCHED_CODE_KEY_CLEAN_DATABASE = 3;
  private static final int MATCHED_CODE_CONTACTS_TABLE = 4;
  private static final int MATCHED_CODE_CONTACTS_TABLE_SINGLE_ROW = 5;
  private static final int MATCHED_CODE_IMAP_LABELS_TABLE = 6;
  private static final int MATCHED_CODE_IMAP_LABELS_SINGLE_ROW = 7;
  private static final int MATCHED_CODE_IMAP_MESSAGES_TABLE = 8;
  private static final int MATCHED_CODE_IMAP_MESSAGES_SINGLE_ROW = 9;
  private static final int MATCHED_CODE_ACCOUNTS_TABLE = 10;
  private static final int MATCHED_CODE_ACCOUNTS_SINGLE_ROW = 11;
  private static final int MATCHED_CODE_ATTACHMENT_TABLE = 12;
  private static final int MATCHED_CODE_ATTACHMENT_SINGLE_ROW = 13;
  private static final int MATCHED_CODE_ACCOUNT_ALIASES_TABLE = 14;
  private static final int MATCHED_CODE_ACCOUNT_ALIASES_ROW = 15;
  private static final int MATCHED_CODE_KEY_ERASE_DATABASE = 16;
  private static final int MATCHED_CODE_ACTION_QUEUE_TABLE = 17;
  private static final int MATCHED_CODE_ACTION_QUEUE_ROW = 18;
  private static final int MATCHED_CODE_ACTION_USER_ID_EMAILS_AND_KEYS_TABLE = 19;
  private static final int MATCHED_CODE_ACTION_USER_ID_EMAILS_AND_KEYS_ROW = 20;

  private static final String SINGLE_APPENDED_SUFFIX = "/#";
  private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

  static {
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, KeysDaoSource.TABLE_NAME_KEYS,
        MATCHED_CODE_KEYS_TABLE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, KeysDaoSource.TABLE_NAME_KEYS +
        SINGLE_APPENDED_SUFFIX, MATCHED_CODE_KEYS_TABLE_SINGLE_ROW);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, FlowcryptContract.CLEAN_DATABASE,
        MATCHED_CODE_KEY_CLEAN_DATABASE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, FlowcryptContract.ERASE_DATABASE,
        MATCHED_CODE_KEY_ERASE_DATABASE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, ContactsDaoSource.TABLE_NAME_CONTACTS,
        MATCHED_CODE_CONTACTS_TABLE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, ContactsDaoSource.TABLE_NAME_CONTACTS +
        SINGLE_APPENDED_SUFFIX, MATCHED_CODE_CONTACTS_TABLE_SINGLE_ROW);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, ImapLabelsDaoSource.TABLE_NAME_IMAP_LABELS,
        MATCHED_CODE_IMAP_LABELS_TABLE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, ImapLabelsDaoSource.TABLE_NAME_IMAP_LABELS +
        SINGLE_APPENDED_SUFFIX, MATCHED_CODE_IMAP_LABELS_SINGLE_ROW);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, MessageDaoSource
            .TABLE_NAME_MESSAGES,
        MATCHED_CODE_IMAP_MESSAGES_TABLE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, MessageDaoSource
        .TABLE_NAME_MESSAGES +
        SINGLE_APPENDED_SUFFIX, MATCHED_CODE_IMAP_MESSAGES_SINGLE_ROW);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, AccountDaoSource.TABLE_NAME_ACCOUNTS,
        MATCHED_CODE_ACCOUNTS_TABLE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, AccountDaoSource.TABLE_NAME_ACCOUNTS +
        SINGLE_APPENDED_SUFFIX, MATCHED_CODE_ACCOUNTS_SINGLE_ROW);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, AttachmentDaoSource.TABLE_NAME_ATTACHMENT,
        MATCHED_CODE_ATTACHMENT_TABLE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, AttachmentDaoSource.TABLE_NAME_ATTACHMENT +
        SINGLE_APPENDED_SUFFIX, MATCHED_CODE_ATTACHMENT_SINGLE_ROW);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, AccountAliasesDaoSource.TABLE_NAME_ACCOUNTS_ALIASES,
        MATCHED_CODE_ACCOUNT_ALIASES_TABLE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, AccountAliasesDaoSource.TABLE_NAME_ACCOUNTS_ALIASES +
        SINGLE_APPENDED_SUFFIX, MATCHED_CODE_ACCOUNT_ALIASES_ROW);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, ActionQueueDaoSource.TABLE_NAME_ACTION_QUEUE +
        SINGLE_APPENDED_SUFFIX, MATCHED_CODE_ACTION_QUEUE_ROW);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, ActionQueueDaoSource.TABLE_NAME_ACTION_QUEUE,
        MATCHED_CODE_ACTION_QUEUE_TABLE);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, UserIdEmailsKeysDaoSource.TABLE_NAME_USER_ID_EMAILS_AND_KEYS +
        SINGLE_APPENDED_SUFFIX, MATCHED_CODE_ACTION_USER_ID_EMAILS_AND_KEYS_ROW);
    URI_MATCHER.addURI(FlowcryptContract.AUTHORITY, UserIdEmailsKeysDaoSource.TABLE_NAME_USER_ID_EMAILS_AND_KEYS,
        MATCHED_CODE_ACTION_USER_ID_EMAILS_AND_KEYS_TABLE);
  }

  private FlowCryptSQLiteOpenHelper hotelDBHelper;

  public SecurityContentProvider() {
  }

  @Override
  public boolean onCreate() {
    hotelDBHelper = (FlowCryptSQLiteOpenHelper) FlowCryptDatabaseManager.getSqLiteOpenHelper();
    if (hotelDBHelper == null) {
      FlowCryptDatabaseManager.initializeInstance(new FlowCryptSQLiteOpenHelper(getContext()));
      hotelDBHelper = (FlowCryptSQLiteOpenHelper) FlowCryptDatabaseManager.getSqLiteOpenHelper();
    }
    return true;
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    Uri result = null;
    if (hotelDBHelper != null) {
      SQLiteDatabase sqLiteDatabase = hotelDBHelper.getWritableDatabase();
      int match = URI_MATCHER.match(uri);

      if (sqLiteDatabase != null) {
        long id;
        switch (match) {
          case MATCHED_CODE_KEYS_TABLE:
            id = sqLiteDatabase.insert(new KeysDaoSource().getTableName(), null, values);
            result = Uri.parse(new KeysDaoSource().getBaseContentUri() + "/" + id);
            break;

          case MATCHED_CODE_CONTACTS_TABLE:
            id = sqLiteDatabase.insert(new ContactsDaoSource().getTableName(), null, values);
            result = Uri.parse(new ContactsDaoSource().getBaseContentUri() + "/" + id);
            break;

          case MATCHED_CODE_IMAP_LABELS_TABLE:
            id = sqLiteDatabase.insert(new ImapLabelsDaoSource().getTableName(), null, values);
            result = Uri.parse(new ImapLabelsDaoSource().getBaseContentUri() + "/" + id);
            break;

          case MATCHED_CODE_IMAP_MESSAGES_TABLE:
            id = sqLiteDatabase.insert(new MessageDaoSource().getTableName(), null, values);
            result = Uri.parse(new MessageDaoSource().getBaseContentUri() + "/" + id);
            break;

          case MATCHED_CODE_ACCOUNTS_TABLE:
            id = sqLiteDatabase.insert(new AccountDaoSource().getTableName(), null, values);
            result = Uri.parse(new AccountDaoSource().getBaseContentUri() + "/" + id);
            break;

          case MATCHED_CODE_ATTACHMENT_TABLE:
            id = sqLiteDatabase.insert(new AttachmentDaoSource().getTableName(),
                null, values);
            result = Uri.parse(new AttachmentDaoSource().getBaseContentUri() + "/" + id);
            break;

          case MATCHED_CODE_ACCOUNT_ALIASES_TABLE:
            id = sqLiteDatabase.insert(new AccountAliasesDaoSource().getTableName(), null, values);
            result = Uri.parse(new AccountAliasesDaoSource().getBaseContentUri() + "/" + id);
            break;

          case MATCHED_CODE_ACTION_QUEUE_TABLE:
            id = sqLiteDatabase.insert(new ActionQueueDaoSource().getTableName(), null, values);
            result = Uri.parse(new ActionQueueDaoSource().getBaseContentUri() + "/" + id);
            break;

          case MATCHED_CODE_ACTION_USER_ID_EMAILS_AND_KEYS_TABLE:
            id = sqLiteDatabase.insert(new UserIdEmailsKeysDaoSource().getTableName(), null, values);
            result = Uri.parse(new UserIdEmailsKeysDaoSource().getBaseContentUri() + "/" + id);
            break;

          default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null && result != null) {
          getContext().getContentResolver().notifyChange(uri, null, false);
        }
      }
    }

    return result;
  }

  @Override
  public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
    int insertedRowsCount = 0;

    if (hotelDBHelper != null) {
      SQLiteDatabase sqLiteDatabase = hotelDBHelper.getWritableDatabase();
      if (sqLiteDatabase != null) {
        sqLiteDatabase.beginTransaction();
        try {
          switch (URI_MATCHER.match(uri)) {
            case MATCHED_CODE_IMAP_MESSAGES_TABLE:
              for (ContentValues contentValues : values) {
                long id = sqLiteDatabase.insert(new MessageDaoSource().getTableName(), null,
                    contentValues);

                //if message not inserted, try to update message with some UID
                if (id <= 0) {
                  id = updateMessageInfo(sqLiteDatabase, contentValues);
                } else {
                  insertedRowsCount++;
                }

                if (id <= 0) {
                  throw new SQLException("Failed to insert row into " + uri);
                }
              }
              break;

            default:
              for (ContentValues contentValues : values) {
                long id = sqLiteDatabase.insert(getMatchedTableName(uri), null, contentValues);
                if (id <= 0) {
                  throw new SQLException("Failed to insert row into " + uri);
                } else {
                  insertedRowsCount++;
                }
              }
          }

          sqLiteDatabase.setTransactionSuccessful();

          if (getContext() != null && insertedRowsCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null, false);
          }
        } catch (Exception e) {
          e.printStackTrace();
          ExceptionUtil.handleError(e);
        } finally {
          sqLiteDatabase.endTransaction();
        }
      }
    }

    return insertedRowsCount;
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    int rowsCount = -1;
    if (hotelDBHelper != null) {
      SQLiteDatabase sqLiteDatabase = hotelDBHelper.getWritableDatabase();
      if (sqLiteDatabase != null) {
        rowsCount = sqLiteDatabase.update(
            getMatchedTableName(uri),
            values,
            selection,
            selectionArgs);

        if (getContext() != null && rowsCount != 0) {
          getContext().getContentResolver().notifyChange(uri, null, false);
        }
      }
    }

    return rowsCount;
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    int rowsCount = -1;
    if (hotelDBHelper != null) {
      SQLiteDatabase sqLiteDatabase = hotelDBHelper.getWritableDatabase();
      int match = URI_MATCHER.match(uri);

      if (sqLiteDatabase != null) {
        switch (match) {
          case MATCHED_CODE_KEY_CLEAN_DATABASE:
            rowsCount = sqLiteDatabase.delete(new AccountDaoSource().getTableName(),
                AccountDaoSource.COL_EMAIL + " = ?", selectionArgs);
            rowsCount += sqLiteDatabase.delete(new ImapLabelsDaoSource().getTableName(),
                ImapLabelsDaoSource.COL_EMAIL + " = ?", selectionArgs);
            rowsCount += sqLiteDatabase.delete(new MessageDaoSource().getTableName(),
                MessageDaoSource.COL_EMAIL + " = ?", selectionArgs);
            rowsCount += sqLiteDatabase.delete(new AttachmentDaoSource().getTableName(),
                AttachmentDaoSource.COL_EMAIL + " = ?", selectionArgs);
            break;

          case MATCHED_CODE_KEY_ERASE_DATABASE:
            rowsCount = sqLiteDatabase.delete(new AccountDaoSource().getTableName(), null, null);
            rowsCount += sqLiteDatabase.delete(new AccountAliasesDaoSource().getTableName(), null, null);
            rowsCount += sqLiteDatabase.delete(new ImapLabelsDaoSource().getTableName(), null, null);
            rowsCount += sqLiteDatabase.delete(new MessageDaoSource().getTableName(), null, null);
            rowsCount += sqLiteDatabase.delete(new AttachmentDaoSource().getTableName(), null, null);
            rowsCount += sqLiteDatabase.delete(new KeysDaoSource().getTableName(), null, null);
            rowsCount += sqLiteDatabase.delete(new UserIdEmailsKeysDaoSource().getTableName(), null, null);
            rowsCount += sqLiteDatabase.delete(new ContactsDaoSource().getTableName(), null, null);
            break;

          case MATCHED_CODE_ACTION_QUEUE_ROW:
            rowsCount = sqLiteDatabase.delete(new ActionQueueDaoSource().getTableName(),
                BaseColumns._ID + " = ?", new String[]{uri.getLastPathSegment()});
            break;

          default:
            rowsCount = sqLiteDatabase.delete(getMatchedTableName(uri), selection, selectionArgs);
        }

        if (getContext() != null && rowsCount != 0) {
          getContext().getContentResolver().notifyChange(uri, null, false);
        }
      }
    }

    return rowsCount;
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder) {
    SQLiteDatabase sqLiteDatabase = hotelDBHelper.getReadableDatabase();

    Cursor cursor = sqLiteDatabase.query(getMatchedTableName(uri),
        projection,
        selection,
        selectionArgs,
        null,
        null,
        sortOrder,
        null);

    if (getContext() != null && cursor != null) {
      cursor.setNotificationUri(getContext().getContentResolver(), uri);
    }

    return cursor;
  }

  @NonNull
  @Override
  public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws
      OperationApplicationException {
    ContentProviderResult[] contentProviderResults = new ContentProviderResult[0];
    if (hotelDBHelper != null) {
      SQLiteDatabase sqLiteDatabase = hotelDBHelper.getWritableDatabase();
      if (sqLiteDatabase != null) {
        sqLiteDatabase.beginTransaction();
        try {
          contentProviderResults = super.applyBatch(operations);
          sqLiteDatabase.setTransactionSuccessful();
        } catch (Exception e) {
          e.printStackTrace();
          ExceptionUtil.handleError(e);
        } finally {
          sqLiteDatabase.endTransaction();
        }
      }
    } else {
      contentProviderResults = super.applyBatch(operations);
    }
    return contentProviderResults;
  }

  @Override
  public String getType(@NonNull Uri uri) {
    final int match = URI_MATCHER.match(uri);
    switch (match) {
      case MATCHED_CODE_KEYS_TABLE:
        return new KeysDaoSource().getRowsContentType();

      case MATCHED_CODE_KEYS_TABLE_SINGLE_ROW:
        return new KeysDaoSource().getSingleRowContentType();

      case MATCHED_CODE_CONTACTS_TABLE:
        return new ContactsDaoSource().getRowsContentType();

      case MATCHED_CODE_CONTACTS_TABLE_SINGLE_ROW:
        return new ContactsDaoSource().getSingleRowContentType();

      case MATCHED_CODE_IMAP_LABELS_TABLE:
        return new ImapLabelsDaoSource().getRowsContentType();

      case MATCHED_CODE_IMAP_LABELS_SINGLE_ROW:
        return new ImapLabelsDaoSource().getSingleRowContentType();

      case MATCHED_CODE_IMAP_MESSAGES_TABLE:
        return new MessageDaoSource().getRowsContentType();

      case MATCHED_CODE_IMAP_MESSAGES_SINGLE_ROW:
        return new MessageDaoSource().getSingleRowContentType();

      case MATCHED_CODE_ACCOUNTS_TABLE:
        return new AccountDaoSource().getRowsContentType();

      case MATCHED_CODE_ACCOUNTS_SINGLE_ROW:
        return new AccountDaoSource().getSingleRowContentType();

      case MATCHED_CODE_ATTACHMENT_TABLE:
        return new AttachmentDaoSource().getRowsContentType();

      case MATCHED_CODE_ATTACHMENT_SINGLE_ROW:
        return new AttachmentDaoSource().getSingleRowContentType();

      case MATCHED_CODE_ACCOUNT_ALIASES_TABLE:
        return new AttachmentDaoSource().getRowsContentType();

      case MATCHED_CODE_ACCOUNT_ALIASES_ROW:
        return new AttachmentDaoSource().getSingleRowContentType();

      case MATCHED_CODE_ACTION_QUEUE_TABLE:
        return new ActionQueueDaoSource().getRowsContentType();

      case MATCHED_CODE_ACTION_QUEUE_ROW:
        return new ActionQueueDaoSource().getSingleRowContentType();

      case MATCHED_CODE_ACTION_USER_ID_EMAILS_AND_KEYS_TABLE:
        return new UserIdEmailsKeysDaoSource().getRowsContentType();

      case MATCHED_CODE_ACTION_USER_ID_EMAILS_AND_KEYS_ROW:
        return new UserIdEmailsKeysDaoSource().getSingleRowContentType();

      default:
        throw new IllegalArgumentException("Unknown uri: " + uri);
    }
  }

  /**
   * Get the matched table name from an input {@link Uri}
   *
   * @param uri An input {@link Uri}
   * @return The matched table name;
   */
  private String getMatchedTableName(@NonNull Uri uri) {
    switch (URI_MATCHER.match(uri)) {
      case MATCHED_CODE_KEYS_TABLE:
        return KeysDaoSource.TABLE_NAME_KEYS;

      case MATCHED_CODE_CONTACTS_TABLE:
        return ContactsDaoSource.TABLE_NAME_CONTACTS;

      case MATCHED_CODE_IMAP_LABELS_TABLE:
        return ImapLabelsDaoSource.TABLE_NAME_IMAP_LABELS;

      case MATCHED_CODE_IMAP_MESSAGES_TABLE:
        return MessageDaoSource.TABLE_NAME_MESSAGES;

      case MATCHED_CODE_ACCOUNTS_TABLE:
        return AccountDaoSource.TABLE_NAME_ACCOUNTS;

      case MATCHED_CODE_ATTACHMENT_TABLE:
        return AttachmentDaoSource.TABLE_NAME_ATTACHMENT;

      case MATCHED_CODE_ACCOUNT_ALIASES_TABLE:
        return AccountAliasesDaoSource.TABLE_NAME_ACCOUNTS_ALIASES;

      case MATCHED_CODE_ACTION_QUEUE_TABLE:
        return ActionQueueDaoSource.TABLE_NAME_ACTION_QUEUE;

      case MATCHED_CODE_ACTION_USER_ID_EMAILS_AND_KEYS_TABLE:
        return UserIdEmailsKeysDaoSource.TABLE_NAME_USER_ID_EMAILS_AND_KEYS;

      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  /**
   * Try to update some message.
   *
   * @param sqLiteDatabase The {@link SQLiteDatabase} which will be used to update a message.
   * @param contentValues  The new information about some message.
   * @return the number of rows affected
   */
  private long updateMessageInfo(SQLiteDatabase sqLiteDatabase, ContentValues contentValues) {
    long id;
    String email = contentValues.getAsString(MessageDaoSource.COL_EMAIL);
    String folder = contentValues.getAsString(MessageDaoSource.COL_FOLDER);
    String uid = contentValues.getAsString(MessageDaoSource.COL_UID);

    id = sqLiteDatabase.update(
        new MessageDaoSource().getTableName(),
        contentValues,
        MessageDaoSource.COL_EMAIL + "= ? AND "
            + MessageDaoSource.COL_FOLDER + " = ? AND "
            + MessageDaoSource.COL_UID + " = ? ",
        new String[]{email, folder, uid});
    return id;
  }


}
