package lab.kulebin.mydictionary.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;


public class EntryPagerAdapter extends PagerAdapter {

    private Context mContext;

    public EntryPagerAdapter(Context context) {
        mContext = context;
    }


    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return false;
    }
}
