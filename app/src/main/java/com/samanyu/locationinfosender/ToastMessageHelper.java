package com.samanyu.locationinfosender;

import android.content.Context;
import android.view.View;
import android.widget.Toast;


/** Basic Wrapper Helper Class Written To Display Toast Messages.  **/
public class ToastMessageHelper {
    private final Context mContext;
    private Toast mToastObject;

    public  ToastMessageHelper(Context context) {
        mContext = context;mToastObject = new Toast(mContext);
    }

    public void setBackgroundColor(int colour) {
        View currentView = mToastObject.getView();
        currentView.setBackgroundColor(colour);
    }

    public void showSmallDurationToast(String message) {
        mToastObject.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void showSmallDurationToast(int messageId) {
        String output = mContext.getString(messageId);
        showSmallDurationToast(output);
    }

    public void showLongDurationToast(String message) {
        mToastObject.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public void showLongDurationToast(int messageId) {
        String output = mContext.getString(messageId);
        showLongDurationToast(output);
    }
}
