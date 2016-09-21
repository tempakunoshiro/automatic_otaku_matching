package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public static LinkedHashSet<String> getTagSetById(Context context, long userId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        List<MyTag> tags = new ArrayList<>();
        try {
            Dao taggerDao =  dbHelper.getDao(MyTagger.class);
            QueryBuilder<MyTagger, Integer> queryBuilder = taggerDao.queryBuilder();
            queryBuilder.where().eq("userId", userId);
            List<MyTagger> taggers = queryBuilder.query();
            for(MyTagger tgg : taggers){
                Dao tagDao =  dbHelper.getDao(MyTag.class);
                QueryBuilder<MyTag, Integer> queryBuilder2 = tagDao.queryBuilder();
                queryBuilder2.where().eq("id", tgg.getTagId());
                tags.addAll(queryBuilder2.query());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LinkedHashSet<String> tagSet = new LinkedHashSet<>();
        for(MyTag t : tags){
            tagSet.add(t.getTag());
        }
        return tagSet;
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
