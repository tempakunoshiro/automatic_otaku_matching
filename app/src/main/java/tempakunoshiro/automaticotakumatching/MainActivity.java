package tempakunoshiro.automaticotakumatching;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.math.BigInteger;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// とりあえずデータベーステスト。出力はログで確認してね（UI書くのが面倒くさかった
public class MainActivity extends AppCompatActivity {
    MainReceiver mainReceiver;
    DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);
        dbHelper.init();

        // 初回起動時のid付加設定
        SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);
        Editor editor = pref.edit();
        if (pref.getLong("user_id", -1L) == -1L) {
            Long udid = new BigInteger(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), 16).longValue();
            long id = (((long) Math.ceil(Math.random()*Integer.MAX_VALUE)) << 32) + udid.hashCode();
            editor.putLong("user_id", id);
            editor.commit();
        }

        // データ追加テスト（idはユニークなのでinsert注意）
        try {
            Dao userDao = dbHelper.getDao(MyUser.class);

            // 追加テスト
            MyUser u1 = new MyUser(1089, "天伯太郎", 0L, "tempakunoshiro", "進捗どうですか");
            userDao.create(u1);
            MyUser u2 = new MyUser(pref.getLong("user_id", -1L), "なん", 0L, "nan_nansofting", "うたわれ二人の白皇発売が楽しみ");
            userDao.createIfNotExists(u2);

            // マッピングテスト
            List<MyUser> users = userDao.queryForAll();
            for(MyUser user : users){
                Log.d("test", user.getId()+", "+user.getName()+", "+user.getTwitterId()+", "+user.getComment());
            }

            System.out.println(MyUser.getInstanceFromId(this, 1089).getName());

            // サービス開始テスト
            MyUser u3 = new MyUser(5753, "ごちうさ難民", 0L, "", "こころがぴょんぴょんするんじゃああああ");
            MyScream s3 = new MyScream(5753, "ごちうさ好きあつまれ！", System.currentTimeMillis());
            List<MyTag> list = Arrays.asList(new MyTag("アニメ"), new MyTag("ご注文はうさぎですか？"), new MyTag("ご注文はうさぎですか？？"));
            Switcher.sendData(this, u3, s3, list, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        mainReceiver = new MainReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Switcher.ACTION_USER_RECEIVED);
        registerReceiver(mainReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mainReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Switcher.ACTION_USER_RECEIVED)){
                List<MyUser> users = intent.getParcelableArrayListExtra("USER");
                for(MyUser user : users){
                    Log.d("test2", user.getId()+", "+user.getName()+", "+user.getTwitterId()+", "+user.getComment());
                }

                Map<Long, MyUser> userMap = new HashMap<>();
                for (MyUser u : users) {
                    userMap.put(u.getId(), u);
                }

                try {
                    Dao screamDao = dbHelper.getDao(MyScream.class);
                    List<MyScream> screams = screamDao.queryForAll();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    for (MyScream scream : screams) {
                        Log.d("test", scream.getId()+", "+ userMap.get(scream.getUserId()).getName() + ", " + scream.getText()+", "+ simpleDateFormat.format(new Date(scream.getTime())));
                        Log.d("test", MyTagger.getTagListFromId(context, scream.getUserId()).toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
