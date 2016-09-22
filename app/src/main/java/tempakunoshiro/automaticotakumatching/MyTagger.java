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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nan on 2016/09/19.
 */
@DatabaseTable(tableName = "tagger")
public class MyTagger implements Parcelable {
    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(canBeNull = false)
    private long tagId;
    @DatabaseField(canBeNull = false)
    private long userId;
    @DatabaseField(canBeNull = false)
    private int orderNum;

    private MyTagger(){}

    protected MyTagger(Parcel in) {
        this.id = in.readLong();
        this.tagId = in.readLong();
        this.userId = in.readLong();
        this.orderNum = in.readInt();
    }

    public MyTagger(long tagId, long userId, int orderNum) {
        this.tagId = tagId;
        this.userId = userId;
        this.orderNum = orderNum;
    }

    public static final Creator<MyTagger> CREATOR = new Creator<MyTagger>() {
        @Override
        public MyTagger createFromParcel(Parcel in) {
            return new MyTagger(in);
        }

        @Override
        public MyTagger[] newArray(int size) {
            return new MyTagger[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(tagId);
        dest.writeLong(userId);
        dest.writeInt(orderNum);
    }

    public long getId() {
        return id;
    }

    public long getTagId() {
        return tagId;
    }

    public long getUserId() {
        return userId;
    }

    public int getOrderNum() {
        return orderNum;
    }
}
