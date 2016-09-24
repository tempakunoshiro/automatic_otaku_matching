package tempakunoshiro.automaticotakumatching;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
        List<MyUser> users = new ArrayList<>();
        while(System.currentTimeMillis() < endTime){
            MyUser u;
            MyScream s;
            try {
                Random rnd = new Random();
                int i = rnd.nextInt(3);
                if(users.size() < 5 || i <= 1){
                    Collections.shuffle(tags);
                    Faker faker = new Faker(Locale.JAPAN);
                    long id =(long) Math.ceil(Math.random() * Long.MAX_VALUE);
                    u = new MyUser(id, faker.name().lastName() + faker.name().firstName(), "twitter", "Comment", Arrays.asList(tags.get(0)), System.currentTimeMillis());
                    s = new MyScream(id, "叫びだよ～", System.currentTimeMillis());
                    Bitmap image = BitmapFactory.decodeResource(getApplicationContext().getResources(), getRandomOtaku());
                    u.saveIconLocalStorage(this, image);
                    users.add(u);
                }else{
                    Collections.shuffle(users);
                    u = users.get(0);
                    s = new MyScream(u.getId(), "もう一度叫んでるよ～", System.currentTimeMillis());
                    if(users.size() < 10){
                        users.remove(users.size() - 1);
                    }
                }

                if(i == 0){
                    Switcher.sendData(this, u);
                }else{
                    Switcher.sendData(this, u, s);
                }
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int getRandomOtaku(){
        List<Integer> resList = new ArrayList<Integer>();
        String path = "android.resource://tempakunoshiro.automaticotakumatching/";
        resList.add(R.drawable.yokootokob);
        resList.add(R.drawable.yokootokog);
        resList.add(R.drawable.yokootokoo);
        resList.add(R.drawable.yokootokop);
        resList.add(R.drawable.yokootokor);
        resList.add(R.drawable.yokootokoy);

        Random random = new Random();
        int res = resList.get(random.nextInt(resList.size()));
        return res;
    }

    public static void sendRandomData(Context context, int intervalMillis) {
        Intent intent = new Intent(context, RandomDataSender.class);
        intent.putExtra("INTERVAL", intervalMillis);
        context.startService(intent);
    }
}
