package lab.kulebin.mydictionary.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.model.Tag;
import lab.kulebin.mydictionary.utils.Converter;

public class EntryCursorAdapter extends CursorAdapter {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;

    public EntryCursorAdapter(final Context context, final Cursor c, final int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_list_entry, parent, false);
        final EntryViewHolder entryViewHolder = new EntryViewHolder(view);
        view.setTag(entryViewHolder);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final EntryViewHolder holder = (EntryViewHolder) view.getTag();
        holder.entryValueTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.VALUE)));
        holder.entryTranslationTextView.setText(cursor.getString(cursor.getColumnIndex(Entry.TRANSLATION)));
        holder.entryTagList.removeAllViews();
        final String tagsStr = cursor.getString(cursor.getColumnIndex(Constants.CURSOR_COLUMN_TAGS));
        if(tagsStr!=null){
            final String[] tagList = tagsStr.split(",");
            final LayoutInflater inflater = LayoutInflater.from(context);
            for (final String tag : tagList) {
                final TextView tagView = (TextView) inflater.inflate(R.layout.view_tag, holder.entryTagList, false);
                tagView.setText(tag);
                holder.entryTagList.addView(tagView);
            }
        }

        final String url = cursor.getString(cursor.getColumnIndex(Entry.IMAGE_URL));
        Glide.with(mContext)
                .load(url)
                .override(WIDTH, HEIGHT)
                .error(VectorDrawableCompat.create(context.getResources(), R.drawable.image_default_entry_96dp, null))
                .into(holder.entryImageView);
    }

    private static class EntryViewHolder {

        TextView entryValueTextView;
        TextView entryTranslationTextView;
        ImageView entryImageView;
        LinearLayout entryTagList;

        public EntryViewHolder(final View v) {
            entryValueTextView = (TextView) v.findViewById(R.id.entry_value);
            entryTranslationTextView = (TextView) v.findViewById(R.id.entry_translate);
            entryImageView = (ImageView) v.findViewById(R.id.entry_image);
            entryTagList = (LinearLayout) v.findViewById(R.id.entry_tag_list);
        }
    }
}
