package lab.kulebin.mydictionary.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lab.kulebin.mydictionary.R;

//TODO show progress indication when any background operation is performed (0,1 s and less - we don't need progress)
public class StartActivity extends AppCompatActivity {

    private static final int SPLASH_SHOW_TIME = 500;

    private Runnable mRunnable;
    private Handler mHandler;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        mRunnable = new Runnable() {

            @Override
            public void run() {
                final Intent intent;
                if (firebaseUser == null) {
                    intent = new Intent(StartActivity.this, SignInActivity.class);
                } else {
                    intent = new Intent(StartActivity.this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        };

        mHandler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.postDelayed(mRunnable, SPLASH_SHOW_TIME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mRunnable);
    }
}
