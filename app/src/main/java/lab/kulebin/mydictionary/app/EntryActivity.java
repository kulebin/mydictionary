package lab.kulebin.mydictionary.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getIntent().getCharSequenceExtra(Constants.EXTRA_SELECTED_DICTIONARY_NAME));
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        getSupportLoaderManager().initLoader(ENTRY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Intent intent = getIntent();
        final int dictionaryMenuId = intent.getIntExtra(
                Constants.EXTRA_SELECTED_DICTIONARY_ID,
                Constants.DEFAULT_SELECTED_DICTIONARY_ID);
        final SortOrder sortOrder = SortOrder.valueOf(intent.getStringExtra(
                Constants.EXTRA_SELECTED_SORT_ORDER
        ));

        return new CursorLoader(
                this,
                UriBuilder.getTableUri(Entry.class),
                ENTRY_PROJECTION,
                Entry.DICTIONARY_MENU_ID + "=?",
                new String[]{String.valueOf(dictionaryMenuId)},
                sortOrder.getEntrySortOrderQueryParam());
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
