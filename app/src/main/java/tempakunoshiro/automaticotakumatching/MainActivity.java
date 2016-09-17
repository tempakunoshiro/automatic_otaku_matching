package tempakunoshiro.automaticotakumatching;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import tempakunoshiro.automaticotakumatching.DatabaseContent.UserColumns;
import tempakunoshiro.automaticotakumatching.DatabaseContent.ScreamColumns;

// とりあえずデータベーステスト。出力はログで確認してね（UI書くのが面倒くさかった
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // データ追加テスト（idはユニークなのでinsert注意）
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        try {
            Dao userDao = dbHelper.getDao(User.class);

            // 追加テスト
            User u1 = new User(1089, "天伯太郎", "大学クラスタ", "豊橋技術科学大学推し", "tempakunoshiro", "進捗どうですか");
            userDao.create(u1);
            User u2 = new User(1000, "なん", "ゲーム", "アクアプラス", "nan_nansofting", "うたわれ二人の白皇発売が楽しみ");
            userDao.createIfNotExists(u2);
            User u3 = new User(2000, "ごちうさ難民", "アニメ", "ご注文はうさぎですか？", "", "こころがぴょんぴょんするんじゃああああ");
            userDao.createOrUpdate(u3);

            // 更新テスト
            UpdateBuilder<User, Integer> updateBuilder = userDao.updateBuilder();
            updateBuilder.where().eq("id", 2000);
            updateBuilder.updateColumnValue("tag", "ご注文はうさぎですか？？");
            updateBuilder.update();

            // マッピングテスト
            List<User> users = userDao.queryForAll();
            for(User user : users){
                Log.d("test", user.getId()+", "+user.getName()+", "+user.getElement()+", "+user.getTag()+", "+user.getTwitterID()+", "+user.getComment());
            }

            Map<Long, User> userMap = new HashMap<>();
            for (User u : users) {
                userMap.put(u.getId(), u);
            }

            // Screamの追加、User名との関連付けテスト
            Dao screamDao = dbHelper.getDao(Scream.class);
            Scream s1 = new Scream(10, 2000, "ごちうさ好きあつまれ！", System.currentTimeMillis());
            screamDao.createIfNotExists(s1);

            List<Scream> screams = screamDao.queryForAll();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            for(Scream scream : screams){

                Log.d("test", scream.getId()+", "+ userMap.get(scream.getUserId()).getName() +", "+scream.getText()+", "+ simpleDateFormat.format(new Date(scream.getTime())));
            }

            DeleteBuilder<User, Integer> userDeleteBuilder =  userDao.deleteBuilder();
            userDeleteBuilder.delete();
            DeleteBuilder<Scream, Integer> screamDeleteBuilder = screamDao.deleteBuilder();
            userDeleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
    }
}
