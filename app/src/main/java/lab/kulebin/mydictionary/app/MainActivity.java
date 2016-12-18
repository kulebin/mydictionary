package lab.kulebin.mydictionary.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.service.FetchDataService;
import lab.kulebin.mydictionary.ui.EntryCursorAdapter;
import lab.kulebin.mydictionary.utils.UriBuilder;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DICTIONARY_LOADER = 0;
    private static final int ENTRY_LOADER = 1;
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
    private EntryCursorAdapter mEntryCursorAdapter;
    private NavigationView mNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent serviceIntent = new Intent(this, FetchDataService.class);
        startService(serviceIntent);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra(Constants.EXTRA_EDIT_ACTIVITY_MODE, EditActivity.EditActivityMode.CREATE);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mUsername = ANONYMOUS;
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
                Intent intent = new Intent(MainActivity.this, EntryActivity.class).putExtra(Constants.EXTRA_ENTRY_POSITION, position);
                startActivity(intent);
            }
        });
        getSupportLoaderManager().initLoader(ENTRY_LOADER, null, this);
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
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(pItem);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem pItem) {
        //TODO navigation by dictionary should be implemented
        switch (pItem.getItemId()) {
            case R.id.navigation_menu_add_dictionary:
                startCreateDictionaryDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
                        String dictionaryName = inputDictionaryName.getText().toString();
                        if (!dictionaryName.equals("")) {

                        }
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
        //addDictionaryDialog.show();
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
                        null,
                        null,
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
                    while (pCursor.moveToNext()) {
                        Menu menu = mNavigationView.getMenu();
                        menu.add(
                                R.id.dictionary_group_navigation_menu,
                                pCursor.getInt(pCursor.getColumnIndex(Dictionary.ID)),
                                Menu.NONE,
                                pCursor.getString(pCursor.getColumnIndex(Dictionary.NAME)));
                    }
                }
                if (pCursor != null) {
                    pCursor.close();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> pLoader) {
        if (pLoader.getId() == ENTRY_LOADER) {
            mEntryCursorAdapter.swapCursor(null);
        }
    }
}