package tempakunoshiro.automaticotakumatching;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import tempakunoshiro.automaticotakumatching.DatabaseContent.UserColumns;
import tempakunoshiro.automaticotakumatching.DatabaseContent.ScreamColumns;

// とりあえずデータベーステスト。出力はログで確認してね（UI書くのが面倒くさかった
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // データ追加テスト（idはユニークなのでinsert注意）
        DatabaseContentProvider dbContProv = new DatabaseContentProvider();

        // レコードをwhereで指定してみた
        if(getContentResolver().query(UserColumns.CONTENT_URI, null, UserColumns._ID +" = "+ 1089, null, null).getCount() == 0){
            ContentValues values = new ContentValues();
            values.put(UserColumns._ID , 1089);
            values.put(UserColumns.NAME, "天白太郎");
            values.put(UserColumns.ELEMENT, "大学クラスタ");
            values.put(UserColumns.TAG, "豊橋技術科学大学推し");
            values.put(UserColumns.TWITTER_ID, "tempakunoshiro");
            values.put(UserColumns.COMMENT, "進捗どうですか");
            getContentResolver().insert(UserColumns.CONTENT_URI, values);
        }

        // レコードをwhereではなくuriで指定してみた
        if(getContentResolver().query(Uri.withAppendedPath(UserColumns.CONTENT_URI, "1000"), null, null, null, null).getCount() == 0){
            ContentValues values = new ContentValues();
            values.put(UserColumns._ID , 1000);
            values.put(UserColumns.NAME, "なん");
            values.put(UserColumns.ELEMENT, "ゲーム");
            values.put(UserColumns.TAG, "アクアプラス");
            values.put(UserColumns.TWITTER_ID, "nan_nansofting");
            values.put(UserColumns.COMMENT, "うたわれ二人の白皇発売が楽しみ");
            // insertはuriで指定してもエラーになるので注意
            // × getContentResolver().insert(Uri.withAppendedPath(UserColumns.CONTENT_URI, "1000"), values);
            getContentResolver().insert(UserColumns.CONTENT_URI, values);
        }

        // insertした後にupdateするテスト
        if(getContentResolver().query(UserColumns.CONTENT_URI, null, UserColumns._ID +" = "+ 2000, null, null).getCount() == 0){
            ContentValues values = new ContentValues();
            values.put(UserColumns._ID , 2000);
            values.put(UserColumns.NAME, "ごちうさ難民");
            values.put(UserColumns.ELEMENT, "アニメ");
            values.put(UserColumns.TAG, "ご注文はうさぎですか？");
            values.put(UserColumns.TWITTER_ID, "");
            values.put(UserColumns.COMMENT, "こころがぴょんぴょんするんじゃああああ");
            getContentResolver().insert(UserColumns.CONTENT_URI, values);
        }

        {
            ContentValues values = new ContentValues();
            values.put(UserColumns.TAG, "ご注文はうさぎですか？？");
            getContentResolver().update(UserColumns.CONTENT_URI, values, UserColumns._ID + " = " + 2000, null);
        }

        // 全レコードを取得
        {
            Cursor cur = getContentResolver().query(UserColumns.CONTENT_URI, null, null, null, null);
            while (cur.moveToNext()) {
                Log.d(String.valueOf(cur.getLong(0)), cur.getString(1) + ", " + cur.getString(2) + ", " + cur.getString(3) + ", " + cur.getString(4) + ", " + cur.getString(5));
            }
        }

        // Screamも追加テスト
        if(getContentResolver().query(ScreamColumns.CONTENT_URI, null, ScreamColumns._ID +" = "+ 10, null, null).getCount() == 0){
            ContentValues values = new ContentValues();
            values.put(ScreamColumns._ID , 10);
            values.put(ScreamColumns.USER_ID, 2000);
            values.put(ScreamColumns.TEXT, "ごちうさ好きあつまれ！");
            values.put(ScreamColumns.TIME, System.currentTimeMillis());
            getContentResolver().insert(ScreamColumns.CONTENT_URI, values);
        }

        // Screamの全レコードを取得 ＋ ユーザーID→ユーザー名解決（Userテーブルのユーザー名のみを取得する） ＋ 投稿時刻整形テスト
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            Cursor cur = getContentResolver().query(ScreamColumns.CONTENT_URI, null, null, null, null);
            while (cur.moveToNext()) {
                Cursor cur2 = getContentResolver().query(UserColumns.CONTENT_URI, new String[]{ UserColumns.NAME }, UserColumns._ID +" = "+ cur.getLong(1), null, null);
                cur2.moveToFirst();
                Log.d(String.valueOf(cur.getLong(0)), cur2.getString(0) + ", " + cur.getString(2) + ", " + simpleDateFormat.format(new Date(cur.getLong(3))));
            }
        }

        // 全レコードを削除
        getContentResolver().delete(UserColumns.CONTENT_URI, null, null);
        getContentResolver().delete(ScreamColumns.CONTENT_URI, null, null);

        setContentView(R.layout.activity_main);
    }
}
