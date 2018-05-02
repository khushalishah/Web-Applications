package com.khushali.placessearch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by khushali on 23-02-2018.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String TABLE_FAV = "favorites";
    public static final String COLUMN_PLACEID = "placeid";
    public static final String COLUMN_PLACE_NAME = "placeName";
    public static final String COLUMN_PLACE_ADDR = "placeAddress";
    public static final String COLUMN_PLACE_PIC = "placeURL";
    public static final String COLUMN_IS_FAV = "isFav";

    private static final String DATABASE_NAME = "ent.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_FAVORITES = "create table "
            +TABLE_FAV +"("
            +COLUMN_PLACEID + " text primary key,"
            +COLUMN_PLACE_NAME + " text not null,"
            +COLUMN_PLACE_ADDR + " text,"
            +COLUMN_PLACE_PIC + " text,"
            +COLUMN_IS_FAV + " integer);";

    public DBHelper(Context context) {
        //Add a DATABASE_VERSION parameter if you will need to update the database structure in the future
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d("DATABASE","CREATING DATABASE");
        database.execSQL(DATABASE_CREATE_FAVORITES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
    }





}
