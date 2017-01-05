package com.samanyu.locationinfosender;

// Required to implement this class so that whenever user selects it,we can store
// into our preference file which would be used later on when we would be sending
// information from the context of service.
public class SelectedApplicationInfo implements java.io.Serializable {
    public String mCompletePackageName;
    public String mDiplayedApplicationName;
}

