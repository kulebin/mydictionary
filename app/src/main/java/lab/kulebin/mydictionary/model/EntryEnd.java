package lab.kulebin.mydictionary.model;

import android.support.annotation.NonNull;

public class EntryEnd {

    @NonNull
    private int mId;
    @NonNull
    private int mDictionaryId;
    @NonNull
    private String mValue;
    private String mTranscription;
    @NonNull
    private long mCreationDate;
    private long mLastEditionDate;
    private String mImageUrl;
    private String mSoundUrl;
    private String [] mTranslationSet;
    private String [] mContextSet;

    @NonNull
    public int getId() {
        return mId;
    }

    public void setId(@NonNull final int pId) {
        mId = pId;
    }

    @NonNull
    public int getDictionaryId() {
        return mDictionaryId;
    }

    public void setDictionaryId(@NonNull final int pDictionaryId) {
        mDictionaryId = pDictionaryId;
    }

    @NonNull
    public String getValue() {
        return mValue;
    }

    public void setValue(@NonNull final String pValue) {
        mValue = pValue;
    }

    public String getTranscription() {
        return mTranscription;
    }

    public void setTranscription(final String pTranscription) {
        mTranscription = pTranscription;
    }

    @NonNull
    public long getCreationDate() {
        return mCreationDate;
    }

    public void setCreationDate(@NonNull final long pCreationDate) {
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

    public String[] getTranslationSet() {
        return mTranslationSet;
    }

    public void setTranslationSet(final String[] pTranslationSet) {
        mTranslationSet = pTranslationSet;
    }

    public String[] getContextSet() {
        return mContextSet;
    }

    public void setContextSet(final String[] pContextSet) {
        mContextSet = pContextSet;
    }
}
