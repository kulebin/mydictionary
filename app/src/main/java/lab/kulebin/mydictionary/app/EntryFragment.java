package lab.kulebin.mydictionary.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import lab.kulebin.mydictionary.R;


public class EntryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.item_entry_pager, container, false);
        Bundle args = getArguments();

        ImageView imageView = (ImageView) rootView.findViewById(R.id.pager_item_image);
        Glide.with(this)
                .load(args.getString(Constants.EXTRA_ENTRY_IMAGE_URL))
                .override(300, 300)
                .into(imageView);

        ((TextView) rootView.findViewById(R.id.pager_item_value)).setText(
                args.getString(Constants.EXTRA_ENTRY_VALUE));

        ((TextView) rootView.findViewById(R.id.pager_item_translation)).setText(
                args.getString(Constants.EXTRA_ENTRY_TRANSLATION));
        ((TextView) rootView.findViewById(R.id.pager_item_context_usage)).setText(
                args.getString(Constants.EXTRA_ENTRY_USAGE_CONTEXT));

        return rootView;
    }
}
