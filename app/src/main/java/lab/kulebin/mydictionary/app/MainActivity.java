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

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.db.Contract;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.json.JsonHelper;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
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
    public static final String FETCH_DATA_TASK_PARAM_URI = "uri";
    public static final String FETCH_DATA_TASK_PARAM_TYPE = "clazz";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ENTRY_LOADER = 0;
    private static final String[] ENTRY_PROJECTION = {
            Entry.ID,
            Entry.VALUE,
            Entry.TRANSLATION,
            Entry.IMAGE_URL
    };


    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private ProgressBar mProgressBar;
    private EntryCursorAdapter mEntryCursorAdapter;
    private ImageView mUserImageView;
    private ThreadManager mThreadManager;
    private ListView mListView;


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
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


        mListView = (ListView) findViewById(R.id.listview_entry);
        mEntryCursorAdapter = new EntryCursorAdapter(this, null, 0);
        mListView.setAdapter(mEntryCursorAdapter);
        getSupportLoaderManager().initLoader(ENTRY_LOADER, null, this);

        //noinspection WrongConstant
        // TODO: 12/9/2016 Method returns null instead of reference on instance of thread manager, should be fixed
        //mThreadManager = (ThreadManager) this.getSystemService(ThreadManager.APP_SERVICE_KEY);
        mThreadManager = new ThreadManager();

        //Fetch data from backend
        for (Class model : Contract.MODELS) {
            String path;
            if (model == Entry.class) {
                path = Api.ENTRIES;
            } else if (model == Dictionary.class) {
                path = Api.DICTIONARIES;
            } else {
                continue;
            }
            Uri builtUri = Uri.parse(Api.BASE_URL).buildUpon()
                    .appendPath(path)
                    .build();
            HashMap<String, Object> fetchDataParams = new HashMap();
            fetchDataParams.put(FETCH_DATA_TASK_PARAM_URI, builtUri);
            fetchDataParams.put(FETCH_DATA_TASK_PARAM_TYPE, model);
            fetchDataTask(fetchDataParams);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
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

    // Will be used in the future when main activity is done
    private void signIn() {
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
        }
    }


    private void fetchDataTask(HashMap pHashMap) {
        if (mThreadManager != null) {
            mThreadManager.execute(
                    new ITask<HashMap, Void, List<?>>() {
                        @Override
                        public List<?> perform(final HashMap pMap, final ProgressCallback<Void> progressCallback) throws Exception {
                            HttpClient httpClient = new HttpClient();
                            Uri uri = (Uri) pMap.get(FETCH_DATA_TASK_PARAM_URI);
                            Class clazz = (Class) pMap.get(FETCH_DATA_TASK_PARAM_TYPE);
                            try {
                                return JsonHelper.parseJson(clazz, httpClient.get(uri.toString()));
                            } catch (JSONException pE) {
                                Log.v(TAG, "Parsing error");
                            }
                            return null;
                        }
                    },
                    pHashMap,
                    new OnResultCallback<List<?>, Void>() {
                        @Override
                        public void onSuccess(final List<?> pList) {
                            if (mProgressBar != null) {
                                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                            }
                            if (pList != null) {
                                storeDataTask(pList);
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
        } else {
            Toast toast = Toast.makeText(this, "Error! Data is not loaded!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    private void storeDataTask(List<?> pList) {
        mThreadManager.execute(
                new ITask<List<?>, Void, Integer>() {
                    @Override
                    public Integer perform(final List<?> pList, final ProgressCallback<Void> progressCallback) throws Exception {
                        Vector<ContentValues> valuesVector = new Vector<>(pList.size());
                        Class clazz = null;
                        if (pList.get(0).getClass() == Entry.class) {
                            for (Entry entry : (List<Entry>) pList) {
                                clazz = Entry.class;
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

                        } else if (pList.get(0).getClass() == Dictionary.class) {
                            for (Dictionary dictionary : (List<Dictionary>) pList) {
                                clazz = Dictionary.class;
                                ContentValues values = new ContentValues();
                                values.put(Dictionary.ID, dictionary.getId());
                                values.put(Dictionary.NAME, dictionary.getName());
                                valuesVector.add(values);
                            }
                        } else {
                            return -1;
                        }
                        if (valuesVector.size() > 0) {
                            ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
                            valuesVector.toArray(valuesArray);
                            MainActivity.this.getContentResolver().bulkInsert(UriBuilder.getTableUri(clazz), valuesArray);
                        }
                        return valuesVector.size();
                    }
                },
                pList,
                new OnResultCallback<Integer, Void>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(final Integer pInteger) {
                        if (pInteger != -1) {
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