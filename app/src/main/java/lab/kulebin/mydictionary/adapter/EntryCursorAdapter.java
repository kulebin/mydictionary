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
import lab.kulebin.mydictionary.model.Entry;

public class EntryCursorAdapter extends CursorAdapter {

    public EntryCursorAdapter(final Context context, final Cursor c, final int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_entry, parent, false);
        EntryViewHolder entryViewHolder = new EntryViewHolder(view);
        view.setTag(entryViewHolder);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        EntryViewHolder holder = (EntryViewHolder) view.getTag();
        holder.entryValueTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.VALUE)));
        holder.entryTranslationTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.TRANSLATION)));
        String url = cursor.getString(cursor.getColumnIndex(Entry.IMAGE_URL));
        if (url != null && !url.isEmpty()) {
            Glide.with(mContext)
                    .load(url)
                    .override(300, 300)
                    .into(holder.entryImageView);
        } else {
            Drawable entryImageDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.image_default_entry_96dp, null);
            holder.entryImageView.setImageDrawable(entryImageDrawable);
        }
    }

    private static class EntryViewHolder {
        TextView entryValueTextView;
        TextView entryTranslationTextView;
        ImageView entryImageView;

        public EntryViewHolder(View v) {
            entryValueTextView = (TextView) v.findViewById(R.id.entry_value);
            entryTranslationTextView = (TextView) v.findViewById(R.id.entry_translate);
            entryImageView = (ImageView) v.findViewById(R.id.entry_image);
        }
    }
}
