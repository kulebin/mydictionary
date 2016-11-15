package lab.kulebin.mydictionary.model;

import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbString;

@Table(name = "ENTRY")
public class Entry {

    @dbInteger
    public static final String ID = "id";
    @dbString
    public static final String VALUE = "value";
    @dbString
    public static final String TRANSLATION = "translation";
    @dbString
    public static final String IMAGE_URL = "imageUrl";

    private int id;
    private String value;
    private String translation;
    private String imageUrl;

    public Entry(){}

    public Entry(final int pId, final String pValue, final String pTranslation, final String pImageUrl) {
        id = pId;
        value = pValue;
        translation = pTranslation;
        imageUrl = pImageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(final int pId) {
        id = pId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String pValue) {
        value = pValue;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(final String pTranslation) {
        translation = pTranslation;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String pImageUrl) {
        imageUrl = pImageUrl;
    }
}
