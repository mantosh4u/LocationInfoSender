package com.samanyu.locationinfosender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.List;

/** General Purpose Helper/Utility Class **/
public class MyUtilityClass {

    public static boolean isIntentSafeToUse(Context context, Intent intent) {
        boolean output = false;
        PackageManager packageManager = context.getPackageManager();
        List activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        output = activities.size() > 0;
        return output;
    }

    public static boolean checkPermissionAndAskIfRequired(Context  activityContext,
                                                          Activity currentActivity,
                                                          String permissionString,
                                                          int returnCode)
    {
        boolean output = false;

        int permissionCheck = ContextCompat.checkSelfPermission(activityContext,permissionString);
        if(permissionCheck == PackageManager.PERMISSION_DENIED)
        {
            /** As this is Permission is not granted to this app, need to request for this **/
            /** Should we show an explanation? **/
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (currentActivity, permissionString)) {

            }
            else
            {
                ActivityCompat.requestPermissions(currentActivity,
                        new String[]{permissionString},
                        returnCode);
            }
        }
        else if (permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
            /** Nothing To Do Here **/
            output = true;
        }

        return  output;
    }

    public static void handleException(Exception exception) {
        String out = exception.getMessage().toString();
        exception.printStackTrace();
    }

}
