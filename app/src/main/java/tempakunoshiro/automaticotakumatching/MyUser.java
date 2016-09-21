package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nan on 2016/09/17.
 */
@DatabaseTable(tableName = "user")
public class MyUser implements Parcelable, Serializable {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField(canBeNull = false)
    private String name;
    private byte[] iconBytes;
    @DatabaseField(canBeNull = false)
    private String twitterId;
    @DatabaseField(canBeNull = false)
    private String comment;
    private List<String> tags;
    @DatabaseField(canBeNull = false)
    private long modifiedTime;

    private MyUser(){}

    protected MyUser(Parcel src) {
        this.id = src.readLong();
        this.name = src.readString();
        int iconBytesLength = src.readInt();
        if(iconBytesLength != 0){
            this.iconBytes = new byte[iconBytesLength];
            src.readByteArray(iconBytes);
        }
        this.twitterId = src.readString();
        this.comment = src.readString();
        boolean[] isNullArray = new boolean[1];
        src.readBooleanArray(isNullArray);
        if(isNullArray[0]){
            this.tags = null;
        }else{
            List<String> tagList = new ArrayList<>();
            src.readStringList(tagList);
            this.tags = tagList;
        }
       this.modifiedTime = src.readLong();
    }

    public MyUser(long id, String name, Bitmap iconBmp, String twitterId, String comment, List<String> tags, long modifiedTime){
        if(id <= 0){
            throw new IllegalArgumentException("idは1以上にしてください");
        }
        this.id = id;
        this.name = name;
        if(iconBmp == null){
            this.iconBytes = null;
        }else{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            iconBmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            this.iconBytes = baos.toByteArray();
        }
        this.twitterId = twitterId;
        this.comment = comment;
        this.tags = tags;
        this.modifiedTime = modifiedTime;
    }

    @Nullable
    public static MyUser getMyUserById(Context context, long userId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        MyUser user = null;
        try {
            Dao userDao = dbHelper.getDao(MyUser.class);
            QueryBuilder<MyUser, Integer> queryBuilder = userDao.queryBuilder();
            queryBuilder.where().eq("id", userId);
            List<MyUser> users = queryBuilder.query();
            if (users.size() != 0) {
                user = users.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(user == null){
            return null;
        }
        user.setIconBytes(MyIcon.getIconBytesById(context, userId));
        user.setTagList(MyTag.getTagListById(context, userId));
        return user;
    }

    public static List<MyUser> getAllMyUser(Context context) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        List<MyUser> users = new ArrayList<>();
        try {
            Dao userDao = dbHelper.getDao(MyUser.class);
            List<MyUser> tempUsers = userDao.queryForAll();
            for(MyUser u : tempUsers){
                u.setIconBytes(MyIcon.getIconBytesById(context, u.getId()));
                u.setTagList(MyTag.getTagListById(context, u.getId()));
                users.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
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
        if(iconBytes == null){
            dest.writeInt(0);
        }else{
            dest.writeInt(iconBytes.length);
            dest.writeByteArray(iconBytes);
        }
        dest.writeString(twitterId);
        dest.writeString(comment);
        if(tags == null){
            boolean[] isNullArray = new boolean[]{ true };
            dest.writeBooleanArray(isNullArray);
        }else{
            boolean[] isNullArray = new boolean[]{ false };
            dest.writeBooleanArray(isNullArray);
            dest.writeStringList(new ArrayList<>(tags));
        }
        dest.writeLong(modifiedTime);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public String getComment() {
        return comment;
    }

    public Bitmap getIcon() {
        if(iconBytes == null){
            return null;
        }
        Bitmap bmp = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
        return bmp;
    }

    public byte[] getIconBytes() {
        return iconBytes;
    }

    public List<String> getTagList() {
        return Collections.unmodifiableList(tags);
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(Bitmap iconBmp) {
        if(iconBmp == null){
            this.iconBytes = null;
        }else{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            iconBmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            this.iconBytes = baos.toByteArray();
        }
    }

    public void setIconBytes(byte[] iconBytes) {
        this.iconBytes = iconBytes;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTagList(List<String> tags) {
        this.tags = tags;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public String toString() {
        return "MyUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", iconBmp=" + iconBytes +
                ", twitterId='" + twitterId + '\'' +
                ", comment='" + comment + '\'' +
                ", tags=" + tags +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}
