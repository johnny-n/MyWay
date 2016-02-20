package com.projects.johnny.myway;

/**
 * An object of this class represents a location that we
 * may obtain a travel time from.
 */
public class MyLocation {
    private String mNameOfPlace;
    private String mAddress;

    public MyLocation(String name, String address) {
        mNameOfPlace = name;
        mAddress = address;
    }

    public String getNameOfPlace() {
        return mNameOfPlace;
    }

    public String getAddress() {
        return mAddress;
    }
}
