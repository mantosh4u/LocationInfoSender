package com.samanyu.locationinfosender;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;


public class SettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    // 20 minutes is default value if user has not set anything.
    public static final String   FALLBACK_SCREEN_TIMEOUT_VALUE = "1200000";
    public static String KEY_SCREEN_TIMEOUT;
    private ListPreference     mScreenTimeoutPreference;

    private Preference   mConfiguredContactPreference;
    public static String KEY_CONTACT_LIST;

    private Preference  mSelectedMessengerPreference;
    public static String KEY_MESSENGER_TYPE_SELECTION;

    public static final boolean WHETHER_TO_SEND_SMS_VALUE = false;
    private CheckBoxPreference mWhetherToSendSMSPreference;
    public  static String KEY_WHETHER_TO_SEND_SMS;


    private void fetchAndupdateSelectedMessengerPreferenceDescription() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String keySelectedAppPackageName = getString(R.string.key_selected_app);
        String currentApplicationPackageName = sharedPreferences.getString(keySelectedAppPackageName, null);
        updateSelectedMessengerPreferenceDescription(currentApplicationPackageName);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        KEY_SCREEN_TIMEOUT = getString(R.string.sms_frequency_key);
        KEY_CONTACT_LIST = getString(R.string.contact_list_key);
        KEY_MESSENGER_TYPE_SELECTION = getString(R.string.messenger_type_selection_key);
        KEY_WHETHER_TO_SEND_SMS = getString(R.string.whether_to_send_SMS_key);

        mScreenTimeoutPreference = (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);

        CharSequence entryValue = mScreenTimeoutPreference.getEntry();
        String entryInSeconds = FALLBACK_SCREEN_TIMEOUT_VALUE;
        // This means this is the first time application has been started and nothing has
        // been set by user.
        if(entryValue == null) {
            mScreenTimeoutPreference.setValue(entryInSeconds);
        } else {
            entryInSeconds = mScreenTimeoutPreference.getValue();
        }
        mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        Integer timeOutValue = new Integer(entryInSeconds);
        updateTimeoutPreferenceDescription(timeOutValue.longValue());

        mConfiguredContactPreference = (Preference)findPreference(KEY_CONTACT_LIST);
        mConfiguredContactPreference.setOnPreferenceClickListener
                (new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent launchContactListActivity = new Intent(
                                getActivity().getApplicationContext(),
                                ContactListActivity.class);
                        startActivity(launchContactListActivity);
                        return false;
                    }
                });

        mSelectedMessengerPreference = (Preference)findPreference(KEY_MESSENGER_TYPE_SELECTION);
        mSelectedMessengerPreference.setOnPreferenceChangeListener(this);
        mSelectedMessengerPreference.setOnPreferenceClickListener(this);
        fetchAndupdateSelectedMessengerPreferenceDescription();

        mSelectedMessengerPreference.setOnPreferenceClickListener
                (new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent launchContactListActivity = new Intent(
                                getActivity().getApplicationContext(),
                                MessengerTypeSelectionActivity.class);
                        getActivity().startActivityForResult
                                (launchContactListActivity, SettingActivity.PICK_MESSENGER_SELECTION_REQUEST);
                        return false;
                    }
                });

        mWhetherToSendSMSPreference = (CheckBoxPreference)findPreference(KEY_WHETHER_TO_SEND_SMS);
        mWhetherToSendSMSPreference.setOnPreferenceChangeListener(this);
    }

    // I think,we need to update the screen summary information over here as well.
    @Override
    public void onStart() {
        super.onStart();
        fetchAndupdateSelectedMessengerPreferenceDescription();
    }


    // Borrowed from the offical Android source code
    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = mScreenTimeoutPreference;
        String summary;
        if (currentTimeout < 0) {
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    if (currentTimeout >= timeout) {
                        best = i;
                    }
                }
                summary = preference.getContext().getString(R.string.screen_frequency_summary,
                        entries[best]);
            }
        }
        preference.setSummary(summary);
    }


    private void updateSelectedMessengerPreferenceDescription(String currentSelectedName) {
        Preference preference = mSelectedMessengerPreference;
        String summary;
        if(TextUtils.isEmpty(currentSelectedName)) {
            summary = "";
        } else  {
            summary = preference.getContext().getString(R.string.messenger_type_selection_summary,
                    currentSelectedName);
        }
        preference.setSummary(summary);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor  = sharedPreferences.edit();

        if(KEY_SCREEN_TIMEOUT.equals(key)) {
            // screen timeout preference value has been changed.
            Long value = Long.parseLong((String)newValue);
            editor.putString(KEY_SCREEN_TIMEOUT,(String)newValue);
            editor.commit();
            updateTimeoutPreferenceDescription(value);

        } else if(KEY_WHETHER_TO_SEND_SMS.equals(key)) {
            // Whether to send SMS attributes has been changed by user
            editor.putBoolean(KEY_WHETHER_TO_SEND_SMS,(Boolean)newValue);
            editor.commit();
        } else if(KEY_MESSENGER_TYPE_SELECTION.equals(key)) {
            //TODO: what to store and how, do not know as of now.
            editor.putString(KEY_MESSENGER_TYPE_SELECTION,(String)newValue);
            editor.commit();
            updateSelectedMessengerPreferenceDescription((String)newValue);

        }
        editor.commit();
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        if(KEY_CONTACT_LIST.equals(key)) {
            // Somebody has clicked on the preference.
        }else if(KEY_MESSENGER_TYPE_SELECTION.equals(key)) {
            //TODO: what to store and how, do not know as of now.

        }
        return false;
    }
}