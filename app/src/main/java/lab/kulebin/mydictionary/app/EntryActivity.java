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
    private static final int DEFAULT_ENTRY_POSITION = 0;
    private static final String[] ENTRY_PROJECTION = {
            Entry.ID,
            Entry.VALUE,
            Entry.TRANSLATION,
            Entry.IMAGE_URL,
            Entry.USAGE_CONTEXT
    };

    private ViewPager viewPager;
    private EntryPagerAdapter mEntryPagerAdapter;
    private SortOrder mSortOrder;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getIntent().getCharSequenceExtra(Constants.EXTRA_SELECTED_DICTIONARY_NAME));
        }

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
        final int dictionaryMenuId = getIntent().getIntExtra(
                Constants.EXTRA_SELECTED_DICTIONARY_ID,
                Constants.DEFAULT_SELECTED_DICTIONARY_ID);

        return new CursorLoader(
                this,
                UriBuilder.getTableUri(Entry.class),
                ENTRY_PROJECTION,
                Entry.DICTIONARY_MENU_ID + "=?",
                new String[]{String.valueOf(dictionaryMenuId)},
                mSortOrder.getEntrySortOrderQueryParam());
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {

        mEntryPagerAdapter = new EntryPagerAdapter(getSupportFragmentManager(), cursor);
        viewPager.setAdapter(mEntryPagerAdapter);

        int entryPosition = DEFAULT_ENTRY_POSITION;
        final Intent intent = getIntent();

        if (intent.hasExtra(Constants.EXTRA_SELECTED_ENTRY_POSITION)) {
            entryPosition = intent.getIntExtra(Constants.EXTRA_SELECTED_ENTRY_POSITION, DEFAULT_ENTRY_POSITION);
        } else if (intent.hasExtra(Constants.EXTRA_ENTRY_ID)) {

            final long entryId = intent.getLongExtra(Constants.EXTRA_ENTRY_ID, Constants.ENTRY_ID_EMPTY);
            while (cursor.moveToNext()) {
                if (cursor.getLong(cursor.getColumnIndex(Entry.ID)) == entryId) {
                    entryPosition = cursor.getPosition();
                    break;
                }
            }
        }

        viewPager.setCurrentItem(entryPosition);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mEntryPagerAdapter.getCursor().close();
    }
}
