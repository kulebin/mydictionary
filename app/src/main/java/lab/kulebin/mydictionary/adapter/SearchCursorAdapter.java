package lab.kulebin.mydictionary.adapter;

import android.content.Context;
import android.database.Cursor;
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

    public SearchCursorAdapter(final Context context, final Cursor c, final int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_search_result, parent, false);
        long entryId = cursor.getLong(cursor.getColumnIndex(Entry.ID));
        SearchCursorAdapter.EntryViewHolder entryViewHolder = new SearchCursorAdapter.EntryViewHolder(view, entryId);
        view.setTag(entryViewHolder);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        SearchCursorAdapter.EntryViewHolder holder = (SearchCursorAdapter.EntryViewHolder) view.getTag();
        holder.entryValueTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.VALUE)));
        holder.entryTranslationTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.TRANSLATION)));
        holder.dictionaryNameTextView.setText(cursor.getString(cursor.getColumnIndex(Dictionary.NAME)));
        holder.dictionaryIdTextView.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(Dictionary.ID))));
        String url = cursor.getString(cursor.getColumnIndex(Entry.IMAGE_URL));
        if (url != null && !url.isEmpty()) {
            Glide.with(mContext)
                    .load(url)
                    .override(300, 300)
                    .into(holder.entryImageView);
        }
    }

    private static class EntryViewHolder {
        TextView entryValueTextView;
        TextView entryTranslationTextView;
        ImageView entryImageView;
        TextView dictionaryIdTextView;
        TextView dictionaryNameTextView;
        long entryId;

        public EntryViewHolder(View v, long id) {
            entryValueTextView = (TextView) v.findViewById(R.id.entry_value);
            entryTranslationTextView = (TextView) v.findViewById(R.id.entry_translate);
            entryImageView = (ImageView) v.findViewById(R.id.entry_image);
            dictionaryIdTextView = (TextView) v.findViewById(R.id.dictionary_id);
            dictionaryNameTextView = (TextView) v.findViewById(R.id.dictionary_name);
            entryId = id;
        }
    }
}
