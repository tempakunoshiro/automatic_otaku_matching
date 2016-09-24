package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Nan on 2016/09/19.
 */
public class MyIcon {
    public final static Uri OTAKU_URI  = Uri.parse("android.resource://tempakunoshiro.automaticotakumatching/" + R.drawable.otaku_icon);

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
