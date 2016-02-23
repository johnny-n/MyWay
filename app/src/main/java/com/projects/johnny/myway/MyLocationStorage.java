package com.projects.johnny.myway;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


// Singleton class for easy storage
public class MyLocationStorage {
    private static MyLocationStorage sLocationStorage;
    private ArrayList<MyLocation> mLocations;

    public static MyLocationStorage get(Context context) {
        if (sLocationStorage == null) {
            sLocationStorage = new MyLocationStorage(context);
        }
        return sLocationStorage;
    }

    public List<MyLocation> getLocations() {
        return mLocations;
    }

    public void addLocation(MyLocation myLocation) {
        mLocations.add(myLocation);
    }

    public void deleteLocation(UUID uuid) {
        if (mLocations.size() > 0) {
            for (int i = 0; i < mLocations.size(); i++) {
                if (mLocations.get(i).uuid().equals(uuid)) {
                    mLocations.remove(i);
                    return;
                }
            }
        }
    }

    private MyLocationStorage(Context context) {
        mLocations = new ArrayList<>();
    }
}

//      Template Singleton class to build off of
//{
//        import android.content.Context;
//
//        import java.util.ArrayList;
//        import java.util.List;
//        import java.util.UUID;
//
//public class CrimeLab {
//    private static CrimeLab sCrimeLab;
//
//    private ArrayList<Crime> mCrimes;
//
//    public static CrimeLab get(Context context) {
//        if (sCrimeLab == null) {
//            sCrimeLab = new CrimeLab(context);
//        }
//        return sCrimeLab;
//    }
//
//    private CrimeLab(Context context) {
//        mCrimes = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            Crime crime = new Crime();
//            crime.setTitle("Crime #" + i);
//            crime.setSolved(i % 2 == 0);
//            mCrimes.add(crime);
//        }
//    }
//
//    public List<Crime> getCrimes() {
//        return mCrimes;
//    }
//
//    public Crime getCrime(UUID id) {
//        for (Crime crime : mCrimes) {
//            if (crime.getId().equals(id)) {
//                return crime;
//            }
//        }
//        return null;
//    }
//}
//
//}