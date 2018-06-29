package com.example.louizibdawi.kmcounterandroid;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Copyright (C) 2017   Tom Kliethermes
 *
 * This file is part of BookyMcBookface and is is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

public class EventDb extends SQLiteOpenHelper {

    public final static String DBNAME = "eventdb";
    private final static int DBVERSION = 5;

    private final static String EVENT_TABLE = "Events";
    private final static String EVENT_NAME = "Name";
    private final static String EVENT_START = "Start";
    private final static String EVENT_END = "Dest";
    private final static String EVENT_NUMPICKER = "NumPicker";
    private final static String EVENT_DATEPICKER = "DatePicker";
    private final static String EVENT_KPT = "KmsPerTrip";
    private final static String EVENT_KPY = "KmsPerYear";
    private final static String EVENT_ONEWAY = "OneWay";

    private Context context;

    private Pattern authorRX;
    private Pattern titleRX;

    public EventDb(Context context) {
        super(context, DBNAME, null, DBVERSION);
        this.context = context;

        String namePrefixRX = "sir|lady|rev(?:erend)?|doctor|dr|mr|ms|mrs|miss";
        String nameSuffixRX = "jr|sr|\\S{1,5}\\.d|[jm]\\.?d|[IVX]+|1st|2nd|3rd|esq";
        String nameInfixRX = "V[ao]n|De|St\\.?";

        authorRX = Pattern.compile("^\\s*(?:(?i:" + namePrefixRX + ")\\.?\\s+)? (.+?)  (?:\\s+|d')?((?:(?:" + nameInfixRX + ")\\s+)? \\S+ (?:\\s+(?i:" + nameSuffixRX + ")\\.?)?)$", Pattern.COMMENTS);
        titleRX = Pattern.compile("^(a|an|the|la|el|le|eine?|der|die)\\s+(.+)$", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("EventDb", "Updating table from " + oldVersion + " to " + newVersion);

        String DROP = "drop table if exists " + EVENT_TABLE;
        db.execSQL(DROP);

        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEventTable =
                "create table " + EVENT_TABLE + "( " +
                        EVENT_NAME + " TEXT PRIMARY KEY," +
                        EVENT_START + " TEXT," +
                        EVENT_END + " TEXT," +
                        EVENT_NUMPICKER + " INT," +
                        EVENT_DATEPICKER + " INT," +
                        EVENT_KPT + " DOUBLE," +
                        EVENT_KPY + " DOUBLE," +
                        EVENT_ONEWAY + " INT" +
                        ")";
        //System.out.println(createEventTable);
        db.execSQL(createEventTable);
        db.execSQL("create index ind_" + EVENT_NAME + " on " + EVENT_TABLE + " (" + EVENT_NAME + ")");
    }

    public Boolean checkForEvent(String eventName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        String ifExists = "SELECT " +EVENT_NAME+ " FROM " +EVENT_TABLE+ " WHERE "
                + EVENT_NAME + " = '" + eventName + "'";

        cursor = db.rawQuery(ifExists, null);

        if(cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    public void removeEvent(String eventName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String deleteEvent = "DELETE FROM " +EVENT_TABLE+ " WHERE " + EVENT_NAME + " = '" + eventName + "'";

        System.out.println(deleteEvent);

        db.execSQL(deleteEvent);
    }

    public void addEvent(String eventName, String start, String dest, int numPicker, int datePicker,
                         double numKmsPerTrip, double numKmsPerYear, int oneWay) {

        SQLiteDatabase db = this.getWritableDatabase();

        String sql =
                "INSERT or replace INTO " + EVENT_TABLE +
                        "(" + EVENT_NAME + ","
                        + EVENT_START + ","
                        + EVENT_END + ","
                        + EVENT_NUMPICKER + ","
                        + EVENT_DATEPICKER + ","
                        + EVENT_KPT + ","
                        + EVENT_KPY + ","
                        + EVENT_ONEWAY + ") " +
                        "VALUES ('"
                        + eventName + "','"
                        + start + "','"
                        + dest + "',"
                        + numPicker + ","
                        + datePicker + ","
                        + numKmsPerTrip + ","
                        + numKmsPerYear + ","
                        + oneWay + ")";

        //System.out.println(sql);

        db.execSQL(sql);

    }

    public List<EventRecord> getEvents() {
        System.out.println("GETTING EVENTS");
        SQLiteDatabase db = this.getReadableDatabase();
        List<EventRecord> events = new ArrayList<>();

        Cursor eventCursor = db.query(EVENT_TABLE, new String[]{EVENT_NAME, EVENT_START,
                        EVENT_END, EVENT_NUMPICKER, EVENT_DATEPICKER, EVENT_KPT,
                        EVENT_KPY, EVENT_ONEWAY},
                null,null,null, null, null);

        System.out.println("Found " + eventCursor.getCount() + " events");

        //If it finds rows
        if(eventCursor.getCount() > 0) {
            //Loop through each event until cursor is past the dest
            while(eventCursor.moveToNext()) {
                events.add(getEventRecord(eventCursor));
            }
        }

        return events;
    }

    @NonNull
    private EventRecord getEventRecord(Cursor eventCursor) {
        EventRecord event = new EventRecord();
        event.name = eventCursor.getString(eventCursor.getColumnIndex(EVENT_NAME));
        event.start = eventCursor.getString(eventCursor.getColumnIndex(EVENT_START));
        event.dest = eventCursor.getString(eventCursor.getColumnIndex(EVENT_END));
        event.numPicker = eventCursor.getInt(eventCursor.getColumnIndex(EVENT_NUMPICKER));
        event.datePicker = eventCursor.getInt(eventCursor.getColumnIndex(EVENT_DATEPICKER));
        event.kpt = eventCursor.getInt(eventCursor.getColumnIndex(EVENT_KPT));
        event.kpy = eventCursor.getInt(eventCursor.getColumnIndex(EVENT_KPY));
        event.oneWay = eventCursor.getInt(eventCursor.getColumnIndex(EVENT_ONEWAY));
        return event;
    }

    public class EventRecord {
        public String name;
        public String start;
        public String dest;
        public int numPicker;
        public int datePicker;
        public int kpt;
        public int kpy;
        public int oneWay;
    }

}
