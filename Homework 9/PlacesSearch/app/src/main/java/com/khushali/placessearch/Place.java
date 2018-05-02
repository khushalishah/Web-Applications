package com.khushali.placessearch;

import android.os.Parcel;
import android.os.Parcelable;

public class Place implements Parcelable {

    String placeid;
    String name;
    String address;
    boolean isFavoriteItem;
    String picURL;

    public Place() {
    }

    public Place(String placeid, String name, String address, boolean isFavoriteItem, String picURL) {
        this.placeid = placeid;
        this.name = name;
        this.address = address;
        this.isFavoriteItem = isFavoriteItem;
        this.picURL = picURL;
    }

    public String getPlaceid() {
        return placeid;
    }

    public void setPlaceid(String placeid) {
        this.placeid = placeid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isFavoriteItem() {
        return isFavoriteItem;
    }

    public void setFavoriteItem(boolean favoriteItem) {
        isFavoriteItem = favoriteItem;
    }

    public String getPicURL() {
        return picURL;
    }

    public void setPicURL(String picURL) {
        this.picURL = picURL;
    }

        @Override
        public int describeContents() {
            // TODO Auto-generated method stub
            return 0;
        }

        /**
         * Storing the Student data to Parcel object
         **/
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(placeid);
            dest.writeString(name);
            dest.writeString(address);
            dest.writeString(picURL);
            dest.writeInt((byte) (isFavoriteItem ? 1 : 0));
        }

        /**
         * Retrieving Student data from Parcel object
         * This constructor is invoked by the method createFromParcel(Parcel source) of
         * the object CREATOR
         **/
        private Place(Parcel in){
            this.placeid = in.readString();
            this.name = in.readString();
            this.address = in.readString();
            this.picURL = in.readString();
            this.isFavoriteItem = in.readByte() != 0;
        }

        public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {

            @Override
            public Place createFromParcel(Parcel source) {
                return new Place(source);
            }

            @Override
            public Place[] newArray(int size) {
                return new Place[size];
            }
        };
}
