package lab.kulebin.mydictionary.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.db.DbHelper;
import lab.kulebin.mydictionary.http.HttpErrorHandler;
import lab.kulebin.mydictionary.http.IHttpClient;
import lab.kulebin.mydictionary.http.IHttpErrorHandler;
import lab.kulebin.mydictionary.http.UrlBuilder;
import lab.kulebin.mydictionary.json.JsonHelper;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.thread.ITask;
import lab.kulebin.mydictionary.thread.OnResultCallback;
import lab.kulebin.mydictionary.thread.ProgressCallback;
import lab.kulebin.mydictionary.thread.ThreadManager;
import lab.kulebin.mydictionary.utils.Converter;
import lab.kulebin.mydictionary.utils.UriBuilder;

//TODO let this activity have base part, and one child to edit, other to create new item
public class EditActivity extends AppCompatActivity {

    public static final String TAG = EditActivity.class.getSimpleName();
    private EditText mEditTextValue;
    private EditText mEditTextTranslation;
    private EditText mEditTextContextUsage;
    private EditText mEditTextImageUrl;
    private Button mEntryCreateButton;
    private boolean mIsDataChanged;
    private long mEntryId;
    private boolean isEditMode;
    private int mDictionaryMenuId;

    private final TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
        }

        @Override
        public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
        }

        @Override
        public void afterTextChanged(final Editable editable) {
            if (!mIsDataChanged) {
                mIsDataChanged = true;
            }
            if (!mEntryCreateButton.isEnabled()) {
                if (isAllDataFilled()) {
                    mEntryCreateButton.setEnabled(true);
                }
            } else if (!isAllDataFilled()) {
                mEntryCreateButton.setEnabled(false);
            }
        }
    };

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            final long entryId;
            final long systemTimeStamp = System.currentTimeMillis();
            if (isEditMode) {
                entryId = mEntryId;
            } else {
                entryId = systemTimeStamp;
            }
            final Entry entry = new Entry(
                    entryId,
                    mDictionaryMenuId,
                    mEditTextValue.getText().toString(),
                    null,
                    systemTimeStamp,
                    mEditTextImageUrl.getText().toString(),
                    null,
                    Converter.convertStringToStringArray(mEditTextTranslation.getText().toString()),
                    Converter.convertStringToStringArray(mEditTextContextUsage.getText().toString())
            );
            storeDataTask(entry);
            finish();
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mEditTextValue = (EditText) findViewById(R.id.edit_text_value);
        mEditTextTranslation = (EditText) findViewById(R.id.edit_text_translation);
        mEditTextContextUsage = (EditText) findViewById(R.id.edit_text_context_usage);
        mEditTextImageUrl = (EditText) findViewById(R.id.edit_text_image_url);
        mEntryCreateButton = (Button) findViewById(R.id.button_create);
        mEntryCreateButton.setEnabled(false);

        final Intent intent = getIntent();
        if (intent.hasExtra(Constants.EXTRA_ENTRY_ID)) {
            isEditMode = true;
            mEntryCreateButton.setText(R.string.BUTTON_SAVE);
            if (actionBar != null) {
                actionBar.setTitle(R.string.TITLE_ACTIVITY_EDIT_MODE);
            }
            mEntryId = intent.getLongExtra(Constants.EXTRA_ENTRY_ID, Constants.ENTRY_ID_EMPTY);
            if (mEntryId > 0) {
                fetchEntryTask(mEntryId);
            }

        } else {
            if (actionBar != null) {
                actionBar.setTitle(R.string.TITLE_ACTIVITY_CREATE_NEW_ENTRY);
            }
            mDictionaryMenuId = intent.getIntExtra(Constants.EXTRA_SELECTED_DICTIONARY_ID,
                    Constants.DEFAULT_SELECTED_DICTIONARY_ID);
            mEntryCreateButton.setText(R.string.BUTTON_CREATE);
        }

        mEditTextValue.addTextChangedListener(mTextWatcher);
        mEditTextTranslation.addTextChangedListener(mTextWatcher);
        mEditTextContextUsage.addTextChangedListener(mTextWatcher);
        mEditTextImageUrl.addTextChangedListener(mTextWatcher);

        mEntryCreateButton.setOnClickListener(mOnClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isAnyDataFilled() && mIsDataChanged) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle(getString(R.string.TITLE_DIALOG_DATA_NOT_SAVED));
                    alertDialogBuilder
                            .setMessage(getString(R.string.TEXT_DIALOG_DATA_NOT_SAVED))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.BUTTON_DIALOG_POSITIVE), new DialogInterface.OnClickListener() {

                                public void onClick(final DialogInterface dialog, final int id) {
                                    finish();
                                }
                            })
                            .setNegativeButton(getString(R.string.BUTTON_DIALOG_NEGATIVE), new DialogInterface.OnClickListener() {

                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            });

                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    return true;
                } else {
                    this.finish();
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

    private void storeDataTask(final Entry pEntry) {
        //noinspection WrongConstant
        ((ThreadManager) getApplication().getSystemService(ThreadManager.APP_SERVICE_KEY)).execute(
                new ITask<Entry, Void, Void>() {

                    @Override
                    public Void perform(final Entry pEntry, final ProgressCallback<Void> progressCallback) throws Exception {

                        final ContentValues values = new ContentValues();
                        values.put(Entry.ID, pEntry.getId());
                        values.put(Entry.DICTIONARY_MENU_ID, pEntry.getDictionaryMenuId());
                        values.put(Entry.VALUE, pEntry.getValue());
                        values.put(Entry.TRANSCRIPTION, pEntry.getTranscription());
                        values.put(Entry.LAST_EDITION_DATE, pEntry.getLastEditionDate());
                        values.put(Entry.IMAGE_URL, pEntry.getImageUrl());
                        values.put(Entry.SOUND_URL, pEntry.getSoundUrl());
                        values.put(Entry.TRANSLATION, Converter.convertStringArrayToString(pEntry.getTranslation()));
                        values.put(Entry.USAGE_CONTEXT, Converter.convertStringArrayToString(pEntry.getUsageContext()));

                        final String url = UrlBuilder.getPersonalisedUrl(
                                new String[]{DbHelper.getTableName(Entry.class), String.valueOf(pEntry.getId())},
                                null
                        );

                        final IHttpClient httpClient = IHttpClient.Impl.newInstance();
                        final IHttpErrorHandler httpErrorHandler = new HttpErrorHandler();
                        httpClient.setErrorHandler(httpErrorHandler);
                        final String response = httpClient.put(url, null, pEntry.toJson());

                        if (pEntry.getLastEditionDate() == JsonHelper.getEntryLastEditionDateFromJson(response)) {
                            if (isEditMode) {
                                getContentResolver().update(
                                        UriBuilder.getTableUri(Entry.class),
                                        values,
                                        Entry.ID + "=?",
                                        new String[]{String.valueOf(pEntry.getId())}
                                );
                            } else {
                                getContentResolver().insert(
                                        UriBuilder.getTableUri(Entry.class),
                                        values
                                );
                            }
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
                    public void onSuccess(final Void pVoid) {
                        final Toast toast = Toast.makeText(getApplicationContext(),
                                R.string.TEXT_RESULT_SUCCESS_ENTRY_STORED,
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

    private void fetchEntryTask(final long pEntryId) {
        //noinspection WrongConstant
        ((ThreadManager) getApplication().getSystemService(ThreadManager.APP_SERVICE_KEY)).execute(
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
                            mDictionaryMenuId = pCursor.getInt(pCursor.getColumnIndex(Entry.DICTIONARY_MENU_ID));
                            mIsDataChanged = false;
                            mEntryCreateButton.setEnabled(false);
                        }
                    }

                    @Override
                    public void onError(final Exception e) {
                        final Toast toast = Toast.makeText(EditActivity.this, R.string.ERROR_ENTRY_NOT_FOUND, Toast.LENGTH_SHORT);
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
}
