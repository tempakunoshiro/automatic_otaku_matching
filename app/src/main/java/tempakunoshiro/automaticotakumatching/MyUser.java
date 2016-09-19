package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Nan on 2016/09/17.
 */
@DatabaseTable(tableName = "user")
public class MyUser implements Parcelable {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private long iconId;
    @DatabaseField(canBeNull = false)
    private String twitterId;
    @DatabaseField(canBeNull = false)
    private String comment;

    private MyUser(){}

    protected MyUser(Parcel src) {
        this.id = src.readLong();
        this.name = src.readString();
        this.iconId = src.readLong();
        this.twitterId = src.readString();
        this.comment = src.readString();
    }

    public MyUser(long id, String name, long iconId, String twitterId, String comment){
        if(id <= 0){
            throw new IllegalArgumentException("idは1以上にしてください");
        }
        this.id = id;
        this.name = name;
        this.iconId = iconId;
        this.twitterId = twitterId;
        this.comment = comment;
    }

    @Nullable
    public static MyUser getInstanceFromId(Context context, long userId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context.getApplicationContext());
        MyUser user = null;
        try {
            Dao userDao =  dbHelper.getDao(MyUser.class);
            QueryBuilder<MyUser, Integer> queryBuilder = userDao.queryBuilder();
            queryBuilder.where().eq("id", userId);
            List<MyUser> users = queryBuilder.query();
            if(users.size() != 0){
                user = users.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static final Creator<MyUser> CREATOR = new Creator<MyUser>() {
        @Override
        public MyUser createFromParcel(Parcel in) {
            return new MyUser(in);
        }

        @Override
        public MyUser[] newArray(int size) {
            return new MyUser[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeLong(iconId);
        dest.writeString(twitterId);
        dest.writeString(comment);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getIconId() {
        return iconId;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public String getComment() {
        return comment;
    }
}
