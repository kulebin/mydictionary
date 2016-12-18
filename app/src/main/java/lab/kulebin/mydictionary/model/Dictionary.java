package lab.kulebin.mydictionary.model;

import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbLong;
import lab.kulebin.mydictionary.db.annotations.dbString;

@Table(name = "dictionary")
public class Dictionary {

    @dbInteger
    public static final String ID = "_id";
    @dbString
    public static final String NAME = "name";
    @dbLong
    public static final String CREATION_DATE = "creationDate";

    private int mId;
    private String mName;
    private long mCreationDate;

    public Dictionary(final int pId, final String pName, final long pCreationDate) {
        mId = pId;
        mName = pName;
        mCreationDate = pCreationDate;
    }

    public long getCreationDate() {
        return mCreationDate;
    }

    public void setCreationDate(final long pCreationDate) {
        mCreationDate = pCreationDate;
    }

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
