package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    @Nullable
    public static byte[] getIconBytesById(Context context, long userId) {
        InputStream is = null;
        try {
            if (!new File(context.getFilesDir().toURI().resolve(String.valueOf(userId) + ".png")).exists()) {
                return getDefaultIconBytes();
            }
            is = new BufferedInputStream(context.openFileInput(String.valueOf(userId) + ".png"));

            byte[] b = new byte[1];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (is.read(b) > 0) {
                baos.write(b);
            }
            baos.close();

            return baos.toByteArray();
        } catch (IOException e) {
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
        return getDefaultIconBytes();
    }

    @Nullable
    public static Bitmap getIconById(Context context, long userId) {
        InputStream is = null;
        try {
            if(!new File(context.getFilesDir().toURI().resolve(String.valueOf(userId) + ".png")).exists()){
                return getDefaultIcon();
            }
            is = new BufferedInputStream(context.openFileInput(String.valueOf(userId) + ".png"));

            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
        } finally {
            try {
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
            }
        }
        return getDefaultIcon();
    }

    private static byte[] getDefaultIconBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getDefaultIcon().compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private static Bitmap getDefaultIcon(){
        return MyApplication.getOtakuIcon();
    }
}
