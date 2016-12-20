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
import lab.kulebin.mydictionary.model.Entry;

public class EntryCursorAdapter extends CursorAdapter {

    public EntryCursorAdapter(final Context context, final Cursor c, final int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_entry, parent, false);
        long entryId = cursor.getLong(cursor.getColumnIndex(Entry.ID));
        EntryViewHolder entryViewHolder = new EntryViewHolder(view, entryId);
        view.setTag(entryViewHolder);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        EntryViewHolder holder = (EntryViewHolder) view.getTag();
        holder.entryValueTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.VALUE)));
        holder.entryTranslationTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.TRANSLATION)));
        String url = cursor.getString(cursor.getColumnIndex(Entry.IMAGE_URL));
        if(url != null && !url.isEmpty()){
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
        long entryId;

        public EntryViewHolder(View v, long id) {
            entryValueTextView = (TextView) v.findViewById(R.id.entry_value);
            entryTranslationTextView = (TextView) v.findViewById(R.id.entry_translate);
            entryImageView = (ImageView) v.findViewById(R.id.entry_image);
            entryId = id;
        }
    }
}
