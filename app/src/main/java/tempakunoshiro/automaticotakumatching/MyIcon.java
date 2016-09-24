package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by Nan on 2016/09/19.
 */
public class MyIcon {
    public final static Uri OTAKU_URI  = Uri.parse("android.resource://tempakunoshiro.automaticotakumatching/" + R.drawable.yokootokob);

    @Nullable
    public static Uri getIconUriById(Context context, long userId) {
        Uri iconUri = Uri.parse(MyApplication.getContext().getFilesDir() + "/" + String.valueOf(userId) + ".png");
        File iconFile = new File(iconUri.toString());
        if(iconFile.exists()){
            return iconUri;
        }
        return OTAKU_URI;
    }
}
