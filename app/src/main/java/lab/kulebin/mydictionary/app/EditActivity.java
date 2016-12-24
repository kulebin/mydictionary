package lab.kulebin.mydictionary.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.json.JsonHelper;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.thread.ITask;
import lab.kulebin.mydictionary.thread.OnResultCallback;
import lab.kulebin.mydictionary.thread.ProgressCallback;
import lab.kulebin.mydictionary.thread.ThreadManager;
import lab.kulebin.mydictionary.utils.Converter;
import lab.kulebin.mydictionary.utils.UriBuilder;

public class EditActivity extends AppCompatActivity {

    public static final String TAG = EditActivity.class.getSimpleName();
    private EditText mEditTextValue;
    private EditText mEditTextTranslation;
    private EditText mEditTextContextUsage;
    private EditText mEditTextImageUrl;
    private Button mEntryCreateButton;
    private boolean mIsDataChanged = false;
    private long mEntryId;
    private long mCreationDate;
    private EditActivityMode mEditActivityMode;
    private int mDictionaryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ActionBar actionBar =  getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mEditTextValue = (EditText) findViewById(R.id.edit_text_value);
        mEditTextTranslation = (EditText) findViewById(R.id.edit_text_translation);
        mEditTextContextUsage = (EditText) findViewById(R.id.edit_text_context_usage);
        mEditTextImageUrl = (EditText) findViewById(R.id.edit_text_image_url);
        mEntryCreateButton = (Button) findViewById(R.id.button_create);

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.EXTRA_EDIT_ACTIVITY_MODE)) {
            mEditActivityMode = (EditActivityMode) intent.getSerializableExtra(Constants.EXTRA_EDIT_ACTIVITY_MODE);
            if (mEditActivityMode == EditActivityMode.EDIT) {
                mEntryCreateButton.setText(R.string.button_save);
                if(actionBar!=null){
                    actionBar.setTitle(R.string.activity_edit_title_mode_edit);
                }
                mEntryId = intent.getLongExtra(Constants.EXTRA_ENTRY_ID, Constants.ENTRY_ID_EMPTY);
                if (mEntryId > 0) {
                    fetchEntryTask(mEntryId);
                }
            } else {
                if(actionBar!=null){
                    actionBar.setTitle(R.string.activity_edit_title_mode_create_new);
                }
                mDictionaryId = intent.getIntExtra(Constants.EXTRA_SELECTED_DICTIONARY_ID,
                        Constants.DEFAULT_SELECTED_DICTIONARY_ID);
                mEntryCreateButton.setText(R.string.button_create);
            }

        }

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
                if (!mIsDataChanged) mIsDataChanged = true;
                if (!isEnabled) {
                    if (isAllDataFilled()) {
                        mEntryCreateButton.setEnabled(true);
                        isEnabled = true;
                    }
                } else if (!isAllDataFilled()) {
                    mEntryCreateButton.setEnabled(false);
                    isEnabled = false;
                }
            }
        };

        mEditTextValue.addTextChangedListener(textWatcher);
        mEditTextTranslation.addTextChangedListener(textWatcher);
        mEditTextContextUsage.addTextChangedListener(textWatcher);
        mEditTextImageUrl.addTextChangedListener(textWatcher);

        mEntryCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                long entryId;
                long systemTimeStamp = System.currentTimeMillis();
                long entryCreationDate;
                if (mEditActivityMode == EditActivityMode.CREATE) {
                    entryId = systemTimeStamp;
                    entryCreationDate = systemTimeStamp;
                } else {
                    entryId = mEntryId;
                    entryCreationDate = mCreationDate;
                }
                Entry entry = new Entry(
                        entryId,
                        mDictionaryId,
                        mEditTextValue.getText().toString(),
                        null,
                        entryCreationDate,
                        systemTimeStamp,
                        mEditTextImageUrl.getText().toString(),
                        null,
                        Converter.convertStringToStringArray(mEditTextTranslation.getText().toString()),
                        Converter.convertStringToStringArray(mEditTextContextUsage.getText().toString())
                );
                storeDataTask(entry);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isAnyDataFilled() && mIsDataChanged) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle(getString(R.string.alert_title_data_not_saved));
                    alertDialogBuilder
                            .setMessage(getString(R.string.alert_body_data_not_saved))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.alert_positive_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            })
                            .setNegativeButton(getString(R.string.alert_negative_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    return true;
                } else {
                    EditActivity.this.finish();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isAllDataFilled() {
        return !TextUtils.isEmpty(mEditTextValue.getText().toString())
                && !TextUtils.isEmpty(mEditTextTranslation.getText().toString());
    }

    private boolean isAnyDataFilled() {
        return !TextUtils.isEmpty(mEditTextValue.getText().toString())
                || !TextUtils.isEmpty(mEditTextTranslation.getText().toString())
                || !TextUtils.isEmpty(mEditTextContextUsage.getText().toString())
                || !TextUtils.isEmpty(mEditTextImageUrl.getText().toString());
    }

    private void storeDataTask(Entry pEntry) {
        new ThreadManager().execute(
                new ITask<Entry, Void, Void>() {
                    @Override
                    public Void perform(final Entry pEntry, final ProgressCallback<Void> progressCallback) throws Exception {

                        ContentValues values = new ContentValues();
                        values.put(Entry.ID, pEntry.getId());
                        values.put(Entry.DICTIONARY_ID, pEntry.getDictionaryId());
                        values.put(Entry.VALUE, pEntry.getValue());
                        values.put(Entry.TRANSCRIPTION, pEntry.getTranscription());
                        values.put(Entry.CREATION_DATE, pEntry.getCreationDate());
                        values.put(Entry.LAST_EDITION_DATE, pEntry.getLastEditionDate());
                        values.put(Entry.IMAGE_URL, pEntry.getImageUrl());
                        values.put(Entry.SOUND_URL, pEntry.getSoundUrl());
                        values.put(Entry.TRANSLATION, Converter.convertStringArrayToString(pEntry.getTranslation()));
                        values.put(Entry.USAGE_CONTEXT, Converter.convertStringArrayToString(pEntry.getUsageContext()));

                        SharedPreferences shp = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
                        final String token = shp.getString(Constants.APP_PREFERENCES_USER_TOKEN, null);

                        Uri uri = Uri.parse(Api.getBaseUrl()).buildUpon()
                                .appendPath(Api.ENTRIES)
                                .appendPath(String.valueOf(pEntry.getId()) + Api.JSON_FORMAT)
                                .appendQueryParameter(Api.PARAM_AUTH, token)
                                .build();
                        HttpClient httpClient = new HttpClient();
                        try {
                            String response = httpClient.put(uri.toString(), null, JsonHelper.buildEntryJsonObject(pEntry).toString());
                            if (pEntry.getLastEditionDate() == JsonHelper.getEntryLastEditionDateFromJson(response)) {
                                if (mEditActivityMode == EditActivityMode.EDIT) {
                                    getContentResolver().update(
                                            UriBuilder.getTableUri(Entry.class),
                                            values,
                                            Entry.ID + "=?",
                                            new String[]{String.valueOf(pEntry.getId())});
                                } else {
                                    getContentResolver().insert(
                                            UriBuilder.getTableUri(Entry.class),
                                            values
                                    );
                                }
                            }
                        } catch (Exception e) {
                            Log.v(TAG, getString(R.string.ERROR_DELETE_REQUEST));
                        }
                        return null;
                    }
                },
                pEntry,
                new OnResultCallback<Void, Void>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(Void pVoid) {
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

    private void fetchEntryTask(long pEntryId) {
        new ThreadManager().execute(
                new ITask<Long, Void, Cursor>() {
                    @Override
                    public Cursor perform(final Long pEntryId, final ProgressCallback<Void> progressCallback) throws Exception {
                        return getContentResolver().query(
                                UriBuilder.getTableUri(Entry.class),
                                null, Entry.ID + "=?",
                                new String[]{String.valueOf(pEntryId)},
                                null);
                    }
                },
                pEntryId,
                new OnResultCallback<Cursor, Void>() {
                    @Override
                    public void onSuccess(final Cursor pCursor) {
                        if (pCursor.getCount() > 0) {
                            pCursor.moveToFirst();
                            mEditTextValue.setText(pCursor.getString(pCursor.getColumnIndex(Entry.VALUE)));
                            mEditTextTranslation.setText(pCursor.getString(pCursor.getColumnIndex(Entry.TRANSLATION)));
                            mEditTextImageUrl.setText(pCursor.getString(pCursor.getColumnIndex(Entry.IMAGE_URL)));
                            mEditTextContextUsage.setText(pCursor.getString(pCursor.getColumnIndex(Entry.USAGE_CONTEXT)));
                            mCreationDate = pCursor.getLong(pCursor.getColumnIndex(Entry.CREATION_DATE));
                            mDictionaryId = pCursor.getInt(pCursor.getColumnIndex(Entry.DICTIONARY_ID));
                            mIsDataChanged = false;
                        }
                    }

                    @Override
                    public void onError(final Exception e) {
                        Toast toast = Toast.makeText(EditActivity.this, R.string.ERROR_FETCHING_ENTRY_FROM_DB, Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {
                    }

                    @Override
                    public void onStart() {
                    }
                }
        );
    }

    public enum EditActivityMode {CREATE, EDIT}
}
