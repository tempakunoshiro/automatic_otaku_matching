package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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

                    long[] idArray = new long[allUsers.size()];
                    for(int i = 0; i < allUsers.size(); i++){
                        idArray[i] = allUsers.get(i).getId();
                    }
                    userIntent.putExtra("USER", idArray);
                    sendBroadcast(userIntent);
                }

                // スクリームデータ送信
                if(scream != null) {
                    Intent screamIntent = new Intent(ACTION_SCREAM_RECEIVED);
                    List<MyScream> screams = new ArrayList<>();
                    screams.addAll(allScreams);

                    long[] idArray = new long[allScreams.size()];
                    for(int i = 0; i < allScreams.size(); i++){
                        idArray[i] = allScreams.get(i).getId();
                    }
                    screamIntent.putExtra("SCREAM", idArray);
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

    public static void sendData(Context context, String base64Str) {
        Intent intent = new Intent(context, Switcher.class);

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(base64Str, Base64.DEFAULT)));
            MyData data = (MyData) ois.readObject();

            intent.putExtra("USER", (Parcelable) data.getUser());
            intent.putExtra("SCREAM", (Parcelable) data.getScream());
            data.getUser().saveIconLocalStorage(context, BitmapFactory.decodeByteArray(data.getIconBytes(), 0, data.getIconBytes().length));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(ois != null){
                    ois.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }

        context.startService(intent);
    }
}
