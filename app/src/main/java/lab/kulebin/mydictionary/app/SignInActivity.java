package lab.kulebin.mydictionary.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.db.Contract;
import lab.kulebin.mydictionary.utils.UriBuilder;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = SignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.email_create_account_button:
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case R.id.email_sign_in_button:
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Connection error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                final GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.e(TAG, "Google Sign In failed.");
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        final SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        setSignInGoogleButtonText(signInButton, getString(R.string.button_signin_with_goggle));
        signInButton.setOnClickListener(this);
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    clearAllData();
                    storeToken(user);
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    protected void setSignInGoogleButtonText(final SignInButton pSignInButton, final String pButtonText) {
        for (int i = 0; i < pSignInButton.getChildCount(); i++) {
            final View v = pSignInButton.getChildAt(i);

            if (v instanceof TextView) {
                final TextView tv = (TextView) v;
                tv.setText(pButtonText);
                return;
            }
        }
    }

    private void createAccount(final String email, final String password) {
        if (!validateForm()) {
            return;
        }

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, R.string.error_authentication_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signIn(final String email, final String password) {
        if (!validateForm()) {
            return;
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, R.string.error_authentication_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, R.string.error_authentication_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void storeToken(final FirebaseUser user) {
        user.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {

            public void onComplete(@NonNull final Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    final String token = task.getResult().getToken();
                    if (token != null) {
                        final SharedPreferences appPreferences = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = appPreferences.edit();
                        editor.putString(Constants.APP_PREFERENCES_USER_TOKEN, token);
                        editor.apply();
                    }
                }
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        final String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError(getString(R.string.edit_error_required_mark));
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        final String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(getString(R.string.edit_error_required_mark));
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void clearAllData(){
        for(final Class clazz : Contract.MODELS){
            this.getContentResolver().delete(UriBuilder.getTableUri(clazz),null,null);
        }
        final SharedPreferences preferences = getSharedPreferences(Constants.APP_PREFERENCES, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}