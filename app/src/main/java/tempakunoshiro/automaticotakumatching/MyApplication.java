package tempakunoshiro.automaticotakumatching;

import android.app.Application;
import android.content.Context;

/**
 * Created by Nan on 2016/09/23.
 */
public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext(){
        return context;
    }
}