package com.developer.devshubhpatel.instamedia;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.View;

import com.developer.devshubhpatel.instamedia.models.FMedia;
import com.developer.devshubhpatel.instamedia.models.IMedia;
import com.developer.devshubhpatel.instamedia.models.Node;
import com.developer.devshubhpatel.instamedia.utils.Constant;
import com.developer.devshubhpatel.instamedia.utils.RetrofitInterface;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.developer.devshubhpatel.instamedia.MainActivity.showSnackbar;


/**
 * Created by patel on 06-07-2016.
 */
public class InitClass extends Application {
    static Object object;
    public static FirebaseRemoteConfig mFirebaseRemoteConfig;
    public static FirebaseAuth mAuth;
    public static DatabaseReference mDatabaseRef;
    public static FirebaseUser mFirebaseUser;

    public static String TAG = "InitClass";


    @Override
    public void onCreate() {
        object = getSystemService(Context.CONNECTIVITY_SERVICE);
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mFirebaseUser = mAuth.getCurrentUser();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        if(Prefs.getBoolean(Constant.FIRST_RUN, true)){

            //fetchRemoteConfig(43200);
        }

        fetchRemoteConfig(60);

        if (mFirebaseUser != null) {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("User_Media").child(mFirebaseUser.getUid());
        }

        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.admob_app_id));


    }

    public static void fetchRemoteConfig(long expireDelay){
        mFirebaseRemoteConfig.fetch(expireDelay).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                   // Toast.makeText(getApplicationContext(), "Fetch Succeeded",Toast.LENGTH_SHORT).show();
                    Log.e("Firebase Remote Config", "Fetch Success");

                    // Once the config is successfully fetched it must be activated before newly fetched
                    // values are returned.
                    mFirebaseRemoteConfig.activateFetched();
                } else {
                    Log.e("Firebase Remote Config", "Fetch Failed");
                }
            }

        });
    }



    protected static boolean isInternetConnected() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) object;
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    public static boolean ShowInternetStatus(View view) {
        if (!isInternetConnected()) {
            Snackbar snackbar = Snackbar
                    .make(view, "Can't Connect Right Now", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            new Thread() {
                                @Override
                                public void run() {
                                    if (!ShowInternetStatus(view)) {
                                        Snackbar.make(view, "Connected to Network", Snackbar.LENGTH_SHORT);
                                    }
                                }
                            }.start();
                        }
                    });
            snackbar.show();
        }
        return isInternetConnected();
    }

    public static String getShortCode(String url) {
        Matcher m = Pattern.compile("www.instagram.com/p/([^/]+)").matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return "ERROR";
    }

    public static String performClipBoardCheck(ClipboardManager cbm) {
        String code = "ERROR";

        if (cbm.hasPrimaryClip()) {
            ClipData cd = cbm.getPrimaryClip();
            if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                String InstaURL = cd.getItemAt(0).getText().toString();

                if (!getShortCode(InstaURL).equals("ERROR")) {
                    code = getShortCode(InstaURL);
                    initJsonRequest(getShortCode(InstaURL));
                }
            }
        }
        return code;
    }


    private static void initJsonRequest(String code) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.instagram.com/p/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        Call<IMedia> call = retrofitInterface.JsonResponseURL(code);

        call.enqueue(new Callback<IMedia>() {
            @Override
            public void onResponse(Call<IMedia> call, Response<IMedia> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "server contacted and received Response");
                    mediaValidate(response.body());
                } else {
                    Log.e(TAG, "server contact Failed");
                    // Snackbar.make(mToolbar, "Response Unsuccessful !", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<IMedia> call, Throwable t) {
                Log.e(TAG, "error");
                //Snackbar.make(mToolbar, "Server Connection Failed !", Snackbar.LENGTH_LONG).show();
            }
        });
    }





    public static int mediaValidate(IMedia media) {
        try {
            if (media != null)
                if (media.getGraphql() != null)
                    if (media.getGraphql().getShortcodeMedia().getTypename() != null) {
                        switch (media.getGraphql().getShortcodeMedia().getTypename()) {
                            case "GraphSidecar":
                                toFMediaCAR(media);
                                return Constant.TYPE_CAR;
                            case "GraphVideo":
                                toFMedia(media);
                                return Constant.TYPE_VID;
                            case "GraphImage":
                                toFMedia(media);
                                return Constant.TYPE_IMG;
                            default:
                                return Constant.TYPE_ERROR;
                        }
                    }
            return Constant.TYPE_ERROR;
        } catch (Exception e) {
            Log.e("Media Check", e.getMessage());
            return Constant.TYPE_ERROR;
        }
    }

    public static List<FMedia> toFMediaCAR(IMedia media) {

        List<FMedia> listFmedia = new ArrayList<FMedia>();

        FMedia fMedia = new FMedia();

        fMedia.setProfileURL(media.getGraphql().getShortcodeMedia().getOwner().getProfilePicUrl());
        fMedia.setAuthorId(media.getGraphql().getShortcodeMedia().getOwner().getUsername());
        fMedia.setFullName(media.getGraphql().getShortcodeMedia().getOwner().getFullName());

        int totaledges = media.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren().getEdges().size();
        for (int i = 0; i < totaledges; i++) {
            FMedia tmp = toFMedia(media.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren().getEdges().get(i).getNode(), fMedia);
            if (tmp != null)
                listFmedia.add(tmp);
        }
        return listFmedia;
    }

    public static FMedia toFMedia(IMedia media) {
        FMedia fMedia = new FMedia();
        try {
            fMedia.setMediaId(media.getGraphql().getShortcodeMedia().getId());
            fMedia.setShortCode(media.getGraphql().getShortcodeMedia().getShortcode());


            fMedia.setProfileURL(media.getGraphql().getShortcodeMedia().getOwner().getProfilePicUrl());
            fMedia.setAuthorId(media.getGraphql().getShortcodeMedia().getOwner().getUsername());
            fMedia.setFullName(media.getGraphql().getShortcodeMedia().getOwner().getFullName());


            fMedia.setDisplayURL(media.getGraphql().getShortcodeMedia().getDisplayUrl());
            fMedia.setVideo(media.getGraphql().getShortcodeMedia().getIsVideo());
            if (fMedia.isVideo())
                fMedia.setDownloadURL(media.getGraphql().getShortcodeMedia().getVideoUrl());

            fMedia.setHeight(media.getGraphql().getShortcodeMedia().getDimensions().getHeight());
            fMedia.setWidth(media.getGraphql().getShortcodeMedia().getDimensions().getWidth());
            fMedia.setStar(false);

            Log.e("FMEDIA CONVERT", "SUCCESS");

        } catch (Exception e) {
            fMedia = null;
            Log.e("FMEDIA CONVERT", "Error " + e.getLocalizedMessage());

        } finally {
            if (fMedia != null)
                writeToFirebase(fMedia);
            return fMedia;
        }
    }

    public static FMedia toFMedia(Node node, FMedia fMedia) {

        try {
            fMedia.setMediaId(node.getId());
            fMedia.setShortCode(node.getShortcode());


            fMedia.setDisplayURL(node.getDisplayUrl());
            fMedia.setVideo(node.getIsVideo());
            if (fMedia.isVideo())
                fMedia.setDownloadURL(node.getVideoUrl());

            fMedia.setHeight(node.getDimensions().getHeight());
            fMedia.setWidth(node.getDimensions().getWidth());
            fMedia.setStar(false);

            Log.e("FMEDIA CONVERT", "SUCCESS");

        } catch (Exception e) {
            fMedia = null;
            Log.e("FMEDIA CONVERT", "Error " + e.getLocalizedMessage());

        } finally {
            if (fMedia != null)
                writeToFirebase(fMedia);
            return fMedia;
        }

    }

    public static void writeToFirebase(FMedia fMedia) {
        if (fMedia != null) {
            if(mDatabaseRef != null)
                mDatabaseRef.child(fMedia.getShortCode()).setValue(fMedia);
            else
                Log.e(TAG,"Firebase Database Ref Null");
        }
    }






    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }


}
