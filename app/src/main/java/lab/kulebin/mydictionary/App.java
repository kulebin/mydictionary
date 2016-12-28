package lab.kulebin.mydictionary;

import android.app.Application;

import com.facebook.stetho.Stetho;

import lab.kulebin.mydictionary.thread.ThreadManager;

public class App extends Application {

    private ThreadManager mThreadManager;

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        mThreadManager = new ThreadManager();
    }

    @Override
    public Object getSystemService(final String pName) {
        if (ThreadManager.APP_SERVICE_KEY.equals(pName)) {
            return mThreadManager;
        }
        return super.getSystemService(pName);
    }
}
