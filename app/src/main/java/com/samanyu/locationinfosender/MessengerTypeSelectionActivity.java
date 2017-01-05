package com.samanyu.locationinfosender;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MessengerTypeSelectionActivity extends AppCompatActivity {
    private ListView    mMainListView = null;
    private ArrayList<String>   mApplicationNames = null;
    private ArrayList<SelectedApplicationInfo>  mDetailedApplicationsInfo = null;
    private ArrayAdapter<String>    mApplicationNamesArrayAdapter = null;
    private SelectedApplicationInfo  mCurrentSelectedApplication = null;
    private static String KEY_SELECTED_APP = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger_type_selection);

        mMainListView = (ListView)findViewById(R.id.list_of_apps);

        mApplicationNames = new ArrayList<String>();
        mDetailedApplicationsInfo = new ArrayList<SelectedApplicationInfo>();
        mCurrentSelectedApplication = new SelectedApplicationInfo();

        mApplicationNamesArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_selectable_list_item,
                android.R.id.text1,
                mApplicationNames);
        mMainListView.setAdapter(mApplicationNamesArrayAdapter);

        KEY_SELECTED_APP = getResources().getString(R.string.key_selected_app);

        mMainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Store the current selected item
                mCurrentSelectedApplication.mDiplayedApplicationName = (((TextView)view).getText()).toString();
                for(SelectedApplicationInfo current: mDetailedApplicationsInfo) {
                    if(current.mDiplayedApplicationName.equals(mCurrentSelectedApplication.mDiplayedApplicationName)) {
                        mCurrentSelectedApplication.mCompletePackageName = current.mCompletePackageName;
                    }
                }
                // Need to change the view color to something else and also store into the preference file.
                // Intent resultIntentToCallingActivity = new Intent();
                Intent resultIntentToCallingActivity = getIntent();
                resultIntentToCallingActivity.putExtra(KEY_SELECTED_APP, mCurrentSelectedApplication);
                setResult(Activity.RESULT_OK, resultIntentToCallingActivity);
                finish();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        final Intent actionSendHandlerIntent = new Intent(Intent.ACTION_SEND, null);
        actionSendHandlerIntent.setType("text/plain");
        final PackageManager packageManager =  getApplicationContext().getPackageManager();

        final List<ResolveInfo> activitesResolveInfo = packageManager.queryIntentActivities
                (actionSendHandlerIntent,0);

        for(ResolveInfo current: activitesResolveInfo) {
            SelectedApplicationInfo detailedInfo = new SelectedApplicationInfo();
            detailedInfo.mCompletePackageName = current.activityInfo.packageName;
            detailedInfo.mDiplayedApplicationName = current.loadLabel(packageManager).toString();
            mApplicationNames.add(detailedInfo.mDiplayedApplicationName);
            mDetailedApplicationsInfo.add(detailedInfo);
        }
        mApplicationNamesArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(KEY_SELECTED_APP, mCurrentSelectedApplication);
    }

}
