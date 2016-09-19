package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Nan on 2016/09/19.
 */
@DatabaseTable(tableName = "icon")
public class MyIcon implements Parcelable {
    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    byte[] iconBytes;

    private MyIcon(){}

    protected MyIcon(Parcel src) {
        this.id = src.readLong();
        int arrayLength = src.readInt();
        byte[] iconBytes = new byte[arrayLength];
        src.readByteArray(iconBytes);
        this.iconBytes = iconBytes;
    }

    public MyIcon(long userId, byte[] iconBytes){
        if(userId <= 0){
            throw new IllegalArgumentException("idは1以上にしてください");
        }
        this.iconBytes = iconBytes;
    }

    public MyIcon(long userId, Bitmap bmp){
        if(userId <= 0){
            throw new IllegalArgumentException("idは1以上にしてください");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
        this.iconBytes = bos.toByteArray();
    }

    @Nullable
    public static MyIcon getInstanceFromId(Context context, long userId) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context.getApplicationContext());
        MyIcon icon = null;
        try {
            Dao iconDao =  dbHelper.getDao(MyIcon.class);
            QueryBuilder<MyIcon, Integer> queryBuilder = iconDao.queryBuilder();
            queryBuilder.where().eq("id", userId);
            List<MyIcon> icons = queryBuilder.query();
            if(icons.size() != 0){
                icon = icons.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return icon;
    }

    public static final Creator<MyIcon> CREATOR = new Creator<MyIcon>() {
        @Override
        public MyIcon createFromParcel(Parcel in) {
            return new MyIcon(in);
        }

        @Override
        public MyIcon[] newArray(int size) {
            return new MyIcon[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(iconBytes.length);
        dest.writeByteArray(iconBytes);
    }

    public long getId() {
        return id;
    }

    public byte[] getIconBytes() {
        return iconBytes;
    }

    public Bitmap getIcon() {
        ByteArrayInputStream baos = new ByteArrayInputStream(iconBytes);
        Bitmap bmp = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
        return bmp;
    }
}
