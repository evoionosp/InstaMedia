package com.developer.devshubhpatel.instamedia.utils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.developer.devshubhpatel.instamedia.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Retrofit;

import static com.developer.devshubhpatel.instamedia.utils.Constant.MESSAGE_PROGRESS;


public class DownloadService extends IntentService {

    public DownloadService() {
        super("Download Service");
    }

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private int totalFileSize;
    public boolean notify = true;


    @Override
    protected void onHandleIntent(Intent intent) {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);




        notify = intent.getBooleanExtra("notify",true);

        if(notify){
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                    .setContentTitle("InstaMedia")
                    .setContentText("Downloading File")
                    .setAutoCancel(true);
            notificationManager.notify(0, notificationBuilder.build());
        }

        initDownload(intent.getStringExtra("fileName"),intent.getStringExtra("fileURL"));

    }

    private void initDownload(String fileName, String fileURL){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.base.url")
                .build();

        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        Call<ResponseBody> request = retrofitInterface.downloadFile(fileURL);
        try {

            downloadFile(request.execute().body(),fileName);

        } catch (IOException e) {

            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();

        }
    }

    private void downloadFile(ResponseBody body,String fileName) throws IOException {

        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_PICTURES+"/InstaMedia");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("FILE SAVE","Directory Path doesn't exists !");
            }
        }
        File outputFile = new File(mediaStorageDir, fileName);
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            totalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(totalFileSize);

            if (currentTime > 1000 * timeCount) {

                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                if(notify) sendNotification(download);
                timeCount++;
            }

            output.write(data, 0, count);
        }
        onDownloadComplete();
        output.flush();
        output.close();
        bis.close();

    }

    private void sendNotification(Download download){

        sendIntent(download);
        notificationBuilder.setProgress(100,download.getProgress(),false);
        notificationBuilder.setContentText(String.format("Downloading... %d/%d MB",download.getCurrentFileSize(),download.getTotalFileSize()));
        notificationManager.notify(0, notificationBuilder.build());
    }


    private void sendIntent(Download download){

        Intent intent = new Intent(MESSAGE_PROGRESS);
        intent.putExtra("download",download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    private void onDownloadComplete(){

        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);


        notificationManager.cancel(0);
        if(notify){
            notificationBuilder.setProgress(0,0,false);
            notificationBuilder.setContentText("Download Completed !");
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(0);
    }

}
