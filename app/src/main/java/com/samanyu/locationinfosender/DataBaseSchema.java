package com.samanyu.locationinfosender;

import android.provider.BaseColumns;

public class DataBaseSchema {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ADDRESS.DB";
    public static final String TABLE_CONTACTS = "CONTACT";

    public static class ContactEntry implements BaseColumns {
        public static final String COLUMN_NAME_NAME = "NAME";
        public static final String COLUMN_NAME_PHNUMBER = "PHONENUMBER";
        public static final String COLUMN_NAME_EMAILADDRESS = "EMAILADDRESS";
        public static final String COLUMN_NAME_POSTALADDRESS = "POSTALADDRESS";
    }
};
