package lab.kulebin.mydictionary.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.utils.UriBuilder;

public class EntryActivity extends AppCompatActivity {

    private static final String[] ENTRY_PROJECTION = {
            Entry.ID,
            Entry.VALUE
    };
    TextView mTextViewId;
    TextView mTextViewValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        long entryId = intent.getLongExtra(Constants.EXTRA_ENTRY_ID, -1);

        mTextViewId = (TextView) findViewById(R.id.text_entry_id);
        mTextViewValue = (TextView) findViewById(R.id.text_entry_value);

        //ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        //viewPager.setAdapter(new EntryPagerAdapter(this));

        Cursor cursor = getContentResolver().query(
                UriBuilder.getTableUri(Entry.class),
                ENTRY_PROJECTION,
                Entry.ID + "=?",
                new String[]{String.valueOf(entryId)},
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            mTextViewId.setText(String.valueOf(cursor.getLong(cursor.getColumnIndex(Entry.ID))));
            mTextViewValue.setText(cursor.getString(cursor.getColumnIndex(Entry.VALUE)));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                EntryActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
