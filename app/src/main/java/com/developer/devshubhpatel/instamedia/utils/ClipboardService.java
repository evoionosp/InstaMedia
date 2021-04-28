package com.developer.devshubhpatel.instamedia.utils;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.developer.devshubhpatel.instamedia.R;
import com.developer.devshubhpatel.instamedia.models.FMedia;
import com.developer.devshubhpatel.instamedia.models.IMedia;
import com.developer.devshubhpatel.instamedia.models.Node;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

import static com.developer.devshubhpatel.instamedia.InitClass.mDatabaseRef;
import static com.developer.devshubhpatel.instamedia.utils.Constant.SHOW_NOTIFICATION;

public class ClipboardService extends Service {
    NotificationManager notificationManager;
    NotificationCompat.Builder builder = null;
    public static ClipboardManager cbm;
    Context context = this;
    public static String TAG = "ClipboardService";
    int count = 0;

    private ClipboardManager.OnPrimaryClipChangedListener
            listener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            performClipBoardCheck(cbm);
        }
    };

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                .addPrimaryClipChangedListener(listener);

        mDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                count++;
                showLiteNotification(count);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        intent = new Intent(context, ClipboardService.class);
        count = 0;
        performClipBoardCheck(cbm);
        if(Prefs.getBoolean(SHOW_NOTIFICATION,true))
            showServiceRunNotification();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(Constant.SERVICE_NOTIFICATION_ID);
        super.onDestroy();
    }

    public static String getShortCode(String url) {
        Matcher m = Pattern.compile("www.instagram.com/p/([^/]+)").matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return "ERROR";
    }

    private void performClipBoardCheck(ClipboardManager cbm) {

        if (cbm.hasPrimaryClip()) {
            ClipData cd = cbm.getPrimaryClip();
            if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                String InstaURL = cd.getItemAt(0).getText().toString();
                if (!getShortCode(InstaURL).equals("ERROR")) {
                    initJsonRequest(getShortCode(InstaURL));
                }
            }
        }
    }


    private void initJsonRequest(String code) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.instagram.com/p/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        retrofit2.Call<IMedia> call = retrofitInterface.JsonResponseURL(code);

        call.enqueue(new retrofit2.Callback<IMedia>() {
            @Override
            public void onResponse(retrofit2.Call<IMedia> call, retrofit2.Response<IMedia> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "server contacted and received Response");
                    mediaValidate(response.body());
                } else {
                    Log.e(TAG, "server contact Failed");
                    // Snackbar.make(mToolbar, "Response Unsuccessful !", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<IMedia> call, Throwable t) {
                Log.e(TAG, "error");
                //Snackbar.make(mToolbar, "Server Connection Failed !", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private  int mediaValidate(IMedia media) {
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


    private List<FMedia> toFMediaCAR(IMedia media) {

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

    private FMedia toFMedia(IMedia media) {
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
        }
        return fMedia;
    }

    private FMedia toFMedia(Node node, FMedia fMedia) {

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

    private void writeToFirebase(FMedia fMedia) {
        if (fMedia != null) {
            if(mDatabaseRef != null) {

                mDatabaseRef.child(fMedia.getShortCode()).setValue(fMedia);
                    //Activity in not foreground, broadcast intent

            }
            else
                Log.e(TAG,"Firebase Database Ref Null");
        }
    }

    public void showServiceRunNotification(){
        Intent stopIntent = new Intent();
        stopIntent.setAction(Constant.ACTION_STOP);
        PendingIntent piStop = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder n = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("InstaMedia")
                .setContentText("Auto Save Service is Running ...")
                .setOngoing(true)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_cancel_white_24dp, "STOP", piStop);
        notificationManager.notify(Constant.SERVICE_NOTIFICATION_ID, n.build());
    }


    public void showLiteNotification(int count){
        Intent openIntent = new Intent();
        openIntent.setAction(Constant.ACTION_OPEN_IMEDIA);
        PendingIntent piOpenIM = PendingIntent.getBroadcast(ClipboardService.this, (int) System.currentTimeMillis(), openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Notification nm = new Notification(context,"InstaMedia","Message",piOpenIM);
        //nm.con

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(count+" Items Saved to InstaMedia")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_open_in_new_white_24dp,
                        getString(R.string.open_imstamedia), piOpenIM)
                .setNumber(count);

        notificationManager.notify(Constant.NOTIFICATION_OPEN_IM_ID, builder.build());
    }

 /*   public void ShowNotification(int type, String author, int img_count) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication());
        Intent dismissIntent = new Intent();
        dismissIntent.setAction(Constant.ACTION_DISMISS);
        PendingIntent piDismiss = PendingIntent.getBroadcast(ClipboardService.this, (int) System.currentTimeMillis(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent downloadIntent = new Intent();
        downloadIntent.setAction(Constant.ACTION_DOWNLOAD);
        PendingIntent piDownload = PendingIntent.getBroadcast(ClipboardService.this, (int) System.currentTimeMillis(), downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        switch (type) {
            case Constant.DOWNLOAD_NOTIFICATION_IMG_ID:

                builder = new NotificationCompat.Builder(getApplication())
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("Download Image")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .addAction(R.drawable.ic_file_download_white_24dp,
                                getString(R.string.download), piDownload)
                        .addAction(R.drawable.ic_share_white_24dp,
                                getString(R.string.download), piDownload);
                notificationManager.notify(Constant.DOWNLOAD_NOTIFICATION_IMG_ID, builder.build());

                break;
            case Constant.DOWNLOAD_NOTIFICATION_VID_ID:

                builder = new NotificationCompat.Builder(getApplication())
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("Download Image")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .addAction(R.drawable.ic_file_download_white_24dp,
                                getString(R.string.download), piDownload)
                        .addAction(R.drawable.ic_share_white_24dp,
                                getString(R.string.download), piDownload);
                notificationManager.notify(Constant.DOWNLOAD_NOTIFICATION_IMG_ID, builder.build());

                break;
            case Constant.DOWNLOAD_NOTIFICATION_CAR_ID:

                builder = new NotificationCompat.Builder(getApplication())
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(getString(R.string.app_name))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .addAction(R.drawable.ic_file_download_white_24dp,
                                getString(R.string.download), piDownload)
                        .addAction(R.drawable.ic_share_white_24dp,
                                getString(R.string.download), piDownload);
                notificationManager.notify(Constant.DOWNLOAD_NOTIFICATION_CAR_ID, builder.build());

                break;
            case Constant.LOADING_NOTIFICATION_ID:
                builder = new NotificationCompat.Builder(getApplication())
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("Fatching Media Info")
                        .setProgress(0, 0, true)
                        .setOngoing(true);
                notificationManager.notify(Constant.LOADING_NOTIFICATION_ID, builder.build());
                break;
            case Constant.RETRY_NOTIFICATION_ID:
                Intent retryIntent = new Intent();
                retryIntent.setAction(Constant.ACTION_RETRY);
                PendingIntent piRetry = PendingIntent.getBroadcast(ClipboardService.this, (int) System.currentTimeMillis(), retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("Faild to Download Media")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .addAction(R.drawable.ic_history_black_24dp,
                                getString(R.string.retry), piRetry);
                notificationManager.notify(Constant.RETRY_NOTIFICATION_ID, builder.build());
                break;
            case Constant.SERVICE_NOTIFICATION_ID:
                Intent stopIntent = new Intent();
                stopIntent.setAction(Constant.ACTION_STOP);
                PendingIntent piStop = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                Notification n = new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("InstaMedia Service")
                        .setContentText("Background Service is running")
                        .setOngoing(true)
                        .addAction(R.drawable.ic_cancel_black_24dp, "STOP", piStop).build();
                notificationManager.notify(Constant.SERVICE_NOTIFICATION_ID, n);
                break;
            default:
                break;
        }

    } */
}