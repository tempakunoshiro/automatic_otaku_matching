package tempakunoshiro.automaticotakumatching;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Nakano on 2016/09/16.
 */
public final class DatabaseContent {
    public static final String AUTHORITY = "tempakunoshiro.automaticotakumatching.data";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String SELECTION_ID_BASED = BaseColumns._ID + " = ? ";

    public static final class UserColumns implements BaseColumns {
        public static final String NAME = "name";
        public static final String ELEMENT = "element";
        public static final String TAG = "tag";
        public static final String TWITTER_ID = "twitter_id";
        public static final String COMMENT = "comment";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(DatabaseContent.CONTENT_URI, "user");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.tempakunoshiro.data_user";
        public static final String CONTENT_USER_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.tempakunoshiro.data_user";
        public static final String[] PROJECTION_ALL ={_ID, NAME, ELEMENT, TAG, TWITTER_ID, COMMENT};
        public static final String SORT_ORDER_DEFAULT = _ID + " ASC";
    }

    public static final class ScreamColumns implements BaseColumns {
        public static final String USER_ID = "user_id";
        public static final String TEXT = "text";
        public static final String TIME = "time";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(DatabaseContent.CONTENT_URI, "scream");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.tempakunoshiro.data_scream";
        public static final String CONTENT_SCREAM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.tempakunoshiro.data_scream";
        public static final String[] PROJECTION_ALL ={_ID, USER_ID, TEXT, TIME};
        public static final String SORT_ORDER_DEFAULT = _ID + " ASC";
    }
}
