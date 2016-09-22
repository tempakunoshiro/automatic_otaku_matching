package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
            boolean isNewUser = false;
            // user追加処理
            Dao userDao = dbHelper.getDao(MyUser.class);
            if(user != null){
                CreateOrUpdateStatus status =  userDao.createOrUpdate(user);
                if(status.isCreated()){
                    isNewUser = true;
                }
            }
            List<MyUser> allUsers = userDao.queryForAll();

            // scream追加処理
            Dao screamDao = dbHelper.getDao(MyScream.class);
            if(scream != null) {
                screamDao.createOrUpdate(scream);
            }
            List<MyScream> allScreams = screamDao.queryForAll();

            if(user != null) {
                // tag追加処理
                Dao tagDao = dbHelper.getDao(MyTag.class);
                Dao taggerDao = dbHelper.getDao(MyTagger.class);

                DeleteBuilder<MyTagger, Integer> deleteBuilder  = taggerDao.deleteBuilder();
                deleteBuilder.where().eq("userId", user.getId());
                deleteBuilder.delete();

                List<String> tags = user.getTagList();
                int orderNum = 0;
                for (String s : tags) {
                    QueryBuilder<MyTag, Integer> queryBuilder = tagDao.queryBuilder();
                    queryBuilder.where().eq("tag", s);
                    List<MyTag> tempTags = queryBuilder.query();
                    if (tempTags.size() == 0) {
                        MyTag t = new MyTag(s);
                        tagDao.createOrUpdate(t);
                        taggerDao.createOrUpdate(new MyTagger(t.getId(), user.getId(), orderNum));
                    } else {
                        taggerDao.createOrUpdate(new MyTagger(tempTags.get(0).getId(), user.getId(), orderNum));
                    }
                    orderNum++;
                }

                // icon追加処理、やや処理重いので非同期注意（SQLアクセスは先にやっとけ）
                byte[] iconBytes = user.getIconBytes();
                Dao iconDao = dbHelper.getDao(MyIcon.class);
                iconDao.createOrUpdate(new MyIcon(user.getId(), iconBytes));
            }

            // データ送信部分
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            long id = user == null ? scream.getUserId() : user.getId();
            // 自分のデータが来た→通信部に送信
            if(id == pref.getLong("USER_ID", -1L)){
                if(user != null && user.getModifiedTime() == 0 && user.getName().equals(getString(R.string.default_name))){
                    return;
                }

                Intent dataIntent = new Intent(ACTION_DATA_RECEIVED);
                if(user != null){
                    user.setIconBytes(MyIcon.getIconBytesById(this, id));
                    user.setTagList(MyTag.getTagListById(this, id));
                    dataIntent.putExtra("USER", (Parcelable) user);
                }
                if(scream != null) {
                    dataIntent.putExtra("SCREAM", (Parcelable) scream);
                }
                sendBroadcast(dataIntent);
            }
            // 他の人のデータが来た→各アクティビティに送信＆マッチング
            else {
                // ユーザーデータ送信
                if(user != null) {
                    Intent userIntent = new Intent(ACTION_USER_RECEIVED);
                    List<MyUser> users = new ArrayList<>();

                    for (MyUser u : allUsers) {
                        u.setIconBytes(MyIcon.getIconBytesById(this, u.getId()));
                        u.setTagList(MyTag.getTagListById(this, u.getId()));
                        users.add(u);
                    }
                    userIntent.putParcelableArrayListExtra("USER", new ArrayList<>(users));
                    sendBroadcast(userIntent);
                }

                // スクリームデータ送信
                if(scream != null) {
                    Intent screamIntent = new Intent(ACTION_SCREAM_RECEIVED);
                    List<MyScream> screams = new ArrayList<>();
                    screams.addAll(allScreams);
                    screamIntent.putParcelableArrayListExtra("SCREAM", new ArrayList<>(screams));
                    sendBroadcast(screamIntent);
                }

                // マッチング部分に処理投げる
                if(user != null && isNewUser) {
                    Intent matchingIntent = new Intent(this, MatchingService.class);
                    matchingIntent.putExtra("USER", (Parcelable) user);
                    startService(matchingIntent);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public static void sendData(Context context, MyUser user, MyScream scream) {
        Intent intent = new Intent(context, Switcher.class);
        intent.putExtra("USER", (Parcelable) user);
        intent.putExtra("SCREAM", (Parcelable) scream);
        context.startService(intent);
    }
}
