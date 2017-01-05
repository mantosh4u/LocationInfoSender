package com.samanyu.locationinfosender;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;


public class SettingActivity extends AppCompatActivity  {
    private SettingsFragment mSettingPreference;

    public static final int PICK_MESSENGER_SELECTION_REQUEST = 222;   //The request code
    public static String KEY_SELECTED_APP = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettingPreference = new SettingsFragment();
        /**
         * Display the fragment as the main content. android.R.id.content gives us the root element
         * of a view, without having to know its actual name/type/ID. This is important as usually
         * hosting activity would have to provide the view where fragments would be displayed. In
         * this case there is no XML file corresponding to this activity but still everything works
         * fine. This has happened as PreferenceFragment does take care about inflating the view for
         * this as we can see that there is no onCreateView method of fragment is not overridden.
         **/
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSettingPreference)
                .commit();

        KEY_SELECTED_APP = getResources().getString(R.string.key_selected_app);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if( (requestCode == PICK_MESSENGER_SELECTION_REQUEST)
                && (data != null)
                && (resultCode == Activity.RESULT_OK)) {
            SelectedApplicationInfo selectedAppInfo = (SelectedApplicationInfo)
                    data.getExtras().getSerializable(KEY_SELECTED_APP);
            String appPackageName = selectedAppInfo.mCompletePackageName;

            if(!TextUtils.isEmpty(appPackageName)) {
                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_SELECTED_APP, (String) appPackageName);
                editor.commit();
            }
        }
    }

}