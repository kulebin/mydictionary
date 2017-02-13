package lab.kulebin.mydictionary.app;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
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

import lab.kulebin.mydictionary.App;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.utils.ContextHolder;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = SignInActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 69;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MIN_KEYBOARD_HEIGHT = 200;

    private FirebaseAuth mFirebaseAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //temporary solution, flag will be deleted when firebase fixes its bug;
    private boolean isAuthStateChanged = true;
    private OnCompleteListener<AuthResult> mAuthResultOnCompleteListener;
    private SignInButton mSignInWithGoogleButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mSignInWithGoogleButton = (SignInButton) findViewById(R.id.sign_in_with_google_button);
        setSignInGoogleButtonText(mSignInWithGoogleButton, getString(R.string.BUTTON_SIGN_IN_WITH_GOGGLE));
        mSignInWithGoogleButton.setOnClickListener(this);

        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && isAuthStateChanged) {
                    isAuthStateChanged = false;
                    storeToken(user);
                }
            }
        };

        mAuthResultOnCompleteListener = new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    String message = task.getException().getMessage();
                    if (message == null) {
                        message = getString(R.string.ERROR_AUTHENTICATION_FAILED);
                    }
                    Toast.makeText(SignInActivity.this, message,
                            Toast.LENGTH_LONG).show();
                }
            }
        };

        setOnSoftKeyboardListener(findViewById(R.id.main_layout));
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.sign_in_with_google_button:
                signInWithGoogle();
                break;
            case R.id.email_create_account_button:
                createAccount();
                break;
            case R.id.email_sign_in_button:
                signInWithEmailPass();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, R.string.ERROR_CONNECTION_GENERAL, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, result.getStatus().getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setSignInGoogleButtonText(final SignInButton pSignInButton, final CharSequence pButtonText) {
        for (int i = 0; i < pSignInButton.getChildCount(); i++) {
            final View v = pSignInButton.getChildAt(i);

            if (v instanceof TextView) {
                final TextView tv = (TextView) v;
                tv.setText(pButtonText);
                return;
            }
        }
    }

    private void createAccount() {
        if (isDataValid()) {
            mFirebaseAuth.createUserWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                    .addOnCompleteListener(this, mAuthResultOnCompleteListener);
        }
    }

    private void signInWithEmailPass() {
        if (isDataValid()) {
            mFirebaseAuth.signInWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                    .addOnCompleteListener(this, mAuthResultOnCompleteListener);
        }
    }

    private void signInWithGoogle() {
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, R.string.ERROR_AUTHENTICATION_FAILED,
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
                        ((App) ContextHolder.get()).getTokenHolder().refreshToken(token);
                        startActivity(new Intent(SignInActivity.this, MainActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }
                } else {
                    Toast.makeText(SignInActivity.this, R.string.ERROR_AUTHENTICATION_FAILED,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isDataValid() {
        boolean isValid = true;
        if (!isValidEmail()) {
            isValid = false;
        }
        if (!isValidPassword()) {
            isValid = false;
        }
        return isValid;
    }

    private boolean isValidEmail() {
        final String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError(getString(R.string.HINT_ERROR_REQUIRED_FILLED_IN_FIELD));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailField.setError(getString(R.string.HINT_ERROR_INVALID_FIELD));
            return false;
        } else {
            mEmailField.setError(null);
            return true;
        }
    }

    private boolean isValidPassword() {
        final String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(getString(R.string.HINT_ERROR_REQUIRED_FILLED_IN_FIELD));
            return false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            mPasswordField.setError(getString(R.string.HINT_ERROR_SHORT_PASSWORD));
            return false;
        } else {
            mPasswordField.setError(null);
            return true;
        }
    }

    private void setOnSoftKeyboardListener(final View pRootView) {
        pRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                final Rect visibleFrame = new Rect();
                pRootView.getWindowVisibleDisplayFrame(visibleFrame);

                final int heightDiff = pRootView.getRootView().getHeight() - (visibleFrame.bottom - visibleFrame.top);
                if (heightDiff > MIN_KEYBOARD_HEIGHT) {
                    mSignInWithGoogleButton.setVisibility(View.GONE);
                } else {
                    mSignInWithGoogleButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}