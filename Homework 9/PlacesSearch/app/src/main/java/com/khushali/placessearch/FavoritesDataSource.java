package com.khushali.placessearch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by siddharth on 07-02-2015.
 */
public class FavoritesDataSource {

        // Database fields
        private SQLiteDatabase database;
        private DBHelper dbHelper;
        private String[] allColumns =  {
                DBHelper.COLUMN_PLACEID,
                DBHelper.COLUMN_PLACE_NAME,
                DBHelper.COLUMN_PLACE_ADDR,
                DBHelper.COLUMN_PLACE_PIC,
                DBHelper.COLUMN_IS_FAV
        };

        public FavoritesDataSource(Context context) {
            dbHelper = new DBHelper(context);
        }

        public void open() throws SQLException {
            database = dbHelper.getWritableDatabase();
        }

        public void close() {
            dbHelper.close();
        }

        public boolean addToFavorites(Place place) {
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_PLACEID, place.getPlaceid());
            values.put(DBHelper.COLUMN_PLACE_NAME, place.getName());
            values.put(DBHelper.COLUMN_PLACE_ADDR, place.getAddress());
            values.put(DBHelper.COLUMN_PLACE_PIC, place.getPicURL());
            if(place.isFavoriteItem())
                values.put(DBHelper.COLUMN_IS_FAV,1);
            else
                values.put(DBHelper.COLUMN_IS_FAV,0);
            long insertId = database.insert(DBHelper.TABLE_FAV, null,
                    values);

            if(insertId==-1){
                return false;
            }

            return true;
        }

        public void deleteFromFavorites(String placeId) {
            database.delete(DBHelper.TABLE_FAV, DBHelper.COLUMN_PLACEID
                    + " = '" + placeId + "'", null);
        }

        public List<Place> getAllFavorites(int offset,int rows) {
            List<Place> favorites = new ArrayList<Place>();

            Cursor cursor = database.rawQuery("SELECT  * FROM " + DBHelper.TABLE_FAV +" LIMIT " + rows + " OFFSET " + offset, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Place place = cursorToPlace(cursor);
                favorites.add(place);
                cursor.moveToNext();
            }
            cursor.close();
            return favorites;
        }

        int getFavoritesCount(){
            Cursor cursor = database.rawQuery("SELECT COUNT("+ DBHelper.COLUMN_PLACEID +") FROM "+DBHelper.TABLE_FAV+";",null);
            cursor.moveToFirst();
            return cursor.getInt(0);
        }

        boolean isAddedToFavorites(String placeId){
            Cursor  cursor = database.rawQuery("SELECT COUNT("+DBHelper.COLUMN_PLACEID+") FROM "+DBHelper.TABLE_FAV+" WHERE "+DBHelper.COLUMN_PLACEID+"="+placeId,null);
            cursor.moveToFirst();
            if(cursor.getCount()==0)
                return false;
            else
                return true;
        }

        Set<String> getAllPlaceIds(){
            Set<String> ids = new HashSet<>();
            Cursor cursor = database.rawQuery("Select "+DBHelper.COLUMN_PLACEID+" from "+DBHelper.TABLE_FAV,null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ids.add(cursor.getString(0));
                cursor.moveToNext();
            }
            return ids;
        }

        private Place cursorToPlace(Cursor cursor) {

            Place place = new Place();
            place.setPlaceid(cursor.getString(0));
            place.setName(cursor.getString(1));
            place.setAddress(cursor.getString(2));
            place.setPicURL(cursor.getString(3));
            int isFav = cursor.getInt(4);
            if(isFav == 1){
                place.setFavoriteItem(true);
            }else {
                place.setFavoriteItem(false);
            }
            return place;
        }




}
