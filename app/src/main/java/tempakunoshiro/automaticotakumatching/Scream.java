package tempakunoshiro.automaticotakumatching;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.StringBufferInputStream;
import java.util.Date;

import tempakunoshiro.automaticotakumatching.DatabaseContent.UserColumns;

/**
 * Created by Nan on 2016/09/17.
 */
@DatabaseTable(tableName = "scream")
public class Scream {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField(canBeNull = false)
    private long userId;
    @DatabaseField(canBeNull = false)
    private String text;
    @DatabaseField(canBeNull = false)
    private long time;

    private Scream(){}

    public Scream(long id, long userId, String text, long time) {
        if(userId <= 0){

        }
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public long getTime() {
        return time;
    }


}
