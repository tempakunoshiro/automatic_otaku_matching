package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import tempakunoshiro.automaticotakumatching.DatabaseContent.UserColumns;
import tempakunoshiro.automaticotakumatching.DatabaseContent.ScreamColumns;

/**
 * Created by Nan on 2016/09/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper singleton = null;

    private static final String DB_NAME = "data_record.db";
    private static final int DB_VERSION = 1;

    // シングルトンだよ
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (singleton == null) {
            singleton = new DatabaseHelper(context);
        }
        return singleton;
    }

    private DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseSchema.CREATE_TBL_USER);
        db.execSQL(DatabaseSchema.CREATE_TBL_SCREAM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseSchema.DROP_TBL_USER);
        db.execSQL(DatabaseSchema.DROP_TBL_SCREAM);
        onCreate(db);
    }
}
