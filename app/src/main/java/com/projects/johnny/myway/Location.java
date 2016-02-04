package com.projects.johnny.myway;

/**
 * An object of this class represents a location that we
 * may obtain a travel time from.
 */
public class Location {
    private String mName;
    private String mAddress;

    public Location(String name, String address) {
        mName = name;
        mAddress = address;
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }
}
