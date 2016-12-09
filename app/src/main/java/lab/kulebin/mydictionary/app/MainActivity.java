package lab.kulebin.mydictionary.app;

import android.content.ContentValues;
import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import java.util.List;
import java.util.Vector;

import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.model.JsonHelper;
import lab.kulebin.mydictionary.thread.ITask;
import lab.kulebin.mydictionary.thread.OnResultCallback;
import lab.kulebin.mydictionary.thread.ProgressCallback;
import lab.kulebin.mydictionary.thread.ThreadManager;
import lab.kulebin.mydictionary.ui.EntryCursorAdapter;
import lab.kulebin.mydictionary.utils.Converter;
import lab.kulebin.mydictionary.utils.UriBuilder;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {


    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String[] ENTRY_PROJECTION = {
            Entry.ID,
            Entry.VALUE,
            Entry.TRANSLATION,
            Entry.IMAGE_URL
    };

    private static final int ENTRY_LOADER = 0;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private ProgressBar mProgressBar;
    private EntryCursorAdapter mEntryCursorAdapter;
    private ImageView mUserImageView;
    private ThreadManager mThreadManager;
    private ListView mListView;
    //private RecyclerView mEntryRecyclerView;
    //private LinearLayoutManager mLinearLayoutManager;
    //private EntryRecyclerAdapter mEntryRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO by clicking on fab tapped Entry Edit activity should be opened
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mUserImageView = (ImageView) findViewById(R.id.user_imageView);

        mUsername = ANONYMOUS;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // temporary commented, sighIn() method is supposed to be used in the future
        //signIn();


        //noinspection WrongConstant
        //mThreadManager = (ThreadManager) this.getSystemService(ThreadManager.APP_SERVICE_KEY);
        mThreadManager = new ThreadManager();
        fetchEntryTask();
        fetchDictionaryTask();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


        mListView = (ListView) findViewById(R.id.listview_entry);
        mEntryCursorAdapter = new EntryCursorAdapter(this, null, 0);
        mListView.setAdapter(mEntryCursorAdapter);
        getSupportLoaderManager().initLoader(ENTRY_LOADER, null, this);
        //mEntryRecyclerView = (RecyclerView) findViewById(R.id.entryRecyclerView);
        //mLinearLayoutManager = new LinearLayoutManager(this);
        //mEntryRecyclerView.setLayoutManager(mLinearLayoutManager);
        //mEntryRecyclerView.setAdapter(mEntryCursorAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //TODO navigation by dictionary should be implemented

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult pConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + pConnectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();

    }

    private void signIn() {
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
        }
    }


    private void fetchEntryTask() {
        mThreadManager.execute(
                new ITask<Void, Void, List<Entry>>() {
                    @Override
                    public List<Entry> perform(final Void pVoid, final ProgressCallback<Void> progressCallback) throws Exception {
                        HttpClient httpClient = new HttpClient();
                        Uri builtUri = Uri.parse(Api.BASE_URL).buildUpon()
                                .appendPath(Api.ENTRIES)
                                .build();
                        try {
                            return JsonHelper.parseJson(Entry.class, httpClient.get(builtUri.toString()));
                        } catch (JSONException pE) {
                            Log.v(TAG, "Parsing error");
                        }
                        return null;
                    }
                },
                null,
                new OnResultCallback<List<Entry>, Void>() {
                    @Override
                    public void onSuccess(final List<Entry> pEntryList) {
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                        }
                        if (pEntryList != null) {
                            //mEntryRecyclerAdapter = new EntryRecyclerAdapter(getApplicationContext(), pEntryList);
                            //mEntryRecyclerView.setAdapter(mEntryRecyclerAdapter);
                            storeEntriesTask(pEntryList);
                        } else {
                            Log.v(TAG, "result is null");
                        }
                    }

                    @Override
                    public void onError(final Exception e) {

                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {
                    }

                    @Override
                    public void onStart() {
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(ProgressBar.VISIBLE);
                        }
                    }
                }
        );
    }

    private void fetchDictionaryTask() {
        mThreadManager.execute(
                new ITask<Void, Void, List<Dictionary>>() {
                    @Override
                    public List<Dictionary> perform(final Void pVoid, final ProgressCallback<Void> progressCallback) throws Exception {
                        HttpClient httpClient = new HttpClient();
                        Uri builtUri = Uri.parse(Api.BASE_URL).buildUpon()
                                .appendPath(Api.DICTIONARIES)
                                .build();

                        try {
                            return JsonHelper.parseJson(Dictionary.class, httpClient.get(builtUri.toString()));
                        } catch (JSONException pE) {
                            Log.v(TAG, "Parsing error");
                        }
                        return null;
                    }
                },
                null,
                new OnResultCallback<List<Dictionary>, Void>() {
                    @Override
                    public void onSuccess(final List<Dictionary> pDictionaryList) {
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                        }
                        if (pDictionaryList != null) {
                            storeDictionariesTask(pDictionaryList);
                        } else {
                            Log.v(TAG, "result is null");
                        }
                    }

                    @Override
                    public void onError(final Exception e) {

                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {
                    }

                    @Override
                    public void onStart() {
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(ProgressBar.VISIBLE);
                        }
                    }
                }
        );
    }

    private void storeEntriesTask(List<Entry> pEntryList) {
        mThreadManager.execute(
                new ITask<List<Entry>, Void, Integer>() {
                    @Override
                    public Integer perform(final List<Entry> pEntryList, final ProgressCallback<Void> progressCallback) throws Exception {
                        Vector<ContentValues> valuesVector = new Vector<>(pEntryList.size());

                        for (Entry entry : pEntryList) {
                            ContentValues values = new ContentValues();
                            values.put(Entry.ID, entry.getId());
                            values.put(Entry.DICTIONARY_ID, entry.getDictionaryId());
                            values.put(Entry.VALUE, entry.getValue());
                            values.put(Entry.TRANSCRIPTION, entry.getTranscription());
                            values.put(Entry.CREATION_DATE, entry.getCreationDate());
                            values.put(Entry.LAST_EDITION_DATE, entry.getLastEditionDate());
                            values.put(Entry.IMAGE_URL, entry.getImageUrl());
                            values.put(Entry.SOUND_URL, entry.getSoundUrl());
                            values.put(Entry.TRANSLATION, Converter.convertStringArrayToString(entry.getTranslation()));
                            values.put(Entry.USAGE_CONTEXT, Converter.convertStringArrayToString(entry.getUsageContext()));
                            valuesVector.add(values);
                        }

                        if (valuesVector.size() > 0) {
                            ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
                            valuesVector.toArray(valuesArray);
                            MainActivity.this.getContentResolver().bulkInsert(UriBuilder.getTableUri(Entry.class), valuesArray);
                        }
                        return valuesVector.size();
                    }
                },
                pEntryList,
                new OnResultCallback<Integer, Void>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(final Integer pInteger) {
                        if (pInteger != null) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Success! " + pInteger + " entries have been stored.",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            Log.v(TAG, String.valueOf(pInteger));
                        } else {
                            Log.v(TAG, "result is null");
                        }
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

    private void storeDictionariesTask(final List<Dictionary> pDictionaryList) {
        mThreadManager.execute(
                new ITask<List<Dictionary>, Void, Integer>() {
                    @Override
                    public Integer perform(final List<Dictionary> pEntryList, final ProgressCallback<Void> progressCallback) throws Exception {
                        Vector<ContentValues> valuesVector = new Vector<>(pEntryList.size());

                        for (Dictionary dictionary : pDictionaryList) {
                            ContentValues values = new ContentValues();
                            values.put(Dictionary.ID, dictionary.getId());
                            values.put(Dictionary.NAME, dictionary.getName());
                            valuesVector.add(values);
                        }

                        if (valuesVector.size() > 0) {
                            ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
                            valuesVector.toArray(valuesArray);
                            MainActivity.this.getContentResolver().bulkInsert(UriBuilder.getTableUri(Dictionary.class), valuesArray);
                        }
                        return valuesVector.size();
                    }
                },
                pDictionaryList,
                new OnResultCallback<Integer, Void>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(final Integer pInteger) {
                        if (pInteger != null) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Success! " + pInteger + " entries have been stored.",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            Log.v(TAG, String.valueOf(pInteger));
                        } else {
                            Log.v(TAG, "result is null");
                        }
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
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        mEntryCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mEntryCursorAdapter.swapCursor(null);
    }
}