package com.samanyu.locationinfosender;

import java.util.HashMap;


public class ContactTableQuearyFactory implements DatabaseQueryFactoryInterface {

    private String mCreateTableQuery = new String();
    private String mInsertEntryQuery = new String();
    private String mSelectAllEntryQuery = new String();
    private String mSelectSpecificEntryQuery = new String();
    private String mDeleteSpecificEntryQuery = new String();
    private String mUpdateSpecificEntry = new String();
    private String mDeleteTableQuery = new String();


    @Override
    public String createTableQuery() {
        mCreateTableQuery = "CREATE TABLE " + DataBaseSchema.TABLE_CONTACTS + " (" +
                DataBaseSchema.ContactEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                DataBaseSchema.ContactEntry.COLUMN_NAME_PHNUMBER + TEXT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                DataBaseSchema.ContactEntry.COLUMN_NAME_EMAILADDRESS + TEXT_TYPE + COMMA_SEP +
                DataBaseSchema.ContactEntry.COLUMN_NAME_POSTALADDRESS + TEXT_TYPE + ")";

        return mCreateTableQuery;
    }

    @Override
    public String insertEntryQuery(HashMap<String, String> attributesValues) {
        mInsertEntryQuery = "INSERT INTO " + DataBaseSchema.TABLE_CONTACTS + " (" +
                DataBaseSchema.ContactEntry.COLUMN_NAME_NAME + COMMA_SEP +
                DataBaseSchema.ContactEntry.COLUMN_NAME_PHNUMBER + COMMA_SEP +
                DataBaseSchema.ContactEntry.COLUMN_NAME_EMAILADDRESS + COMMA_SEP +
                DataBaseSchema.ContactEntry.COLUMN_NAME_POSTALADDRESS + " )" +
                "VALUES" + " (" +
                attributesValues.get(DataBaseSchema.ContactEntry.COLUMN_NAME_NAME)+ COMMA_SEP +
                attributesValues.get(DataBaseSchema.ContactEntry.COLUMN_NAME_PHNUMBER)+ COMMA_SEP +
                attributesValues.get(DataBaseSchema.ContactEntry.COLUMN_NAME_EMAILADDRESS)+ COMMA_SEP +
                attributesValues.get(DataBaseSchema.ContactEntry.COLUMN_NAME_POSTALADDRESS)+
                ")";

        return mInsertEntryQuery;
    }

    @Override
    public String selectAllEntryQuery() {
        mSelectAllEntryQuery = "SELECT * FROM "  +
                DataBaseSchema.TABLE_CONTACTS;
        return mSelectAllEntryQuery;
    }

    @Override
    public String selectSpecificEntryQuery(String whereAttribute, String likeValueOfAttribute) {
        mSelectSpecificEntryQuery = "SELECT * FROM "  +
                DataBaseSchema.TABLE_CONTACTS + " " +
                "WHERE " + whereAttribute + " " +
                "LIKE '" + likeValueOfAttribute + "'";
        return mSelectSpecificEntryQuery;
    }

    @Override
    public String deleteSpecificEntryQuery(String whereAttribute, String likeValueOfAttribute) {
        mDeleteSpecificEntryQuery = whereAttribute + " " + "= ?";
        return mDeleteSpecificEntryQuery;
    }

    @Override
    public String updateSpecificEntryQuery(String setAttribute, String valueOfSetAttribute,
                                           String whereAttribute, String valueOfWhereAttribute) {
        mUpdateSpecificEntry = whereAttribute + " " + "= ?";
        return mUpdateSpecificEntry;
    }

    @Override
    public String deleteEntireTableQuery() {
        mDeleteTableQuery = "DROP TABLE IF EXISTS " + DataBaseSchema.TABLE_CONTACTS;
        return mDeleteTableQuery;
    }
}
