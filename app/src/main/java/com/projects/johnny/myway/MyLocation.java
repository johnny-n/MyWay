package com.projects.johnny.myway;

import java.util.UUID;

/**
 * An object of this class represents a location that we
 * may obtain a travel time from.
 */
public class MyLocation {
    private UUID uuid;
    private String mNameOfPlace;
    private String mAddress;

    public MyLocation(String name, String address) {
        mNameOfPlace = name;
        mAddress = address;
        uuid = UUID.randomUUID();
    }

    public MyLocation(String address) {
        mNameOfPlace = mAddress = address;
        uuid = UUID.randomUUID();
    }

    public String getNameOfPlace() {
        return mNameOfPlace;
    }

    public String getAddress() {
        return mAddress;
    }

    public UUID uuid() {
        return uuid;
    }

    public void setNameOfPlace(String nameOfPlace) {
        mNameOfPlace = nameOfPlace;
    }

    public void setAddress(String address) {
        mAddress = address;
    }
}
