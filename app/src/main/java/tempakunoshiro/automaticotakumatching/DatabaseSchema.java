package tempakunoshiro.automaticotakumatching;

import android.provider.BaseColumns;

import tempakunoshiro.automaticotakumatching.DatabaseContent.UserColumns;
import tempakunoshiro.automaticotakumatching.DatabaseContent.ScreamColumns;

/**
 * Created by Nakano on 2016/09/16.
 */
interface DatabaseSchema {
    String DB_NAME = "data_record.db";

    String TBL_USER = "user";
    String TBL_SCREAM = "scream";

    String CREATE_TBL_USER = "CREATE TABLE " + TBL_USER + " ("
            + UserColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + UserColumns.NAME + " TEXT NOT NULL,"
            + UserColumns.ELEMENT + " TEXT NOT NULL,"
            + UserColumns.TAG + " TEXT NOT NULL,"
            + UserColumns.TWITTER_ID + " TEXT NOT NULL,"
            + UserColumns.COMMENT +  " TEXT NOT NULL)";

    String CREATE_TBL_SCREAM = "CREATE TABLE " + TBL_SCREAM + " ("
            + ScreamColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ScreamColumns.USER_ID + " INTEGER NOT NULL,"
            + ScreamColumns.TEXT + " TEXT NOT NULL,"
            + ScreamColumns.TIME + " INTEGER NOT NULL)";

    String DROP_TBL_USER =
            "DROP TABLE IF EXISTS " + TBL_USER;

    String DROP_TBL_SCREAM =
            "DROP TABLE IF EXISTS " + TBL_SCREAM;
}
