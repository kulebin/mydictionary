package lab.kulebin.mydictionary;

import android.app.Application;

import lab.kulebin.mydictionary.thread.ThreadManager;
import lab.kulebin.mydictionary.utils.BuildTypeUtils;
import lab.kulebin.mydictionary.utils.ContextHolder;

public class App extends Application {

    private ThreadManager mThreadManager;
    private TokenHolder mTokenHolder;

    public void onCreate() {
        super.onCreate();
        ContextHolder.set(this);
        BuildTypeUtils.initStetho(this);
        mThreadManager = new ThreadManager();
    }

    //TODO why we use getSysService here and direct call of getTokenHolder()? use one approach
    @Override
    public Object getSystemService(final String pName) {
        if (ThreadManager.APP_SERVICE_KEY.equals(pName)) {
            return mThreadManager;
        }
        return super.getSystemService(pName);
    }

    public TokenHolder getTokenHolder() {
        if (mTokenHolder == null) {
            mTokenHolder = new TokenHolder();
        }

        return mTokenHolder;
    }
}