package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nan on 2016/09/18.
 */
public class Switcher extends IntentService {
    public static final String ACTION_DATA_RECEIVED = "tempakunoshiro.automaticotakumatching.ACTION_DATA_RECEIVED";
    public static final String ACTION_USER_RECEIVED = "tempakunoshiro.automaticotakumatching.ACTION_USER_RECEIVED";
    public static final String ACTION_SCREAM_RECEIVED = "tempakunoshiro.automaticotakumatching.ACTION_SCREAM_RECEIVED";

    public Switcher() {
        super("Switcher");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MyUser user = intent.getParcelableExtra("USER");
        MyScream scream = intent.getParcelableExtra("SCREAM");
        List<MyTag> tag = intent.getParcelableArrayListExtra("TAG");
        MyIcon icon = intent.getParcelableExtra("ICON");

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        try {
            Dao userDao = dbHelper.getDao(MyUser.class);
            Dao screamDao = dbHelper.getDao(MyScream.class);
            userDao.createOrUpdate(user);
            screamDao.createOrUpdate(scream);

            Dao tagDao = dbHelper.getDao(MyTag.class);
            Dao taggerDao = dbHelper.getDao(MyTagger.class);
            for(MyTag t : tag){
                QueryBuilder<MyTag, Integer> queryBuilder = tagDao.queryBuilder();
                queryBuilder.where().eq("tag", t.getTag());
                List<MyTag> tags = queryBuilder.query();

                if(tags.size() == 0){
                    tagDao.createOrUpdate(t);
                    MyTagger tagger = new MyTagger(t.getId(), user.getId());
                    taggerDao.createOrUpdate(tagger);
                }else{
                    MyTagger tagger = new MyTagger(tags.get(0).getId(), user.getId());
                    QueryBuilder<MyTagger, Integer> queryBuilder2 = taggerDao.queryBuilder();
                    queryBuilder2.where().eq("tagId", tagger.getTagId()).eq("userId", tagger.getUserId());
                    List<MyTagger> taggers = queryBuilder2.query();
                    if(taggers.size() == 0){
                        taggerDao.createOrUpdate(tagger);
                    }
                }
            }

            Dao iconDao = dbHelper.getDao(MyIcon.class);
            {
                QueryBuilder<MyIcon, Integer> queryBuilder = iconDao.queryBuilder();
                queryBuilder.where().eq("id", user.getIconId());
                List<MyIcon> icons = queryBuilder.query();
                if (icons.size() == 0 && icon != null) {
                    iconDao.createOrUpdate(icon);
                }
            }

            SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);
            // 自分
            if(user.getId() == pref.getLong("user_id", -1L)){
                Intent dataIntent = new Intent(ACTION_DATA_RECEIVED);
                dataIntent.putExtra("USER", user);
                dataIntent.putExtra("SCREAM", scream);
                sendBroadcast(dataIntent);
            }
            // 他の人
            else {
                Intent userIntent = new Intent(ACTION_USER_RECEIVED);
                Intent screamIntent = new Intent(ACTION_SCREAM_RECEIVED);
                userIntent.putParcelableArrayListExtra("USER", new ArrayList<>(userDao.queryForAll()));
                screamIntent.putParcelableArrayListExtra("SCREAM", new ArrayList<>(screamDao.queryForAll()));
                sendBroadcast(userIntent);
                sendBroadcast(screamIntent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void sendData(Context context, MyUser user, MyScream scream, List<MyTag> tag, MyIcon icon) {
        Intent intent = new Intent(context, Switcher.class);
        intent.putExtra("USER", user);
        intent.putExtra("SCREAM", scream);
        intent.putParcelableArrayListExtra("TAG", new ArrayList<>(tag));
        intent.putExtra("ICON", icon);
        context.startService(intent);
    }
}
