package lab.kulebin.mydictionary.app;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.db.DbHelper;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.thread.ITask;
import lab.kulebin.mydictionary.thread.OnResultCallback;
import lab.kulebin.mydictionary.thread.ProgressCallback;
import lab.kulebin.mydictionary.thread.ThreadManager;
import lab.kulebin.mydictionary.ui.EntryRecyclerAdapter;
import lab.kulebin.mydictionary.utils.UriBuilder;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String ANONYMOUS = "anonymous";
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private RecyclerView mEntryRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EntryRecyclerAdapter mEntryRecyclerAdapter;
    private ImageView mUserImageView;
    private ThreadManager mThreadManager;


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

        //mThreadManager = (ThreadManager) this.getSystemService(ThreadManager.APP_SERVICE_KEY);
        mThreadManager = new ThreadManager();
        fetchEntryTask();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


        mEntryRecyclerView = (RecyclerView) findViewById(R.id.entryRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mEntryRecyclerView.setLayoutManager(mLinearLayoutManager);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

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

    private List<Entry> parseEntriesJson(String json) throws JSONException {
        List<Entry> entryList = new ArrayList<>();
        JSONArray entryArray = new JSONArray(json);

        for (int i = 0; i < entryArray.length(); i++) {
            JSONObject entryObject = entryArray.getJSONObject(i);
            entryList.add(new Entry(
                    entryObject.getInt(Entry.ID),
                    entryObject.getInt(Entry.DICTIONARY_ID),
                    entryObject.getString(Entry.VALUE),
                    entryObject.isNull(Entry.TRANSCRIPTION) ? null : entryObject.getString(Entry.TRANSCRIPTION),
                    entryObject.getLong(Entry.CREATION_DATE),
                    entryObject.isNull(Entry.LAST_EDITION_DATE) ? -1 : entryObject.getLong(Entry.LAST_EDITION_DATE),
                    entryObject.isNull(Entry.IMAGE_URL) ? null : entryObject.getString(Entry.IMAGE_URL),
                    entryObject.isNull(Entry.SOUND_URL) ? null : entryObject.getString(Entry.SOUND_URL),
                    entryObject.isNull(Entry.TRANSLATION) ? null : Entry.convertStringToStirngArray(entryObject.getString(Entry.TRANSLATION)),
                    entryObject.isNull(Entry.USAGE_CONTEXT) ? null : Entry.convertStringToStirngArray(entryObject.getString(Entry.USAGE_CONTEXT))
            ));
        }
        return entryList;
    }

    private void fetchEntryTask(){
        mThreadManager.execute(
                new ITask<Void, Void, List<Entry>>() {
                    @Override
                    public List<Entry> perform(final Void pVoid, final ProgressCallback<Void> progressCallback) throws Exception {
                        HttpURLConnection urlConnection = null;
                        BufferedReader reader = null;

                        String json = null;

                        try {
                            Uri builtUri = Uri.parse(Api.BASE_URL).buildUpon()
                                    .appendPath(Api.ENTRIES)
                                    .build();

                            URL url = new URL(builtUri.toString());
                            Log.v(TAG, url.toString());

                            // Create the request to OpenWeatherMap, and open the connection
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("GET");
                            urlConnection.connect();

                            // Read the input stream into a String
                            InputStream inputStream = urlConnection.getInputStream();
                            StringBuffer buffer = new StringBuffer();
                            if (inputStream == null) {
                                return null;
                            }
                            reader = new BufferedReader(new InputStreamReader(inputStream));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                buffer.append(line + "\n");
                            }

                            if (buffer.length() == 0) {
                                return null;
                            }
                            json = buffer.toString();
                        } catch (IOException e) {
                            Log.e(TAG, "Error ", e);
                            return null;
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (final IOException e) {
                                    Log.e(TAG, "Error closing stream", e);
                                }
                            }
                        }

                        try {
                            return parseEntriesJson(json);
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
                            mEntryRecyclerAdapter = new EntryRecyclerAdapter(getApplicationContext(), pEntryList);
                            mEntryRecyclerView.setAdapter(mEntryRecyclerAdapter);
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
                    public void onStart(){
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(ProgressBar.VISIBLE);
                        }
                    }
                }
        );
    }

    private void storeEntriesTask(List<Entry> pEntryList){
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
                            values.put(Entry.TRANSLATION, Entry.convertStringArrayToString(entry.getTranslation()));
                            values.put(Entry.USAGE_CONTEXT, Entry.convertStringArrayToString(entry.getUsageContext()));
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
}