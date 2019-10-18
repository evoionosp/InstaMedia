/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.developer.devshubhpatel.instamedia;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.developer.devshubhpatel.instamedia.utils.Constant;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.pixplicity.easyprefs.library.Prefs;


import static com.developer.devshubhpatel.instamedia.InitClass.mAuth;


public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mFirebaseAuth;


    private GoogleApiClient mGoogleApiClient;

    MaterialDialog md;


    TextView tvSkip;

    SignInButton mSignInButton;


    // Firebase instance variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        tvSkip = (TextView) findViewById(R.id.tv_skip_signin);
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);


        if(Prefs.getBoolean(Constant.FIRST_RUN, true)){
            startActivity(new Intent(SignInActivity.this,IntroActivity.class));
//            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//            ClipData clip = ClipData.newPlainText("instagram_link", "https://www.instagram.com/p/BS1H0FDjgl5/");
//            clipboard.setPrimaryClip(clip);
            finish();
        }

        md = new MaterialDialog.Builder(this)
                .title(R.string.sign_in)
                .content(R.string.please_wait)
                .progress(true, 0)
                .theme(Theme.DARK)
                .progressIndeterminateStyle(true)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .build();


        // Set click listeners
        mSignInButton.setOnClickListener(this);
        tvSkip.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

    }
    @Override
    public void onClick(View v) {
        if(InitClass.ShowInternetStatus(v)){
            switch (v.getId()) {
                case R.id.sign_in_button:
                    signIn();
                    break;
                case R.id.tv_skip_signin:
                    signInAnonymously();
                    break;
            }
        }

    }
    private void signIn() {
        if(mAuth.getCurrentUser() == null ||mAuth.getCurrentUser().isAnonymous()) {
            md.show();
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {

                md.dismiss();
                Snackbar snackbar = Snackbar
                        .make(mSignInButton, "Sign In Failed !", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(final View view) {
                                signIn();
                            }
                        });
                snackbar.show();
                Log.e(TAG, "Google Sign In failed. : "+result.getStatus());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        if(mAuth.getCurrentUser() == null) {
            mFirebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            md.dismiss();
                            if (!task.isSuccessful()) {

                                Snackbar snackbar = Snackbar
                                        .make(mSignInButton, "Authentication Failed !", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("RETRY", new View.OnClickListener() {
                                            @Override
                                            public void onClick(final View view) {
                                                signIn();
                                            }
                                        });
                                snackbar.show();
                                Log.w(TAG, "signInWithCredential", task.getException());
                            } else {
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                finish();
                            }
                        }
                    });
        }else{
            Log.w(TAG, "Linking To anonymus started");
            mAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            md.dismiss();
                            if (task.isSuccessful()) {
                                Log.w(TAG, "linkWithCredential:Success");
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                finish();

                            } else {
                                Log.w(TAG, "linkWithCredential:failure", task.getException());
                                try {
                                    if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Log.w(TAG,"FirebaseAuthUserCollisionException Dialog");
                                        new MaterialDialog.Builder(SignInActivity.this)
                                                .title(R.string.user_linked_msg)
                                                .content(R.string.user_linked_cont_msg)
                                                .positiveText(R.string.sign_out)
                                                .positiveColor(getResources().getColor(R.color.colorRed))
                                                .negativeText(R.string.cancel)
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        mAuth.signOut();
                                                    }
                                                })
                                                .show();
                                        return;
                                    }
                                }catch (Exception e){
                                    Log.e(TAG,"FirebaseAuthUserCollisionException Class match error"+e.getLocalizedMessage());
                                }
                                Snackbar snackbar = Snackbar
                                        .make(mSignInButton, "Authentication Failed !", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("RETRY", new View.OnClickListener() {
                                            @Override
                                            public void onClick(final View view) {
                                                signIn();
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    public void signInAnonymously(){
        md.show();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Snackbar snackbar = Snackbar
                                    .make(mSignInButton, "Authentication Failed !", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("RETRY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(final View view) {
                                            signIn();
                                        }
                                    });
                        }else{
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        }
                        md.dismiss();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Snackbar.make(mSignInButton,"Google Play Services error.",Snackbar.LENGTH_LONG).show();
        //Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
