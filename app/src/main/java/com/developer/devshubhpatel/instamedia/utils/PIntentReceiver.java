package com.developer.devshubhpatel.instamedia.utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;;
import android.widget.Toast;

import com.developer.devshubhpatel.instamedia.R;

public class PIntentReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String str = intent.getAction();
        int s = intent.getIntExtra("INTENT_ACTION",0);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        switch(str){
            case Constant.ACTION_STOP :
                context.stopService(new Intent(context, ClipboardService.class));
                Toast.makeText(context, R.string.service_stop,Toast.LENGTH_LONG).show();
                notificationManager.cancel(Constant.SERVICE_NOTIFICATION_ID);
                break;
            case Constant.ACTION_OPEN_IMEDIA :
                Intent appIntent = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setClassName("com.developer.devshubhpatel.instamedia", "com.developer.devshubhpatel.instamedia.MainActivity");
                context.startActivity(appIntent);
                notificationManager.cancel(Constant.NOTIFICATION_OPEN_IM_ID);
                break;
            default:
                break;
        }
    }
}
