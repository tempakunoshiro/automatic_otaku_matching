package tempakunoshiro.automaticotakumatching;

import android.os.Parcelable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Nan on 2016/09/21.
 */
public class MyData implements Serializable {
    private MyUser user;
    private MyScream scream;
    private byte[] iconBytes;

    public MyData(MyUser user, MyScream scream) {
        this.user = user;
        this.scream = scream;
        if(MyApplication.getOtakuIcon().equals(user.getIcon()) || MyApplication.getEmootakuIcon().equals(user.getIcon())){
            this.iconBytes = null;
        }else{
            this.iconBytes = user.getIconBytes();
        }
    }

    public MyUser getUser() {
        return user;
    }

    public MyScream getScream() {
        return scream;
    }

    public byte[] getIconBytes() {
        return iconBytes;
    }

    public String toBase64Data(){
        byte[] outBytes = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.flush();
            outBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(oos != null){
                    oos.close();
                }
                baos.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }

        if(outBytes == null){
            return "";
        }else{
            return Base64.encodeToString(outBytes, Base64.NO_WRAP);
        }
    }
}
