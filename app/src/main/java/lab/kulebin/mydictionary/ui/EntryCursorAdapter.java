package lab.kulebin.mydictionary.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_entry, parent, false);
        EntryViewHolder entryViewHolder = new EntryViewHolder(view);
        view.setTag(entryViewHolder);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        EntryViewHolder holder = (EntryViewHolder) view.getTag();
        holder.entryValueTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.VALUE)));
        holder.entryTranslationTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.TRANSLATION)));
        Glide.with(mContext)
                .load(cursor.getString(cursor.getColumnIndex(Entry.IMAGE_URL)))
                .override(300, 300)
                .into(holder.entryImageView);

    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView entryValueTextView;
        TextView entryTranslationTextView;
        ImageView entryImageView;

        public EntryViewHolder(View v) {
            super(v);
            entryValueTextView = (TextView) itemView.findViewById(R.id.entry_value);
            entryTranslationTextView = (TextView) itemView.findViewById(R.id.entry_translate);
            entryImageView = (ImageView) itemView.findViewById(R.id.entry_image);
        }
    }
}
