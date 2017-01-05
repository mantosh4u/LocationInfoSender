package com.samanyu.locationinfosender;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;



public class ContactListActivity extends BaseCustomWithDataBaseSupportActivity {

    private ListView                mMainListView;
    private AlertDialog             mAlertWindowForEntryAdd;
    ArrayList<ContactInformation>   mDetailContacts = new ArrayList<ContactInformation>();
    ArrayList<String>               mContacts = new ArrayList<String>();
    ArrayAdapter<String>            mPhoneNumberArrayAdapter;
    private AlertDialog             mAlertWindowForDeleteEntry;
    private String                  mCurrentSelectedItem;
    private AlertDialog             mAlertWindowForViewOrUpdateSelectedEntry;

    private SearchView              mContactSearchMenuItemView;
    ArrayList<String>               mContactsCopyWhileSearch = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        mMainListView = (ListView)findViewById(R.id.list_of_contacts);
        mPhoneNumberArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_selectable_list_item,
                android.R.id.text1,
                mContacts);
        mMainListView.setAdapter(mPhoneNumberArrayAdapter);
        mAlertWindowForEntryAdd = initializeDialogWindowLogicForAdd();
        initializeEventListners();
        mAlertWindowForDeleteEntry = initializeDialogWindowForDelete();

        // Support for the ActionBar widgets menu in current activity.
        Toolbar toolbarView = (Toolbar)findViewById(R.id.myToolbar);
        try {
            if (toolbarView != null) {
                setSupportActionBar(toolbarView);
            }
        }catch (Exception exception) {
            String msg = exception.getMessage();
        }
        // Set the title with empty string as I do not want to show the title for this activity.
        setTitle("");

        // handleIntent(getIntent());

    }

    @Override
    protected void onStart () {
        super.onStart();
        /**
         * Read the entered information from database so that it can be displayed once activity
         * is visible to the user.
         **/
        fetchAllInformationFromDatabase();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView. Borrowed from the
        // Android official documentation https://developer.android.com/training/search/setup.html
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem ContactSearchMenuItem = menu.findItem(R.id.search_contact);
        mContactSearchMenuItemView = (SearchView) MenuItemCompat.getActionView(ContactSearchMenuItem);
        mContactSearchMenuItemView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        mContactSearchMenuItemView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mPhoneNumberArrayAdapter.getFilter().filter(newText);
                return false;
            }
        });

        /**
         //Set some of the listener for this view so that we can perform some business logic inside this.
         MenuItemCompat.setOnActionExpandListener(mContactSearchMenuItem, new MenuItemCompat.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
        // When somebody clicks on this menu.
        return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
        // when somebody wants to go back to the original view(press <----).
        // This should help ContactListActivity to retain the original view. So need to get the older values
        // and also notify to the ArrayAdaptor so that view can be updated.
        copyValues(mContactsCopyWhileSearch, mContacts);
        mPhoneNumberArrayAdapter.notifyDataSetChanged();
        return true;
        }
        });
         **/
        mMainListView.refreshDrawableState();
        return true;
    }


    // Based on User Menu Item Selection, execute different logic
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_contact:
                // User has pressed search button hence start search logic.
                return true;
            case R.id.addnew_contact:
                // User has pressed add new contact button hence start executing the logic.
                mAlertWindowForEntryAdd.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** As of now phoneNumber to be mandatory as we display it inside **/
    /** our ContactListActivity class. Hence rest all can be empty.   **/
    private boolean validateEntryBeforeDatabaseAdd(HashMap<String, String> entry) {
        boolean returnValue = true;
        final String phoneNumber  = entry.get(ContactTableDataBaseHelper.sPhonenumberAttribute);
        if((TextUtils.isEmpty(phoneNumber))) {
            returnValue = false;
        }
        return returnValue;
    }

    /** perform the insert operation in database using passed argument in separate task/thread.**/
    private void addInformationIntoDatabase(ContactInformation current) {
        AsyncTask InsertQuery = new InsertQueryTask(current);
        InsertQuery.execute();
    }

    private class InsertQueryTask extends AsyncTask<Object, Void, Void> {
        private ContactInformation mContactInfo;
        private HashMap<String, String> mInsertValues;

        public InsertQueryTask(ContactInformation contact) {
            mContactInfo = contact;
            mInsertValues = new HashMap<String, String>();
        }

        @Override
        protected void onPreExecute() {
            mInsertValues.put(ContactTableDataBaseHelper.sNameAttribute, mContactInfo.getmName());
            mInsertValues.put(ContactTableDataBaseHelper.sPhonenumberAttribute, mContactInfo.getmPhoneNumber());
            mInsertValues.put(ContactTableDataBaseHelper.sEmailaddressAttribute, mContactInfo.getmEmailId());
            mInsertValues.put(ContactTableDataBaseHelper.sPostaladdressAttribute, mContactInfo.getmPostalAddress());
        }

        @Override
        protected Void doInBackground(Object... params) {
            boolean output = validateEntryBeforeDatabaseAdd(mInsertValues);
            if(output) {
                try {
                    long returnValue = getDbTools().executeInsertQuery(mInsertValues);
                }catch (Exception ec) {
                    MyUtilityClass.handleException(ec);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Now Update the list-view with new entry added just now??
            mPhoneNumberArrayAdapter.notifyDataSetChanged();
        }
    }


    private AlertDialog initializeDialogWindowLogicForAdd() {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ContactListActivity.this);
        // Get the layout inflater and and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = this.getLayoutInflater();
        final View entryView = inflater.inflate(R.layout.contact_entry, null);
        alertBuilder.setView(entryView);

        // Initialize Message & Title for this
        alertBuilder.setTitle(R.string.contact_entry_title);
        alertBuilder.setMessage(R.string.contact_entry);

        //Add action buttons
        alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //If clicked ok, check out the user entered data and store inside our master list.
                EditText nameEntry = (EditText)entryView.findViewById(R.id.name_entry);
                EditText phoneNumber = (EditText)entryView.findViewById(R.id.pnumber_entry);
                EditText emailAddress = (EditText)entryView.findViewById(R.id.emailaddress_entry);
                EditText postalAddress = (EditText)entryView.findViewById(R.id.postaladdress_entry);

                String name = nameEntry.getText().toString();
                String phone = phoneNumber.getText().toString();
                String email = emailAddress.getText().toString();
                String postaladdress = postalAddress.getText().toString();

                ContactInformation current = new ContactInformation();
                current.setmName(name);
                current.setmPhoneNumber(phone);
                current.setmEmailId(email);
                current.setmPostalAddress(postaladdress);

                mDetailContacts.add(current);
                mContacts.add(phone);

                addInformationIntoDatabase(current);

                nameEntry.setText("");
                phoneNumber.setText("");
                emailAddress.setText("");
                postalAddress.setText("");
            }
        });

        alertBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        return alertBuilder.create();
    }

    /** perform the delete operation in database using passed argument.**/
    private void deleteInformationFromDatabase(String currentNumber) {
        AsyncTask deleteQuery = new DeleteQueryTask(currentNumber);
        deleteQuery.execute();
    }

    private class DeleteQueryTask extends AsyncTask<Object, Void, Void> {
        private String mCurrentNumber;

        public DeleteQueryTask(String number) {
            mCurrentNumber = number;
        }

        @Override
        protected Void doInBackground(Object... params) {
            String whereAttribute = ContactTableDataBaseHelper.sPhonenumberAttribute;
            String[] deletevalueOfWhereAttribute = new String[1];
            deletevalueOfWhereAttribute[0] = mCurrentNumber;
            try {
                getDbTools().executeDeleteEntriesQuery(whereAttribute, deletevalueOfWhereAttribute);
            } catch (Exception ec) {
                MyUtilityClass.handleException(ec);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Once found the entry, go ahead and remove the entry and notify to adapter
            mContacts.remove(mCurrentNumber);
            mCurrentSelectedItem = "";
            mPhoneNumberArrayAdapter.notifyDataSetChanged();
        }
    }

    private AlertDialog initializeDialogWindowForDelete() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ContactListActivity.this);
        // Initialize Message & Title for this
        alertBuilder.setTitle(R.string.delete_entry);

        //Add action buttons
        alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Find that particular entry, remove from master list and notify adapter to update
                // view of this screen.
                for(int i = 0; i < mContacts.size(); ++i){
                    if(mContacts.get(i).equals(mCurrentSelectedItem))  {
                        // Both array would be aligned hence we can safely remove from both arrays.
                        deleteInformationFromDatabase(mCurrentSelectedItem);
                        // break from the loop.
                        break;
                    }
                }
            }
        });

        alertBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return alertBuilder.create();
    }


    /** perform the fetch operation for a particular entry using passed argument.**/
    private ArrayList<HashMap<String, String>> getInformationFromDatabase(String currentNumber) {
        String whereAttribute = ContactTableDataBaseHelper.sPhonenumberAttribute;
        ArrayList<HashMap<String, String>> getColumn = new ArrayList<HashMap<String, String>>();
        try {
            getColumn = getDbTools().executeSelectQuery(whereAttribute, currentNumber);
        }catch (Exception ec) {
            MyUtilityClass.handleException(ec);
        } finally {
            return getColumn;
        }
    }


    private AlertDialog initializeDialogWindowLogicForViewOrUpdateSelectedEntry() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ContactListActivity.this);
        // Get the layout inflater and and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = this.getLayoutInflater();
        final View entryView = inflater.inflate(R.layout.contact_entry, null);
        alertBuilder.setView(entryView);

        // Initialize Message & Title for this
        alertBuilder.setTitle(R.string.edit_option_title);
        alertBuilder.setMessage(R.string.edit_option);

        //Read information from database corresponding to selected column from the user
        //and update into the view widgets.
        //If clicked ok, check out the user entered data and store inside our master list.
        ArrayList<HashMap<String, String>> getColumn =
                getInformationFromDatabase(mCurrentSelectedItem);

        final EditText nameEntry = (EditText)entryView.findViewById(R.id.name_entry);
        final EditText phoneNumber = (EditText)entryView.findViewById(R.id.pnumber_entry);
        final EditText emailAddress = (EditText)entryView.findViewById(R.id.emailaddress_entry);
        final EditText postalAddress = (EditText)entryView.findViewById(R.id.postaladdress_entry);

        // Since user has selected one entry at a time, its confirm that only one entry would be present.
        final HashMap<String, String> getOneEntry = getColumn.get(0);

        final String currentName = getOneEntry.get(ContactTableDataBaseHelper.sNameAttribute);
        final String currentPhoneNumber = getOneEntry.get(ContactTableDataBaseHelper.sPhonenumberAttribute);
        final String currentemailAddress = getOneEntry.get(ContactTableDataBaseHelper.sEmailaddressAttribute);
        final String currentpostalAddress = getOneEntry.get(ContactTableDataBaseHelper.sPostaladdressAttribute);

        nameEntry.setText(currentName);
        phoneNumber.setText(currentPhoneNumber);
        emailAddress.setText(currentemailAddress);
        postalAddress.setText(currentpostalAddress);


        //Add action buttons
        alertBuilder.setPositiveButton(R.string.edit_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Read all values which are present in the widgets
                // There could be three possible case out of this:
                // 1. Nothing has been changed.
                // 2. At least one attribute is unchanged.
                // 3. All attributes is changed.
                final String updatedName = nameEntry.getText().toString();
                final String updatedPhoneNumber = phoneNumber.getText().toString();
                final String updatedEmailAddress = emailAddress.getText().toString();
                final String updatedPostalAddress = postalAddress.getText().toString();

                //case 1:
                if((updatedName.equals(currentName))
                        &&(updatedPhoneNumber.equals(currentPhoneNumber))
                        &&(updatedEmailAddress.equals(currentemailAddress))
                        &&(updatedPostalAddress.equals(currentpostalAddress))
                        ){
                    return;
                } else {
                    // case 2 & 3: Everything/Something is changed so make a database call accordingly.
                /*
                if(!(updatedName.equals(currentName))
                        &&!(updatedPhoneNumber.equals(currentPhoneNumber))
                        &&!(updatedEmailAddress.equals(currentemailAddress))
                        &&!(updatedPostalAddress.equals(currentpostalAddress)) )
                */

                    String[] updatesetAttribute = new String[4];
                    String[] updatesetAttributeValue = new String[4];

                    updatesetAttribute[0] = ContactTableDataBaseHelper.sNameAttribute;
                    updatesetAttribute[1] = ContactTableDataBaseHelper.sPhonenumberAttribute;
                    updatesetAttribute[2] = ContactTableDataBaseHelper.sEmailaddressAttribute;
                    updatesetAttribute[3] = ContactTableDataBaseHelper.sPostaladdressAttribute;

                    updatesetAttributeValue[0] = updatedName;
                    updatesetAttributeValue[1] = updatedPhoneNumber;
                    updatesetAttributeValue[2] = updatedEmailAddress;
                    updatesetAttributeValue[3] = updatedPostalAddress;

                    String updatewhereAttribute = ContactTableDataBaseHelper.sPhonenumberAttribute;
                    String[] updatevalueOfWhereAttribute = new String[1];
                    updatevalueOfWhereAttribute[0] = mCurrentSelectedItem;
                    int numberOfRows = getDbTools().executeUpdateQuery (updatesetAttribute,
                            updatesetAttributeValue,
                            updatewhereAttribute,
                            updatevalueOfWhereAttribute );

                    if(numberOfRows > 0) {
                        //Successful update into database.
                        // Fist get the index position of current selected item within list and add new value
                        // into list. Now remove the old value from list. Post that call notifyDataSetChanged.
                        int oldValueIndex = mContacts.indexOf(mCurrentSelectedItem);
                        if(oldValueIndex != -1) {
                            mContacts.add(oldValueIndex, updatedPhoneNumber);
                            mContacts.remove(mCurrentSelectedItem);
                            mCurrentSelectedItem = updatedPhoneNumber;
                            mPhoneNumberArrayAdapter.notifyDataSetChanged();
                        }
                    }

                }
            }
        });

        alertBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do Nothing
            }
        });

        return alertBuilder.create();
    }


    private void initializeEventListners() {
        // To Delete a particular selected entry, do long press click on item.
        mMainListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                //Store the current selected item
                mCurrentSelectedItem = (((TextView)view).getText()).toString();
                mAlertWindowForDeleteEntry.show();
                //	true if the callback consumed the long click, false otherwise
                return true;
            }
        });

        // To View/Modify a particular selected entry, user would just click on item.
        // First Implement the click handler and inside that display a new type of alert window
        // which would have  OK-CANCEL-UPDATE buttons so that user can perform the appropriate
        // action on selected item.
        mMainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Store the current selected item
                mCurrentSelectedItem = (((TextView)view).getText()).toString();
                mAlertWindowForViewOrUpdateSelectedEntry = initializeDialogWindowLogicForViewOrUpdateSelectedEntry();
                mAlertWindowForViewOrUpdateSelectedEntry.show();
            }
        });

    }




    /**
     @Override
     protected void onNewIntent(Intent intent)  {
     handleIntent(intent);
     }

     private void copyValues(ArrayList<String> source, ArrayList<String> destination)  {
     destination.clear();
     for(String current: source)  {
     destination.add(current);
     }
     }

     private  void handleIntent(Intent intent){
     if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
     String query = intent.getStringExtra(SearchManager.QUERY);

     //It looks like I do not need to use it for my search logic which is required for my case.
     SearchHelperDatabaseTable db = new SearchHelperDatabaseTable(this, mContacts);
     Cursor outputCursor = db.getNumberMatches(query, null);
     if(outputCursor != null) {
     copyValues(mContacts, mContactsCopyWhileSearch);
     mContacts.clear();
     if(outputCursor.moveToFirst()){
     do{
     mContacts.add(outputCursor.getString(
     outputCursor.getColumnIndex(SearchHelperDatabaseTable.COL_NUMBER)));
     mPhoneNumberArrayAdapter.notifyDataSetChanged();
     } while(outputCursor.moveToNext());
     }
     }

     }
     }
     **/


    private void fetchAllInformationFromDatabase() {
        // Clear the previous stored information as we would be reading fresh data from database.
        mContacts.clear();
        // Now Update the list-view as we have cleared all previous information.
        mPhoneNumberArrayAdapter.notifyDataSetChanged();
        ArrayList<HashMap<String, String>> getAllValues = getDbTools().executeSelectQuery(null, null);
        for(int index = 0; index < getAllValues.size(); ++index)
        {
            HashMap<String, String> getOneEntry = getAllValues.get(index);
            String phoneNumber = getOneEntry.get(ContactTableDataBaseHelper.sPhonenumberAttribute);
            mContacts.add(phoneNumber);
        }
        // Now Update the list-view with new entries added just now??
        mPhoneNumberArrayAdapter.notifyDataSetChanged();
    }

}
