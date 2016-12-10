package lab.kulebin.mydictionary.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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

import lab.kulebin.mydictionary.R;
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
    private boolean isDataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mEditTextValue = (EditText) findViewById(R.id.edit_text_value);
        mEditTextTranslation = (EditText) findViewById(R.id.edit_text_translation);
        mEditTextContextUsage = (EditText) findViewById(R.id.edit_text_context_usage);
        mEditTextImageUrl = (EditText) findViewById(R.id.edit_text_image_url);
        mEntryCreateButton = (Button) findViewById(R.id.button_create);

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
                if (!isDataChanged) isDataChanged = true;
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
                storeDataTask(new Entry(
                        System.currentTimeMillis(),
                        1, // TODO: 12/11/2016 Dictionary ID should be got from selected tab
                        mEditTextValue.getText().toString(),
                        mEditTextTranslation.getText().toString(),
                        System.currentTimeMillis(),
                        -1,
                        mEditTextImageUrl.getText().toString(),
                        null,
                        Converter.convertStringToStringArray(mEditTextTranslation.getText().toString()),
                        Converter.convertStringToStringArray(mEditTextContextUsage.getText().toString())
                ));
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isAnyDataFilled() && isDataChanged) {
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

                        EditActivity.this.getContentResolver().insert(UriBuilder.getTableUri(Entry.class), values);
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
                                "Success! Entry have been stored.",
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
}
