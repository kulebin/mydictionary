package lab.kulebin.mydictionary.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.adapter.EntryPagerAdapter;
import lab.kulebin.mydictionary.db.SortOrder;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.utils.UriBuilder;

public class EntryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ENTRY_LOADER = 0;
    private static final String[] ENTRY_PROJECTION = {
            Entry.ID,
            Entry.VALUE,
            Entry.TRANSLATION,
            Entry.IMAGE_URL,
            Entry.USAGE_CONTEXT
    };

    private ViewPager viewPager;
    private EntryPagerAdapter mEntryPagerAdapter;
    private long mEntryId;
    private int mDictionaryMenuId;
    private SortOrder mSortOrder;
    private int mEntryPosition;
    private String mIntentSender;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        final Intent intent = getIntent();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(intent.getCharSequenceExtra(Constants.EXTRA_SELECTED_DICTIONARY_NAME));
        }

        mIntentSender = intent.getStringExtra(Constants.EXTRA_INTENT_SENDER);
        if (mIntentSender.equals(MainActivity.class.getSimpleName())) {
            mEntryPosition = intent.getIntExtra(Constants.EXTRA_SELECTED_ENTRY_POSITION, 0);
        } else if (mIntentSender.equals(SearchActivity.class.getSimpleName())) {
            mEntryId = intent.getLongExtra(Constants.EXTRA_ENTRY_ID, Constants.ENTRY_ID_EMPTY);
        }
        // TODO params are used only once, you don't need to create fields for them
        mDictionaryMenuId = intent.getIntExtra(
                Constants.EXTRA_SELECTED_DICTIONARY_ID,
                Constants.DEFAULT_SELECTED_DICTIONARY_ID);

        //TODO we get saved order here and in main activity, it's a good point to move this lines to one place
        final SharedPreferences shp = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        mSortOrder = SortOrder.valueOf(shp.getString(
                Constants.APP_PREFERENCES_SORT_ORDER,
                SortOrder.NEWEST.toString()));
        viewPager = (ViewPager) findViewById(R.id.pager);
        getSupportLoaderManager().initLoader(ENTRY_LOADER, null, this);
    }

    //TODO isn't it default behaviour?
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

        return new CursorLoader(
                this,
                UriBuilder.getTableUri(Entry.class),
                ENTRY_PROJECTION,
                Entry.DICTIONARY_MENU_ID + "=?",
                new String[]{String.valueOf(mDictionaryMenuId)},
                mSortOrder.getEntrySortOrderQueryParam());
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {

        mEntryPagerAdapter = new EntryPagerAdapter(getSupportFragmentManager(), cursor);
        viewPager.setAdapter(mEntryPagerAdapter);

        //TODO check if mEntryId exists and is valid, try to use as less params, as possible
        if (mIntentSender.equals(SearchActivity.class.getSimpleName())) {
            while (cursor.moveToNext()) {
                if (cursor.getLong(cursor.getColumnIndex(Entry.ID)) == mEntryId) {
                    mEntryPosition = cursor.getPosition();
                    break;
                }
            }
        }
        viewPager.setCurrentItem(mEntryPosition);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mEntryPagerAdapter.getCursor().close();
    }
}
