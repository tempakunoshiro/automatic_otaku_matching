package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

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
public class MatchingService extends IntentService {
    public static final String ACTION_MATCHING_RECEIVED = "tempakunoshiro.automaticotakumatching.ACTION_MATCHING_RECEIVED";

    public MatchingService() {
        super("MatchingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MyUser user = intent.getParcelableExtra("USER");
        SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);
        MyUser myself = MyUser.getMyUserById(getApplicationContext(), pref.getLong("user_id", -1L));
        if(user == null || myself == null){
            return;
        }

        Set<String> tags1 =  user.getTagSet();
        Set<String> tags2 =  myself.getTagSet();

        int matchingCount = 0;
        for(String t1 : tags1){
            if(tags2.contains(t1)){
                matchingCount++;
            }
        }

        if(matchingCount == 0){
            return;
        }

        // PUSH通知
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("オートマチック・オタク・マッチング")
                        .setContentText("趣味が近いオタクを見つけました！")
                        .setTicker("趣味が近いオタクを見つけました！");
        if(pref.getBoolean("vibrate", true)){
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

//        Intent resultIntent = new Intent(this, MainActivity.class);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1089, builder.build());

        Intent matchingIntent = new Intent(ACTION_MATCHING_RECEIVED);
        sendBroadcast(matchingIntent);
    }
}
