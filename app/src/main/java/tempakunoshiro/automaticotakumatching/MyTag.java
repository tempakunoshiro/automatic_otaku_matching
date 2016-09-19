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
 * Created by Nan on 2016/09/19.
 */
@DatabaseTable(tableName = "tag")
public class MyTag implements Parcelable {
    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(canBeNull = false, unique = true)
    private String tag;

    private MyTag(){}

    protected MyTag(Parcel in) {
        id = in.readLong();
        tag = in.readString();
    }

    public MyTag(String tag) {
        this.tag = tag;
    }

    public static final Creator<MyTag> CREATOR = new Creator<MyTag>() {
        @Override
        public MyTag createFromParcel(Parcel in) {
            return new MyTag(in);
        }

        @Override
        public MyTag[] newArray(int size) {
            return new MyTag[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(tag);
    }

    public long getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }
}
