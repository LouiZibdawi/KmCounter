package com.example.louizibdawi.kmcounterandroid;

import android.app.Application;
import android.content.Context;

/**
 * Created by louizibdawi on 2018-06-09.
 */

public class MyDb extends Application {

    private EventDb db;

    @Override
    public void onCreate() {
        super.onCreate();

        db = new EventDb(this);
    }

    public static EventDb getDB(Context context) {

        return ((MyDb)context.getApplicationContext()).db;
    }

    public static void resetRB(Context context) {
        ((MyDb)context.getApplicationContext()).deleteDatabase("eventdb");
    }

    @Override
    public void onTerminate() {
        if (db!=null) db.close();

        super.onTerminate();
    }
}
