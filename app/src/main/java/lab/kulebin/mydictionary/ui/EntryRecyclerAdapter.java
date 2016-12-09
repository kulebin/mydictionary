package lab.kulebin.mydictionary.ui;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.utils.Converter;

public class EntryRecyclerAdapter extends RecyclerView.Adapter<EntryRecyclerAdapter.EntryViewHolder> {

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

    private List<Entry> mEntryList;
    private Context mContext;

    public EntryRecyclerAdapter(Context pContext, List<Entry> pEntryList){
        this.mContext = pContext;
        this.mEntryList = pEntryList;
    }

    @Override
    public EntryViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
        return new EntryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final EntryViewHolder holder, final int position) {
        holder.entryValueTextView.setText(mEntryList.get(position).getValue());
        holder.entryTranslationTextView.setText(Converter.convertStringArrayToString(mEntryList.get(position).getTranslation()));
        Glide.with(mContext)
                .load(mEntryList.get(position).getImageUrl())
                .override(300, 300)
                .into(holder.entryImageView);
    }

    @Override
    public int getItemCount() {
        return mEntryList.size();
    }
}
