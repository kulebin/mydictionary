package lab.kulebin.mydictionary.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.json.JsonHelper;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.service.FetchDataService;
import lab.kulebin.mydictionary.thread.ITask;
import lab.kulebin.mydictionary.thread.OnResultCallback;
import lab.kulebin.mydictionary.thread.ProgressCallback;
import lab.kulebin.mydictionary.thread.ThreadManager;
import lab.kulebin.mydictionary.ui.EntryCursorAdapter;
import lab.kulebin.mydictionary.utils.UriBuilder;

import static lab.kulebin.mydictionary.app.Constants.ANONYMOUS;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DICTIONARY_LOADER = 0;
    private static final int ENTRY_LOADER = 1;
    private static final String[] ENTRY_PROJECTION = {
            Entry.ID,
            Entry.VALUE,
            Entry.TRANSLATION,
            Entry.IMAGE_URL
    };
    DrawerLayout mDrawerLayout;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private EntryCursorAdapter mEntryCursorAdapter;
    private NavigationView mNavigationView;
    private Cursor mDictionaryMenuCursor;
    private int mSelectedDictionaryId;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        Intent serviceIntent = new Intent(this, FetchDataService.class);
        startService(serviceIntent);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra(Constants.EXTRA_EDIT_ACTIVITY_MODE, EditActivity.EditActivityMode.CREATE)
                        .putExtra(Constants.EXTRA_SELECTED_DICTIONARY_ID, mSelectedDictionaryId);
                startActivity(intent);
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        SharedPreferences shp = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        mSelectedDictionaryId = shp.getInt(
                Constants.APP_PREFERENCES_SELECTED_DICTIONARY_ID,
                Constants.DEFAULT_SELECTED_DICTIONARY_ID);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mUsername = Constants.ANONYMOUS;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        getSupportLoaderManager().initLoader(DICTIONARY_LOADER, null, this);

        final ListView listView = (ListView) findViewById(R.id.listview_entry);
        mEntryCursorAdapter = new EntryCursorAdapter(this, null, 0);
        listView.setAdapter(mEntryCursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Intent intent = new Intent(MainActivity.this, EntryActivity.class)
                        .putExtra(Constants.EXTRA_ENTRY_POSITION, position)
                        .putExtra(Constants.EXTRA_SELECTED_DICTIONARY_ID, mSelectedDictionaryId)
                        .putExtra(Constants.EXTRA_SELECTED_DICTIONARY_NAME, mToolbar.getTitle());
                startActivity(intent);
            }
        });
        getSupportLoaderManager().initLoader(ENTRY_LOADER, null, this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        getMenuInflater().inflate(R.menu.main, pMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem pItem) {
        switch (pItem.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.action_delete_dictionary:
                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle(getString(R.string.alert_title_confirm_entry_deletion));
                alertDialogBuilder
                        .setMessage(getString(R.string.alert_body_confirm_dictionary_deletion))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.alert_positive_button), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteDictionaryTask();
                            }
                        })
                        .setNegativeButton(getString(R.string.alert_negative_button), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(pItem);
        }
    }

    private void deleteDictionaryTask() {
        new ThreadManager().execute(
                new ITask<Void, Void, Void>() {
                    @Override
                    public Void perform(final Void pVoid, final ProgressCallback<Void> progressCallback) throws Exception {

                        Cursor dictionaryCursor = getContentResolver().query(
                                UriBuilder.getTableUri(Dictionary.class),
                                new String[]{Dictionary.CREATION_DATE},
                                Dictionary.ID + "=?",
                                new String[]{String.valueOf(mSelectedDictionaryId)},
                                null);
                        long dictionaryCreationDate;
                        if (dictionaryCursor != null) {
                            dictionaryCursor.moveToFirst();
                            dictionaryCreationDate = dictionaryCursor.getLong(dictionaryCursor.getColumnIndex(Dictionary.CREATION_DATE));
                            dictionaryCursor.close();
                        } else {
                            throw new Exception("Dictionary cursor is null");
                        }

                        Uri dictionaryUri = Uri.parse(Api.BASE_URL).buildUpon()
                                .appendPath(Api.DICTIONARIES)
                                .appendPath(String.valueOf(dictionaryCreationDate))
                                .build();
                        String dictionaryUrl = dictionaryUri.toString() + Api.JSON_FORMAT;


                        HttpClient httpClient;
                        Cursor entryIdsCursor = null;
                        try {
                            httpClient = new HttpClient();
                            if (httpClient.delete(dictionaryUrl).equals(HttpClient.DELETE_RESPONSE_OK)) {
                                entryIdsCursor = getContentResolver().query(
                                        UriBuilder.getTableUri(Entry.class),
                                        new String[]{Entry.ID},
                                        Entry.DICTIONARY_ID + "=?",
                                        new String[]{String.valueOf(mSelectedDictionaryId)},
                                        null);
                                if (entryIdsCursor != null) {
                                    while (entryIdsCursor.moveToNext()) {
                                        Uri entryUri = Uri.parse(Api.BASE_URL).buildUpon()
                                                .appendPath(Api.ENTRIES)
                                                .appendPath(String.valueOf(entryIdsCursor.getLong(entryIdsCursor.getColumnIndex(Entry.ID))))
                                                .build();
                                        String entryUrl = entryUri.toString() + Api.JSON_FORMAT;
                                        httpClient.delete(entryUrl);
                                    }
                                    getContentResolver().delete(
                                            UriBuilder.getTableUri(Dictionary.class, String.valueOf(mSelectedDictionaryId)),
                                            null,
                                            null
                                    );
                                }
                            } else {
                                Toast.makeText(MainActivity.this,
                                        R.string.ERROR_CONNECTION_DELETE, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.v(TAG, getString(R.string.ERROR_DELETE_REQUEST));
                        } finally {
                            if (entryIdsCursor != null) {
                                entryIdsCursor.close();
                            }
                        }
                        return null;
                    }
                },
                null,
                new OnResultCallback<Void, Void>() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(final Void pVoid) {

                    }

                    @Override
                    public void onError(final Exception e) {
                        Toast.makeText(MainActivity.this,
                                R.string.ERROR_DELETE_DICTIONARY, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {

                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem pItem) {
        switch (pItem.getItemId()) {
            case R.id.navigation_menu_add_dictionary:
                startCreateDictionaryDialog();
                break;
            default:
                mNavigationView.getMenu().findItem(mSelectedDictionaryId).setChecked(false);
                pItem.setChecked(true);
                mToolbar.setTitle(pItem.getTitle());
                mSelectedDictionaryId = pItem.getItemId();
                getSupportLoaderManager().restartLoader(ENTRY_LOADER, null, this);
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startCreateDictionaryDialog() {
        AlertDialog.Builder addDictionaryDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        addDictionaryDialogBuilder.setTitle(R.string.dialog_title_add_dictionary);

        final EditText inputDictionaryName = new EditText(this);
        inputDictionaryName.setInputType(InputType.TYPE_CLASS_TEXT);
        inputDictionaryName.setHint(R.string.dialog_hint_add_dictionary);
        addDictionaryDialogBuilder.setView(inputDictionaryName)
                .setPositiveButton(R.string.alert_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createAndStoreDictionary(inputDictionaryName.getText().toString());
                    }
                })
                .setNegativeButton(R.string.alert_negative_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog addDictionaryDialog = addDictionaryDialogBuilder.show();
        final Button positiveButton = addDictionaryDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        TextWatcher textWatcher = new TextWatcher() {
            boolean isEnabled = false;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isEnabled) {
                    if (!TextUtils.isEmpty(inputDictionaryName.getText().toString())) {
                        positiveButton.setEnabled(true);
                        isEnabled = true;
                    }
                } else if (TextUtils.isEmpty(inputDictionaryName.getText().toString())) {
                    positiveButton.setEnabled(false);
                    isEnabled = false;
                }
            }
        };
        inputDictionaryName.addTextChangedListener(textWatcher);
    }

    private void createAndStoreDictionary(final String pDictionaryName) {
        new ThreadManager().execute(
                new ITask<String, Void, Void>() {
                    @Override
                    public Void perform(final String pDictionaryName, final ProgressCallback<Void> progressCallback) throws Exception {

                        Cursor cursor = getContentResolver().query(
                                UriBuilder.getTableUri(Dictionary.class),
                                new String[]{Dictionary.ID},
                                null,
                                null,
                                Dictionary.ID + " ASC");

                        int dictionaryId = 0;
                        if (cursor != null && cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                if (dictionaryId != cursor.getInt(cursor.getColumnIndex(Dictionary.ID))) {
                                    break;
                                }
                                dictionaryId++;
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                        }

                        long creationDate = System.currentTimeMillis();
                        Dictionary dictionary = new Dictionary(
                                dictionaryId,
                                pDictionaryName,
                                creationDate);

                        ContentValues values = new ContentValues();
                        values.put(Dictionary.ID, dictionaryId);
                        values.put(Dictionary.NAME, pDictionaryName);
                        values.put(Dictionary.CREATION_DATE, creationDate);

                        Uri uri = Uri.parse(Api.BASE_URL).buildUpon()
                                .appendPath(Api.DICTIONARIES)
                                .appendPath(String.valueOf(creationDate))
                                .build();
                        String url = uri.toString() + Api.JSON_FORMAT;
                        HttpClient httpClient = new HttpClient();
                        try {
                            httpClient.put(url, null, JsonHelper.buildDictionaryJsonObject(dictionary).toString());
                            getContentResolver().insert(
                                    UriBuilder.getTableUri(Dictionary.class),
                                    values
                            );
                            mSelectedDictionaryId = dictionaryId;
                        } catch (Exception e) {
                            Log.v(TAG, getString(R.string.ERROR_CREATE_DICTIONARY));
                        }
                        return null;
                    }
                },
                pDictionaryName,
                new OnResultCallback<Void, Void>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(Void pVoid) {
                        getSupportLoaderManager().restartLoader(ENTRY_LOADER, null, MainActivity.this);
                        Toast toast = Toast.makeText(getApplicationContext(),
                                R.string.RESULT_SUCCESS_ENTRY_STORED,
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    @Override
                    public void onError(final Exception e) {
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {

                    }
                }
        );
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult pConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + pConnectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();

    }

    //TODO Should be used in the future when main activity is done
    private void signIn() {
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int pId, final Bundle pArgs) {

        String entrySortOrder = Entry.CREATION_DATE + " DESC";
        String dictionarySortOrder = Dictionary.CREATION_DATE + " DESC";

        switch (pId) {
            case ENTRY_LOADER:
                return new CursorLoader(
                        this,
                        UriBuilder.getTableUri(Entry.class),
                        ENTRY_PROJECTION,
                        Entry.DICTIONARY_ID + "=?",
                        new String[]{String.valueOf(mSelectedDictionaryId)},
                        entrySortOrder);
            case DICTIONARY_LOADER:
                return new CursorLoader(
                        this,
                        UriBuilder.getTableUri(Dictionary.class),
                        null,
                        null,
                        null,
                        dictionarySortOrder);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> pLoader, final Cursor pCursor) {
        switch (pLoader.getId()) {
            case ENTRY_LOADER:
                mEntryCursorAdapter.swapCursor(pCursor);
                break;
            case DICTIONARY_LOADER:
                if (pCursor != null && pCursor.getCount() > 0) {
                    Menu menu = mNavigationView.getMenu();
                    menu.removeGroup(R.id.dictionary_group_navigation_menu);
                    boolean isMenuItemSelected = false;
                    int dictionaryIdColumnIndex = pCursor.getColumnIndex(Dictionary.ID);
                    int dictionaryNameColumnIndex = pCursor.getColumnIndex(Dictionary.NAME);
                    while (pCursor.moveToNext()) {
                        int dictionaryId = pCursor.getInt(dictionaryIdColumnIndex);
                        menu.add(
                                R.id.dictionary_group_navigation_menu,
                                dictionaryId,
                                Menu.NONE,
                                pCursor.getString(dictionaryNameColumnIndex));
                        if (dictionaryId == mSelectedDictionaryId) {
                            menu.findItem(dictionaryId).setChecked(true);
                            mToolbar.setTitle(pCursor.getString(dictionaryNameColumnIndex));
                            isMenuItemSelected = true;
                        }
                    }
                    if (!isMenuItemSelected) {
                        pCursor.moveToFirst();
                        int firstMenuItemId = pCursor.getInt(dictionaryIdColumnIndex);
                        menu.findItem(firstMenuItemId).setChecked(true);
                        mToolbar.setTitle(pCursor.getString(dictionaryNameColumnIndex));
                        mSelectedDictionaryId = firstMenuItemId;
                        getSupportLoaderManager().restartLoader(ENTRY_LOADER, null, this);
                    }
                }
                mDictionaryMenuCursor = pCursor;
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences appPreferences = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putInt(Constants.APP_PREFERENCES_SELECTED_DICTIONARY_ID, mSelectedDictionaryId);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDictionaryMenuCursor != null) {
            mDictionaryMenuCursor.close();
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> pLoader) {
        if (pLoader.getId() == ENTRY_LOADER) {
            mEntryCursorAdapter.swapCursor(null);
        }
    }
}