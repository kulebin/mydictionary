package lab.kulebin.mydictionary.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.graphics.drawable.VectorDrawableCompat;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;
import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.adapter.EntryCursorAdapter;
import lab.kulebin.mydictionary.db.SortOrder;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.http.IHttpClient;
import lab.kulebin.mydictionary.json.JsonHelper;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.service.FetchDataService;
import lab.kulebin.mydictionary.thread.ITask;
import lab.kulebin.mydictionary.thread.OnResultCallback;
import lab.kulebin.mydictionary.thread.ProgressCallback;
import lab.kulebin.mydictionary.thread.ThreadManager;
import lab.kulebin.mydictionary.utils.UriBuilder;

import static lab.kulebin.mydictionary.Constants.ANONYMOUS;

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
    private EntryCursorAdapter mEntryCursorAdapter;
    private NavigationView mNavigationView;
    private Cursor mDictionaryMenuCursor;
    private int mSelectedDictionaryId;
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private String mUserPhotoUrl;
    private String mUsername;
    private String mUserEmail;
    private String mToken;
    private TextView mTextViewNoEntry;
    private SortOrder mSortOrder;

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu pMenu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, pMenu);
        switch (mSortOrder) {
            case NEWEST:
                pMenu.findItem(R.id.sort_by_newest).setChecked(true);
                break;
            case OLDEST:
                pMenu.findItem(R.id.sort_by_oldest).setChecked(true);
                break;
            case A_Z:
                pMenu.findItem(R.id.sort_by_a_z).setChecked(true);
                break;
            case Z_A:
                pMenu.findItem(R.id.sort_by_z_a).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem pItem) {
        switch (pItem.getItemId()) {
            case R.id.main_menu_action_search:
                final Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.sort_by_newest:
                if (mSortOrder != SortOrder.NEWEST) {
                    mSortOrder = SortOrder.NEWEST;
                    pItem.setChecked(true);
                    getSupportLoaderManager().restartLoader(ENTRY_LOADER, null, this);
                }
                return true;
            case R.id.sort_by_oldest:
                if (mSortOrder != SortOrder.OLDEST) {
                    mSortOrder = SortOrder.OLDEST;
                    pItem.setChecked(true);
                    getSupportLoaderManager().restartLoader(ENTRY_LOADER, null, this);
                }
                return true;
            case R.id.sort_by_a_z:
                if (mSortOrder != SortOrder.A_Z) {
                    mSortOrder = SortOrder.A_Z;
                    pItem.setChecked(true);
                    getSupportLoaderManager().restartLoader(ENTRY_LOADER, null, this);
                }
                return true;
            case R.id.sort_by_z_a:
                if (mSortOrder != SortOrder.Z_A) {
                    mSortOrder = SortOrder.Z_A;
                    pItem.setChecked(true);
                    getSupportLoaderManager().restartLoader(ENTRY_LOADER, null, this);
                }
                return true;
            case R.id.action_sign_out:
                signOut();
                return true;
            case R.id.action_delete_dictionary:
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(getString(R.string.alert_title_confirm_entry_deletion));
                alertDialogBuilder
                        .setMessage(getString(R.string.alert_body_confirm_dictionary_deletion))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.alert_positive_button), new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                deleteDictionaryTask();
                            }
                        })
                        .setNegativeButton(getString(R.string.alert_negative_button), new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(pItem);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem pItem) {
        switch (pItem.getItemId()) {
            case R.id.action_add_dictionary:
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

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult pConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + pConnectionResult);
        Toast.makeText(this, "Connection error.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public Loader<Cursor> onCreateLoader(final int pId, final Bundle pArgs) {

        mProgressBar.setVisibility(View.VISIBLE);
        final String dictionarySortOrder = Dictionary.CREATION_DATE + Constants.SQL_SORT_QUERY_DESC;

        switch (pId) {
            case ENTRY_LOADER:
                return new CursorLoader(
                        this,
                        UriBuilder.getTableUri(Entry.class),
                        ENTRY_PROJECTION,
                        Entry.DICTIONARY_ID + "=?",
                        new String[]{String.valueOf(mSelectedDictionaryId)},
                        mSortOrder.getEntrySortOrderQueryParam());
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
        mProgressBar.setVisibility(View.GONE);
        switch (pLoader.getId()) {
            case ENTRY_LOADER:
                if (pCursor.getCount() > 0 && mTextViewNoEntry.isShown()) {
                    mTextViewNoEntry.setVisibility(View.GONE);
                } else if (pCursor.getCount() == 0 && !mTextViewNoEntry.isShown()) {
                    mTextViewNoEntry.setVisibility(View.VISIBLE);
                }
                mEntryCursorAdapter.swapCursor(pCursor);
                break;
            case DICTIONARY_LOADER:
                if (pCursor != null && pCursor.getCount() > 0) {
                    final Menu menu = mNavigationView.getMenu();
                    menu.removeGroup(R.id.dictionary_group_navigation_menu);
                    boolean isMenuItemSelected = false;
                    final int dictionaryIdColumnIndex = pCursor.getColumnIndex(Dictionary.ID);
                    final int dictionaryNameColumnIndex = pCursor.getColumnIndex(Dictionary.NAME);
                    while (pCursor.moveToNext()) {
                        final int dictionaryId = pCursor.getInt(dictionaryIdColumnIndex);
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
                        final int firstMenuItemId = pCursor.getInt(dictionaryIdColumnIndex);
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
    public void onLoaderReset(final Loader<Cursor> pLoader) {
        if (pLoader.getId() == ENTRY_LOADER) {
            mEntryCursorAdapter.swapCursor(null);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mUsername = Constants.ANONYMOUS;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        signIn();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                if (mSelectedDictionaryId == Constants.DEFAULT_SELECTED_DICTIONARY_ID) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder
                            .setMessage(getString(R.string.alert_body_no_dictionary_added))
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.alert_positive_button), new DialogInterface.OnClickListener() {

                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.dismiss();
                                }
                            });

                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    final Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    intent.putExtra(Constants.EXTRA_EDIT_ACTIVITY_MODE, EditActivity.EditActivityMode.CREATE)
                            .putExtra(Constants.EXTRA_SELECTED_DICTIONARY_ID, mSelectedDictionaryId);
                    startActivity(intent);
                }
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTextViewNoEntry = (TextView) findViewById(R.id.text_no_entry_in_dictionary);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        final SharedPreferences shp = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        mSelectedDictionaryId = shp.getInt(
                Constants.APP_PREFERENCES_SELECTED_DICTIONARY_ID,
                Constants.DEFAULT_SELECTED_DICTIONARY_ID);
        mSortOrder = SortOrder.valueOf(shp.getString(Constants.APP_PREFERENCES_SORT_ORDER, SortOrder.NEWEST.toString()));

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        final View navigationHeaderLayout = mNavigationView.getHeaderView(0);
        final TextView userNameTextView = (TextView) navigationHeaderLayout.findViewById(R.id.nav_header_user_name);
        userNameTextView.setText(mUsername);
        final TextView userEmailTextView = (TextView) navigationHeaderLayout.findViewById(R.id.nav_header_email);
        userEmailTextView.setText(mUserEmail);
        final CircleImageView userPhotoImageView = (CircleImageView) navigationHeaderLayout.findViewById(R.id.user_imageView);
        if (mUserPhotoUrl != null) {
            Glide.with(this)
                    .load(mUserPhotoUrl)
                    .into(userPhotoImageView);
        } else {
            final Drawable userPhotoDrawable = VectorDrawableCompat.create(this.getResources(), R.drawable.ic_account_circle_76dp, null);
            userPhotoImageView.setImageDrawable(userPhotoDrawable);
        }
        mNavigationView.setNavigationItemSelectedListener(this);

        getSupportLoaderManager().initLoader(DICTIONARY_LOADER, null, this);

        final ListView listView = (ListView) findViewById(R.id.listview_entry);
        mEntryCursorAdapter = new EntryCursorAdapter(this, null, 0);
        listView.setAdapter(mEntryCursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final Intent intent = new Intent(MainActivity.this, EntryActivity.class)
                        .putExtra(Constants.EXTRA_INTENT_SENDER, MainActivity.class.getSimpleName())
                        .putExtra(Constants.EXTRA_SELECTED_ENTRY_POSITION, position)
                        .putExtra(Constants.EXTRA_SELECTED_DICTIONARY_ID, mSelectedDictionaryId)
                        .putExtra(Constants.EXTRA_SELECTED_DICTIONARY_NAME, mToolbar.getTitle());
                startActivity(intent);
            }
        });
        getSupportLoaderManager().initLoader(ENTRY_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences shp = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        mToken = shp.getString(Constants.APP_PREFERENCES_USER_TOKEN, null);
        if (mToken != null) {
            final Intent serviceIntent = new Intent(this, FetchDataService.class);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        final SharedPreferences appPreferences = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = appPreferences.edit();
        editor.putInt(Constants.APP_PREFERENCES_SELECTED_DICTIONARY_ID, mSelectedDictionaryId);
        editor.putString(Constants.APP_PREFERENCES_SORT_ORDER, mSortOrder.toString());
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDictionaryMenuCursor != null) {
            mDictionaryMenuCursor.close();
        }
    }

    private void deleteDictionaryTask() {
        new ThreadManager().execute(
                new ITask<Void, Void, Void>() {

                    @Override
                    public Void perform(final Void pVoid, final ProgressCallback<Void> progressCallback) throws Exception {

                        final Cursor dictionaryCursor = getContentResolver().query(
                                UriBuilder.getTableUri(Dictionary.class),
                                new String[]{Dictionary.CREATION_DATE},
                                Dictionary.ID + "=?",
                                new String[]{String.valueOf(mSelectedDictionaryId)},
                                null);
                        final long dictionaryCreationDate;
                        if (dictionaryCursor != null) {
                            dictionaryCursor.moveToFirst();
                            dictionaryCreationDate = dictionaryCursor.getLong(dictionaryCursor.getColumnIndex(Dictionary.CREATION_DATE));
                            dictionaryCursor.close();
                        } else {
                            throw new Exception("Dictionary cursor is null");
                        }

                        final Uri dictionaryUri;
                        if (mToken != null) {
                            dictionaryUri = Uri.parse(Api.getBaseUrl()).buildUpon()
                                    .appendPath(Api.DICTIONARIES)
                                    .appendPath(dictionaryCreationDate + Api.JSON_FORMAT)
                                    .appendQueryParameter(Api.PARAM_AUTH, mToken)
                                    .build();
                        } else {
                            signOut();
                            throw new Exception("Token is null");
                        }

                        final HttpClient httpClient;
                        Cursor entryIdsCursor = null;
                        try {
                            httpClient = new HttpClient();
                            if (httpClient.delete(dictionaryUri.toString()).equals(HttpClient.DELETE_RESPONSE_OK)) {
                                entryIdsCursor = getContentResolver().query(
                                        UriBuilder.getTableUri(Entry.class),
                                        new String[]{Entry.ID},
                                        Entry.DICTIONARY_ID + "=?",
                                        new String[]{String.valueOf(mSelectedDictionaryId)},
                                        null);
                                if (entryIdsCursor != null) {
                                    while (entryIdsCursor.moveToNext()) {
                                        final Uri entryUri = Uri.parse(Api.getBaseUrl()).buildUpon()
                                                .appendPath(Api.ENTRIES)
                                                .appendPath(entryIdsCursor.getLong(entryIdsCursor.getColumnIndex(Entry.ID))
                                                        + Api.JSON_FORMAT)
                                                .appendQueryParameter(Api.PARAM_AUTH, mToken)
                                                .build();
                                        httpClient.delete(entryUri.toString());
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
                        } catch (final Exception e) {
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

    private void startCreateDictionaryDialog() {
        final AlertDialog.Builder addDictionaryDialogBuilder = new AlertDialog.Builder(this);
        addDictionaryDialogBuilder.setTitle(R.string.dialog_title_add_dictionary);

        final LayoutInflater inflater = LayoutInflater.from(this);
        final View dialogView = inflater.inflate(R.layout.edit_text_add_dictionary_name, null);
        final EditText inputDictionaryName = (EditText) dialogView.findViewById(R.id.edit_text_add_dictionary);

        addDictionaryDialogBuilder.setView(dialogView)
                .setPositiveButton(R.string.alert_positive_button, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        createAndStoreDictionary(inputDictionaryName.getText().toString());
                    }
                })
                .setNegativeButton(R.string.alert_negative_button, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog addDictionaryDialog = addDictionaryDialogBuilder.show();
        final Button positiveButton = addDictionaryDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        final TextWatcher textWatcher = new TextWatcher() {

            boolean isEnabled;

            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
            }

            @Override
            public void afterTextChanged(final Editable editable) {
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

                        final Cursor cursor = getContentResolver().query(
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

                        final long creationDate = System.currentTimeMillis();
                        final Dictionary dictionary = new Dictionary(
                                dictionaryId,
                                pDictionaryName,
                                creationDate);

                        final ContentValues values = new ContentValues();
                        values.put(Dictionary.ID, dictionaryId);
                        values.put(Dictionary.NAME, pDictionaryName);
                        values.put(Dictionary.CREATION_DATE, creationDate);

                        final Uri uri = Uri.parse(Api.getBaseUrl()).buildUpon()
                                .appendPath(Api.DICTIONARIES)
                                .appendPath(creationDate + Api.JSON_FORMAT)
                                .appendQueryParameter(Api.PARAM_AUTH, mToken)
                                .build();
                        final IHttpClient httpClient = new HttpClient();
                        try {
                            httpClient.put(uri.toString(), null, JsonHelper.buildDictionaryJsonObject(dictionary).toString());
                            getContentResolver().insert(
                                    UriBuilder.getTableUri(Dictionary.class),
                                    values
                            );
                            mSelectedDictionaryId = dictionaryId;
                        } catch (final Exception e) {
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
                    public void onSuccess(final Void pVoid) {
                        getSupportLoaderManager().restartLoader(ENTRY_LOADER, null, MainActivity.this);
                        final Toast toast = Toast.makeText(getApplicationContext(),
                                R.string.RESULT_SUCCESS_DICTIONARY_STORED,
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    @Override
                    public void onError(final Exception e) {
                        final Toast toast = Toast.makeText(getApplicationContext(),
                                "Error! Dictionary has not been created!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {

                    }
                }
        );
    }

    private void signIn() {
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            mUserPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            mUserEmail = mFirebaseUser.getEmail();
        }
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        mUsername = ANONYMOUS;
        startActivity(new Intent(this, SignInActivity.class));
    }
}