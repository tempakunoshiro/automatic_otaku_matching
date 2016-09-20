package tempakunoshiro.automaticotakumatching;

/**
 * Created by Nan on 2016/09/17.
 */

public class User {

    private long id;

    private String name;

    private String element;

    private String tag;

    private String twitterID;

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