package com.samanyu.locationinfosender;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;


/**
 * This is small base class written which has support for database connectivity. I wrote
 * this class so that all actual activity within this app can be derived from this.
 **/

public class BaseCustomWithDataBaseSupportActivity extends AppCompatActivity {

    private GenericDataBaseHelper mDataBaseHelper = null;
    private DatabaseQueryFactoryInterface mQueryFactory  = null;

    /** The object that allows me to manipulate the database. **/
    public GenericDataBaseHelper getDbTools() { return mDataBaseHelper; }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mQueryFactory  = new ContactTableQuearyFactory();
        // Open the database connection
        mDataBaseHelper = new ContactTableDataBaseHelper
                (getApplicationContext(),mQueryFactory);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database connection if any exists.
        if(mDataBaseHelper != null) {
            mDataBaseHelper.close();
        }
    }


    /** This is used to display the message(success/failure) based on helper thread outcome. **/
    public void commonHandleMessage(Context context, Message msg) {
        /** Fetch what child thread has set the values while completing its execution. **/
        Bundle bundle = msg.getData();
        /** Toast message when child/helper thread complete with success/failure. **/
    }
}