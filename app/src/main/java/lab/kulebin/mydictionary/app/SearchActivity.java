package lab.kulebin.mydictionary.app;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.adapter.SearchCursorAdapter;
import lab.kulebin.mydictionary.db.DbHelper;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.utils.UriBuilder;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SEARCH_RESULT_LOADER = 0;
    private static final String SEARCH_QUERY_PARAM = "SEARCH_QUERY_PARAM";
    private SearchCursorAdapter mSearchResultCursorAdapter;
    private ProgressBar mProgressBar;
    private TextView mNoSearchResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ListView listView = (ListView) findViewById(R.id.listview_search_result);
        mProgressBar = (ProgressBar) findViewById(R.id.search_progressBar);
        mNoSearchResultView = (TextView) findViewById(R.id.search_no_result);
        mSearchResultCursorAdapter = new SearchCursorAdapter(this, null, 0);
        listView.setAdapter(mSearchResultCursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                int selectedDictionaryId = Constants.DEFAULT_SELECTED_DICTIONARY_ID;
                String selectedDictionaryName = null;
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor.moveToPosition(position)) {
                    selectedDictionaryId = cursor.getInt(cursor.getColumnIndex(Entry.DICTIONARY_ID));
                    selectedDictionaryName = cursor.getString(cursor.getColumnIndex(Dictionary.NAME));
                }

                Intent intent = new Intent(SearchActivity.this, EntryActivity.class)
                        .putExtra(Constants.EXTRA_ENTRY_POSITION, position)
                        .putExtra(Constants.EXTRA_SELECTED_DICTIONARY_ID, selectedDictionaryId)
                        .putExtra(Constants.EXTRA_SELECTED_DICTIONARY_NAME, selectedDictionaryName);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_search_menu, menu);


        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search_menu_action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(final String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                Bundle bundle = new Bundle();
                bundle.putString(SEARCH_QUERY_PARAM, newText);
                if (!newText.equals("")) {
                    getSupportLoaderManager().restartLoader(SEARCH_RESULT_LOADER, bundle, SearchActivity.this);
                } else {
                    mSearchResultCursorAdapter.swapCursor(null);
                    mNoSearchResultView.setVisibility(View.GONE);
                }
                return true;
            }
        });
        MenuItem searchMenuItem = menu.findItem(R.id.search_menu_action_search);
        MenuItemCompat.expandActionView(searchMenuItem);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                SearchActivity.this.finish();
                return true;
            }
        });
        return true;
    }


    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        String entryTableName = DbHelper.getTableName(Entry.class);
        String dictionaryTableName = DbHelper.getTableName(Dictionary.class);

        String[] projection = {
                entryTableName + "." + Entry.ID,
                entryTableName + "." + Entry.VALUE,
                entryTableName + "." + Entry.TRANSLATION,
                entryTableName + "." + Entry.IMAGE_URL,
                entryTableName + "." + Entry.DICTIONARY_ID,
                dictionaryTableName + "." + Dictionary.NAME
        };

        String entrySortOrder = entryTableName +
                "." + Entry.DICTIONARY_ID + " DESC," +
                entryTableName +
                "." + Entry.CREATION_DATE + " DESC";

        String selection = entryTableName + "." + Entry.VALUE + " LIKE ? OR " +
                entryTableName + "." + Entry.TRANSLATION + " LIKE ?";

        String queryParam = "%" + args.getString(SEARCH_QUERY_PARAM) + "%";
        return new CursorLoader(
                this,
                UriBuilder.getTableUri(Entry.class, Dictionary.class),
                projection,
                selection,
                new String[]{queryParam, queryParam},
                entrySortOrder
        );
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor pCursor) {
        mProgressBar.setVisibility(View.GONE);
        if (pCursor.getCount() == 0 && !mNoSearchResultView.isShown()) {
            mNoSearchResultView.setVisibility(View.VISIBLE);
        } else if (pCursor.getCount() > 0 && mNoSearchResultView.isShown()) {
            mNoSearchResultView.setVisibility(View.GONE);
        }
        mSearchResultCursorAdapter.swapCursor(pCursor);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mSearchResultCursorAdapter.swapCursor(null);
    }
}
