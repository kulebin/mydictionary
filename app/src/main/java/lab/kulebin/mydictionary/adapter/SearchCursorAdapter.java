package lab.kulebin.mydictionary.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.model.Dictionary;
import lab.kulebin.mydictionary.model.Entry;

public class SearchCursorAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_SEARCH = 0;
    private static final int VIEW_TYPE_SEARCH_WITH_HEADER = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    private Cursor mCursor;

    public SearchCursorAdapter(final Context context, final Cursor c, final int flags) {
        super(context, c, flags);
        this.mCursor = c;
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        mCursor = cursor;
        final int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_SEARCH:
                layoutId = R.layout.item_list_search;
                break;
            case VIEW_TYPE_SEARCH_WITH_HEADER:
                layoutId = R.layout.item_list_search_with_header;
                break;
        }

        final View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        final SearchCursorAdapter.EntryViewHolder entryViewHolder = new SearchCursorAdapter.EntryViewHolder(view);
        view.setTag(entryViewHolder);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        mCursor = cursor;
        final SearchCursorAdapter.EntryViewHolder holder = (SearchCursorAdapter.EntryViewHolder) view.getTag();
        holder.entryValueTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.VALUE)));
        holder.entryTranslationTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.TRANSLATION)));
        if (getItemViewType(cursor.getPosition()) == VIEW_TYPE_SEARCH_WITH_HEADER) {
            holder.dictionaryNameHeader.setText(cursor.getString(cursor.getColumnIndex(Dictionary.NAME)));
        }
        final String url = cursor.getString(cursor.getColumnIndex(Entry.IMAGE_URL));
        Glide.with(mContext)
                .load(url)
                .override(WIDTH, HEIGHT)
                .error(R.drawable.image_default_entry_96dp)
                .into(holder.entryImageView);
    }

    @Override
    public int getItemViewType(final int pPosition) {
        if (mCursor != null && pPosition > 0) {
            mCursor.moveToPosition(pPosition);
            final int dictionaryId = mCursor.getInt(mCursor.getColumnIndex(Entry.DICTIONARY_ID));
            mCursor.moveToPosition(pPosition - 1);
            if (mCursor.getInt(mCursor.getColumnIndex(Entry.DICTIONARY_ID)) == dictionaryId) {
                mCursor.moveToNext();
                return VIEW_TYPE_SEARCH;
            }
            mCursor.moveToNext();
        }
        return VIEW_TYPE_SEARCH_WITH_HEADER;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    private static class EntryViewHolder {

        TextView entryValueTextView;
        TextView entryTranslationTextView;
        ImageView entryImageView;
        TextView dictionaryNameHeader;
        View v;

        public EntryViewHolder(final View v) {
            this.v = v;
            entryValueTextView = (TextView) v.findViewById(R.id.entry_value);
            entryTranslationTextView = (TextView) v.findViewById(R.id.entry_translate);
            entryImageView = (ImageView) v.findViewById(R.id.entry_image);
            dictionaryNameHeader = (TextView) v.findViewById(R.id.dictionary_name);
        }
    }
}
