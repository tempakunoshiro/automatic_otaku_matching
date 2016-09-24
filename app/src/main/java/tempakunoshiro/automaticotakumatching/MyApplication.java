package tempakunoshiro.automaticotakumatching;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Nan on 2016/09/23.
 */
public class MyApplication extends Application {
    private static Context context;

    private static Bitmap otakuIcon;
    private static Bitmap emootakuIcon;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        otakuIcon = BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.drawable.otaku_icon);
        emootakuIcon = BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.drawable.emootaku_icon);
    }

    public static Context getContext(){
        return context;
    }

    public static Bitmap getEmootakuIcon() {
        return emootakuIcon;
    }

    public static Bitmap getOtakuIcon() {
        return otakuIcon;
    }
}