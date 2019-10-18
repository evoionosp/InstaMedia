package com.developer.devshubhpatel.instamedia.utils;

import android.app.Application;
import android.os.Environment;

import java.io.File;

/**
 * Created by patel on 03-06-2016.
 */
public class Constant {

    public static String FIRST_RUN = "boolean_first_time_app_run";
    public static String SHOW_NOTIFICATION = "show_service_notification";

    public static final int SERVICE_NOTIFICATION_ID = 1259;
    public static final int NOTIFICATION_OPEN_IM_ID = 1444;
    public static final String  ACTION_STOP = "com.developer.devshubhpatel.instamedia.STOP";
    public static final String ACTION_OPEN_IMEDIA = "com.developer.devshubhpatel.instamedia.OPENIM";

    public static String MESSAGE_PROGRESS = "message_progress";


    public static final int  TYPE_ERROR = -1 ;
    public static final int  TYPE_IMG = 0 ;
    public static final int  TYPE_VID = 1 ;
    public static final int  TYPE_CAR = 2 ;


    public static final File TEMP_DIR = new File(Environment.getExternalStorageDirectory()+"/Android/data/com.developer.devshubhpatel.instamedia");
    public static final File SAVE_DIR = new File(Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_PICTURES+"/InstaMedia");



}

