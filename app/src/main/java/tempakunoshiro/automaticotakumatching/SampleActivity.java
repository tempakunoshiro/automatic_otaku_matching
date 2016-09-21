package tempakunoshiro.automaticotakumatching;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// とりあえずデータベーステスト。出力はログで確認してね（UI書くのが面倒くさかった
public class SampleActivity extends AppCompatActivity {
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

            // サービス開始テスト
            Bitmap b1 = BitmapFactory.decodeResource(getResources(), R.drawable.kazusa);
            MyUser u1 = new MyUser(pref.getLong("user_id", -1L), "なん", b1, "nan_nansofting", "うたわれ二人の白皇発売が楽しみ", new HashSet<String>(Arrays.asList("アニメ", "ゲーム", "アクアプラス")), System.currentTimeMillis());
            Switcher.sendData(this ,u1);

            MyUser u2 = new MyUser(1089, "天伯太郎", null, "tempakunoshiro", "進捗どうですか", new HashSet<String>(Arrays.asList("大学", "豊橋技術科学大学")), System.currentTimeMillis());
            Switcher.sendData(this ,u2);
            MyUser u3 = new MyUser(5753, "ごちうさ難民", null, "", "こころがぴょんぴょんするんじゃああああ", new HashSet<String>(Arrays.asList("アニメ", "ご注文はうさぎですか？", "ご注文はうさぎですか？？")), System.currentTimeMillis());
            MyScream s3 = new MyScream(5753, "ごちうさ好きあつまれ！", System.currentTimeMillis());
            Switcher.sendData(this, u3, s3);

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
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                List<MyUser> users = intent.getParcelableArrayListExtra("USER");
                for(MyUser user : users){
                    Log.d("testUser", user.getId()+", "+user.getName()+", "+user.getIcon()+", "+user.getTwitterId()+", "+user.getComment()+", "+String.valueOf(user.getTagSet())+", "+simpleDateFormat.format(new Date(user.getModifiedTime())));
                }

                Map<Long, MyUser> userMap = new HashMap<>();
                for (MyUser u : users) {
                    userMap.put(u.getId(), u);
                }

                try {
                    Dao screamDao = dbHelper.getDao(MyScream.class);
                    List<MyScream> screams = screamDao.queryForAll();
                    for (MyScream scream : screams) {
                        MyUser myUser = userMap.get(scream.getUserId());
                        String name = myUser == null ?  "" : myUser.getName();
                        Log.d("testScream", scream.getId()+", "+ name + ", " + scream.getText()+", "+ simpleDateFormat.format(new Date(scream.getTime())));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
