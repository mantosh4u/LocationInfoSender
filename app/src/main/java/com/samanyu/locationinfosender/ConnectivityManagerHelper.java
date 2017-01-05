package com.samanyu.locationinfosender;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;


/**
 * Hosting modules(activity/service/fragments) would require to pass their context information
 * to this class while creating the object of this. Post that they can just use other logic
 * without worrying much about the details of internals.
 */

public class ConnectivityManagerHelper {
    private ConnectivityManager mConnectivityManager;

    public ConnectivityManagerHelper(Context context) {
        mConnectivityManager = (ConnectivityManager)context.getSystemService
                (Context.CONNECTIVITY_SERVICE);
    }

    /**
     * This method was deprecated in API level 23. This method does not support multiple
     * connected networks of the same type. Use getAllNetworks() and getNetworkInfo
     * (android.net.Network) instead.
     */
    private NetworkInfo getNetworkOfProvidedType(int networkType) {
        NetworkInfo networkInfo = null;
        // Post M(API 23) handling
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network[] networks = mConnectivityManager.getAllNetworks();
            for(Network current: networks) {
                NetworkInfo currentNetworkInfo = mConnectivityManager.getNetworkInfo(current);
                if(currentNetworkInfo.getType() == networkType) {
                    networkInfo = currentNetworkInfo;
                    return networkInfo;
                }
            }
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            // Pre M(API 23) and prior to this
            NetworkInfo currentNetworkInfo = mConnectivityManager.getNetworkInfo(networkType);
            return currentNetworkInfo;
        }
        return networkInfo;
    }

    private boolean commonNetworkBasedConnectionFetched(int networkType) {
        boolean out = false;
        NetworkInfo networkInfo = getNetworkOfProvidedType(networkType);
        if(networkInfo != null) {
            out = networkInfo.isConnected();
        }
        return out;
    }

    public boolean isBlueToothNetworkBasedConnected() {
        return commonNetworkBasedConnectionFetched(ConnectivityManager.TYPE_BLUETOOTH);
    }

    public  boolean isDummyNetworkBasedConnected() {
        return commonNetworkBasedConnectionFetched(ConnectivityManager.TYPE_DUMMY);
    }

    public  boolean isEthernetNetworkBasedConnected() {
        return commonNetworkBasedConnectionFetched(ConnectivityManager.TYPE_ETHERNET);
    }

    public  boolean isMobileDataNetworkBasedConnected() {
        return commonNetworkBasedConnectionFetched(ConnectivityManager.TYPE_MOBILE);
    }

    public  boolean isMobileDUNNetworkBasedConnected() {
        return commonNetworkBasedConnectionFetched(ConnectivityManager.TYPE_MOBILE_DUN);
    }

    public  boolean isVPNNetworkBasedConnected() {
        return commonNetworkBasedConnectionFetched(ConnectivityManager.TYPE_VPN);
    }

    public  boolean isWIFINetworkBasedConnected() {
        return commonNetworkBasedConnectionFetched(ConnectivityManager.TYPE_WIFI);
    }

    public  boolean isWIMAXNetworkBasedConnected() {
        return commonNetworkBasedConnectionFetched(ConnectivityManager.TYPE_WIMAX);
    }


    public boolean isDeviceOnlineNetworkConnectecd() {
        boolean out = false;
        NetworkInfo activeNetworkInfo =  mConnectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null) {
            out = activeNetworkInfo.isConnectedOrConnecting();
        }
        return out;
    }
}