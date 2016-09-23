package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

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
            TableUtils.createTable(connectionSource, MyUser.class);
            TableUtils.createTable(connectionSource, MyScream.class);
            TableUtils.createTable(connectionSource, MyTag.class);
            TableUtils.createTable(connectionSource, MyTagger.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "データベースの作成エラー", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i2) {
        try {
            TableUtils.dropTable(connectionSource, MyUser.class, true);
            TableUtils.dropTable(connectionSource, MyScream.class, true);
            TableUtils.dropTable(connectionSource, MyTag.class, true);
            TableUtils.dropTable(connectionSource, MyTagger.class, true);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "データベースの更新エラー", e);
        }
        onCreate(sqLiteDatabase, connectionSource);
    }

    public void init() {
        try {
            TableUtils.dropTable(getConnectionSource(), MyUser.class, true);
            TableUtils.dropTable(getConnectionSource(), MyScream.class, true);
            TableUtils.dropTable(getConnectionSource(), MyTag.class, true);
            TableUtils.dropTable(getConnectionSource(), MyTagger.class, true);
            TableUtils.createTable(getConnectionSource(), MyUser.class);
            TableUtils.createTable(getConnectionSource(), MyScream.class);
            TableUtils.createTable(getConnectionSource(), MyTag.class);
            TableUtils.createTable(getConnectionSource(), MyTagger.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
