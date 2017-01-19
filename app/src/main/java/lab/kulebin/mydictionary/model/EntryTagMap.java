package lab.kulebin.mydictionary.model;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbLong;
import lab.kulebin.mydictionary.db.annotations.dbUnique;

@Table(name = "entryTagMap")
public class EntryTagMap {


    @dbLong
    public static final String ID = Constants.ID_COLUMN;
    @dbLong
    public static final String ENTRY_ID = "entryId";
    @dbLong
    public static final String TAG_ID = "tagId";

}
