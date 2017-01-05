package com.samanyu.locationinfosender;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

/**
 * Based on my understanding, MainActivity act like FACADE pattern/class. This class
 * would use all other modules in some way to accomplish the task. This probably would
 * means this class to be less reusable in somewhere else.
 */

/**
 * Steps followed to create the layout/steps for this activity.
 * 1. Define The Buttons to START/STOP this app.
 * 2. Add the support for the Application Bar in this activity.
 * 3. Ask and verify the runtime permissions which are must to have.
 * 4. If everything works fine, initiates/start the service.
 * 5. After this, it is not supposed to do anything.
 */

public class MainActivity extends AppCompatActivity {

    // Define various START/STOP button and other useful widgets handles.
    private ImageButton  mStartApp;
    private ImageButton  mStopApp;
    private View         mMainCompleteWindowView;

    // This would be used in the callback of call requestPermissions
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 111;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 222;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 333;
    // Whether activity does have critical runtime permission to work
    private boolean mIsAccessFineLocation = false;
    private boolean mIsAccessCoarseLocation = false;
    private boolean mIsSendSMS  = false;
    // Whether Service has already started once user pressed the button.
    private static boolean mIsServiceStarted = false;
    private ToastMessageHelper mToastMessageHelper;

    private static long sCachedCurrentTimeOutValue = 0;
    private static String sCachedCurrentSelectedApp;

    // Check about network and GPS is connected or not so that we can display appropriate message
    // to user and accordingly he can ON/OFF.
    private ConnectivityManagerHelper  mConnectivityManagerHelper = null;
    private LocationManagerHelper      mLocationManagerHelper     = null;

    private String getCurrentPreferenceValue(String keyValue, String defaultValue) {
        String output = null;
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        output = sharedPreferences.getString(keyValue, defaultValue);
        return output;
    }

    private boolean getCurrentPreferenceValue(String keyValue, boolean defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        boolean output = sharedPreferences.getBoolean(keyValue, defaultValue);
        return output;
    }

    /**
     * This method basically takes care about whether user had changed some setting
     * and resumed back to application main screen. As of now it is only timeout value
     * changes would trigger the restart, however in future when we would have more settings
     * we might require to read all those information on by one and check if any of these are
     * not as old one, probably we might require to restart the service.
     */
    private boolean whetherRestartService() {
        boolean outputTimeOutValue = false;
        boolean outputCurrentSelectedApp = false;

        String keyScreenTimeOutName = getString(R.string.sms_frequency_key);
        String currentTimeOut = getCurrentPreferenceValue(keyScreenTimeOutName,
                String.valueOf(SettingsFragment.FALLBACK_SCREEN_TIMEOUT_VALUE));
        long CurrentTimeOutValue = (long)Integer.parseInt(currentTimeOut);
        if(CurrentTimeOutValue != sCachedCurrentTimeOutValue) {
            outputTimeOutValue = true;
            sCachedCurrentTimeOutValue = CurrentTimeOutValue;
        }

        String keySelectedAppPackageName = getString(R.string.key_selected_app);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String CurrentSelectedApp = sharedPreferences.getString(keySelectedAppPackageName, null);
        if(!TextUtils.isEmpty(CurrentSelectedApp)) {
            if (!CurrentSelectedApp.equals(sCachedCurrentSelectedApp)) {
                outputCurrentSelectedApp = true;
                sCachedCurrentSelectedApp = CurrentSelectedApp;
            }
        }
        return (outputTimeOutValue||outputCurrentSelectedApp);
    }

    private boolean handleAPermission(String Permission, int outputCode) {
        boolean output = false;
        output = MyUtilityClass.checkPermissionAndAskIfRequired
                (getApplicationContext(),
                        MainActivity.this,
                        Permission,
                        outputCode);
        return output;
    }

