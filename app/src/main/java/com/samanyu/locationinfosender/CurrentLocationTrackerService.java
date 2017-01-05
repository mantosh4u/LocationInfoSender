package com.samanyu.locationinfosender;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class CurrentLocationTrackerService extends Service {
    /**
     * Initialize the various attributes for tracking the user location.
     */
    private LocationManager mLocationManager = null;
    private String mCurrentProvider = LocationManager.GPS_PROVIDER;
    private long   mMinTime = sOneMinute;
    private float  mMinDistance = 100;     //In meter
    private LocationListener mLocationListener  = null;

    private List<String> mProvidersList = new ArrayList<String>();

    private String mCurrentLocation = new String();
    private String mLastKnownLocation = new String();

    /**
     * Sometime due to unavailability of network disconnection we might not get the
     * currentLocation and it might be empty hence we would be caching the last five
     * received updates in the queue order.
     */
    private static final int sCachedSizeList = 5;
    private LinkedList<String> mCurrentCachedLocation = new LinkedList<String>();
    private String mLastSendSMSInformation = new String();

    private NotificationCompat.Builder mBuilder = null;
    private int mId = 999;

    private Timer mTimerForScheduledWork;
    private static final int sOneSecond = 1000;
    private static final int sOneMinute = 60*sOneSecond;

    private long mCurrentTimeOutValue;
    private ArrayList<String> mContactNumberList;
    private String mCurrentApplicationPackageName;
    private boolean mCurrentWhetherToSendSMSValue;

    private GenericDataBaseHelper mDataBaseHelper = null;
    private DatabaseQueryFactoryInterface mQueryFactory  = null;


    public CurrentLocationTrackerService() { }

    @Override
    public void onCreate() {
        mTimerForScheduledWork = new Timer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * The system invokes this method when the service is no longer used and is being destroyed.
     * Your service should implement this to clean up any resources such as threads, registered
     * listeners, or receivers. This is the last call that the service receives.
     */
    @Override
    public void onDestroy() {
        // Update the status bar with current location address for the last time before tracking
        // is stopped by the user. This would(hopefully) provide better user experience.
        updateStatusBarWithCurrentInformation();
        if(mTimerForScheduledWork != null) {
            mTimerForScheduledWork.cancel();
        }
        try {
            mLocationManager.removeUpdates(mLocationListener);
        }catch (SecurityException exception) {
            MyUtilityClass.handleException(exception);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mLocationManager = (LocationManager)getApplicationContext().
                getSystemService(Context.LOCATION_SERVICE);
        if(mLocationManager != null) {
            try {
                initialzeAndUpdateAboutAllProviders();
                // This is required as while startup as well, we require to update our information.
                readAndUpdateCurrentConfiguration();
                executePerodicTask();
                mLocationListener = new myLocationListener();
                // Request the update after every 5 minute and after 1000 meter of distance.
                mLocationManager.requestLocationUpdates
                        (mCurrentProvider, 5*mMinTime, 5*mMinDistance, mLocationListener);
            } catch (SecurityException ec) {
                MyUtilityClass.handleException(ec);
            } catch (Exception ec)  {
                MyUtilityClass.handleException(ec);
            }
        }
        return Service.START_STICKY;
    }

    /**
     * Get the Provider lists and update the mCurrentProvider to the first
     * of provider in the list of system returned.
     */
    private void initialzeAndUpdateAboutAllProviders() {
        mProvidersList = mLocationManager.getProviders(false);
        for (int index = 0; index < mProvidersList.size(); index++) {
            String currentOut = updateLastKnownLocation(mProvidersList.get(index));
            if (!TextUtils.isEmpty(currentOut)) {
                mLastKnownLocation = currentOut;
                insertIntoCachedList(mLastKnownLocation);
                mCurrentProvider = mProvidersList.get(index);
                break;
            }
        }
    }

    /**
     * This should be called by all possible places where chances of getting  better
     * information is possible.
     * @param currentProvider The current used provider to track down.
     */
    private String updateLastKnownLocation(String currentProvider) {
        StringBuilder out = new StringBuilder();
        try {
            Location lastKnownLocation =
                    mLocationManager.getLastKnownLocation(currentProvider);
            if (lastKnownLocation != null) {
                out = getCurrentLocationInProperFormat(lastKnownLocation);
            }
        }catch(SecurityException exception){
            MyUtilityClass.handleException(exception);
        }
        return out.toString();
    }

    private StringBuilder getCurrentLocationInProperFormat(Location location) {
        StringBuilder out = new StringBuilder();
        double longitude =  location.getLongitude();
        double latitude  =  location.getLatitude();

        // Now convert the longitude and latitude into the meaningful ways which
        // would be understood by everyone.
        try {
            Geocoder geocode = new Geocoder(this, Locale.getDefault());
            List<Address> getSoundingLocations = geocode.getFromLocation(latitude, longitude, 1);
            for(int index = 0; index < getSoundingLocations.size(); ++index)  {

                Address current = getSoundingLocations.get(index);
                for (int n = 0; n <= current.getMaxAddressLineIndex(); n++) {
                    out.append(current.getAddressLine(n));
                }
            }
        }catch (IOException exception) {
            MyUtilityClass.handleException(exception);
        }
        return out;
    }


    // Implementation Of LocationListener interface.
    private class myLocationListener implements LocationListener {

        /**
         * Called when the location has changed.There are no restrictions on the use of the
         * supplied Location object.
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            StringBuilder out = getCurrentLocationInProperFormat(location);
            if (!TextUtils.isEmpty(out.toString())) {
                mCurrentLocation = out.toString();
                insertIntoCachedList(mCurrentLocation);
                mLastKnownLocation = mCurrentLocation;
            }
            // Update  the status bar whenever there is changed location event received.
            updateStatusBarWithCurrentInformation();
        }

        /**
         * Called when the provider status changes. This method is called when a provider
         * is unable to fetch a location or if the provider has recently become available
         * after a period of unavailability.
         * int: OUT_OF_SERVICE if the provider is out of service, and this is not expected
         * to change in the near future; TEMPORARILY_UNAVAILABLE if the provider is
         * temporarily unavailable but is expected to be available shortly; and AVAILABLE
         * if the provider is currently available.
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // if provider status is available, I should update my current infrastructure
            // with these values, otherwise ignore this.
            if(status == LocationProvider.AVAILABLE) {
                mCurrentProvider = provider;
                String currentOut = updateLastKnownLocation(mCurrentProvider);
                if (!TextUtils.isEmpty(currentOut)) {
                    mLastKnownLocation = currentOut;
                    insertIntoCachedList(mLastKnownLocation);
                }
            }
        }


        /**
         * Called when the provider is enabled by the user.
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {
            mCurrentProvider = provider;
            String currentOut = updateLastKnownLocation(mCurrentProvider);
            if (!TextUtils.isEmpty(currentOut)) {
                mLastKnownLocation = currentOut;
                insertIntoCachedList(mLastKnownLocation);
            }
        }

        /**
         * Called when the provider is disabled by the user. If requestLocationUpdates is
         * called on an already disabled provider, this method is called immediately.
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {
            // Here we are not suppose to do anything as provider disabled notification
            // has been received.
        }
    }

    /**
     * Get the Timeout value set by the user and read the current list of contact number. This
     * should be read in every execution of our cycle as during this time frame somebody might
     * have configured/updated from setting.
     */
    private  void readAndUpdateCurrentConfiguration() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        String keyScreenTimeOutName = getString(R.string.sms_frequency_key);
        String currentTimeOut = sharedPreferences.getString(keyScreenTimeOutName,
                SettingsFragment.FALLBACK_SCREEN_TIMEOUT_VALUE);
        mCurrentTimeOutValue = (long)Integer.parseInt(currentTimeOut);
        mContactNumberList = new ArrayList<String>();
        mCurrentApplicationPackageName = new String();
        // Open the database connection.
        DatabaseQueryFactoryInterface QueryFactory  = new ContactTableQuearyFactory();

        GenericDataBaseHelper DataBaseHelper = new ContactTableDataBaseHelper
                (getApplicationContext(),QueryFactory);

        ArrayList<HashMap<String, String>> getAllValues = DataBaseHelper.executeSelectQuery(null, null);
        for(int index = 0; index < getAllValues.size(); ++index)
        {
            HashMap<String, String> getOneEntry = getAllValues.get(index);
            String phoneNumber = getOneEntry.get(ContactTableDataBaseHelper.sPhonenumberAttribute);
            mContactNumberList.add(phoneNumber);
        }
        // Close the database connection if any exists.
        DataBaseHelper.close();

        String keyWhetherToSendSMSValueName = getString(R.string.whether_to_send_SMS_key);
        mCurrentWhetherToSendSMSValue = sharedPreferences.getBoolean(
                keyWhetherToSendSMSValueName,SettingsFragment.WHETHER_TO_SEND_SMS_VALUE);

        String keySelectedAppPackageName = getString(R.string.key_selected_app);
        mCurrentApplicationPackageName = sharedPreferences.getString(keySelectedAppPackageName, null);
    }

    private void insertIntoCachedList(String newNode) {
        // check the size is less than initial capacity assigned to this.
        if(mCurrentCachedLocation.size() < sCachedSizeList) {
            mCurrentCachedLocation.addFirst(newNode);
        }
        // otherwise the size is full and need to erase the last stuff so that
        // we can insert the new stuff at the front.
        else {
            mCurrentCachedLocation.removeLast();
            mCurrentCachedLocation.addFirst(newNode);
        }
    }

    private String updateLocationBasedOnCurrentOrLastKnown() {
        if(!TextUtils.isEmpty(mCurrentLocation)) {
            return mCurrentLocation;
        }else if(!TextUtils.isEmpty(mLastKnownLocation)) {
            return mLastKnownLocation;
        }else {
            /**
             * If we are here,before using the CachedLocationArray whether we can get some latest
             * information in mCurrentLocation or mLastKnownLocation. We can not update the
             * mCurrentLocation on our will as it would be updated based on the framework events.
             * Hence I should at least try to fetch the mLastKnownLocation and see in case I get
             * anything except the empty/null. If we still get the empty, I think we would have
             * to rely on our CachedLocationArray information.
             */
            String currentOut = updateLastKnownLocation(mCurrentProvider);
            if(!TextUtils.isEmpty(currentOut)) {
                return currentOut;
            }
            /**
             * if mCurrentLocation and mLastKnownLocation are empty, we would start reading the
             * cached array and send it to the user.
             */
            for (String lastFetchedLocation : mCurrentCachedLocation) {
                if (!TextUtils.isEmpty(lastFetchedLocation)) {
                    return lastFetchedLocation;
                }
            }
        }

        //If everything is exhausted, send the empty string.
        return (new String());
    }



    private void initailizeIntentToSendIntentOverStatusBar(String message) {
        /**
         * Create the Intent object and then verify whether there is any applications
         * which can handle this intent or not. If there are any, we should proceed further
         * and start using it. Basically there could be two types of applications..one being
         * the phone number based and another being the email id. So We would require to fill
         * all information which could be used from either of these type of applications.
         */

        Intent notificationIntent = new Intent(Intent.ACTION_SEND);
        notificationIntent.putExtra(Intent.EXTRA_TEXT, message);
        StringBuilder subjectLine = new StringBuilder();
        subjectLine.append(getString(R.string.status_bar_header))
                .append(Calendar.getInstance().getTime().toString());
        notificationIntent.putExtra(Intent.EXTRA_SUBJECT,subjectLine.toString());
        notificationIntent.setType("text/plain");
        notificationIntent.setPackage(mCurrentApplicationPackageName);

        /*
        Intent finalNotificationIntentAfterWrap = Intent.createChooser(notificationIntent,
                getResources().getText(R.string.send_to));
        boolean  whetherSafeToUseTarget = MyUtilityClass.isIntentSafeToUse(
                getApplicationContext(),finalNotificationIntentAfterWrap);
        */

        boolean  whetherSafeToUseTarget = MyUtilityClass.isIntentSafeToUse(
                getApplicationContext(), notificationIntent);

        if(whetherSafeToUseTarget == true) {
            /**
             * Retrieve a PendingIntent that will start a new activity, like calling
             * Context.startActivity(Intent).Note that the activity will be started outside of the
             * context of an existing activity, so you must use the Intent.FLAG_ACTIVITY_NEW_TASK
             * launch flag in the Intent.
             */
            PendingIntent finalPendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent, /* finalNotificationIntentAfterWrap,*/
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            //Supply a PendingIntent to send when the notification is clicked.
            mBuilder.setContentIntent(finalPendingIntent);
        }

    }

    private void updateStatusBarWithCurrentInformation() {
        String location = updateLocationBasedOnCurrentOrLastKnown();
        if(!TextUtils.isEmpty(location)) {

            StringBuilder headerInfo = new StringBuilder();
            headerInfo.append(getString(R.string.status_bar_header));

            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_menu_view)
                    .setContentTitle(headerInfo.toString())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(location));
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Now as we have notified on status bar, its time to create an intent so that when
            // user clicks on the message, he/she can get the list of all possible options to
            // send it to whomsoever.
            initailizeIntentToSendIntentOverStatusBar(location);

            // mId allows you to update the notification later on.
            notificationManager.notify(mId, mBuilder.build());
        }

    }

    private void sendSMSToAllConfiguredWithCurrentInformatiton() {
        if(mCurrentWhetherToSendSMSValue == false)  {
            return;
        }
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String location = updateLocationBasedOnCurrentOrLastKnown();
            if (!TextUtils.isEmpty(location)||!TextUtils.isEmpty(mLastSendSMSInformation))
            {
                if(mLastSendSMSInformation.equals(location)) {
                    return;
                }
                // "location" is the information which we would be sending via SMS.Store it.
                mLastSendSMSInformation = location;
                for (String destination : mContactNumberList) {
                    smsManager.sendTextMessage(destination, null, location, null, null);
                }
            }
        } catch (Exception exception){
            MyUtilityClass.handleException(exception);
        }
    }


    /**
     * Now start the timer and assign the work. This is to monitor and allow something to
     * execute periodically.
     * TimerTask(timerTask): task to be scheduled.
     * delay(xdelay) in milliseconds before task is to be executed.
     * time in milliseconds(mCurrentTimeOutValue) between successive task executions.
     */
    private void executePerodicTask() {
        try
        {
            MyTimerTask timerTask  = new MyTimerTask();
            long xdelay = 0;
            long xperiod = mCurrentTimeOutValue;
            mTimerForScheduledWork.schedule(timerTask, xdelay, xperiod);
        }
        catch (Exception exception) {
            MyUtilityClass.handleException(exception);
        }
    }

    /**
     * Implementation Of TimerTask interface.
     */
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            /**
             * Update the information on status-bar along with the SMS message(
             * if not revoked by user)
             */
            readAndUpdateCurrentConfiguration();
            updateStatusBarWithCurrentInformation();
            sendSMSToAllConfiguredWithCurrentInformatiton();
            readAndUpdateCurrentConfiguration();
        }
    }
}