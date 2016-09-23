package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public MyUser(long id, String name, String twitterId, String comment, List<String> tags, long modifiedTime){
        if(id <= 0){
            throw new IllegalArgumentException("idは1以上にしてください");
        }
        this.id = id;
        this.name = name;
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

    public boolean saveIconLocalStorage(Context context, Bitmap iconBmp) {
        OutputStream os = null;
        Bitmap tmpIcon = null;
        try {
            os = new BufferedOutputStream(context.openFileOutput(String.valueOf(id) + ".png", Context.MODE_PRIVATE));
            tmpIcon = iconBmp.copy(Bitmap.Config.ARGB_8888, true);
            return tmpIcon.compress(Bitmap.CompressFormat.PNG, 100, os);
        } catch (IOException e) {
        } finally {
            if (tmpIcon != null) {
                tmpIcon.recycle();
            }
            try {
                if(os != null){
                    os.close();
                }
            } catch (IOException e) {
            }
        }
        return false;
    }

    private byte[] loadIconBytesLocalStorage(Context context) {
        InputStream is = null;
        try {
            if(!new File(context.getFilesDir().toURI().resolve(String.valueOf(id) + ".png")).exists()){
                return getDefaultIconBytes();
            }
            is = new BufferedInputStream(context.openFileInput(String.valueOf(id) + ".png"));

            byte[] b = new byte[1];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (is.read(b) > 0) {
                baos.write(b);
            }
            baos.close();

            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
            }
        }
        return getDefaultIconBytes();
    }

    private Bitmap loadIconLocalStorage(Context context) {
        InputStream is = null;
        try {
            if(!new File(context.getFilesDir().toURI().resolve(String.valueOf(id) + ".png")).exists()){
                return getDefaultIcon();
            }
            is = new BufferedInputStream(context.openFileInput(String.valueOf(id) + ".png"));

            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
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
        Bitmap iconBmp = null;
        iconBmp = loadIconLocalStorage(MyApplication.getContext());
        return iconBmp;
    }

    public byte[] getIconBytes() {
        byte[] iconBytes = null;
        iconBytes = loadIconBytesLocalStorage(MyApplication.getContext());
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

    private byte[] getDefaultIconBytes(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getDefaultIcon().compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private Bitmap getDefaultIcon(){
        if("エモ=オタク".equals(name)){
            return BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.drawable.emootaku_icon);
        }else{
            return BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.drawable.otaku_icon);
        }
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
                ", twitterId='" + twitterId + '\'' +
                ", comment='" + comment + '\'' +
                ", tags=" + tags +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}
