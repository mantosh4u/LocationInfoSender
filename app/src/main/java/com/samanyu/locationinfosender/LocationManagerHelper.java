package com.samanyu.locationinfosender;

import android.content.Context;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;

public class LocationManagerHelper {
    private LocationManager mLocationManager;
    private List<String> mListOfNamesOfLocationProvider  = null;

    LocationManagerHelper(Context context) {
        mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        mListOfNamesOfLocationProvider =  new ArrayList<>();
        mListOfNamesOfLocationProvider = mLocationManager.getProviders(false);
    }

    public List<String> getAllNamesOfLocationProvider() {
        return mListOfNamesOfLocationProvider;
    }

    private boolean commonLocationProviderFetched(String locationProvider) {
        boolean out = mLocationManager.isProviderEnabled(locationProvider);
        return out;
    }

    public boolean isGPSProviderIsOn() {
        return commonLocationProviderFetched(LocationManager.GPS_PROVIDER);
    }

    public boolean isNetworkBasedProviderIsOn() {
        return commonLocationProviderFetched(LocationManager.NETWORK_PROVIDER);
    }

    public boolean isPassiveBasedProviderIsOn() {
        return commonLocationProviderFetched(LocationManager.PASSIVE_PROVIDER);
    }

    public boolean isAnyProviderIsOn() {
        return (mLocationManager.getProviders(true).size() > 0);
    }

}