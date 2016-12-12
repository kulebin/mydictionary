package lab.kulebin.mydictionary.model;

import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbLong;
import lab.kulebin.mydictionary.db.annotations.dbString;

@Table(name = "dictionary")
public class Dictionary {

    @dbLong
    public static final String ID = "_id";
    @dbString
    public static final String NAME = "name";

    private long mId;
    private String mName;

    public Dictionary(final long pId, final String pName) {
        mId = pId;
        mName = pName;
    }

    public long getId() {
        return mId;
    }

    public void setId(final long pId) {
        mId = pId;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String pName) {
        mName = pName;
    }
}
