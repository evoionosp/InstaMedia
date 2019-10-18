package com.developer.devshubhpatel.instamedia;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.developer.devshubhpatel.instamedia.fragment.AboutFragment;
import com.developer.devshubhpatel.instamedia.fragment.MediaFragment;
import com.developer.devshubhpatel.instamedia.fragment.SettingsFragment;
import com.developer.devshubhpatel.instamedia.utils.ClipboardService;
import com.developer.devshubhpatel.instamedia.utils.Constant;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.developer.devshubhpatel.instamedia.InitClass.fetchRemoteConfig;
import static com.developer.devshubhpatel.instamedia.InitClass.mAuth;
import static com.developer.devshubhpatel.instamedia.InitClass.mDatabaseRef;
import static com.developer.devshubhpatel.instamedia.InitClass.mFirebaseRemoteConfig;
import static com.developer.devshubhpatel.instamedia.InitClass.performClipBoardCheck;

/**
 * Created by patel on 23-04-2017.
 */

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static String TAG = "Main Activity";
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static boolean isServiceRunning = false;
    FirebaseUser mFirebaseUser;
    String mUsername, mPhotoUrl, mUserEmail;
    public static ClipboardManager cbm;
    public Toolbar mToolbar;
    public static NotificationManager notificationManager;
    InterstitialAd interstitialAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUsername = "Sign In";
        mUserEmail = " ";

        mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            if(!mFirebaseUser.isAnonymous()){
                mUserEmail = mFirebaseUser.getEmail();
                mUsername = mFirebaseUser.getDisplayName();
                if (mFirebaseUser.getPhotoUrl() != null) {
                    mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
                }
            }
            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("User_Media").child(mFirebaseUser.getUid());
        }

        setContentView(R.layout.main_activity);

        Context mContext = getApplicationContext();
        cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        initToolbar();
        initViewPagerAndTabs();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Prefs.getBoolean(Constant.FIRST_RUN, true)){
            Prefs.putBoolean(Constant.FIRST_RUN, false);
            startActivity(new Intent(this,ClipboardService.class));
        }




        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();

                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };


        InitClass.ShowInternetStatus(mToolbar);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());

        //startService(new Intent(MainActivity.this, ClipboardService.class));

        checkPermission(this);



        performClipBoardCheck(cbm);
        notificationManager.cancel(Constant.NOTIFICATION_OPEN_IM_ID);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.app_name));
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
    }

    private void initViewPagerAndTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new MediaFragment(), getString(R.string.media));
        //Bundle bundle = new Bundle();
        //bundle.putStringArrayList(ImageGalleryActivity.KEY_IMAGES, getImageList());
        //bundle.putString(ImageGalleryActivity.KEY_TITLE, "Unsplash Images");
        //pagerAdapter.addFragment(ImageGalleryFragment.newInstance(bundle),"Downloaded");
        pagerAdapter.addFragment(new SettingsFragment(), getString(R.string.settings));
        pagerAdapter.addFragment(new AboutFragment(), getString(R.string.about));
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }




    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        if(mFirebaseRemoteConfig.getBoolean("show_fullscreen_ad_enter"))
            if (interstitialAd.isLoaded()) {
                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        super.onAdFailedToLoad(errorCode);
                    }
                });
                interstitialAd.show();
            }

    }

    @Override
    protected void onResume() {
        performClipBoardCheck(cbm);
        if(mFirebaseRemoteConfig.getBoolean("show_fullscreen_ad_enter"))
            if (interstitialAd.isLoaded()) {
                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        super.onAdFailedToLoad(errorCode);
                    }
                });
                interstitialAd.show();
            }
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        Log.i("PERMISSIONS", "Permission Granted");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        Log.i("PERMISSIONS", "Permission Denied");
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Log.i("PERMISSIONS", "Permission Denied Permanently");
            new AppSettingsDialog.Builder(this).build().show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_open_insta) {
            openInstagram();
            return true;
        }
        if (id == R.id.action_refresh) {
            performClipBoardCheck(cbm);
            fetchRemoteConfig(60);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openInstagram(){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        } else {
            showSnackbar(mToolbar,"Instagram not Installed on Device",Snackbar.LENGTH_SHORT);
        }
    }

    public static void showSnackbar(View view, String message, int length) {
        Snackbar.make(view, message, length).show();
    }

    public static void showToast(Context context, String message, int length) {
        Toast.makeText(context, message, length).show();
    }

    public static void checkPermission(Activity activity) {
        int resultWR = ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int resultRD = ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (resultWR == PackageManager.PERMISSION_GRANTED && resultRD == PackageManager.PERMISSION_GRANTED)
            return;
        else {
            EasyPermissions.requestPermissions(activity, "Storage access required to download Photos and Videos", 1001,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

    }

    @Override
    public void onBackPressed() {
        if(mFirebaseRemoteConfig.getBoolean("show_fullscreen_ad_exit")) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        finish();
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        super.onAdFailedToLoad(errorCode);
                        finish();
                    }
                });
                interstitialAd.show();
            } else {
                super.onBackPressed();
            }
        } else{
            super.onBackPressed();
        }
    }
}