package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.github.javafaker.Faker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nan on 2016/09/22.
 */
public class RandomDataSender extends IntentService {
    public RandomDataSender() {
        super("RandomDataSender");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int interval = intent.getIntExtra("INTERVAL", 3000);
        long endTime = System.currentTimeMillis() + 360000;
        List<String> tags = Arrays.asList("アニメ", "アイドル", "鉄道", "ゲーム", "パソコン", "文学", "プロレス", "ガンダム", "特撮", "フィギュア", "クイズ");
        while(System.currentTimeMillis() < endTime){
            try {
                Collections.shuffle(tags);
                Faker faker = new Faker(Locale.JAPAN);
                long id =(long) Math.ceil(Math.random() * Long.MAX_VALUE);
                MyUser u = new MyUser(id, faker.name().lastName() + faker.name().firstName(), null, "twitter", "Comment", Arrays.asList(tags.get(0)), System.currentTimeMillis());
                MyScream s = new MyScream(id, "叫びだよ～", System.currentTimeMillis());
                Switcher.sendData(this, u, s);
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendRandomData(Context context, int intervalMillis) {
        Intent intent = new Intent(context, RandomDataSender.class);
        intent.putExtra("INTERVAL", intervalMillis);
        context.startService(intent);
    }
}
