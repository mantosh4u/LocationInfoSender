package com.samanyu.locationinfosender;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;


public class ContactTableDataBaseHelper extends GenericDataBaseHelper {

    public static final  String sTableName
            = DataBaseSchema.TABLE_CONTACTS;
    public static final String sNameAttribute
            = DataBaseSchema.ContactEntry.COLUMN_NAME_NAME;
    public static final String sPhonenumberAttribute
            = DataBaseSchema.ContactEntry.COLUMN_NAME_PHNUMBER;
    public static final String sEmailaddressAttribute
            = DataBaseSchema.ContactEntry.COLUMN_NAME_EMAILADDRESS;
    public static final String sPostaladdressAttribute
            = DataBaseSchema.ContactEntry.COLUMN_NAME_POSTALADDRESS;


    public ContactTableDataBaseHelper(Context context, DatabaseQueryFactoryInterface queryFactory) {
        super(context,queryFactory);
    }

    @Override
    public long executeInsertQuery(HashMap<String, String> attributeValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues inputIntoDB = new ContentValues();
        executeInsertQueryHelper(inputIntoDB, attributeValues);
        long returnValue = database.insert(sTableName, null, inputIntoDB);
        database.close();
        return returnValue;
    }

    private void executeInsertQueryHelper(ContentValues output, HashMap<String, String> inputValue) {
        output.put(sNameAttribute, inputValue.get(sNameAttribute));
        output.put(sPhonenumberAttribute, inputValue.get(sPhonenumberAttribute));
        output.put(sEmailaddressAttribute, inputValue.get(sEmailaddressAttribute));
        output.put(sPostaladdressAttribute,  inputValue.get(sPostaladdressAttribute));
    }

    @Override
    public ArrayList<HashMap<String,String>> executeSelectQuery
            (String whereAttribute , String likeValueOfAttribute) {
        ArrayList<HashMap<String,String>> returnValue = new ArrayList<HashMap<String,String>>();
        SQLiteDatabase database = this.getReadableDatabase();
        String query = null;
        // Null/Empty values means customer wants everything(*)
        if(TextUtils.isEmpty(whereAttribute)|| TextUtils.isEmpty(likeValueOfAttribute)) {
            query = mQueryFactory.selectAllEntryQuery();
        } else {
            query = mQueryFactory.selectSpecificEntryQuery(whereAttribute,likeValueOfAttribute);
        }
        Cursor cursor = database.rawQuery(query, null);
        executeSelectQueryHelper(cursor, returnValue);

        database.close();
        return returnValue;
    }

    private void executeSelectQueryHelper(Cursor input,ArrayList<HashMap<String,String>> output) {
        if(input.moveToFirst()){
            do{
                HashMap<String, String> contactMap = new HashMap<String, String>();

                contactMap.put(sNameAttribute,
                        input.getString(input.getColumnIndex(sNameAttribute)) );
                contactMap.put(sPhonenumberAttribute,
                        input.getString(input.getColumnIndex(sPhonenumberAttribute)) );
                contactMap.put(sEmailaddressAttribute,
                        input.getString(input.getColumnIndex(sEmailaddressAttribute)) );
                contactMap.put(sPostaladdressAttribute,
                        input.getString(input.getColumnIndex(sPostaladdressAttribute)) );

                output.add(contactMap);
            } while(input.moveToNext());
        }
    }

    @Override
    public void executeDeleteTableQuery() {
        SQLiteDatabase database = this.getWritableDatabase();
        executeDeleteTableQueryHelper(database);
        database.close();
        return;
    }

    private void executeDeleteTableQueryHelper(SQLiteDatabase database) {
        database.execSQL(mQueryFactory.deleteEntireTableQuery());
    }

    @Override
    public int executeDeleteEntriesQuery(String whereAttribute, String[] likeValueOfAttribute) {
        SQLiteDatabase database = this.getWritableDatabase();
        int returnValue = database.delete(sTableName,
                mQueryFactory.deleteSpecificEntryQuery(whereAttribute,null),
                likeValueOfAttribute);
        database.close();
        return returnValue;
    }

    private void executeDeleteEntriesQueryHelper() {
        // Nothing to implement at this moment.
    }

    @Override
    public int executeUpdateQuery(String[] setAttribute, String[] valueOfSetAttribute,
                                  String whereAttribute, String[] valueOfWhereAttribute) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues inputIntoDB = new ContentValues();
        for(int index = 0; index < setAttribute.length; index++) {
            inputIntoDB.put(setAttribute[index], valueOfSetAttribute[index]);
        }

        int returnValue = database.update(sTableName,
                inputIntoDB,
                mQueryFactory.updateSpecificEntryQuery(null, null,whereAttribute,null ),
                valueOfWhereAttribute);

        database.close();
        return  returnValue;
    }

}