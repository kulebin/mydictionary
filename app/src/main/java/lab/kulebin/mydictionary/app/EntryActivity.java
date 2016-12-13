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
import android.view.MenuItem;

import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.ui.EntryPagerAdapter;
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
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mPosition = intent.getIntExtra(Constants.EXTRA_ENTRY_POSITION, -1);
        viewPager = (ViewPager) findViewById(R.id.pager);
        getSupportLoaderManager().initLoader(ENTRY_LOADER, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                EntryActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

        String sortOrder = Entry.CREATION_DATE + " DESC";

        return new CursorLoader(
                this,
                UriBuilder.getTableUri(Entry.class),
                ENTRY_PROJECTION,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        mEntryPagerAdapter = new EntryPagerAdapter(
                getSupportFragmentManager(), cursor);
        viewPager.setAdapter(mEntryPagerAdapter);
        viewPager.setCurrentItem(mPosition);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mEntryPagerAdapter.getCursor().close();
    }
}
