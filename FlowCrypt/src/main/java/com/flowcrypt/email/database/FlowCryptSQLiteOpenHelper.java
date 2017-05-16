package com.flowcrypt.email.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.flowcrypt.email.database.dao.source.KeysDaoSource;


/**
 * A helper class to manage database creation and version management.
 *
 * @author DenBond7
 *         Date: 13.05.2017
 *         Time: 12:20
 *         E-mail: DenBond7@gmail.com
 */
public class FlowCryptSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String COLUMN_NAME_COUNT = "COUNT(*)";
    public static final String DB_NAME = "flowcrypt.db";
    public static final int DB_VERSION = 1;

    private static final String TAG = FlowCryptSQLiteOpenHelper.class.getSimpleName();
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    public FlowCryptSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static void dropTable(SQLiteDatabase sqLiteDatabase, String tableName) {
        sqLiteDatabase.execSQL(DROP_TABLE + tableName);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(KeysDaoSource.BOOKING_TABLE_SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.i(TAG, "Database updated from OLD_VERSION = " + Integer.toString(oldVersion)
                + " to NEW_VERSION = " + Integer.toString(newVersion));
    }
}