package tempakunoshiro.automaticotakumatching;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Nan on 2016/09/21.
 */
public class MyData implements Serializable {
    private MyUser user;
    private MyScream scream;

    public MyData(MyUser user, MyScream scream) {
        this.user = user;
        this.scream = scream;
    }

    public MyUser getUser() {
        return user;
    }

    public MyScream getScream() {
        return scream;
    }
}
