package lab.kulebin.mydictionary.adapter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import lab.kulebin.mydictionary.app.Constants;
import lab.kulebin.mydictionary.app.EntryFragment;
import lab.kulebin.mydictionary.model.Entry;

public class EntryPagerAdapter extends FragmentStatePagerAdapter {

    private Cursor mCursor;

    public EntryPagerAdapter(final FragmentManager pFm, final Cursor pCursor) {
        super(pFm);
        this.mCursor = pCursor;
    }

    @Override
    public Fragment getItem(final int position) {

        final Fragment fragment = new EntryFragment();
        mCursor.moveToPosition(position);

        final Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ENTRY_ID, mCursor.getLong(mCursor.getColumnIndex(Entry.ID)));
        args.putString(Constants.EXTRA_ENTRY_VALUE, mCursor.getString(mCursor.getColumnIndex(Entry.VALUE)));
        args.putString(Constants.EXTRA_ENTRY_TRANSLATION, mCursor.getString(mCursor.getColumnIndex(Entry.TRANSLATION)));
        args.putString(Constants.EXTRA_ENTRY_IMAGE_URL, mCursor.getString(mCursor.getColumnIndex(Entry.IMAGE_URL)));
        args.putString(Constants.EXTRA_ENTRY_USAGE_CONTEXT, mCursor.getString(mCursor.getColumnIndex(Entry.USAGE_CONTEXT)));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
