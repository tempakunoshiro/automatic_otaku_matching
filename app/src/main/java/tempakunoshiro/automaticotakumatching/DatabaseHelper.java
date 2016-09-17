package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import tempakunoshiro.automaticotakumatching.DatabaseContent.UserColumns;
import tempakunoshiro.automaticotakumatching.DatabaseContent.ScreamColumns;

/**
 * Created by Nan on 2016/09/15.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
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
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Scream.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "データベースの作成エラー", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i2) {
        try {
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, Scream.class, true);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "データベースの更新エラー", e);
        }
        onCreate(sqLiteDatabase, connectionSource);
    }
}
