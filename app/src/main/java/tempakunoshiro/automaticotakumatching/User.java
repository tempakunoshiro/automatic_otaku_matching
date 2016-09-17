package tempakunoshiro.automaticotakumatching;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Nan on 2016/09/17.
 */
@DatabaseTable(tableName = "user")
public class User {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String element;
    @DatabaseField(canBeNull = false)
    private String tag;
    @DatabaseField(canBeNull = false)
    private String twitterID;
    @DatabaseField(canBeNull = false)
    private String comment;

    private User(){}

    public User(long id, String name, String element, String tag, String twitterID, String comment){
        this.id = id;
        this.name = name;
        this.element = element;
        this.tag = tag;
        this.twitterID = twitterID;
        this.comment = comment;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getElement() {
        return element;
    }

    public String getTag() {
        return tag;
    }

    public String getTwitterID() {
        return twitterID;
    }

    public String getComment() {
        return comment;
    }

}
