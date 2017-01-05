package com.samanyu.locationinfosender;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;



public class GenericDataBaseHelper extends SQLiteOpenHelper{

    public DatabaseQueryFactoryInterface mQueryFactory;


    public GenericDataBaseHelper(Context context, DatabaseQueryFactoryInterface queryFactory) {
        super(context, DataBaseSchema.DATABASE_NAME, null, DataBaseSchema.DATABASE_VERSION);
        mQueryFactory = queryFactory;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }


    /**
     * Called when the database is created for the first time. This is where the creation of tables
     * and the initial population of the tables should happen.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(mQueryFactory.createTableQuery());
    }

    /**
     * Called when the database needs to be upgraded. The implementation should use this method to
     * drop tables, add tables, or do anything else it needs to upgrade to the new schema version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(mQueryFactory.deleteEntireTableQuery());
        onCreate(db);
    }

    public long executeInsertQuery(HashMap<String, String> attributeValues) {
        return  -1;
    }

    public ArrayList<HashMap<String,String>> executeSelectQuery
            (String whereAttribute , String likeValueOfAttribute) {
        return new ArrayList<HashMap<String,String>>();
    }

    public void executeDeleteTableQuery() {
        return;
    }

    public int executeDeleteEntriesQuery(String whereAttribute, String[] likeValueOfAttribute) {
        return -1;
    }

    public int executeUpdateQuery(String[] setAttribute, String[] valueOfSetAttribute,
                                  String whereAttribute, String[] valueOfWhereAttribute) {
        return -1;
    }

}
