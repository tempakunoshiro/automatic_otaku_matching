package tempakunoshiro.automaticotakumatching;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Nakano on 2016/09/16.
 */
public final class DatabaseContent {
    public static final class UserColumns implements BaseColumns {
        public static final String NAME = "name";
        public static final String ELEMENT = "element";
        public static final String TAG = "tag";
        public static final String TWITTER_ID = "twitter_id";
        public static final String COMMENT = "comment";
    }

    public static final class ScreamColumns implements BaseColumns {
        public static final String USER_ID = "user_id";
        public static final String TEXT = "text";
        public static final String TIME = "time";
    }
}
