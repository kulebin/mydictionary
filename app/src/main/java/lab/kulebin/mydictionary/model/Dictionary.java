package lab.kulebin.mydictionary.model;

import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbString;

@Table(name="DICTIONARY")
public class Dictionary {

    @dbInteger
    public static final String ID = "id";
    @dbString
    public static final String NAME = "name";

    private int id;
    private String name;
}