    /**
     * This method takes care regarding the startup permission which is must to have for this
     * particular application. Later on based on some settings, we can ask for more permission.
     */
    private void handlePermissionsForStartup() {
        mIsAccessFineLocation = handleAPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                MY_PERMISSIONS_REQUEST_LOCATION);
        mIsAccessCoarseLocation = handleAPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
                MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
    }

    private void startServiceCommand(){
        if(mIsServiceStarted == false) {
            Intent startService = new Intent(getApplicationContext(),
                    CurrentLocationTrackerService.class);
            mIsServiceStarted = true;
            startService(startService);
        } else {
            //mToastMessageHelper.showLongDurationToast(R.string.already_service_started);
        }
    }

    private void stopServiceCommand(){
        if(mIsServiceStarted == true) {
            Intent stopService = new Intent(getApplicationContext(),
                    CurrentLocationTrackerService.class);
            mIsServiceStarted = false;
            //mToastMessageHelper.showLongDurationToast(R.string.stop_service);
            stopService(stopService);
        }

    }

    private void restartServiceCommand() {
        stopServiceCommand();
        startServiceCommand();
    }

    /**
     * If SMS is enabled by the user, now I should ask to enable the permission for this.
     * @return Whether SMS is enabled or not.
     */
    private boolean isSMSEnabledByUser() {
        String keyWhetherToSendSMSName = getString(R.string.whether_to_send_SMS_key);
        boolean keyWhetherToSendSMS = getCurrentPreferenceValue(keyWhetherToSendSMSName,
                SettingsFragment.WHETHER_TO_SEND_SMS_VALUE);
        return keyWhetherToSendSMS;
    }

    private void startAppEventHandler(View view) {
        if(((mIsAccessFineLocation || mIsAccessCoarseLocation))) {
            startServiceCommand();
        } else {
            // This condition represent that, somebody wants to start the tracking
            // without providing complete permission, hence here we should again call
            // of handlePermissions.
            mToastMessageHelper.showLongDurationToast(R.string.cannt_start_service);
        }
    }

    private void stopAppEventHandler(View view) {
        stopServiceCommand();
    }


    private AlertDialog initializeDialogWindowLogicForDataNetwork() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        // Set content for this window
        alertBuilder.setMessage(R.string.cellulardata_on);
        alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Intent/Action for data to ON.
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                if(MyUtilityClass.isIntentSafeToUse(getApplicationContext(),intent) == true) {
                    startActivity(intent);
                }else  {
                    //Display Some popup so that user would manually go there and change the setting
                }
            }
        });
        alertBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // There is nothing to do here.
            }
        });
        return  alertBuilder.create();
    }

    private AlertDialog initializeDialogWindowLogicForLocationProvider() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        // Set content for this window
        alertBuilder.setMessage(R.string.location_service_on);
        alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Intent/Action for Location Provider to ON.
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                if(MyUtilityClass.isIntentSafeToUse(getApplicationContext(),intent) == true) {
                    startActivity(intent);
                }else  {
                    //Display Some popup so that user would manually go there and change the setting
                }
            }
        });
        alertBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //There is nothing to do here.
            }
        });
        return  alertBuilder.create();
    }


    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    // Based on User Menu Item Selection, execute different logic
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionSettings:
                // User chose the "Settings" item, show the app settings UI...
                Intent launchSettingActivity = new Intent(getApplicationContext(),
                        SettingActivity.class);
                startActivity(launchSettingActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //Helper method which would return the value based on whether permission is granted or not.
    private boolean isPermissionGranted(int[] grantResults){
        boolean out = false;
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            out = true;
        }
        return out;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults)  {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION:
                if(isPermissionGranted(grantResults)) {
                    mIsAccessCoarseLocation = true;
                }
                break;
            case MY_PERMISSIONS_REQUEST_LOCATION:
                if(isPermissionGranted(grantResults)) {
                    mIsAccessFineLocation = true;
                }
                break;
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
                if(isPermissionGranted(grantResults)) {
                    mIsSendSMS = true;
                }
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainCompleteWindowView = findViewById(R.id.MainLayoutContainer);

        mStartApp = (ImageButton)findViewById(R.id.startApp);
        mStopApp  = (ImageButton)findViewById(R.id.stopApp);

        mToastMessageHelper = new ToastMessageHelper(getApplicationContext());

        //Write event handlers for above grabbed buttons.
        mStartApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAppEventHandler(view);
            }
        });

        mStopApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAppEventHandler(view);
            }
        });

        // Support for the ActionBar widgets menu in current activity. If theme already has
        // support to ToolBar widget, then setting it again throws the exception which can
        // be ignored.
        Toolbar toolbarView = (Toolbar)findViewById(R.id.myToolbar);
        try {
            if (toolbarView != null) {
                setSupportActionBar(toolbarView);
            }
        }catch (Exception exception) {
            String msg = exception.getMessage();
        }

        handlePermissionsForStartup();
        String keyScreenTimeOutName = getString(R.string.sms_frequency_key);
        String currentTimeOut = getCurrentPreferenceValue(keyScreenTimeOutName,
                String.valueOf(SettingsFragment.FALLBACK_SCREEN_TIMEOUT_VALUE));
        sCachedCurrentTimeOutValue = (long)Integer.parseInt(currentTimeOut);

        String keySelectedAppPackageName = getString(R.string.key_selected_app);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        sCachedCurrentSelectedApp = sharedPreferences.getString(keySelectedAppPackageName, null);

        mConnectivityManagerHelper = new ConnectivityManagerHelper(getApplicationContext());
        mLocationManagerHelper = new LocationManagerHelper(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Help GC to collect these stuff.
        mConnectivityManagerHelper = null;
        mLocationManagerHelper = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean output = whetherRestartService();
        // If something has changed which required to restart the service(if it is already started)
        // ,first we need to stop the service and then start it again.
        if(output && mIsServiceStarted) {
            //restartServiceCommand();
            new RestartServiceTask().execute();
        }
    }

    private AlertDialog initializeDialogForSpinningBar() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        // Get the layout inflater and and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = this.getLayoutInflater();
        final View entryView = inflater.inflate(R.layout.progressbar_display, null);
        alertBuilder.setView(entryView);
        // Initialize Message & Title for this
        alertBuilder.setTitle(R.string.retart_service);
        return alertBuilder.create();
    }

    private class RestartServiceTask extends AsyncTask<Void, Void, Void> {
        private AlertDialog  mAlertWindowForProgressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAlertWindowForProgressBar = initializeDialogForSpinningBar();
            // Display the alertwindow on top and also main activity view to Invisible.
            mAlertWindowForProgressBar.show();
            mMainCompleteWindowView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Hide/dismiss the alertwindow as we are done with its use and also bring mainview on top.
            mAlertWindowForProgressBar.dismiss();
            mMainCompleteWindowView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Actual heavy work which would execute on the different thread  created by AsyncTask internally.
            // This method should not be doing anything related to GUI access or manipulation.
            restartServiceCommand();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isDeviceNetworkOn = mConnectivityManagerHelper.isDeviceOnlineNetworkConnectecd();
        if(isDeviceNetworkOn == false) {
            /**
             In case there is no network connection, we should display the toast message so that
             it looks  better compared to AlertDialog window.
             AlertDialog deviceNetworkNotConnected = initializeDialogWindowLogicForDataNetwork();
             deviceNetworkNotConnected.show();
             **/
            mToastMessageHelper.showLongDurationToast(R.string.cellulardata_warning_message);
        }

        boolean isGPSProviderOn     = mLocationManagerHelper.isGPSProviderIsOn();
        boolean isNetworkProviderOn = mLocationManagerHelper.isNetworkBasedProviderIsOn();
        boolean isPassiveProviderOn = mLocationManagerHelper.isPassiveBasedProviderIsOn();

        if(isGPSProviderOn == false) {
            AlertDialog locationProviderNotEnabled = initializeDialogWindowLogicForLocationProvider();
            locationProviderNotEnabled.show();
        }

        boolean isSMSEnabled = isSMSEnabledByUser();
        // If SMSEnabled feature = true and mIsSendSMS = false, then ask for the permission.
        if(isSMSEnabled && !mIsSendSMS) {
            mIsSendSMS = handleAPermission(Manifest.permission.SEND_SMS,
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
    }

} //End of class

