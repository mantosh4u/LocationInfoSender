package com.samanyu.locationinfosender;

import java.util.HashMap;

public interface DatabaseQueryFactoryInterface {
    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";

    public String createTableQuery();
    public String insertEntryQuery(HashMap<String, String> attributesNamesAndValues);
    public String selectAllEntryQuery();
    public String selectSpecificEntryQuery(String whereAttribute,String likeValueOfAttribute);
    public String deleteSpecificEntryQuery(String whereAttribute, String likeValueOfAttribute);
    public String updateSpecificEntryQuery(String setAttribute, String valueOfSetAttribute,
                                           String whereAttribute, String valueOfWhereAttribute);
    public String deleteEntireTableQuery();
}