package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shivamsahu on 06/02/18.
 */

public class GroupMessengerDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GroupMessengerDB.db";


    private static final int VERSION = 1;

    public GroupMessengerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "  + GroupMessengerContract.GroupMessengerEntry.TABLE_NAME + " (" +
                GroupMessengerContract.GroupMessengerEntry._ID  + " INTEGER PRIMARY KEY, " +
                GroupMessengerContract.GroupMessengerEntry.COLUMN_KEY + " TEXT NOT NULL UNIQUE, " +
                GroupMessengerContract.GroupMessengerEntry.COLUMN_VALUE    + " TEXT NOT NULL);";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GroupMessengerContract.GroupMessengerEntry.TABLE_NAME);
        onCreate(db);
    }
}
