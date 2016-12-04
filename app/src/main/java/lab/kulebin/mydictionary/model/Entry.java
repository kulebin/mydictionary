package lab.kulebin.mydictionary.model;

import lab.kulebin.mydictionary.db.annotations.Table;
import lab.kulebin.mydictionary.db.annotations.dbInteger;
import lab.kulebin.mydictionary.db.annotations.dbLong;
import lab.kulebin.mydictionary.db.annotations.dbString;

@Table(name = "entry")
public class Entry {

    private static final String STRING_SEPARATOR = "||";

    @dbInteger
    public static final String ID = "id";
    @dbInteger
    public static final String DICTIONARY_ID = "dictionaryId";
    @dbString
    public static final String VALUE = "value";
    @dbString
    public static final String TRANSCRIPTION = "transcription";
    @dbLong
    public static final String CREATION_DATE = "creationDate";
    @dbLong
    public static final String LAST_EDITION_DATE = "lastEditionDate";
    @dbString
    public static final String IMAGE_URL = "imageUrl";
    @dbString
    public static final String SOUND_URL = "soundUrl";
    @dbString
    public static final String TRANSLATION = "translation";
    @dbString
    public static final String USAGE_CONTEXT = "usageContext";


    private int mId;
    private int mDictionaryId;
    private String mValue;
    private String mTranscription;
    private long mCreationDate;
    private long mLastEditionDate;
    private String mImageUrl;
    private String mSoundUrl;
    private String[] mTranslation;
    private String[] mUsageContext;

    //TODO should be deleted
    public Entry() {
    }

    public Entry(final int pId,
                 final int pDictionaryId,
                 final String pValue,
                 final String pTranscription,
                 final long pCreationDate,
                 final long pLastEditionDate,
                 final String pImageUrl,
                 final String pSoundUrl,
                 final String[] pTranslation,
                 final String[] pUsageContext) {
        mId = pId;
        mDictionaryId = pDictionaryId;
        mValue = pValue;
        mTranscription = pTranscription;
        mCreationDate = pCreationDate;
        mLastEditionDate = pLastEditionDate;
        mImageUrl = pImageUrl;
        mSoundUrl = pSoundUrl;
        mTranslation = pTranslation;
        mUsageContext = pUsageContext;
    }

    public int getId() {
        return mId;
    }

    public void setId(final int pId) {
        mId = pId;
    }

    public int getDictionaryId() {
        return mDictionaryId;
    }

    public void setDictionaryId(final int pDictionaryId) {
        mDictionaryId = pDictionaryId;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(final String pValue) {
        mValue = pValue;
    }

    public String getTranscription() {
        return mTranscription;
    }

    public void setTranscription(final String pTranscription) {
        mTranscription = pTranscription;
    }

    public long getCreationDate() {
        return mCreationDate;
    }

    public void setCreationDate(final long pCreationDate) {
        mCreationDate = pCreationDate;
    }

    public long getLastEditionDate() {
        return mLastEditionDate;
    }

    public void setLastEditionDate(final long pLastEditionDate) {
        mLastEditionDate = pLastEditionDate;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(final String pImageUrl) {
        mImageUrl = pImageUrl;
    }

    public String getSoundUrl() {
        return mSoundUrl;
    }

    public void setSoundUrl(final String pSoundUrl) {
        mSoundUrl = pSoundUrl;
    }

    public String[] getTranslation() {
        return mTranslation;
    }

    public void setTranslation(final String[] pTranslation) {
        mTranslation = pTranslation;
    }

    public String[] getUsageContext() {
        return mUsageContext;
    }

    public void setUsageContext(final String[] pUsageContext) {
        mUsageContext = pUsageContext;
    }

    public static String[] convertStringToStirngArray(String pString) {
        return pString.split(STRING_SEPARATOR);
    }

    public static String convertStringArrayToString(String[] pStringArray) {
        if (pStringArray != null) {
            StringBuilder builder = new StringBuilder();
            for (String string : pStringArray) {
                builder.append(string);
                builder.append(STRING_SEPARATOR);
            }
            return builder.toString();
        }
        return null;
    }
}
