package edu.buffalo.cse.cse486586.groupmessenger2;

import android.net.Uri;
import android.provider.BaseColumns;
/**
 * Created by shivamsahu on 11/02/18.
 */


/**
 * Created by shivamsahu on 06/02/18.
 */

public class GroupMessengerContract  {

    public static class GroupMessengerEntry implements BaseColumns{

        // Table and Table columns
        public static final String TABLE_NAME = "group_messenger";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_VALUE = "value";

    }
}
