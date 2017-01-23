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

    private static final int SPLASH_SHOW_TIME = 1000;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //TODO what if I close app when splash is shown?
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                final Intent intent;
                if (firebaseUser == null) {
                    intent = new Intent(StartActivity.this, SignInActivity.class);
                } else {
                    //TODO will it restore token if expired?
                    firebaseUser.reload();
                    intent = new Intent(StartActivity.this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, SPLASH_SHOW_TIME);
    }
}
