package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        try {
            Dao userDao = dbHelper.getDao(MyUser.class);
            if(user != null){
                userDao.createOrUpdate(user);
            }
            List<MyUser> allUsers = userDao.queryForAll();

            Dao screamDao = dbHelper.getDao(MyScream.class);
            if(scream != null) {
                screamDao.createOrUpdate(scream);
            }
            List<MyScream> allScreams = screamDao.queryForAll();

            if(user != null) {
                Dao tagDao = dbHelper.getDao(MyTag.class);
                Dao taggerDao = dbHelper.getDao(MyTagger.class);

                Set<String> tags = user.getTagSet();
                for (String s : tags) {
                    QueryBuilder<MyTag, Integer> queryBuilder = tagDao.queryBuilder();
                    queryBuilder.where().eq("tag", s);
                    List<MyTag> tempTags = queryBuilder.query();
                    if (tempTags.size() == 0) {
                        MyTag t = new MyTag(s);
                        tagDao.createOrUpdate(t);
                        taggerDao.createOrUpdate(new MyTagger(t.getId(), user.getId()));
                    } else {
                        QueryBuilder<MyTagger, Integer> queryBuilder2 = taggerDao.queryBuilder();
                        Where<MyTagger, Integer> where = queryBuilder2.where();
                        where.and(where.eq("tagId", tempTags.get(0).getId()), where.eq("userId", user.getId()));
                        List<MyTagger> taggers = queryBuilder2.query();
                        if (taggers.size() == 0) {
                            taggerDao.createOrUpdate(new MyTagger(tempTags.get(0).getId(), user.getId()));
                        }
                    }
                }

                // icon処理重いので非同期注意
                Bitmap icon = user.getIcon();
                Dao iconDao = dbHelper.getDao(MyIcon.class);
                {
                    QueryBuilder<MyIcon, Integer> queryBuilder = iconDao.queryBuilder();
                    queryBuilder.where().eq("id", user.getId());
                    List<MyIcon> icons = queryBuilder.query();
                    if (icons.size() == 0 && icon != null) {
                        iconDao.createOrUpdate(new MyIcon(user.getId(), icon));
                    }
                }
            }

            SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);

            long id = user == null ? scream.getUserId() : user.getId() ;
            // 自分
            if(id == pref.getLong("user_id", -1L)){
                Intent dataIntent = new Intent(ACTION_DATA_RECEIVED);
                if(user != null){
                    dataIntent.putExtra("USER", (Parcelable) new MyUser(user, MyIcon.getIconBytesById(this, id), MyTag.getTagSetById(this, id)));
                }
                if(scream != null) {
                    dataIntent.putExtra("SCREAM", (Parcelable) scream);
                }
                sendBroadcast(dataIntent);
            }
            // 他の人
            else {
                if(user != null) {
                    Intent userIntent = new Intent(ACTION_USER_RECEIVED);
                    List<MyUser> users = new ArrayList<>();

                    for (MyUser u : allUsers) {
                        users.add(new MyUser(u, MyIcon.getIconBytesById(this, u.getId()), MyTag.getTagSetById(this, u.getId())));
                    }
                    userIntent.putParcelableArrayListExtra("USER", new ArrayList<>(users));
                    sendBroadcast(userIntent);
                }

                if(scream != null) {
                    Intent screamIntent = new Intent(ACTION_SCREAM_RECEIVED);
                    List<MyScream> screams = new ArrayList<>();
                    screams.addAll(allScreams);
                    screamIntent.putParcelableArrayListExtra("SCREAM", new ArrayList<>(screams));
                    sendBroadcast(screamIntent);
                }

                if(user != null) {
                    Intent matchingIntent = new Intent(this, MatchingService.class);
                    matchingIntent.putExtra("USER", (Parcelable) user);
                    startService(matchingIntent);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void sendData(Context context, MyUser user, MyScream scream) {
        Intent intent = new Intent(context, Switcher.class);
        intent.putExtra("USER", (Parcelable) user);
        intent.putExtra("SCREAM", (Parcelable) scream);
        context.startService(intent);
    }

    public static void sendData(Context context, MyUser user) {
        Intent intent = new Intent(context, Switcher.class);
        intent.putExtra("USER", (Parcelable) user);
        context.startService(intent);
    }

    public static void sendData(Context context, MyScream scream) {
        Intent intent = new Intent(context, Switcher.class);
        intent.putExtra("SCREAM", (Parcelable) scream);
        context.startService(intent);
    }
}
