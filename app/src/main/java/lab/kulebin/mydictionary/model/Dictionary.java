package lab.kulebin.mydictionary.model;

import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbString;

@Table(name = "dictionary")
public class Dictionary {

    @dbInteger
    public static final String ID = "id";
    @dbString
    public static final String NAME = "name";

    private int mId;
    private String mName;

    public int getId() {
        return mId;
    }

    public void setId(final int pId) {
        mId = pId;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String pName) {
        mName = pName;
    }
}
